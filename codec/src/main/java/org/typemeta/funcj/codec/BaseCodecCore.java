package org.typemeta.funcj.codec;

import org.typemeta.funcj.codec.utils.ReflectionUtils;
import org.typemeta.funcj.functions.Functions;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * Base class for classes which implement an encoding into a specific target type.
 * @param <IN>      the encoded input type
 * @param <OUT>     the encoded output type
 */
@SuppressWarnings("unchecked")
public abstract class BaseCodecCore<IN, OUT> implements CodecCoreInternal<IN, OUT> {

    /**
     * A map from class name to {@code Codec}, associating a class with its {@code Codec}.
     * Although {@code Codec}s can be registered by the caller prior to en/decoding,
     * the primary populator of the registry is this {@code CodecCore} implementation.
     * As and when new classes are encountered, they are inspected via Reflection,
     * and a {@code Codec} is constructed and registered.
     */
    protected final ConcurrentMap<String, Codec<?, IN, OUT>> codecRegistry = new ConcurrentHashMap<>();

    /**
     * A map from class name to {@code TypeConstructor}, associating a class with its {@code TypeConstructor}.
     * Although {@code TypeConstructor}s can be registered by the caller prior to en/decoding,
     * the primary populator of the registry is this {@code CodecCore} implementation.
     * As and when new classes are encountered, they are inspected via Reflection,
     * and a {@code TypeConstructor} is constructed and registered.
     */
    protected final ConcurrentMap<String, TypeConstructor<?>> typeCtorRegistry = new ConcurrentHashMap<>();

    /**
     * A map from class name to its type proxy, associating a class with its type proxy.
     */
    protected final Map<String, Class<?>> typeProxyRegistry = new HashMap<>();

    protected BaseCodecCore() {
    }

    @Override
    public abstract CodecConfig config();

    @Override
    public <T> void registerCodec(Class<? extends T> clazz, Codec<T, IN, OUT> codec) {
        registerCodec(config().classToName(clazz), codec);
    }

    @Override
    public <T> void registerCodec(String name, Codec<T, IN, OUT> codec) {
        synchronized (codecRegistry) {
            codecRegistry.put(name, codec);
        }
    }

    @Override
    public <T> ObjectCodecBuilder<T, IN, OUT> registerCodec(Class<T> clazz) {
        return objectCodecDeferredRegister(clazz);
    }

    @Override
    public <T> void registerStringProxyCodec(
            Class<T> type,
            Functions.F<T, String> encode,
            Functions.F<String, T> decode) {
        registerCodec(type, new Codecs.StringProxyCodec<T, IN, OUT>(this, type, encode, decode));
    }

    @Override
    public <T> void registerTypeProxy(Class<T> clazz, Class<? super T> proxyType) {
        registerTypeProxy(config().classToName(clazz), proxyType);
    }

    @Override
    public void registerTypeProxy(String name, Class<?> proxyType) {
        typeProxyRegistry.put(name, proxyType);
    }

    @Override
    public <T> void registerTypeConstructor(
            Class<? extends T> clazz,
            TypeConstructor<T> typeCtor) {
        typeCtorRegistry.put(config().classToName(clazz), typeCtor);
    }

    @Override
    public <T> OUT encode(Class<? super T> clazz, T val, OUT out) {
        return getCodec(clazz).encodeWithCheck(val, out);
    }

    @Override
    public <T> T decode(Class<? super T> clazz, IN in) {
        return (T)getCodec(clazz).decodeWithCheck(in);
    }

    @Override
    public <T> Class<T> remapType(Class<T> clazz) {
        final String typeName = config().classToName(clazz);
        if (typeProxyRegistry.containsKey(typeName)) {
            return (Class<T>) typeProxyRegistry.get(typeName);
        } else {
            return clazz;
        }
    }

    @Override
    public <T> TypeConstructor<T> getTypeConstructor(Class<T> clazz) {
        final String name = config().classToName(clazz);
        return (TypeConstructor<T>) typeCtorRegistry.computeIfAbsent(
                name,
                n -> TypeConstructor.create(clazz));
    }

    @Override
    public <T> Codec<T, IN, OUT> getCodec(Class<T> clazz) {
        return getCodec(
                config().classToName(remapType(clazz)),
                () -> createCodec(clazz)
        );
    }

    @Override
    public <T> Codec<T, IN, OUT> getCodec(
            String name,
            Supplier<Codec<T, IN, OUT>> codecSupp) {
        // First attempt, without locking.
        if (codecRegistry.containsKey(name)) {
            return (Codec<T, IN, OUT>)codecRegistry.get(name);
        } else {
            final CodecRef<T, IN, OUT> codecRef;
            // Lock and try again.
            synchronized(codecRegistry) {
                if (codecRegistry.containsKey(name)) {
                    return (Codec<T, IN, OUT>) codecRegistry.get(name);
                } else {
                    // Ok, it's definitely not there, so add a CodecRef.
                    codecRef = new CodecRef<T, IN, OUT>();
                    codecRegistry.put(name, codecRef);
                }
            }

            // Initialise the CodecRef, and overwrite the registry entry with the real Codec.
            codecRegistry.put(name, codecRef.setIfUninitialised(codecSupp::get));

            return (Codec<T, IN, OUT>)codecRegistry.get(name);
        }
    }

    @Override
    public <T> Codec<Collection<T>, IN, OUT> getCollCodec(
            Class<Collection<T>> collType,
            Codec<T, IN, OUT> elemCodec) {
        final String name = config().classToName(collType, elemCodec.type());
        return getCodec(name, () -> createCollCodec(collType, elemCodec));
    }

    @Override
    public <K, V> Codec<Map<K, V>, IN, OUT> getMapCodec(
            Class<Map<K, V>> mapType,
            Class<K> keyType,
            Class<V> valType) {
        final String name = config().classToName(mapType, keyType, valType);
        return getCodec(name, () -> createMapCodec(mapType, keyType, valType));
    }

    @Override
    public <V> Codec<Map<String, V>, IN, OUT> createMapCodec(
            Class<Map<String, V>> mapType,
            Class<V> valType) {
        final String name = config().classToName(mapType, String.class, valType);
        return getCodec(name, () -> createMapCodec(mapType, valType));
    }

    @Override
    public <V> Codec<Map<String, V>, IN, OUT> getMapCodec(
            Class<Map<String, V>> mapType,
            Class<V> valType) {
        final String name = config().classToName(mapType, String.class, valType);
        return getCodec(name, () -> createMapCodec(mapType, valType));
    }

    @Override
    public <K, V> Codec<Map<K, V>, IN, OUT> getMapCodec(
            Class<Map<K, V>> mapType,
            Codec<K, IN, OUT> keyCodec,
            Codec<V, IN, OUT> valueCodec) {
        final String name = config().classToName(mapType, keyCodec.type(), valueCodec.type());
        return getCodec(name, () -> createMapCodec(mapType, keyCodec, valueCodec));
    }

    @Override
    public <V> Codec<Map<String, V>, IN, OUT> getMapCodec(
            Class<Map<String, V>> mapType,
            Codec<V, IN, OUT> valueCodec) {
        final String name = config().classToName(mapType, String.class, valueCodec.type());
        return getCodec(name, () -> createMapCodec(mapType, valueCodec));
    }

    @Override
    public <K, V> Codec<Map<K, V>, IN, OUT> createMapCodec(
            Class<Map<K, V>> mapType,
            Class<K> keyType,
            Class<V> valType)  {
        final Codec<V, IN, OUT> valueCodec = getCodec(valType);
        if (String.class.equals(keyType)) {
            return (Codec)createMapCodec((Class<Map<String, V>>)(Class)mapType, valueCodec);
        } else {
            final Codec<K, IN, OUT> keyCodec = getCodec(keyType);
            return createMapCodec(mapType, keyCodec, valueCodec);
        }
    }

    @Override
    public <T> Codec<T, IN, OUT> createCodec(Class<T> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(boolean.class)) {
                return (Codec<T, IN, OUT>)booleanCodec();
            } else if (clazz.equals(byte.class)) {
                return (Codec<T, IN, OUT>) byteCodec();
            } else if (clazz.equals(char.class)) {
                return (Codec<T, IN, OUT>) charCodec();
            } else if (clazz.equals(short.class)) {
                return (Codec<T, IN, OUT>) shortCodec();
            } else if (clazz.equals(int.class)) {
                return (Codec<T, IN, OUT>) intCodec();
            } else if (clazz.equals(long.class)) {
                return (Codec<T, IN, OUT>) longCodec();
            } else if (clazz.equals(float.class)) {
                return (Codec<T, IN, OUT>) floatCodec();
            } else if (clazz.equals(double.class)) {
                return (Codec<T, IN, OUT>) doubleCodec();
            } else {
                throw new IllegalStateException("Unexpected primitive type - " + clazz);
            }
        } else {
            if (clazz.isArray()) {
                final Class<?> elemType = clazz.getComponentType();
                if (elemType.equals(boolean.class)) {
                    return (Codec<T, IN, OUT>) booleanArrayCodec();
                } else if (elemType.equals(byte.class)) {
                    return (Codec<T, IN, OUT>) byteArrayCodec();
                } else if (elemType.equals(char.class)) {
                    return (Codec<T, IN, OUT>) charArrayCodec();
                } else if (elemType.equals(short.class)) {
                    return (Codec<T, IN, OUT>) shortArrayCodec();
                } else if (elemType.equals(int.class)) {
                    return (Codec<T, IN, OUT>) intArrayCodec();
                } else if (elemType.equals(long.class)) {
                    return (Codec<T, IN, OUT>) longArrayCodec();
                } else if (elemType.equals(float.class)) {
                    return (Codec<T, IN, OUT>) floatArrayCodec();
                } else if (elemType.equals(double.class)) {
                    return (Codec<T, IN, OUT>) doubleArrayCodec();
                } else {
                    if (elemType.equals(Boolean.class)) {
                        return (Codec<T, IN, OUT>) createObjectArrayCodec((Class)clazz, Boolean.class, booleanCodec());
                    } else if (elemType.equals(Byte.class)) {
                        return (Codec<T, IN, OUT>) createObjectArrayCodec((Class)clazz, Byte.class, byteCodec());
                    } else if (elemType.equals(Character.class)) {
                        return (Codec<T, IN, OUT>) createObjectArrayCodec((Class)clazz, Character.class, charCodec());
                    } else if (elemType.equals(Short.class)) {
                        return (Codec<T, IN, OUT>) createObjectArrayCodec((Class)clazz, Short.class, shortCodec());
                    } else if (elemType.equals(Integer.class)) {
                        return (Codec<T, IN, OUT>) createObjectArrayCodec((Class)clazz, Integer.class, intCodec());
                    } else if (elemType.equals(Long.class)) {
                        return (Codec<T, IN, OUT>) createObjectArrayCodec((Class)clazz, Long.class, longCodec());
                    } else if (elemType.equals(Float.class)) {
                        return (Codec<T, IN, OUT>) createObjectArrayCodec((Class)clazz, Float.class, floatCodec());
                    } else if (elemType.equals(Double.class)) {
                        return (Codec<T, IN, OUT>) createObjectArrayCodec((Class)clazz, Double.class, doubleCodec());
                    } else {
                        final Codec<Object, IN, OUT> elemCodec = getCodec((Class<Object>)elemType);
                        return (Codec<T, IN, OUT>) createObjectArrayCodec((Class)clazz, (Class<Object>) elemType, elemCodec);
                    }
                }
            } else if (clazz.isEnum()) {
                return enumCodec((Class) clazz);
            } else if (clazz.equals(Boolean.class)) {
                return (Codec<T, IN, OUT>) booleanCodec();
            } else if (clazz.equals(Byte.class)) {
                return (Codec<T, IN, OUT>) byteCodec();
            } else if (clazz.equals(Character.class)) {
                return (Codec<T, IN, OUT>) charCodec();
            } else if (clazz.equals(Short.class)) {
                return (Codec<T, IN, OUT>) shortCodec();
            } else if (clazz.equals(Integer.class)) {
                return (Codec<T, IN, OUT>) intCodec();
            } else if (clazz.equals(Long.class)) {
                return (Codec<T, IN, OUT>) longCodec();
            } else if (clazz.equals(Float.class)) {
                return (Codec<T, IN, OUT>) floatCodec();
            } else if (clazz.equals(Double.class)) {
                return (Codec<T, IN, OUT>) doubleCodec();
            } else if (clazz.equals(String.class)) {
                return (Codec<T, IN, OUT>) stringCodec();
            } else if (Map.class.isAssignableFrom(clazz)) {
                final ReflectionUtils.TypeArgs typeArgs = ReflectionUtils.getTypeArgs(clazz, Map.class);
                if (typeArgs.size() == 2) {
                    final Class keyType = typeArgs.get(0);
                    final Class valueType = typeArgs.get(1);
                    return (Codec<T, IN, OUT>) getMapCodec((Class)clazz, keyType, valueType);
                } else {
                    return (Codec<T, IN, OUT>) getMapCodec((Class)clazz, Object.class, Object.class);
                }
            } else if (Collection.class.isAssignableFrom(clazz)) {
                final Codec<Object, IN, OUT> elemCodec;
                final ReflectionUtils.TypeArgs typeArgs = ReflectionUtils.getTypeArgs(clazz, Collection.class);
                if (typeArgs.size() == 1) {
                    final Class<Object> elemType = (Class<Object>) typeArgs.get(0);
                    elemCodec = getCodec(elemType);
                } else {
                    elemCodec = getCodec(Object.class);
                }
                return (Codec<T, IN, OUT>) getCollCodec((Class<Collection<Object>>) clazz, elemCodec);
            } else {
                return createObjectCodec(clazz);
            }
        }
    }

    @Override
    public <T> Codec<T, IN, OUT> createObjectCodec(Class<T> clazz) {
        final Map<String, FieldCodec<IN, OUT>> fieldCodecs = new LinkedHashMap<>();
        Class<?> clazz2 = clazz;
        for (int depth = 0; !clazz2.equals(Object.class); depth++) {
            final Field[] fields = clazz2.getDeclaredFields();
            for (Field field : fields) {
                final int fm = field.getModifiers();
                if (!Modifier.isStatic(fm) && !Modifier.isTransient(fm)) {
                    final String fieldName = getFieldName(field, depth, fieldCodecs.keySet());
                    fieldCodecs.put(fieldName, createFieldCodec(field));
                }
            }
            clazz2 = clazz2.getSuperclass();
        }

        return createObjectCodec(clazz, fieldCodecs);
    }

    @Override
    public <T> Codec<T, IN, OUT> createObjectCodec(
            Class<T> clazz,
            Map<String, FieldCodec<IN, OUT>> fieldCodecs) {
        final class ResultAccumlatorImpl implements ObjectMeta.ResultAccumlator<T> {
            final T val;

            ResultAccumlatorImpl(Class<T> clazz) {
                this.val = getTypeConstructor(clazz).construct();
            }

            @Override
            public T construct() {
                return val;
            }
        }

        final List<ObjectMeta.Field<T, IN, OUT, ResultAccumlatorImpl>> fieldMetas =
                fieldCodecs.entrySet().stream()
                        .map(en -> {
                            final String name = en.getKey();
                            final FieldCodec<IN, OUT> codec = en.getValue();
                            return new ObjectMeta.Field<T, IN, OUT, ResultAccumlatorImpl>() {
                                @Override
                                public String name() {
                                    return name;
                                }

                                @Override
                                public OUT encodeField(T val, OUT out) {
                                    return codec.encodeField(val, out);
                                }

                                @Override
                                public ResultAccumlatorImpl decodeField(ResultAccumlatorImpl acc, IN in) {
                                    codec.decodeField(acc.val, in);
                                    return acc;
                                }
                            };
                        }).collect(toList());

        return createObjectCodec(
                clazz,
                new ObjectMeta<T, IN, OUT, ResultAccumlatorImpl>() {
                        @Override
                        public Iterator<Field<T, IN, OUT, ResultAccumlatorImpl>> iterator() {
                            return fieldMetas.iterator();
                        }

                        @Override
                        public ResultAccumlatorImpl startDecode() {
                            return new ResultAccumlatorImpl(clazz);
                        }

                        @Override
                        public int size() {
                            return fieldMetas.size();
                        }
                }
        );
    }

    @Override
    public <T> ObjectCodecBuilder<T, IN, OUT> createObjectCodecBuilder(Class<T> clazz) {
        return new ObjectCodecBuilder<T, IN, OUT>(this, clazz);
    }

    @Override
    public <T> ObjectCodecBuilder<T, IN, OUT> objectCodecDeferredRegister(Class<T> clazz) {
        return new ObjectCodecBuilder<T, IN, OUT>(this, clazz) {
            @Override
            protected Codec<T, IN, OUT> registration(Codec<T, IN, OUT> codec) {
                registerCodec(clazz, codec);
                return codec;
            }
        };
    }

    @Override
    public <T> Codec<T, IN, OUT> createObjectCodec(
            Class<T> clazz,
            Map<String, ObjectCodecBuilder.FieldCodec<T, IN, OUT>> fieldCodecs,
            Functions.F<Object[], T> ctor) {
        final class ResultAccumlatorImpl implements ObjectMeta.ResultAccumlator<T> {
            final Object[] ctorArgs;
            int i = 0;

            ResultAccumlatorImpl(Class<T> clazz) {
                this.ctorArgs = new Object[fieldCodecs.size()];
            }

            @Override
            public T construct() {
                return ctor.apply(ctorArgs);
            }
        }

        final List<ObjectMeta.Field<T, IN, OUT, ResultAccumlatorImpl>> fieldMetas =
                fieldCodecs.entrySet().stream()
                        .map(en -> {
                            final String name = en.getKey();
                            final ObjectCodecBuilder.FieldCodec<T, IN, OUT> codec = en.getValue();
                            return new ObjectMeta.Field<T, IN, OUT, ResultAccumlatorImpl>() {
                                @Override
                                public String name() {
                                    return name;
                                }

                                @Override
                                public OUT encodeField(T val, OUT out) {
                                    return codec.encodeField(val, out);
                                }

                                @Override
                                public ResultAccumlatorImpl decodeField(ResultAccumlatorImpl acc, IN in) {
                                    acc.ctorArgs[acc.i++] = codec.decodeField(in);
                                    return acc;
                                }
                            };
                        }).collect(toList());

        return createObjectCodec(
                clazz,
                new ObjectMeta<T, IN, OUT, ResultAccumlatorImpl>() {
                        @Override
                        public Iterator<Field<T, IN, OUT, ResultAccumlatorImpl>> iterator() {
                            return fieldMetas.iterator();
                        }

                        @Override
                        public ResultAccumlatorImpl startDecode() {
                            return new ResultAccumlatorImpl(clazz);
                        }

                        @Override
                        public int size() {
                            return fieldMetas.size();
                        }
                }
        );
    }

    @Override
    public String getFieldName(Field field, int depth, Set<String> existingNames) {
        String name = field.getName();
        while (existingNames.contains(name)) {
            name = "*" + name;
        }
        return name;
    }

    @Override
    public <T> FieldCodec<IN, OUT> createFieldCodec(Field field) {
        final Class<T> clazz = (Class<T>)field.getType();
        if (clazz.isPrimitive()) {
            if (clazz.equals(boolean.class)) {
                return new FieldCodec.BooleanFieldCodec<IN, OUT>(field, booleanCodec());
            } else if (clazz.equals(byte.class)) {
                return new FieldCodec.ByteFieldCodec<IN, OUT>(field, byteCodec());
            } else if (clazz.equals(char.class)) {
                return new FieldCodec.CharFieldCodec<IN, OUT>(field, charCodec());
            } else if (clazz.equals(short.class)) {
                return new FieldCodec.ShortFieldCodec<IN, OUT>(field, shortCodec());
            } else if (clazz.equals(int.class)) {
                return new FieldCodec.IntegerFieldCodec<IN, OUT>(field, intCodec());
            } else if (clazz.equals(long.class)) {
                return new FieldCodec.LongFieldCodec<IN, OUT>(field, longCodec());
            } else if (clazz.equals(float.class)) {
                return new FieldCodec.FloatFieldCodec<IN, OUT>(field, floatCodec());
            } else if (clazz.equals(double.class)) {
                return new FieldCodec.DoubleFieldCodec<IN, OUT>(field, doubleCodec());
            } else {
                throw new IllegalStateException("Unexpected primitive type - " + clazz);
            }
        } else if (clazz.isArray()) {
            final Class<?> elemType = clazz.getComponentType();
            if (elemType.equals(boolean.class)) {
                return new FieldCodec.BooleanArrayFieldCodec<IN, OUT>(field, booleanArrayCodec());
            } else if (elemType.equals(byte.class)) {
                return new FieldCodec.ByteArrayFieldCodec<IN, OUT>(field, byteArrayCodec());
            } else if (elemType.equals(char.class)) {
                return new FieldCodec.CharArrayFieldCodec<IN, OUT>(field, charArrayCodec());
            } else if (elemType.equals(short.class)) {
                return new FieldCodec.ShortArrayFieldCodec<IN, OUT>(field, shortArrayCodec());
            } else if (elemType.equals(int.class)) {
                return new FieldCodec.IntegerArrayFieldCodec<IN, OUT>(field, intArrayCodec());
            } else if (elemType.equals(long.class)) {
                return new FieldCodec.LongArrayFieldCodec<IN, OUT>(field, longArrayCodec());
            } else if (elemType.equals(float.class)) {
                return new FieldCodec.FloatArrayFieldCodec<IN, OUT>(field, floatArrayCodec());
            } else if (elemType.equals(double.class)) {
                return new FieldCodec.DoubleArrayFieldCodec<IN, OUT>(field, doubleArrayCodec());
            } else {
                final Codec<Object[], IN, OUT> codec = getCodec((Class<Object[]>)clazz);
                return new FieldCodec.ObjectArrayFieldCodec<>(field, codec);
            }
        } else {
            Codec<T, IN, OUT> codec;

            if (clazz.isEnum() ||
                    clazz.equals(Boolean.class) ||
                    clazz.equals(Byte.class) ||
                    clazz.equals(Character.class) ||
                    clazz.equals(Short.class) ||
                    clazz.equals(Integer.class) ||
                    clazz.equals(Long.class) ||
                    clazz.equals(Float.class) ||
                    clazz.equals(Double.class) ||
                    clazz.equals(String.class)) {
                codec = getCodec(clazz);
            } else if (Map.class.isAssignableFrom(clazz)) {
                final ReflectionUtils.TypeArgs typeArgs = ReflectionUtils.getTypeArgs(field, Map.class);
                if (typeArgs.size() == 2) {
                    final Class keyType = typeArgs.get(0);
                    final Class valueType = typeArgs.get(1);
                    codec = (Codec<T, IN, OUT>) getMapCodec((Class)clazz, keyType, valueType);
                } else {
                    codec = (Codec<T, IN, OUT>) getMapCodec((Class)clazz, Object.class, Object.class);
                }
            } else if (Collection.class.isAssignableFrom(clazz)) {
                final Codec<Object, IN, OUT> elemCodec;
                final ReflectionUtils.TypeArgs typeArgs = ReflectionUtils.getTypeArgs(field, Collection.class);
                if (typeArgs.size() == 1) {
                    final Class<Object> elemType = (Class<Object>) typeArgs.get(0);
                    elemCodec = getCodec(elemType);
                } else {
                    elemCodec = getCodec(Object.class);
                }
                codec = (Codec<T, IN, OUT>) getCollCodec((Class<Collection<Object>>) clazz, elemCodec);
            } else {
                codec = getCodec(clazz);
            }

            return new FieldCodec.ObjectFieldCodec<>(field, codec);
        }
    }
}

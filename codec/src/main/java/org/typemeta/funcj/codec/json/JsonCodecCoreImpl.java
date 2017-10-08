package org.typemeta.funcj.codec.json;

import org.typemeta.funcj.codec.*;
import org.typemeta.funcj.control.Exceptions;
import org.typemeta.funcj.json.*;
import org.typemeta.funcj.util.*;

import java.lang.reflect.Array;
import java.util.*;

@SuppressWarnings("unchecked")
public class JsonCodecCoreImpl extends BaseCodecCore<JSValue> implements JsonCodecCore {

    public JsonCodecCoreImpl() {
    }

    public String typeFieldName() {
        return "@type";
    }

    public String keyFieldName() {
        return "@key";
    }

    public String valueFieldName() {
        return "@value";
    }

    private final Codec.NullCodec<JSValue> nullCodec = new Codec.NullCodec<JSValue>() {
        @Override
        public boolean isNull(JSValue enc) {
            return enc.isNull();
        }

        @Override
        public JSValue encode(Object val, JSValue enc) {
            return JSAPI.nul();
        }

        @Override
        public Object decode(JSValue enc) {
            enc.asNull();
            return null;
        }
    };

    public <T> JSValue encode(T val) {
        return encode((Class<T>)val.getClass(), val);
    }

    public <T> JSValue encode(Class<T> type, T val) {
        return super.encode(type, val, null);
    }

    @Override
    public JsonCodecException wrapException(Exception ex) {
        if (ex instanceof JsonCodecException) {
            return (JsonCodecException)ex;
        } else {
            return new JsonCodecException(ex);
        }
    }

    @Override
    public Codec.NullCodec<JSValue> nullCodec() {
        return nullCodec;
    }

    protected final Codec.BooleanCodec<JSValue> booleanCodec = new Codec.BooleanCodec<JSValue>() {

        @Override
        public JSValue encodePrim(boolean val) {
            return JSAPI.bool(val);
        }

        @Override
        public boolean decodePrim(JSValue enc) {
            return enc.asBool().getValue();
        }
    };

    @Override
    public Codec.BooleanCodec<JSValue> booleanCodec() {
        return booleanCodec;
    }

    protected final Codec<boolean[], JSValue> booleanArrayCodec = new Codec<boolean[], JSValue>() {

        @Override
        public JSValue encode(boolean[] vals, JSValue enc) {
            final List<JSValue> nodes = new ArrayList<>(vals.length);
            for (boolean val : vals) {
                nodes.add(booleanCodec().encode(val, enc));
            }
            return JSAPI.arr(nodes);
        }

        @Override
        public boolean[] decode(JSValue enc) {
            final JSArray arrNode = enc.asArray();
            final boolean[] vals = new boolean[arrNode.size()];
            int i = 0;
            for (JSValue node : arrNode) {
                vals[i++] = booleanCodec().decode(node);
            }
            return vals;
        }
    };

    @Override
    public Codec<boolean[], JSValue> booleanArrayCodec() {
        return booleanArrayCodec;
    }

    protected final Codec.ByteCodec<JSValue> byteCodec = new Codec.ByteCodec<JSValue>() {

        @Override
        public JSValue encodePrim(byte val) {
            return JSAPI.num(val);
        }

        @Override
        public byte decodePrim(JSValue enc) {
            return enc.asNumber().toByte();
        }
    };

    @Override
    public Codec.ByteCodec<JSValue> byteCodec() {
        return byteCodec;
    }

    protected final Codec<byte[], JSValue> byteArrayCodec = new Codec<byte[], JSValue>() {

        @Override
        public JSValue encode(byte[] vals, JSValue enc) {
            final List<JSValue> nodes = new ArrayList<>(vals.length);
            for (byte val : vals) {
                nodes.add(byteCodec().encode(val, enc));
            }
            return JSAPI.arr(nodes);
        }

        @Override
        public byte[] decode(JSValue enc) {
            final JSArray arrNode = enc.asArray();
            final byte[] vals = new byte[arrNode.size()];
            int i = 0;
            for (JSValue node : arrNode) {
                vals[i++] = byteCodec().decode(node);
            }
            return vals;
        }
    };

    @Override
    public Codec<byte[], JSValue> byteArrayCodec() {
        return byteArrayCodec;
    }

    protected final Codec.CharCodec<JSValue> charCodec = new Codec.CharCodec<JSValue>() {

        @Override
        public JSValue encodePrim(char val) {
            return JSAPI.str(String.valueOf(val));
        }

        @Override
        public char decodePrim(JSValue enc) {
            final String s = enc.asString().getValue();
            if (s.length() == 1) {
                return s.charAt(0);
            } else {
                throw new JsonCodecException(
                        "Unexpected String of length " + s.length() + " when decoding a char");
            }
        }
    };

    @Override
    public Codec.CharCodec<JSValue> charCodec() {
        return charCodec;
    }

    protected final Codec<char[], JSValue> charArrayCodec = new Codec<char[], JSValue>() {

        @Override
        public JSValue encode(char[] vals, JSValue enc) {
            final List<JSValue> nodes = new ArrayList<>(vals.length);
            for (char val : vals) {
                nodes.add(charCodec().encode(val, enc));
            }
            return JSAPI.arr(nodes);
        }

        @Override
        public char[] decode(JSValue enc) {
            final JSArray arrNode = enc.asArray();
            final char[] vals = new char[arrNode.size()];
            int i = 0;
            for (JSValue node : arrNode) {
                vals[i++] = charCodec().decode(node);
            }
            return vals;
        }
    };

    @Override
    public Codec<char[], JSValue> charArrayCodec() {
        return charArrayCodec;
    }

    protected final Codec.ShortCodec<JSValue> shortCodec = new Codec.ShortCodec<JSValue>() {

        @Override
        public JSValue encodePrim(short val) {
            return JSAPI.num(val);
        }

        @Override
        public short decodePrim(JSValue enc) {
            return enc.asNumber().toShort();
        }
    };

    @Override
    public Codec.ShortCodec<JSValue> shortCodec() {
        return shortCodec;
    }

    protected final Codec<short[], JSValue> shortArrayCodec = new Codec<short[], JSValue>() {

        @Override
        public JSValue encode(short[] vals, JSValue enc) {
            final List<JSValue> nodes = new ArrayList<>(vals.length);
            for (short val : vals) {
                nodes.add(shortCodec().encode(val, enc));
            }
            return JSAPI.arr(nodes);
        }

        @Override
        public short[] decode(JSValue enc) {
            final JSArray arrNode = enc.asArray();
            final short[] vals = new short[arrNode.size()];
            int i = 0;
            for (JSValue node : arrNode) {
                vals[i++] = shortCodec().decode(node);
            }
            return vals;
        }
    };

    @Override
    public Codec<short[], JSValue> shortArrayCodec() {
        return shortArrayCodec;
    }

    protected final Codec.IntCodec<JSValue> intCodec = new Codec.IntCodec<JSValue>() {

        @Override
        public JSValue encodePrim(int val) {
            return JSAPI.num(val);
        }

        @Override
        public int decodePrim(JSValue enc) {
            return enc.asNumber().toInt();
        }
    };

    @Override
    public Codec.IntCodec<JSValue> intCodec() {
        return intCodec;
    }

    protected final Codec<int[], JSValue> intArrayCodec = new Codec<int[], JSValue>() {

        @Override
        public JSValue encode(int[] vals, JSValue enc) {
            final List<JSValue> nodes = new ArrayList<>(vals.length);
            for (int val : vals) {
                nodes.add(intCodec().encode(val, enc));
            }
            return JSAPI.arr(nodes);
        }

        @Override
        public int[] decode(JSValue enc) {
            final JSArray arrNode = enc.asArray();
            final int[] vals = new int[arrNode.size()];
            int i = 0;
            for (JSValue node : arrNode) {
                vals[i++] = intCodec().decode(node);
            }
            return vals;
        }
    };

    @Override
    public Codec<int[], JSValue> intArrayCodec() {
        return intArrayCodec;
    }

    protected final Codec.LongCodec<JSValue> longCodec = new Codec.LongCodec<JSValue>() {

        @Override
        public JSValue encodePrim(long val) {
            return JSAPI.num(val);
        }

        @Override
        public long decodePrim(JSValue enc) {
            return enc.asNumber().toLong();
        }
    };

    @Override
    public Codec.LongCodec<JSValue> longCodec() {
        return longCodec;
    }

    protected final Codec<long[], JSValue> longArrayCodec = new Codec<long[], JSValue>() {

        @Override
        public JSValue encode(long[] vals, JSValue enc) {
            final List<JSValue> nodes = new ArrayList<>(vals.length);
            for (long val : vals) {
                nodes.add(longCodec().encode(val, enc));
            }
            return JSAPI.arr(nodes);
        }

        @Override
        public long[] decode(JSValue enc) {
            final JSArray arrNode = enc.asArray();
            final long[] vals = new long[arrNode.size()];
            int i = 0;
            for (JSValue node : arrNode) {
                vals[i++] = longCodec().decode(node);
            }
            return vals;
        }
    };

    @Override
    public Codec<long[], JSValue> longArrayCodec() {
        return longArrayCodec;
    }

    protected final Codec.FloatCodec<JSValue> floatCodec = new Codec.FloatCodec<JSValue>() {

        @Override
        public JSValue encodePrim(float val) {
            return JSAPI.num(val);
        }

        @Override
        public float decodePrim(JSValue enc) {
            return enc.asNumber().toFloat();
        }
    };

    @Override
    public Codec.FloatCodec<JSValue> floatCodec() {
        return floatCodec;
    }

    protected final Codec<float[], JSValue> floatArrayCodec = new Codec<float[], JSValue>() {

        @Override
        public JSValue encode(float[] vals, JSValue enc) {
            final List<JSValue> nodes = new ArrayList<>(vals.length);
            for (float val : vals) {
                nodes.add(floatCodec().encode(val, enc));
            }
            return JSAPI.arr(nodes);
        }

        @Override
        public float[] decode(JSValue enc) {
            final JSArray arrNode = enc.asArray();
            final float[] vals = new float[arrNode.size()];
            int i = 0;
            for (JSValue node : arrNode) {
                vals[i++] = floatCodec().decode(node);
            }
            return vals;
        }
    };

    @Override
    public Codec<float[], JSValue> floatArrayCodec() {
        return floatArrayCodec;
    }

    protected final Codec.DoubleCodec<JSValue> doubleCodec = new Codec.DoubleCodec<JSValue>() {

        @Override
        public JSValue encodePrim(double val) {
            return JSAPI.num(val);
        }

        @Override
        public double decodePrim(JSValue enc) {
            return enc.asNumber().oDouble();
        }
    };

    @Override
    public Codec.DoubleCodec<JSValue> doubleCodec() {
        return doubleCodec;
    }

    protected final Codec<double[], JSValue> doubleArrayCodec = new Codec<double[], JSValue>() {

        @Override
        public JSValue encode(double[] vals, JSValue enc) {
            final List<JSValue> nodes = new ArrayList<>(vals.length);
            for (double val : vals) {
                nodes.add(doubleCodec().encode(val, enc));
            }
            return JSAPI.arr(nodes);
        }

        @Override
        public double[] decode(JSValue enc) {
            final JSArray arrNode = enc.asArray();
            final double[] vals = new double[arrNode.size()];
            int i = 0;
            for (JSValue node : arrNode) {
                vals[i++] = doubleCodec().decode(node);
            }
            return vals;
        }
    };

    @Override
    public Codec<double[], JSValue> doubleArrayCodec() {
        return doubleArrayCodec;
    }

    protected final Codec<String, JSValue> stringCodec = new Codec<String, JSValue>() {
        @Override
        public JSValue encode(String val, JSValue enc) {
            return JSAPI.str(val);
        }

        @Override
        public String decode(JSValue enc) {
            return enc.asString().getValue();
        }
    };

    @Override
    public Codec<String, JSValue> stringCodec() {
        return stringCodec;
    }

    @Override
    public <EM extends Enum<EM>> Codec<EM, JSValue> enumCodec(Class<? super EM> enumType) {
        return new Codec<EM, JSValue>() {
            @Override
            public JSValue encode(EM val, JSValue enc) {
                return JSAPI.str(val.name());
            }

            @Override
            public EM decode(Class<EM> dynType, JSValue enc) {
                final Class<EM> type = dynType != null ? dynType : (Class<EM>)enumType;
                return EM.valueOf(type, enc.asString().getValue());
            }
        };
    }

    @Override
    public <V> Codec<Map<String, V>, JSValue> mapCodec(Codec<V, JSValue> valueCodec) {
        return new JsonMapCodecs.StringMapCodec<V>(this, valueCodec);
    }

    @Override
    public <K, V> Codec<Map<K, V>, JSValue> mapCodec(Codec<K, JSValue> keyCodec, Codec<V, JSValue> valueCodec) {
        return new JsonMapCodecs.MapCodec<K, V>(this, keyCodec, valueCodec);
    }

    @Override
    public <T> Codec<Collection<T>, JSValue> collCodec(Class<T> elemType, Codec<T, JSValue> elemCodec) {
        return new Codec<Collection<T>, JSValue>() {
            @Override
            public JSValue encode(Collection<T> vals, JSValue enc) {
                final List<JSValue> nodes = new ArrayList<>(vals.size());
                for (T val : vals) {
                    nodes.add(elemCodec.encode(val, enc));
                }
                return JSAPI.arr(nodes);
            }

            @Override
            public Collection<T> decode(Class<Collection<T>> dynType, JSValue enc) {
                final Class<T> dynElemType = (Class<T>)dynType.getComponentType();
                final JSArray arrNode = enc.asArray();
                final Collection<T> vals = Exceptions.wrap(
                        () -> getTypeConstructor(dynType).construct(),
                        ex -> wrapException(ex));

                for (JSValue node : arrNode) {
                    vals.add(elemCodec.decode(dynElemType, node));
                }

                return vals;

            }
        };
    }

    @Override
    public <T> Codec<T[], JSValue> objectArrayCodec(Class<T> elemType, Codec<T, JSValue> elemCodec) {
        return new Codec<T[], JSValue>() {
            @Override
            public JSValue encode(T[] vals, JSValue enc) {
                final List<JSValue> nodes = new ArrayList<>(vals.length);
                for (T val : vals) {
                    nodes.add(elemCodec.encode(val, enc));
                }
                return JSAPI.arr(nodes);
            }

            @Override
            public T[] decode(Class<T[]> dynType, JSValue enc) {
                final Class<T> dynElemType = (Class<T>)dynType.getComponentType();
                final JSArray arrNode = enc.asArray();
                final T[] vals = (T[]) Array.newInstance(elemType, arrNode.size());
                int i = 0;
                for (JSValue node : arrNode) {
                    vals[i++] = elemCodec.decode(dynElemType, node);
                }
                return vals;
            }
        };
    }

    @Override
    public <T> Codec<T, JSValue> dynamicCodec(Class<T> stcType) {
        return new Codec<T, JSValue>() {
            @Override
            public JSValue encode(T val, JSValue enc) {
                final Class<? extends T> dynType = (Class<? extends T>)val.getClass();
                if (dynType.equals(stcType)) {
                    return JsonCodecCoreImpl.this.getNullUnsafeCodec(stcType).encode(val, enc);
                } else {
                    return JSAPI.obj(
                            JSAPI.field(
                                    typeFieldName(),
                                    JSAPI.str(classToName(dynType))),
                            JSAPI.field(
                                    valueFieldName(),
                                    encode2(JsonCodecCoreImpl.this.getNullUnsafeCodec(dynType), val, enc))
                    );
                }
            }

            protected <S extends T> JSValue encode2(Codec<S, JSValue> codec, T val, JSValue enc) {
                return codec.encode((S)val, enc);
            }

            @Override
            public T decode(JSValue enc) {
                if (enc.isObject()) {
                    final JSObject objNode = enc.asObject();
                    final String typeFieldName = typeFieldName();
                    final String valueFieldName = valueFieldName();
                    if (objNode.size() == 2 &&
                            objNode.containsName(typeFieldName) &&
                            objNode.containsName(valueFieldName)) {
                        final JSValue typeNode = objNode.get(typeFieldName());
                        final JSValue valueNode = objNode.get(valueFieldName());
                        return decode2(valueNode, nameToClass(typeNode.asString().getValue()));
                    }
                }

                final Codec<T, JSValue> codec = JsonCodecCoreImpl.this.getNullUnsafeCodec(stcType);
                return codec.decode(stcType, enc);
            }

            protected <S extends T> S decode2(JSValue in, Class<S> dynType) {
                final Codec<S, JSValue> codec = JsonCodecCoreImpl.this.getNullUnsafeCodec(dynType);
                return codec.decode(dynType, in);
            }
        };
    }

    @Override
    public <T> Codec<T, JSValue> dynamicCodec(Codec<T, JSValue> codec, Class<T> stcType) {
        return new Codec<T, JSValue>() {
            @Override
            public JSValue encode(T val, JSValue enc) {
                final Class<? extends T> dynType = (Class<? extends T>)val.getClass();
                if (dynType.equals(stcType)) {
                    return codec.encode(val, enc);
                } else {
                    return JSAPI.obj(
                            JSAPI.field(typeFieldName(), JSAPI.str(classToName(dynType))),
                            JSAPI.field(valueFieldName(), codec.encode(val, enc))
                    );
                }
            }

            @Override
            public T decode(JSValue enc) {
                if (enc.isObject()) {
                    final JSObject objNode = enc.asObject();
                    final String typeFieldName = typeFieldName();
                    final String valueFieldName = valueFieldName();
                    if (objNode.size() == 2 &&
                            objNode.containsName(typeFieldName) &&
                            objNode.containsName(valueFieldName)) {
                        final JSValue typeNode = objNode.get(typeFieldName());
                        final Class<?> dynType = nameToClass(typeNode.asString().getValue());
                        final JSValue valueNode = objNode.get(valueFieldName());
                        return codec.decode((Class<T>) dynType, valueNode);
                    }
                }

                return codec.decode(stcType, enc);
            }
        };
    }

    @Override
    public <T, RA extends ObjectMeta.ResultAccumlator<T>> Codec<T, JSValue> createObjectCodec(
            ObjectMeta<T, JSValue, RA> objMeta) {
        return new Codec<T, JSValue>() {
            @Override
            public JSValue encode(T val, JSValue enc) {
                final List<JSObject.Field> fields =
                        Functors.map(
                                field -> JSAPI.field(field.name(), field.encodeField(val, enc)),
                                objMeta
                        );
                return JSAPI.obj(fields);
            }

            @Override
            public T decode(Class<T> dynType, JSValue enc) {
                final JSObject objNode = enc.asObject();
                return Folds.foldLeft(
                        (acc, field) -> field.decodeField(acc, objNode.get(field.name())),
                        objMeta.startDecode(dynType),
                        objMeta
                ).construct();
            }
        };
    }
}
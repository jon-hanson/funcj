package org.typemeta.funcj.codec.json;

import org.typemeta.funcj.codec.CodecCoreIntl;
import org.typemeta.funcj.codec.Codecs;
import org.typemeta.funcj.json.model.JSAPI;
import org.typemeta.funcj.json.model.JsValue;

import java.util.Optional;

import static org.typemeta.funcj.util.Exceptions.unwrap;
import static org.typemeta.funcj.util.Exceptions.wrap;

@SuppressWarnings("unchecked")
public class JsonCodecs {
    public static JsonCodecCoreImpl registerAll(JsonCodecCoreImpl core) {
        core.registerCodec(Optional.class, new JsonCodecs.OptionalCodec(core));
        return Codecs.registerAll(core);
    }

    public static class OptionalCodec<T> extends Codecs.CodecBase<Optional<T>, JsValue, JsValue> {

        protected OptionalCodec(CodecCoreIntl<JsValue, JsValue> core) {
            super(core);
        }

        @Override
        public JsValue encode(Optional<T> val, JsValue enc) {
            return unwrap(() -> val.map(t -> wrap(() -> core.dynamicCodec().encode(t, enc)))
                    .orElseGet(JSAPI::obj));
        }

        @Override
        public Optional<T> decode(JsValue enc) {
            if (enc.isObject() && enc.asObject().isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of((T)core.dynamicCodec().decode(enc));
            }
        }
    }
}

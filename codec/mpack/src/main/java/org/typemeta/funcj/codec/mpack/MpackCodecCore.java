package org.typemeta.funcj.codec.mpack;

import org.typemeta.funcj.codec.CodecAPI;
import org.typemeta.funcj.codec.CodecCoreDelegate;
import org.typemeta.funcj.codec.impl.CodecCoreImpl;
import org.typemeta.funcj.codec.mpack.MpackTypes.*;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for classes which implement an encoding via MessagePack.
 */
public class MpackCodecCore
        extends CodecCoreDelegate<InStream, OutStream, Config>
        implements CodecAPI.IO {

    public MpackCodecCore(MpackCodecFormat format) {
        super(new CodecCoreImpl<>(format));
    }

    public MpackCodecCore(Config config) {
        this(new MpackCodecFormat(config));
    }

    public MpackCodecCore() {
        this(new MpackConfigImpl());
    }

    /**
     * Encode the given value into byte data and write the results to the {@link OutputStream} object.
     * The static type determines whether type information is written to recover the value's
     * dynamic type.
     * @param type      the static type of the value
     * @param value     the value to be encoded
     * @param os        the output stream to which the byte data is written
     * @param <T>       the static type of the value
     * @return          the output stream
     */
    @Override
    public <T> OutputStream encode(Class<? super T> type, T value, OutputStream os) {
        try (final OutStream out = MpackTypes.outputOf(os)) {
            encodeImpl(type, value, out);
            return os;
        }
    }

    /**
     * Decode a value by reading byte data from the given {@link InputStream} object.
     * @param type      the static type of the value to be decoded.
     * @param is        the input stream from which byte data is read
     * @param <T>       the static type of the value
     * @return          the decoded value
     */
    @Override
    public <T> T decode(Class<? super T> type, InputStream is) {
        try (final InStream in = MpackTypes.inputOf(is)) {
            return decodeImpl(type, in);
        }
    }
}

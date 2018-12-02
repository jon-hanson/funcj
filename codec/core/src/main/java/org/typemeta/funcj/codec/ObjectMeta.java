package org.typemeta.funcj.codec;

import org.typemeta.funcj.codec.CodecFormat.Input;
import org.typemeta.funcj.codec.CodecFormat.Output;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Abstraction for creating an object fby accumulating the fields that comprise it.
 * @param <T>       the type of object being created
 * @param <IN>      the encoded input type
 * @param <OUT>     the encoded output type
 * @param <RA>      the concrete implementation type for {@code ResultAccumlator}
 */
public interface ObjectMeta<
        T,
        IN extends Input<IN>,
        OUT extends Output<OUT>,
        RA extends ObjectMeta.ResultAccumlator<T>
        > extends Iterable<ObjectMeta.Field<T, IN, OUT, RA>> {
    interface ResultAccumlator<T> {
        T construct();
    }

    interface Field<T, IN, OUT, RA> {
        String name();
        OUT encodeField(T val, OUT out);
        RA decodeField(RA acc, IN in);
    }

    RA startDecode();

    default Stream<Field<T, IN, OUT, RA>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
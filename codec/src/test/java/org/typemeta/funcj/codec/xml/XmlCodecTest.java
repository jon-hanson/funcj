package org.typemeta.funcj.codec.xml;

import org.junit.Assert;
import org.typemeta.funcj.codec.*;

import java.io.*;

public class XmlCodecTest extends TestBase {
    final static XmlCodecCore codec = Codecs.xmlCodec();

    public static final String rootElemName = "root";

    static {
        prepareCodecCore(codec);
        codec.registerTypeConstructor(TestTypes.NoEmptyCtor.class, () -> TestTypes.NoEmptyCtor.create(false));
    }

    @Override
    protected <T> void roundTrip(T val, Class<T> clazz) {
        final StringWriter sw = new StringWriter();
        codec.encode(clazz, val, sw, rootElemName);

        final StringReader sr = new StringReader(sw.toString());

        if (printData) {
            System.out.println(sw);
        }

        final T val2 = codec.decode(clazz, sr, rootElemName);

        if (!printData && !val.equals(val2)) {
            System.out.println(sw);
        }

        Assert.assertEquals(val, val2);
    }
}

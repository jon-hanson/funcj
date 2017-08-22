package org.funcj.codec;

import org.funcj.codec.TestDataBase.NoEmptyCtor;
import org.funcj.codec.xml.XmlCodecCore;
import org.funcj.control.Exceptions;
import org.junit.Assert;
import org.w3c.dom.*;

import javax.xml.parsers.*;

import static org.funcj.codec.xml.XmlUtils.nodeToString;

public class XmlCodecTest extends TestBase {
    final static XmlCodecCore codec = XmlCodecCore.of();

    public static final DocumentBuilder docBuilder;

    static {
        docBuilder = Exceptions.wrap(
                () -> DocumentBuilderFactory.newInstance().newDocumentBuilder());
    }
    static {
        codec.registerTypeConstructor(NoEmptyCtor.class, () -> NoEmptyCtor.create(false));
        registerLocalDateCodec(codec);
        registerCustomCodec(codec);
    }

    @Override
    protected <T> void roundTrip(T val, Class<T> clazz) {

        final Document doc = docBuilder.newDocument();

        final Element elem = codec.encode(clazz, val, doc.createElement(clazz.getSimpleName()));

        final T val2 = codec.decode(clazz, elem);

        if (printData || !val.equals(val2)) {
            java.lang.System.out.println(nodeToString(elem, true));
        }

        Assert.assertEquals(val, val2);
    }
}
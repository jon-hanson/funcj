package org.typemeta.funcj.codec.jsons;

import org.junit.Test;

public class JsonTokeniserTest {
    @Test
    public void test() throws Exception {
        FileUtils.openResource("/example.json")
                .map(JsonTokeniser::new)
                .map(jt -> {
                    JsonTokeniser.Event ev;
                    while ((ev = jt.getNextEvent()) != JsonTokeniser.Event.Enum.EOF) {
                        System.out.println(ev);
                    }
                    return 0;
                }).getOrThrow();
    }
}

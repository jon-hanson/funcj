package org.typemeta.funcj.control;

import org.junit.*;

import static org.typemeta.funcj.control.StateRec.*;

public class StateRecTest {
    @Test
    public void testState() {
        final String r =
            put("efgh")
                .flatMap(u -> get())
                .flatMap(s -> put(s + "1234"))
                .flatMap(u -> get())
                .eval("abcd");

        Assert.assertEquals("efgh1234", r);
    }

    @Test
    public void testState2() {
        final String r =
            StateRec.<String>get()
                .flatMap(s -> put(s + "1234"))
                .flatMap(u -> get())
                .eval("abcd");
        Assert.assertEquals("abcd1234", r);
    }
}
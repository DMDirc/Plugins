/*
 * Copyright (c) 2006-2014 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.calc;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class EvaluatorTest {

    private final String input;
    private final float output;

    public EvaluatorTest(String input, String output) {
        this.input = input;
        this.output = Float.parseFloat(output);
    }

    @Test
    public void testEvaluator() throws ParseException {
        final Parser p = new Parser(new Lexer(input));
        final Evaluator e = new Evaluator(p.parse());
        assertEquals(output, e.evaluate().floatValue(), 0.01);
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        final Object[][] data = {
            {"1", "1"},
            {"-1", "-1"},
            {"1+1", "2"},
            {"(1)", "1"},
            {"(((1)))", "1"},
            {"2(1*1)", "2"},
            {"2+2*3/4-1", "2.5"},
            {"1.0000(17.5+0.5)(1.000)", "18"},
            {"2^3", "8"},
            {"+3", "3"},
            {"20%5", "0"},
        };

        return Arrays.asList(data);
    }

}

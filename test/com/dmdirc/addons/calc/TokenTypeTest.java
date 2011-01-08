/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import java.util.List;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokenTypeTest {

    @Test
    public void testSearchValueOf() {
        List<TokenType> res = TokenType.searchValueOf("NUMBER_*");
        assertEquals(2, res.size());
        assertTrue(res.contains(TokenType.NUMBER_FLOAT));
        assertTrue(res.contains(TokenType.NUMBER_INT));

        res = TokenType.searchValueOf("START");
        assertEquals(1, res.size());
        assertTrue(res.contains(TokenType.START));
    }

    @Test
    public void testMatch() {
        assertEquals(0, TokenType.START.match("Foo", 0));
        assertEquals(-1, TokenType.START.match("Foo", 1));
        assertEquals(1, TokenType.NUMBER_INT.match("1+3", 0));
        assertEquals(4, TokenType.NUMBER_INT.match("1234+3", 0));
        assertEquals(3, TokenType.END.match("1+3", 3));
        assertEquals(-1, TokenType.END.match("1+3", 2));
    }

    @Test
    public void testGetFollowers() {
        assertTrue(TokenType.END.getFollowers().isEmpty());
        assertFalse(TokenType.START.getFollowers().isEmpty());
    }

    /** Checks that tokens which can't be evaluated throw an exception. */
    @Test(expected=AbstractMethodError.class)
    public void testNonEvaluatable() {
        TokenType.BRACKET_CLOSE.evaluate(null);
    }

}

/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import static org.junit.Assert.*;

public class LexerTest {

    @Test
    public void testBasicNumber() throws ParseException {
        final Lexer lexer = new Lexer("123");
        final List<Token> tokens = lexer.tokenise();

        assertEquals(3, tokens.size());
        assertEquals(TokenType.START, tokens.get(0).getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(1).getType());
        assertEquals("123", tokens.get(1).getContent());
        assertEquals(TokenType.END, tokens.get(2).getType());
    }

    @Test
    public void testComplexString() throws ParseException {
        final Lexer lexer = new Lexer("(123 / 2.0) * ((3)+\t   (-1))");
        final List<Token> tokens = lexer.tokenise();

        System.out.println(Arrays.toString(tokens.toArray()));

        assertEquals(18, tokens.size());
        assertEquals(TokenType.START, tokens.get(0).getType());
        assertEquals(TokenType.BRACKET_OPEN, tokens.get(1).getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(2).getType());
        assertEquals("123", tokens.get(2).getContent());
        assertEquals(TokenType.OP_DIVIDE, tokens.get(3).getType());
        assertEquals(TokenType.NUMBER_FLOAT, tokens.get(4).getType());
        assertEquals("2.0", tokens.get(4).getContent());
        assertEquals(TokenType.BRACKET_CLOSE, tokens.get(5).getType());
        assertEquals(TokenType.OP_MULT, tokens.get(6).getType());
        assertEquals(TokenType.BRACKET_OPEN, tokens.get(7).getType());
        assertEquals(TokenType.BRACKET_OPEN, tokens.get(8).getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(9).getType());
        assertEquals("3", tokens.get(9).getContent());
        assertEquals(TokenType.BRACKET_CLOSE, tokens.get(10).getType());
        assertEquals(TokenType.OP_PLUS, tokens.get(11).getType());
        assertEquals(TokenType.BRACKET_OPEN, tokens.get(12).getType());
        assertEquals(TokenType.MOD_NEGATIVE, tokens.get(13).getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(14).getType());
        assertEquals("1", tokens.get(14).getContent());
        assertEquals(TokenType.BRACKET_CLOSE, tokens.get(15).getType());
        assertEquals(TokenType.BRACKET_CLOSE, tokens.get(16).getType());
        assertEquals(TokenType.END, tokens.get(17).getType());
    }

    @Test
    public void testBrackets() throws ParseException {
        final Lexer lexer = new Lexer("((1))");
        final List<Token> tokens = lexer.tokenise();

        assertEquals(7, tokens.size());
        assertEquals(TokenType.START, tokens.get(0).getType());
        assertEquals(TokenType.BRACKET_OPEN, tokens.get(1).getType());
        assertEquals(TokenType.BRACKET_OPEN, tokens.get(2).getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(3).getType());
        assertEquals(TokenType.BRACKET_CLOSE, tokens.get(4).getType());
        assertEquals(TokenType.BRACKET_CLOSE, tokens.get(5).getType());
        assertEquals(TokenType.END, tokens.get(6).getType());
    }

    @Test
    public void testImplicitMult() throws ParseException {
        final Lexer lexer = new Lexer("1(2)(3)");
        final List<Token> tokens = lexer.tokenise();

        assertEquals(11, tokens.size());
        assertEquals(TokenType.START, tokens.get(0).getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(1).getType());
        assertEquals(TokenType.OP_MULT, tokens.get(2).getType());
        assertEquals(TokenType.BRACKET_OPEN, tokens.get(3).getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(4).getType());
        assertEquals(TokenType.BRACKET_CLOSE, tokens.get(5).getType());
        assertEquals(TokenType.OP_MULT, tokens.get(6).getType());
        assertEquals(TokenType.BRACKET_OPEN, tokens.get(7).getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(8).getType());
        assertEquals(TokenType.BRACKET_CLOSE, tokens.get(9).getType());
        assertEquals(TokenType.END, tokens.get(10).getType());
    }

    private void doIllegalTest(final String input, final int offset) {
        try {
            fail(Arrays.toString(new Lexer(input).tokenise().toArray()));
        } catch (ParseException ex) {
            assertEquals(offset, ex.getErrorOffset());
        }
    }

    @Test public void testIllegalEnd1() { doIllegalTest("", 0); }
    @Test public void testIllegalEnd2() { doIllegalTest("3+", 2); }
    @Test public void testIllegalEnd3() { doIllegalTest("(", 1); }
    @Test public void testIllegalOp1() { doIllegalTest("1++2", 2); }
    @Test public void testIllegalOp2() { doIllegalTest("*1", 0); }

}

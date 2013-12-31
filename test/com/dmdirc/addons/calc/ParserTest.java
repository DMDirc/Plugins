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
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    @Test
    public void testTokenTypesByPrecedence() {
        int last = Integer.MAX_VALUE;

        for (TokenType type : Parser.TOKENS_BY_PRECEDENCE) {
            assertTrue(type.getPrecedence() <= last);
            last = type.getPrecedence();
        }
    }

    @Test
    public void testFindTokenType() {
        final List<TreeToken> tokens = new ArrayList<>();
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_CLOSE, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_CLOSE, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_OPEN, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_CLOSE, null)));
        tokens.add(new TreeToken(new Token(TokenType.OP_DIVIDE, null)));

        assertEquals(0, Parser.findTokenType(tokens, TokenType.BRACKET_CLOSE));
        assertEquals(2, Parser.findTokenType(tokens, TokenType.BRACKET_OPEN));
        assertEquals(4, Parser.findTokenType(tokens, TokenType.OP_DIVIDE));
        assertEquals(-1, Parser.findTokenType(tokens, TokenType.OP_MULT));

        tokens.get(0).setProcessed();
        assertEquals(1, Parser.findTokenType(tokens, TokenType.BRACKET_CLOSE));
    }

    @Test
    public void testParseBracket() throws ParseException {
        final Parser parser = new Parser(null);

        final List<TreeToken> tokens = new ArrayList<>();
        tokens.add(new TreeToken(new Token(TokenType.START, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_OPEN, null)));
        tokens.add(new TreeToken(new Token(TokenType.NUMBER_INT, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_CLOSE, null)));
        tokens.add(new TreeToken(new Token(TokenType.END, null)));

        parser.parseBracket(tokens, 3);
        assertEquals(3, tokens.size());
        assertEquals(TokenType.START, tokens.get(0).getToken().getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(1).getToken().getType());
        assertEquals(TokenType.END, tokens.get(2).getToken().getType());
    }

    @Test
    public void testParseBracket2() throws ParseException {
        final Parser parser = new Parser(null);

        final List<TreeToken> tokens = new ArrayList<>();
        tokens.add(new TreeToken(new Token(TokenType.START, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_OPEN, null)));
        tokens.add(new TreeToken(new Token(TokenType.NUMBER_INT, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_OPEN, null)));
        tokens.add(new TreeToken(new Token(TokenType.NUMBER_INT, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_CLOSE, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_CLOSE, null)));
        tokens.add(new TreeToken(new Token(TokenType.END, null)));

        parser.parseBracket(tokens, 5);
        assertEquals(6, tokens.size());
        assertEquals(TokenType.START, tokens.get(0).getToken().getType());
        assertEquals(TokenType.BRACKET_OPEN, tokens.get(1).getToken().getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(2).getToken().getType());
        assertEquals(TokenType.NUMBER_INT, tokens.get(3).getToken().getType());
        assertEquals(TokenType.BRACKET_CLOSE, tokens.get(4).getToken().getType());
        assertEquals(TokenType.END, tokens.get(5).getToken().getType());
    }

    @Test(expected=ParseException.class)
    public void testUnmatchedBracket() throws ParseException {
        final Parser parser = new Parser(null);

        final List<TreeToken> tokens = new ArrayList<>();
        tokens.add(new TreeToken(new Token(TokenType.START, null)));
        tokens.add(new TreeToken(new Token(TokenType.NUMBER_INT, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_CLOSE, null)));
        tokens.add(new TreeToken(new Token(TokenType.END, null)));

        parser.parseBracket(tokens, 3);
    }

    @Test
    public void testParseNumber() {
        final Parser parser = new Parser(null);

        final List<TreeToken> tokens = new ArrayList<>();
        tokens.add(new TreeToken(new Token(TokenType.START, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_OPEN, null)));
        tokens.add(new TreeToken(new Token(TokenType.NUMBER_INT, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_CLOSE, null)));
        tokens.add(new TreeToken(new Token(TokenType.END, null)));

        parser.parseNumber(tokens, 2);
        assertEquals(5, tokens.size());
        assertTrue(tokens.get(2).isProcessed());
    }

    @Test
    public void testParseHidden() {
        final Parser parser = new Parser(null);

        final List<TreeToken> tokens = new ArrayList<>();
        tokens.add(new TreeToken(new Token(TokenType.START, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_OPEN, null)));
        tokens.add(new TreeToken(new Token(TokenType.NUMBER_INT, null)));
        tokens.add(new TreeToken(new Token(TokenType.BRACKET_CLOSE, null)));
        tokens.add(new TreeToken(new Token(TokenType.END, null)));

        parser.parseHiddenOperator(tokens, 0);
        assertEquals(4, tokens.size());
        assertTrue(tokens.get(0).getToken().getType() == TokenType.BRACKET_OPEN);

        parser.parseHiddenOperator(tokens, 3);
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(2).getToken().getType() == TokenType.BRACKET_CLOSE);
    }

    @Test
    public void testParseUnaryOps() {
        final Parser parser = new Parser(null);

        final List<TreeToken> tokens = new ArrayList<>();
        tokens.add(new TreeToken(new Token(TokenType.START, null)));
        tokens.add(new TreeToken(new Token(TokenType.MOD_NEGATIVE, null)));
        tokens.add(new TreeToken(new Token(TokenType.NUMBER_INT, null)));
        tokens.add(new TreeToken(new Token(TokenType.END, null)));

        parser.parseUnaryOperator(tokens, 1);
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).getToken().getType() == TokenType.START);
        assertTrue(tokens.get(1).getToken().getType() == TokenType.MOD_NEGATIVE);
        assertTrue(tokens.get(2).getToken().getType() == TokenType.END);

        assertTrue(tokens.get(1).isProcessed());
        assertEquals(1, tokens.get(1).getChildren().size());
        assertEquals(TokenType.NUMBER_INT, tokens.get(1).getChildren().get(0).getToken().getType());
    }

    @Test
    public void testParseBinaryOps() {
        final Parser parser = new Parser(null);

        final List<TreeToken> tokens = new ArrayList<>();
        tokens.add(new TreeToken(new Token(TokenType.START, null)));
        tokens.add(new TreeToken(new Token(TokenType.NUMBER_INT, "15")));
        tokens.add(new TreeToken(new Token(TokenType.OP_MINUS, null)));
        tokens.add(new TreeToken(new Token(TokenType.NUMBER_INT, "10")));
        tokens.add(new TreeToken(new Token(TokenType.END, null)));

        parser.parseBinaryOperator(tokens, 2);

        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).getToken().getType() == TokenType.START);
        assertTrue(tokens.get(1).getToken().getType() == TokenType.OP_MINUS);
        assertTrue(tokens.get(2).getToken().getType() == TokenType.END);

        assertTrue(tokens.get(1).isProcessed());
        assertEquals(2, tokens.get(1).getChildren().size());
        assertEquals(TokenType.NUMBER_INT, tokens.get(1).getChildren().get(0).getToken().getType());
        assertEquals("15", tokens.get(1).getChildren().get(0).getToken().getContent());
        assertEquals(TokenType.NUMBER_INT, tokens.get(1).getChildren().get(1).getToken().getType());
        assertEquals("10", tokens.get(1).getChildren().get(1).getToken().getContent());
    }

}

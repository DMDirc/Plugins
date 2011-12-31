/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The parser takes the output from a {@link Lexer} and applies precdence rules
 * to build the tokens into a tree.
 *
 * @author chris
 */
public class Parser {

    /** The lexer whose output will be parsed. */
    protected final Lexer lexer;
    /** A list of token types sorted by their precendece. */
    protected static final List<TokenType> TOKENS_BY_PRECEDENCE;

    static {
        TOKENS_BY_PRECEDENCE = new ArrayList<TokenType>(Arrays.asList(
                TokenType.values()));
        Collections.sort(TOKENS_BY_PRECEDENCE,
                new TokenTypePrecedenceComparator());
    }

    /**
     * Creates a new parser for the specified lexer.
     *
     * @param lexer The lexer whose output should be parsed
     */
    public Parser(final Lexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Parses the output of this parser's lexer, and returns a {@link TreeToken}
     * representing the parsed formula.
     *
     * @return A token tree corresponding to the lexer's token output
     * @throws ParseException If the lexer encounters a parse error, or if an
     * error occurs while parsing the lexer's output (such as a non-sensical
     * formula such as one involving a mis-matched bracket).
     */
    public TreeToken parse() throws ParseException {
        final List<TreeToken> tokens = new ArrayList<TreeToken>();

        for (Token token : lexer.tokenise()) {
            tokens.add(new TreeToken(token));
        }

        return parse(tokens);
    }

    /**
     * Parses the specified tokens into a tree.
     *
     * @param tokens The tokens to be parsed
     * @return A single tree containing all of the specified tokens
     * @throws ParseException If the tokens contain mismatched brackets
     */
    protected TreeToken parse(final List<TreeToken> tokens)
            throws ParseException {
        while (tokens.size() > 1) {
            for (TokenType type : TOKENS_BY_PRECEDENCE) {
                final int offset = findTokenType(tokens, type);

                if (offset > -1) {
                    switch (type.getArity()) {
                        case HIDDEN:
                            parseHiddenOperator(tokens, offset);
                            break;
                        case BINARY:
                            parseBinaryOperator(tokens, offset);
                            break;
                        case UNARY:
                            parseUnaryOperator(tokens, offset);
                            break;
                        case NULLARY:
                            parseNullaryOperator(tokens, offset);
                            break;
                    }

                    break;
                }
            }
        }

        return tokens.get(0);
    }

    /**
     * Parses an operator that takes no operands.
     *
     * @param tokens The supply of tokens from which the operator will be parsed
     * @param offset The offset at which the operator occurs
     * @throws ParseException If the operator is a bracket and that bracket is
     * mismatched
     */
    protected void parseNullaryOperator(final List<TreeToken> tokens,
            final int offset)
            throws ParseException {
        if (tokens.get(offset).getToken().getType()
                == TokenType.BRACKET_CLOSE
                || tokens.get(offset).getToken().getType()
                == TokenType.BRACKET_OPEN) {
            parseBracket(tokens, offset);
        } else {
            parseNumber(tokens, offset);
        }
    }

    /**
     * Parses a bracket operator.
     *
     * @param tokens The supply of tokens from which the operator will be parsed
     * @param offset The offset at which the operator occurs
     * @throws ParseException If the operator is a bracket and that bracket is
     * mismatched
     */
    protected void parseBracket(final List<TreeToken> tokens, final int offset)
            throws ParseException {
        final List<TreeToken> stack = new ArrayList<TreeToken>();

        for (int i = offset - 1; i > 0; i--) {
            if (tokens.get(i).getToken().getType() == TokenType.BRACKET_OPEN
                    && !tokens.get(i).isProcessed()) {
                tokens.add(i, parse(stack));
                tokens.get(i).setProcessed();
                tokens.remove(i + 1);
                tokens.remove(i + 1);
                return;
            } else {
                stack.add(0, tokens.get(i));
                tokens.remove(i);
            }
        }

        throw new ParseException("Couldn't find matching opening bracket",
                offset);
    }

    /**
     * Parses an operator that takes two operands.
     *
     * @param tokens The supply of tokens from which the operator will be parsed
     * @param offset The offset at which the operator occurs
     */
    protected void parseBinaryOperator(final List<TreeToken> tokens,
            final int offset) {
        tokens.get(offset).addChild(tokens.get(offset - 1));
        tokens.get(offset).addChild(tokens.get(offset + 1));
        tokens.get(offset).setProcessed();

        tokens.remove(offset + 1);
        tokens.remove(offset - 1);
    }

    /**
     * Parses an operator that takes one operand.
     *
     * @param tokens The supply of tokens from which the operator will be parsed
     * @param offset The offset at which the operator occurs
     */
    protected void parseUnaryOperator(final List<TreeToken> tokens,
            final int offset) {
        tokens.get(offset).addChild(tokens.get(offset + 1));
        tokens.get(offset).setProcessed();
        tokens.remove(offset + 1);
    }

    /**
     * Parses an operator that does not actually correspond to a piece of the
     * input (such as the START and END operators).
     *
     * @param tokens The supply of tokens from which the operator will be parsed
     * @param offset The offset at which the operator occurs
     */
    protected void parseHiddenOperator(final List<TreeToken> tokens,
            final int offset) {
        tokens.remove(offset);
    }

    /**
     * Parses a number.
     *
     * @param tokens The supply of tokens from which the operator will be parsed
     * @param offset The offset at which the operator occurs
     */
    protected void parseNumber(final List<TreeToken> tokens, final int offset) {
        tokens.get(offset).setProcessed();
    }

    /**
     * Retrieves the offset of the first token within the input list that has
     * a type corresponding to the specified {@link TokenType}.
     *
     * @param tokens The tokens to be searched
     * @param type The desired token type
     * @return The index of the first token with that type, or -1 if none found
     */
    protected static int findTokenType(final List<TreeToken> tokens,
            final TokenType type) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getToken().getType() == type && !tokens.get(i)
                    .isProcessed()) {
                return i;
            }
        }

        return -1;
    }

    /**
     * A class which compares token types based on their precendence.
     */
    protected static class TokenTypePrecedenceComparator implements
            Comparator<TokenType> {

        /** {@inheritDoc} */
        @Override
        public int compare(final TokenType o1, final TokenType o2) {
            return o2.getPrecedence() - o1.getPrecedence();
        }

    }

}

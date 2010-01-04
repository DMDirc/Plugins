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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The lexer takes a String input and produces an ordered list of {@link Token}s
 * corresponding to the input.
 *
 * @author chris
 */
public class Lexer {

    /** The input string. */
    final String input;

    /**
     * Creates a new lexer for the specified input string.
     *
     * @param input The string to be tokenised
     */
    public Lexer(final String input) {
        this.input = input.replaceAll("\\s+", "");
    }

    /**
     * Tokenises the input string into an ordered list of tokens.
     *
     * @return A list of tokens corresponding to the input string
     * @throws ParseException If an expected token is not found
     */
    public List<Token> tokenise() throws ParseException {
        final List<Token> res = new ArrayList<Token>();
        List<TokenType> possibles = Arrays.asList(TokenType.values());

        boolean cont = true;
        int i = 0;

        do {
            boolean found = false;

            for (TokenType type : possibles) {
                final int match = type.match(input, i);

                if (match > -1) {
                    res.add(new Token(type, input.substring(i, match)));

                    possibles = type.getFollowers();
                    i = match;
                    found = true;
                    cont = type != TokenType.END;

                    break;
                }
            }

            if (!found) {
                throw new ParseException("No legal token found at offset "
                        + i + ". Expecting one of: "
                        + Arrays.toString(possibles.toArray()), i);
            }
        } while (cont);

        return res;
    }

}

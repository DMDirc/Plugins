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

/**
 * Describes a distinct piece of a mathematical formula, as tokenised by a
 * {@link Lexer}.
 */
public class Token {

    /** The type of this token. */
    private final TokenType type;

    /** The content of this token, if any. */
    private final String content;

    /**
     * Creates a new token.
     *
     * @param type The type of the token
     * @param content The content of the token
     */
    public Token(final TokenType type, final String content) {
        this.type = type;
        this.content = content;
    }

    /**
     * Retrieves the content of this token.
     *
     * @return This token's content
     */
    public String getContent() {
        return content;
    }

    /**
     * Retrieves the type of this token.
     *
     * @return This token's type
     */
    public TokenType getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[type: " + type + "; content: " + content + "]";
    }

}

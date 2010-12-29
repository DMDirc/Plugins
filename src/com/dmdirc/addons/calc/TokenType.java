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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Describes the different types of possible token, their arities, precedence,
 * and the types of token that may follow them.
 * 
 * @author chris
 */
public enum TokenType {

    /** The start of an input string. */
    START(TokenTypeArity.HIDDEN, "^", 0, "NUMBER_*", "BRACKET_OPEN", "MOD_*"),
    /** The end of an input string. */
    END(TokenTypeArity.HIDDEN, "$", 0),

    /** An opening bracket. */
    BRACKET_OPEN(TokenTypeArity.NULLARY, "\\(", 0, "NUMBER_*", "MOD_*",
                "BRACKET_OPEN"),
    /** A closing bracket. */
    BRACKET_CLOSE(TokenTypeArity.NULLARY, "\\)", 50, "OP_*", "BRACKET_*",
                "END"),

    /** A floating point number. */
    NUMBER_FLOAT(TokenTypeArity.NULLARY, "[0-9]+\\.[0-9]+", 1, "OP_*",
                "BRACKET_*", "END") {
        /** {@inheritDoc} */
        @Override
        public Number evaluate(final TreeToken token) {
            return Float.valueOf(token.getToken().getContent());
        }
    },

    /** An integer. */
    NUMBER_INT(TokenTypeArity.NULLARY, "[0-9]+", 1, "OP_*",
                "BRACKET_*", "END") {
        /** {@inheritDoc} */
        @Override
        public Number evaluate(final TreeToken token) {
            return Float.valueOf(token.getToken().getContent());
        }
    },

    /** A modifier signalling the following number is positive. */
    MOD_POSITIVE(TokenTypeArity.UNARY, "\\+", 100, "NUMBER_*") {
        /** {@inheritDoc} */
        @Override
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate();
        }
    },

    /** A modifier signalling the following number is negative. */
    MOD_NEGATIVE(TokenTypeArity.UNARY, "-", 100, "NUMBER_*") {
        /** {@inheritDoc} */
        @Override
        public Number evaluate(final TreeToken token) {
            return -1 * token.getChildren().get(0).evaluate().floatValue();
        }
    },

    /** The addition operator. */
    OP_PLUS(TokenTypeArity.BINARY, "\\+", 7, "NUMBER_*", "BRACKET_OPEN") {
        /** {@inheritDoc} */
        @Override
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate().floatValue()
                    + token.getChildren().get(1).evaluate().floatValue();
        }
    },

    /** The subtraction operator. */
    OP_MINUS(TokenTypeArity.BINARY, "-", 6, "NUMBER_*", "BRACKET_OPEN") {
        /** {@inheritDoc} */
        @Override
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate().floatValue()
                    - token.getChildren().get(1).evaluate().floatValue();
        }
    },

    /** The multiplication operator. */
    OP_MULT(TokenTypeArity.BINARY, "(?=\\()|\\*", 9, "NUMBER_*",
            "BRACKET_OPEN") {
        /** {@inheritDoc} */
        @Override
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate().floatValue()
                    * token.getChildren().get(1).evaluate().floatValue();
        }
    },

    /** The division operator. */
    OP_DIVIDE(TokenTypeArity.BINARY, "/", 10, "NUMBER_*", "BRACKET_OPEN") {
        /** {@inheritDoc} */
        @Override
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate().floatValue()
                    / token.getChildren().get(1).evaluate().floatValue();
        }
    },

    /** The modulo operator. */
    OP_MOD(TokenTypeArity.BINARY, "%", 8, "NUMBER_*", "BRACKET_OPEN") {
        /** {@inheritDoc} */
        @Override
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate().floatValue()
                    % token.getChildren().get(1).evaluate().floatValue();
        }
    },

    /** The power operator. */
    OP_POWER(TokenTypeArity.BINARY, "\\^", 11, "NUMBER_*", "BRACKET_OPEN") {
        /** {@inheritDoc} */
        @Override
        public Number evaluate(final TreeToken token) {
            return new Float(Math.pow(token.getChildren().get(0).evaluate()
                    .doubleValue(),
                    token.getChildren().get(1).evaluate().doubleValue()));
        }
    };

    /** The string representation of tokens that may follow this one. */
    private final String[] strfollows;
    /** The precedence of this token. */
    private final int precedence;
    /** The list of tokens that may follow this one. */
    private List<TokenType> follows;
    /** The regular expression used to match this token. */
    private final Pattern regex;
    /** The arity of this token. */
    private final TokenTypeArity arity;

    /**
     * Creates a new token type with the specified arguments.
     *
     * @param arity The arity of this token
     * @param regex The regular expression used to match this token
     * @param precedence The precendence of this token
     * @param follows The names of the tokens which may follow this one
     */
    TokenType(final TokenTypeArity arity, final String regex,
            final int precedence, final String ... follows) {
        this.arity = arity;
        this.strfollows = follows;
        this.precedence = precedence;
        this.regex = Pattern.compile(regex);
    }

    /**
     * Retrieves a list of token types that may follow this one.
     *
     * @return A list of this token type's possible followers
     */
    public synchronized List<TokenType> getFollowers() {
        if (follows == null) {
            follows = new ArrayList<TokenType>();

            for (int i = 0; i < strfollows.length; i++) {
                follows.addAll(searchValueOf(strfollows[i]));
            }
        }

        return follows;
    }

    /**
     * Retrieves the arity of this token type.
     *
     * @return This token type's arity
     */
    public TokenTypeArity getArity() {
        return arity;
    }

    /**
     * Retrieves the precedence of this token type.
     *
     * @return This token type's precedence
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * Attempts to match this token type against the specified input string
     * (starting at the specified offset).
     *
     * @param input The string to be matched
     * @param offset The offset within the string to start at
     * @return -1 if no match was made, otherwise the number of characters that
     * were matched as part of this token type.
     */
    public int match(final String input, final int offset) {
        final Matcher matcher = regex.matcher(input);
        matcher.useAnchoringBounds(false);
        matcher.useTransparentBounds(true);

        return matcher.find(offset) && matcher.start() == offset
                ? matcher.end() : -1;
    }

    /**
     * Evaluates the specified token of this token type into a number.
     *
     * @param token The token to be evaluated
     * @return A numerical representation of the specified token
     */
    public Number evaluate(final TreeToken token) {
        throw new AbstractMethodError("Can't evaluate this token type");
    }

    /**
     * Retrieves a list of types which match the specified name. The name
     * may end with an asterisk (*), which is treated as a wild card.
     *
     * @param name The name to be searched for
     * @return A list of matching tokens
     */
    protected static List<TokenType> searchValueOf(final String name) {
        final List<TokenType> res = new ArrayList<TokenType>();

        for (TokenType token : values()) {
            if ((name.endsWith("*") && token.name().startsWith(name.substring(0,
                    name.length() - 1))) || name.equals(token.name())) {
                res.add(token);
            }
        }

        return res;
    }
}
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

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a tree of {@link Token}s.
 *
 * @author chris
 */
public class TreeToken {

    /** The children of this node. */
    private final List<TreeToken> children = new ArrayList<TreeToken>();

    /** The token at the root of the tree. */
    private final Token token;

    /** Whether or not this tree has been processed. */
    private boolean processed = false;

    /**
     * Creates a new tree with the specified token at the root.
     *
     * @param token The root token
     */
    public TreeToken(final Token token) {
        this.token = token;
    }

    /**
     * Retrieves the (direct) children of this tree.
     *
     * @return This tree's children
     */
    public List<TreeToken> getChildren() {
        return children;
    }

    /**
     * Retrieves the root token of this tree.
     *
     * @return This tree's token
     */
    public Token getToken() {
        return token;
    }

    /**
     * Adds the specified child to this tree.
     *
     * @param token The child to be added
     */
    public void addChild(final TreeToken token) {
        children.add(token);
    }

    /**
     * Determines if this tree has been processed.
     *
     * @return True if the tree has been processed, false otherwise
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * Sets the processed flag of this tree to true.
     */
    public void setProcessed() {
        processed = true;
    }

    /**
     * Evaluates this tree to return a number.
     *
     * @return A numerical evaluation of this tree.
     */
    public Number evaluate() {
        return token.getType().evaluate(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[token: " + token + "; children: " + children + "; processed: "
                + processed + "]";
    }

}

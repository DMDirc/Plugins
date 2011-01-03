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

import java.text.ParseException;

/**
 * Outputs a string which can be read by the unix `dot` utility to show a
 * directed graph showing the tokens parsed by a {@link Parser}.
 */
public class DotOutputter {

    /** The parser which will be read. */
    private final Parser parser;

    /** Counter for assigning node IDs. */
    private int nodes = 0;

    /**
     * Creates a new DotOutputter for the specified parser.
     *
     * @param parser The parser whose output should be shown
     */
    public DotOutputter(final Parser parser) {
        this.parser = parser;
    }

    /**
     * Outputs a string which may be read by the `dot` utility to create a
     * directed graph of tokens.
     *
     * @return A string describing the parse tree from the specified parser
     * @throws ParseException If the parser encounters an exception
     */
    public String output() throws ParseException {
        return "digraph astoutput { " + output(0, parser.parse()) + " }";
    }

    /**
     * Outputs the relevant text for the specified token, assigning it an ID
     * specified by the node parameter.
     *
     * @param node The ID of the node to be used in the output
     * @param token The token to be outputted
     * @return A string corresponding to the dot representation of the
     * specified node and its children
     */
    protected String output(final int node, final TreeToken token) {
        final StringBuilder out = new StringBuilder();
        out.append("node").append(node).append(" [label=\"");
        out.append(token.getToken().getType());
        out.append("\\n").append(token.getToken().getContent()).append("\"];");

        for (TreeToken child : token.getChildren()) {
            final int id = ++nodes;

            out.append(output(id, child));
            out.append("node").append(node).append(" -> node");
            out.append(id).append(";");
        }

        return out.toString();
    }

}

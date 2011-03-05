/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.harness.ui;

import java.awt.Component;

import org.uispec4j.finder.ComponentMatcher;

/**
 * Simple UISpec4J component matcher that matches by type.
 */
public class ClassComponentMatcher implements ComponentMatcher {

    private final Class<?> clazz;

    /**
     * Creates a new component matcher, searching for any components matching
     * the specified class in the specified component.
     *
     * @param clazz Class to search for
     */
    public ClassComponentMatcher(final Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * {@inheritDoc}
     * 
     * @param component Component to match against
     * 
     * @return true iif the component matches
     */
    @Override
    public boolean matches(final Component component) {
        return component.getClass() == clazz;
    }
}

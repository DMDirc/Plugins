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

package com.dmdirc.addons.ui_swing.components.text;

import com.dmdirc.util.URLBuilder;

import javax.annotation.Nullable;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;

/**
 * DMDirc html kit, extends HTMLEditor kit to use DMDircHTMLFactory.
 */
public class DMDircHTMLEditorKit extends HTMLEditorKit {

    private static final long serialVersionUID = 1;
    private final ViewFactory defaultFactory;

    /**
     * Creates a new instance of {@link DMDircHTMLEditorKit}.
     *
     * @param urlBuilder The URL builder to use to construct image URLs. If {@code null}, then only
     *                   standard URLs will be handled (not DMDirc-specific ones).
     */
    public DMDircHTMLEditorKit(@Nullable final URLBuilder urlBuilder) {
        defaultFactory = new DMDircHTMLFactory(urlBuilder);
    }

    
    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

}

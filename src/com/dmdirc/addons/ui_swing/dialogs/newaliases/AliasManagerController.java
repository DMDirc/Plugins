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

package com.dmdirc.addons.ui_swing.dialogs.newaliases;

/**
 * Alias manager dialog controller reacts to actions in the UI.
 */
public class AliasManagerController {

    private final AliasManagerModel model;
    private final AliasManagerDialog dialog;

    public AliasManagerController(final AliasManagerDialog dialog,
            final AliasManagerModel model) {
        this.model = model;
        this.dialog = dialog;
    }

    public void addAlias(final String name, final int minArguments, final String substitutions) {
        model.addAlias(name, minArguments, substitutions);
    }

    public void removeAlias(final String name) {
        model.removeAlias(name);
    }

    public void editAlias(final String name, final int minArguments, final String substitutions) {
        model.editAlias(name, minArguments, substitutions);
    }

    public void saveAndCloseDialog() {
        model.save();
        dialog.dispose();
    }

    public void discardAndCloseDialog() {
        dialog.dispose();
    }

}

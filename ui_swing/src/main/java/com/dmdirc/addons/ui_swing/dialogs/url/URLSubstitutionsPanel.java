/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.dialogs.url;

import com.dmdirc.addons.ui_swing.components.substitutions.Substitution;
import com.dmdirc.addons.ui_swing.components.substitutions.SubstitutionLabel;
import com.dmdirc.addons.ui_swing.components.substitutions.SubstitutionsPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

/**
 * URL Substitutions panel.
 */
public class URLSubstitutionsPanel extends SubstitutionsPanel<List<String>> {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;

    /**
     * Instantiates a new URL substitutions panel.
     */
    public URLSubstitutionsPanel() {
        super("Substitutions may be used as part of the launch command",
                SubstitutionsPanel.Alignment.VERTICAL, null);
    }

    /**
     * Instantiates a new URL substitutions panel.
     *
     * @param subs list of substitutions.
     */
    public URLSubstitutionsPanel(final List<String> subs) {
        super("Substitutions may be used as part of the launch command", subs);
    }

    @Override
    public void setType(final List<String> type) {
        SwingUtilities.invokeLater(() -> {
            substitutions = new ArrayList<>();

            if (type != null) {
                substitutions.addAll(type.stream()
                        .map(sub -> new SubstitutionLabel(new Substitution(sub, sub)))
                        .collect(Collectors.toList()));
             }
            layoutComponents();
        });
    }

}

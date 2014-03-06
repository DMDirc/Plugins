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

package com.dmdirc.addons.ui_swing.dialogs.actioneditor;

import com.dmdirc.actions.ActionSubstitutor;
import com.dmdirc.actions.ActionSubstitutorFactory;
import com.dmdirc.addons.ui_swing.components.substitutions.Substitution;
import com.dmdirc.addons.ui_swing.components.substitutions.SubstitutionLabel;
import com.dmdirc.addons.ui_swing.components.substitutions.SubstitutionsPanel;
import com.dmdirc.interfaces.actions.ActionType;

import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

/**
 * Action substitutions panel
 */
public class ActionSubstitutionsPanel extends SubstitutionsPanel<ActionType> {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 1;
    /** Factory to use to create {@link ActionSubstitutor}s. */
    private final ActionSubstitutorFactory substitutorFactory;

    /**
     * Instantiates the panel.
     *
     * @param substitutorFactory Factory to use to create {@link ActionSubstitutor}s.
     */
    public ActionSubstitutionsPanel(final ActionSubstitutorFactory substitutorFactory) {
        super("Substitutions may be used in the response and target fields",
                SubstitutionsPanel.Alignment.VERTICAL,
                null);

        this.substitutorFactory = substitutorFactory;
    }

    /**
     * Sets the action type for this substitution panel.
     *
     * @param type New action type
     */
    @Override
    public void setType(final ActionType type) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                substitutions = new ArrayList<>();

                if (type != null) {
                    final ActionSubstitutor sub = substitutorFactory.getActionSubstitutor(type);

                    for (final Entry<String, String> entry : sub.getComponentSubstitutions().
                            entrySet()) {
                        substitutions.add(new SubstitutionLabel(new Substitution(entry.getValue(),
                                entry.getKey())));
                    }

                    for (final String entry : sub.getConfigSubstitutions()) {
                        substitutions.add(new SubstitutionLabel(new Substitution(entry,
                                entry)));
                    }

                    for (final Entry<String, String> entry : sub.getServerSubstitutions().
                            entrySet()) {
                        substitutions.add(new SubstitutionLabel(new Substitution(entry.getValue(),
                                entry.getKey())));
                    }
                }

                layoutComponents();
                validate();
                layoutComponents();
            }
        });
    }

}
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

package com.dmdirc.addons.ui_swing.components.renderers;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateChainEntry;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

/**
 * Renderer for Certificate chain entries, shows the verified icon and name.
 */
public class CertificateChainEntryCellRenderer extends DefaultListCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Icon to use for invalid entries. */
    private final Icon invalidIcon = new IconManager(IdentityManager
            .getGlobalConfig()).getIcon("cross");
    /** Icon to use for trusted entries. */
    private final Icon trustedIcon = new IconManager(IdentityManager
            .getGlobalConfig()).getIcon("tick");
    /** Icon to use for other entries. */
    private final Icon icon = new IconManager(IdentityManager
            .getGlobalConfig()).getIcon("nothing");

    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);
        if (value instanceof CertificateChainEntry) {
            final CertificateChainEntry entry = (CertificateChainEntry) value;

            setText(entry.getName());
            if (entry.isInvalid()) {
                setIcon(invalidIcon);
            } else if (entry.isTrusted()) {
                setIcon(trustedIcon);
            } else {
                setIcon(icon);
            }
        }
        return this;
    }
}

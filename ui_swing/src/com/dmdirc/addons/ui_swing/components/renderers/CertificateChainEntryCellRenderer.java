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

package com.dmdirc.addons.ui_swing.components.renderers;

import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateChainEntry;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;

/**
 * Renderer for Certificate chain entries, shows the verified icon and name.
 */
public class CertificateChainEntryCellRenderer extends DMDircListCellRenderer<CertificateChainEntry> {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Icon to use for invalid entries. */
    private final Icon invalidIcon;
    /** Icon to use for trusted entries. */
    private final Icon trustedIcon;
    /** Icon to use for other entries. */
    private final Icon icon;

    /**
     * Creates a new renderer.
     *
     * @param iconManager Icon manager
     * @param renderer    Parent renderer
     */
    public CertificateChainEntryCellRenderer(final IconManager iconManager,
            final ListCellRenderer<? super CertificateChainEntry> renderer) {
        super(renderer);
        icon = iconManager.getIcon("nothing");
        trustedIcon = iconManager.getIcon("tick");
        invalidIcon = iconManager.getIcon("cross");
    }

    @Override
    protected void renderValue(final JLabel label, final CertificateChainEntry value,
            final int index, final boolean isSelected,
            final boolean hasFocus) {
        label.setText(value.getName());
        if (value.isInvalid()) {
            label.setIcon(invalidIcon);
        } else if (value.isTrusted()) {
            label.setIcon(trustedIcon);
        } else {
            label.setIcon(icon);
        }
    }

}

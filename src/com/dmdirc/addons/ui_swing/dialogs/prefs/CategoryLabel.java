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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.config.prefs.PreferencesCategory;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;

import net.miginfocom.layout.PlatformDefaults;

/**
 * Category Label.
 */
public class CategoryLabel extends JLabel {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = -1659415238166842265L;
    /** Panel gap. */
    private final int padding = (int)
            (1.5 * PlatformDefaults.getUnitValueX("related").getValue());
    /** Parent list. */
    private final JList parentList;

    /**
     * Creates a new category label.
     *
     * @param parentList Parent list
     * @param category Parent category
     * @param numCats Number of categories shown
     * @param index Index of this label
     */
    public CategoryLabel(final JList parentList,
            final PreferencesCategory category, final int numCats,
            final int index) {
        super();
        
        this.parentList = parentList;

        setText(category.getTitle());
        new IconLoader(this, category.getIcon()).executeInExecutor();

        int level = 0;
        PreferencesCategory temp = category;
        while (temp.getParent() != null) {
            temp = temp.getParent();
            level++;
        }

        setPreferredSize(new Dimension(100000, Math.max(16, getFont().getSize())
                + padding));
        setBorder(BorderFactory.createEmptyBorder(padding / 2, padding + level
                * 18, padding / 2, padding));
        setBackground(parentList.getBackground());
        setForeground(parentList.getForeground());
        setOpaque(true);
        setToolTipText(null);

        if (category.getPath().equals(category.getTitle())) {
            boolean hasChildren = false;
            for (PreferencesCategory child : category.getSubcats()) {
                if (!child.isInline()) {
                    hasChildren = true;
                    break;
                }
            }

            hasChildren = hasChildren || index + 1 == numCats;

            setBackground(UIManager.getColor("ToolTip.background"));
            setForeground(UIManager.getColor("ToolTip.foreground"));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, hasChildren ? 1 : 0,
                    0, UIManager.getColor("ToolTip.background").darker()
                    .darker()), getBorder()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param icon New icon
     */
    @Override
    public void setIcon(final Icon icon) {
        super.setIcon(icon);

        if (parentList != null) {
            parentList.repaint();
        }
    }

}

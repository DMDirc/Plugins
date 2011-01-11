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

package com.dmdirc.addons.ui_swing.components.addonpanel;

import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * Renders an addon for display in the plugin panel.
 */
public class AddonCell extends JPanel {

    /** Addon toggle object. */
    private final Object info;
    /** Name of the addon. */
    private final TextLabel name;
    /** Version of the addon. */
    private final TextLabel version;
    /** Author of the addon. */
    private final TextLabel author;
    /** Description of the addon. */
    private final TextLabel desc;

    /**
     * Creates a new addon cell representing the specified addon info.
     *
     * @param info PluginInfoToggle or ThemeToggle
     */
    public AddonCell(final Object info) {
        super();

        name = new TextLabel("", false);
        version = new TextLabel("", false);
        author = new TextLabel("", false);
        desc = new TextLabel("", false);

        this.info = info;
        init();
    }

    /**
     * Initialises the addon cell.
     */
    private void init() {
        setLayout(new MigLayout("fill, ins 3 0 0 0"));

        Color foreground = UIManager.getColor("Table.foreground");
        if (info instanceof AddonToggle) {
            final AddonToggle plugin = (AddonToggle) info;

            if (!plugin.getState()) {
                foreground = foreground.brighter().brighter().brighter();
            }

            name.setText(plugin.getName());
            version.setText(plugin.getVersion());
            author.setText(plugin.getAuthor());
            desc.setText(plugin.getDescription());
        }

        name.setForeground(foreground);
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        version.setForeground(foreground);
        author.setForeground(foreground);
        desc.setForeground(foreground);
        desc.setBorder(BorderFactory.createEmptyBorder((int) PlatformDefaults.
                getPanelInsets(0).getValue(), 0, 0, 0));

        add(name, "gapleft 3, wmin 50%, wmax 50%");
        add(version, "wmin 25%, wmax 25%");
        add(author, "gapright 3, wmin 24.9%, wmax 24.9%, alignx right");
        add(desc, "newline, span, grow, pushy, gapleft 3, gapright 3, wmax 99%");
        add(new JSeparator(), "newline, span, growx, pushx");
    }

    /**
     * Returns the addon toggle object associated with this cell.
     *
     * @return Addon toggle
     */
    public Object getObject() {
        return info;
    }

    /**
     * Is this addon enabled or disabled?
     *
     * @return true iif enabled
     */
    public boolean isToggled() {
        if (info instanceof AddonToggle) {
            return ((AddonToggle) info).getState();
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void setForeground(final Color color) {
        if (name != null) {
            name.setForeground(color);
        }
        if (version != null) {
            version.setForeground(color);
        }
        if (author != null) {
            author.setForeground(color);
        }
        if (desc != null) {
            desc.setForeground(color);
        }
    }
}

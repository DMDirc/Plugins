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

import com.dmdirc.ChannelClientProperty;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.util.colours.Colour;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/** Renders the nicklist. */
public final class NicklistRenderer extends DefaultListCellRenderer implements
        ConfigChangeListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 5;
    /** Config manager. */
    private final AggregateConfigProvider config;
    /** Nicklist alternate background colour. */
    private Color altBackgroundColour;
    /** Show nick colours. */
    private boolean showColours;
    /** The list that we're using for the nicklist. */
    private final JList<ChannelClientInfo> nicklist;
    /** Colour manager to use to resolve colours. */
    private final ColourManager colourManager;

    /**
     * Creates a new instance of NicklistRenderer.
     *
     * @param config        ConfigManager for the associated channel
     * @param nicklist      The nicklist that we're rendering for.
     * @param colourManager Colour manager to use to resolve colours.
     */
    public NicklistRenderer(
            final AggregateConfigProvider config,
            final JList<ChannelClientInfo> nicklist,
            final ColourManager colourManager) {
        this.config = config;
        this.nicklist = nicklist;
        this.colourManager = colourManager;

        config.addChangeListener("ui", "shownickcoloursinnicklist", this);
        config.addChangeListener("ui", "nicklistbackgroundcolour", this);
        config.addChangeListener("ui", "backgroundcolour", this);
        config.addChangeListener("ui", "nickListAltBackgroundColour", this);
        altBackgroundColour = UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "ui", "nickListAltBackgroundColour",
                                "ui", "nicklistbackgroundcolour",
                                "ui", "backgroundcolour"), null));
        showColours = config.getOptionBool("ui", "shownickcoloursinnicklist");
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);

        if (!isSelected && (index & 1) == 1) {
            setBackground(altBackgroundColour);
        }

        final Map<?, ?> map = ((ChannelClientInfo) value).getMap();

        if (showColours && map != null) {
            if (map.containsKey(ChannelClientProperty.NICKLIST_FOREGROUND)) {
                setForeground(UIUtilities.convertColour((Colour) map.get(
                        ChannelClientProperty.NICKLIST_FOREGROUND)));
            }

            if (map.containsKey(ChannelClientProperty.NICKLIST_BACKGROUND)) {
                setBackground(UIUtilities.convertColour((Colour) map.get(
                        ChannelClientProperty.NICKLIST_BACKGROUND)));
            }
        }

        return this;
    }

    @Override
    public void configChanged(final String domain, final String key) {
        if ("shownickcoloursinnicklist".equals(key)) {
            showColours = config.getOptionBool("ui", "shownickcoloursinnicklist");

        } else {
            altBackgroundColour = UIUtilities.convertColour(
                    colourManager.getColourFromString(
                            config.getOptionString(
                                    "ui", "nickListAltBackgroundColour",
                                    "ui", "nicklistbackgroundcolour",
                                    "ui", "backgroundcolour"), null));
        }
        nicklist.repaint();
    }

}
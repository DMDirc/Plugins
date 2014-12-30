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
import com.dmdirc.addons.ui_swing.EDTInvocation;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.colours.Colour;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/** Renders the nicklist. */
public final class NicklistRenderer extends DefaultListCellRenderer {

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
        config.getBinder().bind(this, NicklistRenderer.class);
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

    @ConfigBinding(domain = "ui", key = "shownickcoloursinnicklist",
            invocation = EDTInvocation.class)
    public void handleShowColoursInNickList(final String value) {
        showColours = config.getOptionBool("ui", "shownickcoloursinnicklist");
        nicklist.repaint();
    }

    @ConfigBinding(domain = "ui", key = "nickListAltBackgroundColour",
            fallbacks = {"ui", "nicklistbackgroundcolour", "ui", "backgroundcolour"},
            invocation = EDTInvocation.class)
    public void handleColours(final String value) {
        altBackgroundColour = UIUtilities.convertColour(
                colourManager.getColourFromString(value, null));
        nicklist.repaint();
    }

}

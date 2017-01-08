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

package com.dmdirc.addons.ui_swing.components.renderers;

import com.dmdirc.addons.ui_swing.EDTInvocation;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.binding.ConfigBinding;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/** Renders the nicklist. */
public final class NicklistRenderer extends DMDircListCellRenderer<GroupChatUser> {

    /** Config manager. */
    private final AggregateConfigProvider config;
    /** Nicklist alternate background colour. */
    private Color altBackgroundColour;
    /** Show nick colours. */
    private boolean showColours;
    /** The list that we're using for the nicklist. */
    private final JList<GroupChatUser> nicklist;
    /** Colour manager to use to resolve colours. */
    private final ColourManager colourManager;

    /**
     * Creates a new instance of NicklistRenderer.
     *
     * @param config        ConfigManager for the associated channel
     * @param nicklist      The nicklist that we're rendering for.
     * @param colourManager Colour manager to use to resolve colours.
     */
    public NicklistRenderer(final ListCellRenderer<? super GroupChatUser> renderer,
            final AggregateConfigProvider config,
            final JList<GroupChatUser> nicklist,
            final ColourManager colourManager) {
        super(renderer);
        this.config = config;
        this.nicklist = nicklist;
        this.colourManager = colourManager;
        config.getBinder().bind(this, NicklistRenderer.class);
    }

    @Override
    protected void renderValue(final JLabel label, final GroupChatUser value, final int index,
            final boolean isSelected, final boolean hasFocus) {
        if (!isSelected && (index & 1) == 1) {
            label.setBackground(altBackgroundColour);
        }
        if (showColours) {
            value.getDisplayProperty(DisplayProperty.FOREGROUND_COLOUR).ifPresent(
                    c -> label.setForeground(UIUtilities.convertColour(c)));
            value.getDisplayProperty(DisplayProperty.BACKGROUND_COLOUR).ifPresent(
                    c -> label.setForeground(UIUtilities.convertColour(c)));
        }
        label.setText(value.getImportantMode() + value.getNickname());
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

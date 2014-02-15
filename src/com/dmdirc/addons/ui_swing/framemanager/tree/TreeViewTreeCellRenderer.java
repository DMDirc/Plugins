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

package com.dmdirc.addons.ui_swing.framemanager.tree;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.Styliser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

/**
 * Displays a node in a tree according to its type.
 */
public class TreeViewTreeCellRenderer implements TreeCellRenderer,
        ConfigChangeListener {

    /** Parent frame manager. */
    private final TreeFrameManager manager;
    /** Config manager. */
    private final AggregateConfigProvider config;
    /** The colour manager to use to resolve colours. */
    private final ColourManager colourManager;
    /** Styliser to use. */
    private final Styliser styliser;
    /** Rollover colours. */
    private Color rolloverColour;
    /** Active bold. */
    private boolean activeBold;
    /** Active background. */
    private Color activeBackground;
    /** Active foreground. */
    private Color activeForeground;

    /**
     * Creates a new instance of TreeViewTreeCellRenderer.
     *
     * @param config Config manager to retrieve settings from
     * @param colourManager The colour manager to use to resolve colours.
     * @param manager Parent TreeFrameManager
     */
    public TreeViewTreeCellRenderer(
            final AggregateConfigProvider config,
            final ColourManager colourManager,
            final TreeFrameManager manager) {
        this.manager = manager;

        this.config = config;
        this.colourManager = colourManager;

        styliser = new Styliser(null, config);

        setColours();

        config.addChangeListener("ui", this);
        config.addChangeListener("treeview", this);
    }

    /**
     * Configures the renderer based on the passed parameters.
     *
     * @param tree JTree for this renderer.
     * @param value node to be rendered.
     * @param sel whether the node is selected.
     * @param expanded whether the node is expanded.
     * @param leaf whether the node is a leaf.
     * @param row the node's row.
     * @param hasFocus whether the node has focus.
     *
     * @return RendererComponent for this node.
     */
    @Override
    public Component getTreeCellRendererComponent(final JTree tree,
            final Object value, final boolean sel, final boolean expanded,
            final boolean leaf, final int row, final boolean hasFocus) {

        if (value == null) {
            return new JLabel("Node == null");
        }
        final NodeLabel label = ((TreeViewNode) value).getLabel();
        if (label == null) {
            return new JLabel("Label == null");
        }
        boolean bold;
        Color background = tree.getBackground();
        Color foreground = tree.getForeground();

        if (label.isRollover()) {
            background = rolloverColour;
        }

        final Color colour = label.getNotificationColour();
        if (colour != null) {
            foreground = colour;
        }

        if (label.isSelected()) {
            bold = activeBold;
            if (!tree.getBackground().equals(activeBackground)) {
                background = activeBackground;
            }
            foreground = activeForeground;
        } else {
            bold = false;
        }

        final StringBuilder sb = new StringBuilder();
        if (bold) {
            sb.append(Styliser.CODE_BOLD);
        }
        sb.append(Styliser.CODE_HEXCOLOUR);
        sb.append(UIUtilities.getHex(foreground));
        sb.append(',');
        sb.append(UIUtilities.getHex(background));
        label.setBackground(background);
        label.setOpaque(true);
        label.setTextStyle(styliser, sb.toString());
        label.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
        label.setPreferredSize(new Dimension(100000, tree.getFontMetrics(
                UIManager.getFont("Tree.font")).getHeight() + 2));

        return label;
    }

    /** Sets the colours for the renderer. */
    private void setColours() {
        rolloverColour = UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "ui", "treeviewRolloverColour",
                                "treeview", "backgroundcolour",
                                "ui", "backgroundcolour"), null));
        activeBackground = UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "ui", "treeviewActiveBackground",
                                "treeview", "backgroundcolour",
                                "ui", "backgroundcolour"), null));
        activeForeground = UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "ui", "treeviewActiveForeground",
                                "treeview", "foregroundcolour",
                                "ui", "foregroundcolour"), null));
        activeBold = config.getOptionBool("ui", "treeviewActiveBold");

        manager.getTree().repaint();
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if (("ui".equals(domain) || "treeview".equals(domain))
                && ("treeviewRolloverColour".equals(key)
                || "treeviewActiveBackground".equals(key)
                || "treeviewActiveForeground".equals(key)
                || "treeviewActiveBold".equals(key)
                || "backgroundcolour".equals(key)
                || "foregroundcolour".equals(key))) {
            setColours();
        }
    }
}

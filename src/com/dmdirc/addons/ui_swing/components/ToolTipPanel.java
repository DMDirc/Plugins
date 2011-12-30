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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.ui.IconManager;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;
import org.jdesktop.jxlayer.plaf.LayerUI;

/**
 * Panel to display tool tips of a component.
 */
public class ToolTipPanel extends JPanel implements MouseListener {

    /** Serial version UID. */
    private static final long serialVersionUID = -8929794537312606692L;
    /** Default tool tip. */
    private final String defaultHelp;
    /** Tool tip display. */
    private final TextLabel tooltip;
    /** Error icon. */
    private final JLabel icon;
    /** Whether or not this is a warning. */
    private String warningText = null;
    /** Map of registered components to their tool tips. */
    private final Map<JComponent, String> tooltips;

    /**
     * Instantiates a new tool tip panel.
     *
     * @param iconManager Icon Manager
     * @param helpText Default help message when idle
     */
    public ToolTipPanel(final IconManager iconManager, final String helpText) {
        super(new MigLayout("hidemode 3"));

        defaultHelp = helpText;
        tooltips = new HashMap<JComponent, String>();
        icon = new JLabel(iconManager.getIcon("warning"));

        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
        setBorder(BorderFactory.createEtchedBorder());

        tooltip = new TextLabel();
        reset();

        add(icon, "aligny top");
        add(tooltip, "grow, push");
    }

    /**
     * Resets the content of the tool tip.
     */
    protected final void reset() {
        final SimpleAttributeSet sas = new SimpleAttributeSet();

        StyleConstants.setForeground(sas, Color.BLACK);
        StyleConstants.setBackground(sas, Color.WHITE);
        if (warningText == null || warningText.isEmpty()) {
            tooltip.setText(defaultHelp);
            icon.setVisible(false);
            StyleConstants.setItalic(sas, true);
        } else {
            icon.setVisible(true);
            tooltip.setText(warningText);
        }
        tooltip.getDocument().setParagraphAttributes(0, tooltip.getDocument().
                getLength(), sas, true);
    }

    /**
     * Sets the content of the tool tip area to the specified text.
     *
     * @param text The text to be displayed
     */
    protected void setText(final String text) {
        if (tooltip == null) {
            return;
        }

        tooltip.setText(text);
        if (tooltip.getDocument() == null || text == null) {
            return;
        }

        icon.setVisible(false);
        final SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setItalic(sas, false);
        StyleConstants.setForeground(sas, Color.BLACK);
        StyleConstants.setBackground(sas, Color.WHITE);
        tooltip.getDocument().setParagraphAttributes(0, text.length(), sas,
                true);
    }

    /**
     * Sets whether or not this tool tip should be rendered as a warning.
     *
     * @param warning Warning string, null or empty to reset.
     * @since 0.6.3
     */
    public void setWarning(final String warning) {
        warningText = warning;
        reset();
    }

    /**
     * Registers a component with this tool tip handler.
     *
     * @param component Component to register
     */
    public void registerTooltipHandler(final JComponent component) {
        registerTooltipHandler(component, component.getToolTipText());
        component.setToolTipText(null);
    }

    /**
     * Registers a component with this tool tip handler.
     *
     * @param component Component to register
     * @param tooltipText Tool tip text for the component
     */
    @SuppressWarnings("unchecked")
    public void registerTooltipHandler(final JComponent component,
            final String tooltipText) {
        if (component == null) {
            return;
        }
        tooltips.put(component, tooltipText);
        if (component instanceof JXLayer<?>) {
            final LayerUI<JComponent> layerUI =
                    new AbstractLayerUI<JComponent>() {

                private static final long serialVersionUID =
                        -8698248993206174390L;

                /** {@inheritDoc} */
                @Override
                protected void processMouseEvent(final MouseEvent e,
                        final JXLayer<? extends JComponent> comp) {
                    if (e.getID() == MouseEvent.MOUSE_ENTERED) {
                        setText(tooltips.get(comp));
                    } else if (e.getID() == MouseEvent.MOUSE_EXITED && comp.
                            getMousePosition() == null) {
                        reset();
                    }
                    super.processMouseEvent(e, comp);
                }
            };
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    ((JXLayer<JComponent>) component).setUI(layerUI);
                }
            });
        } else {
            component.addMouseListener(this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        //Not used
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        //Not used
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        //Not used
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        if (e.getSource() instanceof JComponent) {
            setText(tooltips.get(e.getSource()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        reset();
    }
}

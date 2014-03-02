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

package com.dmdirc.addons.osd;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import net.miginfocom.swing.MigLayout;

/**
 * The OSD Window is an always-on-top window designed to convey information about events to the
 * user.
 */
public class OsdWindow extends JDialog implements MouseListener, MouseMotionListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 2;
    /** The OSD Manager that owns this window. */
    private final OsdManager osdManager;
    /** The manager to use to parse colours. */
    private final ColourManager colourManager;
    /** OSD Label. */
    private final JLabel label;
    /** OSD Panel. */
    private final JPanel panel;
    /** Starting positions of the mouse. */
    private int startX;
    private int startY;
    /** Desired position. */
    private volatile int desiredX;
    private volatile int desiredY;
    /** Is this a config instance? */
    private final boolean config;
    /** Timeout before the windows are automatically closed */
    private final Integer timeout;

    /**
     * Creates a new instance of OsdWindow.
     *
     * @param mainFrame          The window to associate with.
     * @param identityController The controller to read/write settings with.
     * @param colourManager      The manager to use to parse colours.
     * @param timeout            Timeout period for the window. Set to -1 to use value from config
     * @param text               The text to be displayed in the OSD window
     * @param config             Is the window being configured (should it timeout and allow itself
     *                           to be moved)
     * @param x                  The x-axis position for the OSD Window
     * @param y                  The y-axis position for the OSD window
     * @param osdManager         The manager that owns this OSD Window
     * @param domain             This plugin's settings domain
     */
    public OsdWindow(
            final MainFrame mainFrame,
            final IdentityController identityController,
            final OsdManager osdManager, final ColourManager colourManager,
            final int timeout, final String text, final boolean config, final int x,
            final int y, final String domain) {
        super(mainFrame, false);

        this.colourManager = colourManager;
        this.config = config;
        this.osdManager = osdManager;

        if (timeout < 0) {
            this.timeout = identityController.getGlobalConfiguration().getOptionInt(domain,
            "timeout", false);
        } else {
            this.timeout = timeout;
        }

        setFocusableWindowState(false);
        setAlwaysOnTop(true);
        setResizable(false);
        setUndecorated(true);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        desiredX = x;
        desiredY = y;

        setLocation(x, y);

        panel = new JPanel();
        panel.setBorder(new LineBorder(Color.BLACK));
        panel.setBackground(UIUtilities.convertColour(
                colourManager.getColourFromString(identityController.getGlobalConfiguration()
                        .getOptionString(domain, "bgcolour"), null)));

        final int width = identityController.getGlobalConfiguration().getOptionInt(domain, "width");
        setContentPane(panel);
        setLayout(new MigLayout("wmin " + width + ", wmax " + width + ", ins rel, fill"));

        label = new JLabel(text);
        label.setForeground(UIUtilities.convertColour(
                colourManager.getColourFromString(identityController.getGlobalConfiguration()
                        .getOptionString(domain, "fgcolour"), null)));
        label.setFont(label.getFont().deriveFont((float) identityController
                .getGlobalConfiguration().getOptionInt(domain, "fontSize")));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, "alignx center, hmin " + label.getFont().getSize());

        setVisible(true);
        pack();

        if (config) {
            addMouseMotionListener(this);
            addMouseListener(this);
        } else {
            addMouseListener(this);
            if (this.timeout != null && this.timeout > 0) {
                new Timer("OSD Display Timer").schedule(new TimerTask() {
                    
                    @Override
                    public void run() {
                        osdManager.closeWindow(OsdWindow.this);
                    }
                }, this.timeout * 1000);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (!config) {
            osdManager.closeWindow(this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        if (config) {
            startX = e.getPoint().x;
            startY = e.getPoint().y;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        final Point p = e.getLocationOnScreen();
        p.translate(-1 * startX, -1 * startY);
        setLocation(p);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        // Do nothing
    }

    /**
     * Sets the font size that this OSD uses.
     *
     * @param size The new size of the font
     */
    public void setFontSize(final int size) {
        label.setFont(label.getFont().deriveFont((float) size));
    }

    /**
     * Sets the background colour for this OSD.
     *
     * @param colour The background colour to use
     */
    public void setBackgroundColour(final String colour) {
        panel.setBackground(UIUtilities.convertColour(colourManager.getColourFromString(colour,
                Colour.WHITE)));
    }

    /**
     * Sets the foreground colour for this OSD.
     *
     * @param colour The foreground colour to use
     */
    public void setForegroundColour(final String colour) {
        label.setForeground(UIUtilities.convertColour(colourManager.getColourFromString(colour,
                Colour.WHITE)));
    }

    
    @Override
    public void setVisible(final boolean b) {
        super.setVisible(b);

        if (b) {
            transferFocusBackward();
        }
    }

    /**
     * Retrieves the desired x offset of this OSD window.
     *
     * @since 0.6.3
     * @see #setDesiredLocation(int, int)
     * @return The desired offset of this window
     */
    public int getDesiredX() {
        return desiredX;
    }

    /**
     * Retrieves the desired y offset of this OSD window.
     *
     * @since 0.6.3
     * @see #setDesiredLocation(int, int)
     * @return The desired offset of this window
     */
    public int getDesiredY() {
        return desiredY;
    }

    /**
     * Sets the desired location of this OSD window, and queues an event to move the window to the
     * desired location at some point in the future.
     * <p>
     * This method WILL NOT alter the location immediately, but will schedule an event in the AWT
     * event despatch thread which will be executed in the future.
     * <p>
     * This method will immediately update the values returned by the {@link #getDesiredX()} and
     * {@link #getDesiredY()} methods, but the {@link #getX()} and {@link #getY()} methods will
     * continue to reflect the actual location of the window.
     * <p>
     * This method is thread safe.
     *
     * @param x The desired x offset of this window
     * @param y The desired y offset of this window
     *
     * @since 0.6.3
     */
    public void setDesiredLocation(final int x, final int y) {
        this.desiredX = x;
        this.desiredY = y;

        UIUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                setLocation(getDesiredX(), getDesiredY());
            }
        });
    }

    
    @Override
    public String toString() {
        return label.getText();
    }

}

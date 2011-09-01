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

package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.ui.StatusBarComponent;

import java.awt.Color;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

/**
 * A panel shown in the status bar which displays a {@link StatusbarPopupWindow}
 * when the user mouses over it.
 *
 * @param <T> Type of component used to trigger this panel
 * @since 0.6.3m1
 */
public abstract class StatusbarPanel<T extends JComponent> extends JPanel
        implements StatusBarComponent, MouseListener {

    /** The label we use to show information. */
    protected final T label;
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** The popup window we're using to show extra info. */
    private StatusbarPopupWindow dialog;
    /** Non selected border. */
    protected final Border nonSelectedBorder;
    /** Selected border. */
    protected final Border selectedBorder;

    /**
     * Creates a new {@link StatusbarPanel}, using the specified label.
     *
     * @param label The component to be displayed in the status bar
     */
    public StatusbarPanel(final T label) {
        this(label, new EtchedBorder(), new SidelessEtchedBorder(
                SidelessEtchedBorder.Side.TOP));
    }

    /**
     * Creates a new {@link StatusbarPanel}.
     *
     * @param label The component to be displayed in the status bar
     * @param nonSelectedBorder The border for when the panel is unselected
     * @param selectedBorder The border for when for the panel is selected
     */
    public StatusbarPanel(final T label, final Border nonSelectedBorder,
            final Border selectedBorder) {
        super();

        this.label = label;
        this.nonSelectedBorder = nonSelectedBorder;
        this.selectedBorder = selectedBorder;

        setBorder(nonSelectedBorder);
        setLayout(new MigLayout("ins 0 rel 0 rel, aligny center"));
        add(label);

        addMouseListener(this);
    }

    /**
     * Returns the component associated with this panel.
     *
     * @return Assocaited component
     */
    public T getComponent() {
        return label;
    }

    /**
     * Closes and reopens the dialog to update information and border positions.
     */
    public final void refreshDialog() {
        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (StatusbarPanel.this) {
                    if (dialog != null) {
                        closeDialog();
                        openDialog();
                    }
                }
            }
        });
    }

    /**
     * Opens the information dialog.
     */
    protected void openDialog() {
        synchronized (StatusbarPanel.this) {
            if (dialog == null) {
                setBackground(getPopupBackground());
                setForeground(getPopupForeground());
                setBorder(selectedBorder);
                dialog = getWindow();
                dialog.setVisible(true);
            }
        }
    }

    /**
     * Closes the information dialog.
     */
    protected void closeDialog() {
        synchronized (StatusbarPanel.this) {
            if (dialog != null) {
                setBackground(null);
                setForeground(null);
                setBorder(nonSelectedBorder);
                dialog.setVisible(false);
                dialog.dispose();
                dialog = null;
            }
        }
    }

    /**
     * Checks if this dialog is open.
     *
     * @return is the dialog open
     */
    protected final boolean isDialogOpen() {
        synchronized (StatusbarPanel.this) {
            if (dialog != null) {
                return dialog.isVisible();
            }
            return false;
        }
    }

    /**
     * Returns the dialog being shown by this panel.
     *
     * @return Visible dialog, or null if not visible
     */
    protected final StatusbarPopupWindow getDialog() {
        return dialog;
    }

    /**
     * Returns the colour to be used for the panel's foreground.
     *
     * @return Foreground colour
     */
    protected Color getPopupForeground() {
        return UIManager.getColor("ToolTip.foreground");
    }

    /**
     * Returns the colour to be used for the panel's background.
     *
     * @return Background colour
     */
    protected Color getPopupBackground() {
        return UIManager.getColor("ToolTip.background");

    }

    /**
     * Retrieves the implementation of {@link StatusbarPopupWindow} that should
     * be shown by this panel when the user mouses over it.
     *
     * @return A concrete {@link StatusbarPopupWindow} implementation to use
     */
    protected abstract StatusbarPopupWindow getWindow();
}

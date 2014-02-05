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

package com.dmdirc.addons.ui_swing.dialogs;

import com.dmdirc.ui.CoreUIUtils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * Provides common methods for dialogs.
 */
public class StandardDialog extends JDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Parent window. */
    private final Window owner;
    /** The OK button for this frame. */
    private JButton okButton;
    /** The cancel button for this frame. */
    private JButton cancelButton;

    /**
     * Creates a new instance of StandardDialog.
     *
     * @param owner The frame that owns this dialog
     * @param modal Whether to display modally or not
     */
    public StandardDialog(final Frame owner, final boolean modal) {
        this(owner, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
    }

    /**
     * Creates a new instance of StandardDialog.
     *
     * @param owner The frame that owns this dialog
     * @param modal Whether to display modally or not
     */
    public StandardDialog(final Window owner, final ModalityType modal) {
        super(owner, modal);
        this.owner = owner;

        if (owner != null) {
            setIconImages(owner.getIconImages());
        }
        orderButtons(new JButton(), new JButton());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /** {@inheritDoc} */
    @Override
    public void setTitle(final String title) {
        super.setTitle("DMDirc: " + title);
    }

    /**
     * Displays the dialog centering on the parent window.
     */
    public void display() {
        display(owner);
    }

    /**
     * Displays the dialog if it is not visible, otherwise requests focus.
     */
    public void displayOrRequestFocus() {
        if (isVisible()) {
            requestFocus();
        } else {
            display();
        }
    }

    /**
     * Displays the dialog centering on the specified window.
     *
     * @param owner Window to center on
     */
    public void display(final Component owner) {
        if (isVisible()) {
            return;
        }
        addWindowListener(new WindowAdapter() {

            /** {@inheritDoc} */
            @Override
            public void windowClosing(final WindowEvent e) {
                executeAction(getCancelButton());
            }
        });
        centreOnOwner();
        pack();
        centreOnOwner();
        setVisible(false);
        setVisible(true);
    }

    /**
     * Displays the dialog centering on the parent window, blocking until
     * complete.
     */
    public void displayBlocking() {
        displayBlocking(owner);
    }

    /**
     * Displays the dialog centering on the specified window, blocking until
     * complete.
     *
     * @param owner Window to center on
     */
    public void displayBlocking(final Component owner) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Unable to display blocking dialog"
                    + " in the EDT.");
        }
        final Semaphore semaphore = new Semaphore(0);
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                display(owner);
                addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosed(final WindowEvent e) {
                        semaphore.release();
                    }
                });
            }
        });
        semaphore.acquireUninterruptibly();
    }


    /**
     * Centres this dialog on its owner, or the screen if no owner is present.
     */
    public void centreOnOwner() {
        if (owner == null) {
            CoreUIUtils.centreWindow(this);
        } else {
            setLocationRelativeTo(owner);
        }
    }

    /**
     * Returns the window owner for this dialog.
     *
     * @return Parent window or null
     */
    public Window getParentWindow() {
        return owner;
    }

    /**
     * Sets the specified button up as the OK button.
     * @param button The target button
     */
    protected void setOkButton(final JButton button) {
        okButton = button;
        button.setText("OK");
        button.setDefaultCapable(false);
    }

    /**
     * Sets the specified button up as the Cancel button.
     * @param button The target button
     */
    protected void setCancelButton(final JButton button) {
        cancelButton = button;
        button.setText("Cancel");
        button.setDefaultCapable(false);
    }

    /**
     * Gets the left hand button for a dialog.
     * @return left JButton
     */
    protected JButton getLeftButton() {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return getOkButton();
        } else {
            return getCancelButton();
        }
    }

    /**
     * Gets the right hand button for a dialog.
     * @return right JButton
     */
    protected JButton getRightButton() {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return getCancelButton();
        } else {
            return getOkButton();
        }
    }

    /**
     * Orders the OK and Cancel buttons in an appropriate order for the current
     * operating system.
     * @param leftButton The left-most button
     * @param rightButton The right-most button
     */
    protected void orderButtons(final JButton leftButton,
            final JButton rightButton) {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            // Windows - put the OK button on the left
            setOkButton(leftButton);
            setCancelButton(rightButton);
        } else {
            // Everything else - adhere to usability guidelines and put it on
            // the right.
            setOkButton(rightButton);
            setCancelButton(leftButton);
        }
        leftButton.setPreferredSize(new Dimension(100, 25));
        rightButton.setPreferredSize(new Dimension(100, 25));
        leftButton.setMinimumSize(new Dimension(100, 25));
        rightButton.setMinimumSize(new Dimension(100, 25));
    }

    /**
     * Retrieves the OK button for this form.
     * @return The form's OK button
     */
    public JButton getOkButton() {
        return okButton;
    }

    /**
     * Retrieves the Cancel button for this form.
     * @return The form's cancel button
     */
    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Simulates the user clicking on the specified target button.
     * @param target The button to use
     */
    public void executeAction(final JButton target) {
        if (target != null && target.isEnabled()) {
            target.doClick();
        }
    }

    /**
     * This method is called when enter is pressed anywhere in the dialog
     * except on a button. By default this method does nothing.
     *
     * @return Returns true if the key press has been handled and is not be
     * be forwarded on by default this is false
     */
    public boolean enterPressed() {
        return false;
    }

    /**
     * This method is called when ctrl + enter is pressed anywhere in the
     * dialog. By default this method presses the OK button.
     *
     * @return Returns true if the key press has been handled and is not be
     * be forwarded on by default this is true
     */
    public boolean ctrlEnterPressed() {
        executeAction(getOkButton());
        return true;
    }

    /**
     * This method is called when enter is pressed anywhere in the dialog
     * except on a button. By default this method presses the cancel button.
     *
     * @return Returns true if the key press has been handled and is not be
     * be forwarded on by default this is true
     */
    public boolean escapePressed() {
        executeAction(getCancelButton());
        return true;
    }
}

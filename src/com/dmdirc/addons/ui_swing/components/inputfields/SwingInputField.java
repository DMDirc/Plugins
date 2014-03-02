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

package com.dmdirc.addons.ui_swing.components.inputfields;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.colours.ColourPickerDialog;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.InputField;
import com.dmdirc.interfaces.ui.InputValidationListener;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.collections.ListenerList;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/** Swing input field. */
public class SwingInputField extends JComponent implements InputField,
        KeyListener, InputValidationListener, PropertyChangeListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Colour picker. */
    private ColourPickerDialog colourPicker;
    /** Input field text field. */
    private final JTextField textField;
    /** Line wrap indicator. */
    private final JLabel wrapIndicator;
    /** Error indicator. */
    private final JLabel errorIndicator;
    /** Listener list. */
    private final ListenerList listeners;
    /** Parent Window. */
    private final Window parentWindow;
    /** The config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** The icon manager to use for icons. */
    private final IconManager iconManager;
    /** The colour manager to use. */
    private final ColourManager colourManager;

    /**
     * Instantiates a new swing input field.
     *
     * @param parentWindow  Parent window
     * @param globalConfig  The configuration to read settings from.
     * @param iconManager   The icon manager to use for icons.
     * @param colourManager The colour manager to use.
     */
    @Inject
    public SwingInputField(
            final MainFrame parentWindow,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @GlobalConfig final IconManager iconManager,
            final ColourManager colourManager) {
        super();

        this.parentWindow = parentWindow;
        this.globalConfig = globalConfig;
        this.iconManager = iconManager;
        this.colourManager = colourManager;

        listeners = new ListenerList();

        textField = new JTextField();
        textField.setFocusTraversalKeysEnabled(false);
        textField.addKeyListener(this);
        textField.setOpaque(true);
        wrapIndicator = new JLabel(iconManager.getIcon("linewrap"));
        wrapIndicator.setVisible(false);
        errorIndicator = new JLabel(iconManager.getIcon("input-error"));
        errorIndicator.setVisible(false);

        setLayout(new MigLayout("ins 0, hidemode 3"));

        final FontMetrics fm = textField.getFontMetrics(textField.getFont());
        add(textField, "grow, push, hmin " + (fm.getMaxAscent() + fm.
                getMaxDescent() + PlatformDefaults.getPanelInsets(0).getValue()));
        add(wrapIndicator, "");
        add(errorIndicator, "");

        setActionMap(textField.getActionMap());
        setInputMap(SwingInputField.WHEN_FOCUSED,
                textField.getInputMap(SwingInputField.WHEN_FOCUSED));
        setInputMap(SwingInputField.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                textField.getInputMap(SwingInputField.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
        setInputMap(SwingInputField.WHEN_IN_FOCUSED_WINDOW,
                textField.getInputMap(SwingInputField.WHEN_IN_FOCUSED_WINDOW));
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener(this);
    }

    @Override
    public void requestFocus() {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textField.requestFocus();
            }
        });
    }

    @Override
    public boolean requestFocusInWindow() {
        return UIUtilities.invokeAndWait(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                return textField.requestFocusInWindow();
            }
        });
    }

    @Override
    public void showColourPicker(final boolean irc, final boolean hex) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (globalConfig.getOptionBool("general", "showcolourdialog")) {
                    colourPicker = new ColourPickerDialog(SwingInputField.this,
                            colourManager, iconManager, irc, hex, parentWindow);
                    colourPicker.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent actionEvent) {
                            try {
                                textField.getDocument().
                                        insertString(textField.getCaretPosition(),
                                                actionEvent.getActionCommand(), null);
                            } catch (final BadLocationException ex) {
                                //Ignore, wont happen
                            }
                            colourPicker.dispose();
                            colourPicker = null;
                        }
                    });
                    colourPicker.setVisible(true);
                    colourPicker.setLocation((int) textField.getLocationOnScreen().
                            getX(),
                            (int) textField.getLocationOnScreen().getY() - colourPicker.getHeight());
                }
            }
        });
    }

    @Override
    public void hideColourPicker() {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (colourPicker != null) {
                    colourPicker.dispose();
                    colourPicker = null;
                }
            }
        });
    }

    /**
     * Returns the textfield for this inputfield.
     *
     * @return JTextField
     */
    public JTextField getTextField() {
        return UIUtilities.invokeAndWait(new Callable<JTextField>() {

            @Override
            public JTextField call() {
                return textField;
            }
        });
    }

    @Override
    public void addActionListener(final ActionListener listener) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textField.addActionListener(listener);
            }
        });
    }

    @Override
    public void addKeyListener(final KeyListener listener) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                listeners.add(KeyListener.class, listener);
            }
        });
    }

    @Override
    public void removeActionListener(final ActionListener listener) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textField.removeActionListener(listener);
            }
        });
    }

    @Override
    public void removeKeyListener(final KeyListener listener) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                listeners.remove(KeyListener.class, listener);
            }
        });
    }

    @Override
    public String getSelectedText() {
        return UIUtilities.invokeAndWait(new Callable<String>() {

            @Override
            public String call() {
                return textField.getSelectedText();
            }
        });
    }

    @Override
    public int getSelectionEnd() {
        return UIUtilities.invokeAndWait(new Callable<Integer>() {

            @Override
            public Integer call() {
                return textField.getSelectionEnd();
            }
        });
    }

    @Override
    public int getSelectionStart() {
        return UIUtilities.invokeAndWait(new Callable<Integer>() {

            @Override
            public Integer call() {
                return textField.getSelectionStart();
            }
        });
    }

    @Override
    public String getText() {
        return UIUtilities.invokeAndWait(new Callable<String>() {

            @Override
            public String call() {
                return textField.getText();
            }
        });
    }

    @Override
    public void setText(final String text) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textField.setText(text);
            }
        });
    }

    @Override
    public int getCaretPosition() {
        return UIUtilities.invokeAndWait(new Callable<Integer>() {

            @Override
            public Integer call() {
                return textField.getCaretPosition();
            }
        });
    }

    @Override
    public void setCaretPosition(final int position) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textField.setCaretPosition(position);
            }
        });
    }

    /**
     * Replaces the selection with the specified text.
     *
     * @param clipboard Text to replace selection with
     */
    public void replaceSelection(final String clipboard) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textField.replaceSelection(clipboard);
            }
        });
    }

    /**
     * Sets the caret colour to the specified coloour.
     *
     * @param optionColour Colour for the caret
     */
    public void setCaretColor(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textField.setCaretColor(optionColour);
            }
        });
    }

    /**
     * Sets the foreground colour to the specified coloour.
     *
     * @param optionColour Colour for the caret
     */
    @Override
    public void setForeground(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textField.setForeground(optionColour);
            }
        });
    }

    /**
     * Sets the background colour to the specified coloour.
     *
     * @param optionColour Colour for the caret
     */
    @Override
    public void setBackground(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textField.setBackground(optionColour);
            }
        });
    }

    @Override
    public boolean hasFocus() {
        return UIUtilities.invokeAndWait(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                return textField.hasFocus();
            }
        });
    }

    @Override
    public boolean isFocusOwner() {
        return UIUtilities.invokeAndWait(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                return textField.isFocusOwner();
            }
        });
    }

    /**
     * Sets the start index of the selection for this component.
     *
     * @param selectionStart Start index
     */
    public void setSelectionStart(final int selectionStart) {
        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textField.setSelectionStart(selectionStart);
            }
        });
    }

    /**
     * Sets the end index of the selection for this component.
     *
     * @param selectionEnd End index
     */
    public void setSelectionEnd(final int selectionEnd) {
        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textField.setSelectionEnd(selectionEnd);
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param e Key event
     */
    @Override
    public void keyTyped(final KeyEvent e) {
        for (final KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyTyped(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Key event
     */
    @Override
    public void keyPressed(final KeyEvent e) {
        for (final KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyPressed(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Key event
     */
    @Override
    public void keyReleased(final KeyEvent e) {
        for (final KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyReleased(e);
        }
    }

    @Override
    public void illegalCommand(final String reason) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                errorIndicator.setVisible(true);
                errorIndicator.setToolTipText(reason);
                wrapIndicator.setVisible(false);
            }
        });
    }

    @Override
    public void legalCommand() {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                errorIndicator.setVisible(false);
                errorIndicator.setToolTipText(null);
            }
        });
    }

    @Override
    public void wrappedText(final int count) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                wrapIndicator.setVisible(count > 1);
                wrapIndicator.setToolTipText(count + " lines");
                errorIndicator.setVisible(false);
            }
        });
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (!isFocusOwner()) {
            hideColourPicker();
        }
    }

}

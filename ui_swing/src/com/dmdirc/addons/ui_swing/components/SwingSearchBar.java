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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.SearchAction;
import com.dmdirc.addons.ui_swing.components.frames.InputTextFrame;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.addons.ui_swing.textpane.TextPane;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.ui.SearchBar;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.IRCDocument;
import com.dmdirc.ui.messages.IRCDocumentSearcher;
import com.dmdirc.ui.messages.LinePosition;
import com.dmdirc.util.collections.ListenerList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/**
 * Status bar, shows message and info on the gui.
 */
public final class SwingSearchBar extends JPanel implements ActionListener,
        KeyListener, SearchBar, DocumentListener, ConfigChangeListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 6;
    /** Frame parent. */
    private final TextFrame parent;
    /** Colour Manager. */
    private final ColourManager colourManager;
    /** Close button. */
    private ImageButton<Object> closeButton;
    /** Next match button. */
    private JButton nextButton;
    /** Previous match button. */
    private JButton prevButton;
    /** Case sensitive checkbox. */
    private JCheckBox caseCheck;
    /** Search text field. */
    private ValidatingJTextField searchBox;
    /** Line to search from. */
    private int line;
    /** Listener list. */
    private final ListenerList listeners;
    /** Search validate text. */
    private SearchValidator validator;
    /** Wrap indicator. */
    private JLabel wrapIndicator;

    /**
     * Creates a new instance of StatusBar.
     *
     * @param newParent   parent frame for the dialog
     * @param iconManager Icon manager to retrieve icons from
     */
    public SwingSearchBar(final TextFrame newParent,
            final IconManager iconManager,
            final ColourManager colourManager) {
        listeners = new ListenerList();

        this.parent = newParent;
        this.colourManager = colourManager;

        getInputMap(JComponent.WHEN_FOCUSED).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "searchAction");

        getActionMap().put("searchAction", new SearchAction(this));

        initComponents(iconManager);
        layoutComponents();
        addListeners();
    }

    /** Initialises components. */
    private void initComponents(final IconManager iconManager) {
        closeButton = new ImageButton<>("close", parent.getIconManager().getIcon("close-inactive"),
                parent.getIconManager().getIcon("close-active"));
        nextButton = new JButton();
        prevButton = new JButton();
        caseCheck = new JCheckBox();
        validator = new SearchValidator();
        searchBox = new ValidatingJTextField(iconManager, validator);
        wrapIndicator = new JLabel("Search wrapped", parent.getIconManager().getIcon("linewrap"),
                JLabel.LEFT);

        nextButton.setText("Later");
        prevButton.setText("Earlier");
        nextButton.setEnabled(false);
        prevButton.setEnabled(false);
        caseCheck.setText("Case sensitive");
        wrapIndicator.setVisible(false);

        line = -1;

        setColours();
    }

    /** Lays out components. */
    private void layoutComponents() {
        this.setLayout(new MigLayout("ins 0, fill, hidemode 3"));

        add(closeButton);
        add(searchBox, "growx, pushx, sgy all");
        add(prevButton, "sgx button, sgy all");
        add(nextButton, "sgx button, sgy all");
        add(caseCheck, "sgy all");
        add(wrapIndicator, "");
    }

    /** Adds listeners to components. */
    private void addListeners() {
        closeButton.addActionListener(this);
        searchBox.addKeyListener(this);
        nextButton.addActionListener(this);
        prevButton.addActionListener(this);
        caseCheck.addActionListener(this);
        searchBox.getDocument().addDocumentListener(this);

        parent.getContainer().getConfigManager().addChangeListener(
                "ui", "backgroundcolour", this);
        parent.getContainer().getConfigManager().addChangeListener(
                "ui", "foregroundcolour", this);
    }

    /**
     * {@inheritDoc}.
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == closeButton) {
            close();
        } else if (e.getSource() == nextButton) {
            search(Direction.DOWN, searchBox.getText(), caseCheck.isSelected());
        } else if (e.getSource() == prevButton) {
            search(Direction.UP, searchBox.getText(), caseCheck.isSelected());
        } else if (e.getSource() == caseCheck) {
            validator.setValidates(true);
            searchBox.checkError();
            line = parent.getTextPane().getLastVisibleLine();
        }
    }

    /** {@inheritDoc}. */
    @Override
    public void open() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                validator.setValidates(true);
                searchBox.checkError();
                setVisible(true);
                getFocus();
            }
        });
    }

    /** {@inheritDoc}. */
    @Override
    public void close() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setVisible(false);
                if (parent instanceof InputTextFrame) {
                    ((InputTextFrame) parent).getInputField().
                            requestFocusInWindow();
                } else {
                    parent.requestFocusInWindow();
                }
            }
        });
    }

    /** {@inheritDoc}. */
    @Override
    public void search(final String text, final boolean caseSensitive) {
        if (!searchBox.getText().isEmpty()) {
            if (line == -1) {
                line = parent.getTextPane().getLastVisibleLine();
            }
            search(Direction.UP, text, caseSensitive);
        }
    }

    /** {@inheritDoc}. */
    @Override
    public void search(final Direction direction, final String text,
            final boolean caseSensitive) {
        wrapIndicator.setVisible(false);

        final boolean up = Direction.UP == direction;

        final TextPane textPane = parent.getTextPane();
        final IRCDocument document = textPane.getDocument();
        final IRCDocumentSearcher searcher = new IRCDocumentSearcher(text,
                document,
                caseSensitive);
        searcher.setPosition(textPane.getSelectedRange());

        final LinePosition result = up ? searcher.searchUp() : searcher.
                searchDown();

        if (result != null) {
            if ((textPane.getSelectedRange().getEndLine() != 0 || textPane.
                    getSelectedRange().getEndPos() != 0)
                    && (up && result.getEndLine() > textPane.getSelectedRange().getEndLine()
                    || !up && result.getStartLine() < textPane.getSelectedRange().getStartLine())) {
                wrapIndicator.setVisible(true);
                textPane.setScrollBarPosition(result.getEndLine());
                textPane.setSelectedText(result);
                validator.setValidates(true);
                searchBox.checkError();
            } else {
                //found, select and return found
                textPane.setScrollBarPosition(result.getEndLine());
                textPane.setSelectedText(result);
                validator.setValidates(true);
                searchBox.checkError();
            }
        }
    }

    /**
     * Returns the textfield used in this search bar.
     *
     * @return Search textfield
     */
    public JTextField getTextField() {
        return searchBox;
    }

    /**
     * {@inheritDoc}.
     *
     * @param event Key event
     */
    @Override
    public void keyPressed(final KeyEvent event) {
        if (event.getSource() == searchBox) {
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                close();
            } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                search(Direction.UP, searchBox.getText(),
                        caseCheck.isSelected());
            } else if (event.getKeyCode() != KeyEvent.VK_F3 && event.
                    getKeyCode() != KeyEvent.VK_F) {
                line = parent.getTextPane().getLastVisibleLine();
            }
        }

        for (KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyPressed(event);
        }
    }

    /**
     * {@inheritDoc}.
     *
     * @param event Key event
     */
    @Override
    public void keyTyped(final KeyEvent event) {
        //Ignore
    }

    /**
     * {@inheritDoc}.
     *
     * @param event Key event
     */
    @Override
    public void keyReleased(final KeyEvent event) {
        //Ignore
    }

    /** Focuses the search box in the search bar. */
    public void getFocus() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                searchBox.requestFocusInWindow();
                searchBox.setSelectionStart(0);
                searchBox.setSelectionEnd(searchBox.getText().length());
            }
        });
    }

    /** {@inheritDoc}. */
    @Override
    public String getSearchPhrase() {
        return searchBox.getText();
    }

    /** {@inheritDoc}. */
    @Override
    public boolean isCaseSensitive() {
        return caseCheck.isSelected();
    }

    /** {@inheritDoc}. */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        validator.setValidates(true);
        searchBox.checkError();
        nextButton.setEnabled(!searchBox.getText().isEmpty());
        prevButton.setEnabled(!searchBox.getText().isEmpty());
    }

    /** {@inheritDoc}. */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        validator.setValidates(true);
        searchBox.checkError();
        nextButton.setEnabled(!searchBox.getText().isEmpty());
        prevButton.setEnabled(!searchBox.getText().isEmpty());
    }

    /** {@inheritDoc}. */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        //Ignore
    }

    @Override
    public void addKeyListener(final KeyListener l) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                listeners.add(KeyListener.class, l);
            }
        });
    }

    @Override
    public void removeKeyListener(final KeyListener l) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                listeners.remove(KeyListener.class, l);
            }
        });
    }

    @Override
    public void configChanged(final String domain, final String key) {
        setColours();
    }

    /** Sets the colours used in this document. */
    private void setColours() {
        final AggregateConfigProvider config = parent.getContainer().getConfigManager();

        searchBox.setForeground(UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "ui", "foregroundcolour"), null)));
        searchBox.setBackground(UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "ui", "backgroundcolour"), null)));
        searchBox.setCaretColor(UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "ui", "foregroundcolour"), null)));
    }

}

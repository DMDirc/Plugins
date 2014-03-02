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

package com.dmdirc.addons.ui_swing.components.substitutions;

import com.dmdirc.addons.ui_swing.dialogs.actioneditor.StringTransferable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.text.JTextComponent;

/**
 * Action substitution label.
 */
public class SubstitutionLabel extends JButton implements MouseListener,
        DragGestureListener, ActionListener, FocusListener {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 1;
    /** Drag source. */
    private DragSource dragSource;
    /** Substitution. */
    private final transient Substitution substitution;
    /** Previously selected component. */
    private Component previousComponent;

    /**
     * Instantiates the panel.
     *
     * @param substitution Action substitition
     */
    public SubstitutionLabel(final Substitution substitution) {
        super();

        this.substitution = substitution;

        initComponents();
        addListeners();
    }

    /** Initialises the components. */
    private void initComponents() {
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY, this);

        setText(substitution.getName());
        setFont(getFont().deriveFont(getFont().getSize() - 2f));

        setBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        setContentAreaFilled(false);
        setMargin(new Insets(0, 0, 0, 0));
    }

    /** Adds the listeners. */
    private void addListeners() {
        addMouseListener(this);
        addFocusListener(this);
        addActionListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }

    
    @Override
    public void dragGestureRecognized(final DragGestureEvent dge) {
        dragSource.startDrag(dge, Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR), new StringTransferable(substitution
                .toString()), null);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (previousComponent instanceof JTextComponent) {
            ((JTextComponent) previousComponent).replaceSelection(
                    substitution.toString());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Focus event
     */
    @Override
    public void focusGained(final FocusEvent e) {
        previousComponent = e.getOppositeComponent();
    }

    /**
     * {@inheritDoc}
     *
     * @param e Focus event
     */
    @Override
    public void focusLost(final FocusEvent e) {
        //Ignore
    }

}

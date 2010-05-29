/*
 *
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components.reorderablelist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Simple panel to reorder a JList using buttons.
 */
public class ListReorderButtonPanel extends JPanel implements
        ListSelectionListener, ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** List to reoder. */
    private JList list;
    /** Default list model. */
    private DefaultListModel model;
    /** Up button. */
    private JButton up;
    /** Down button. */
    private JButton down;

    /**
     * Creates a panel to reoder a jlist.
     *
     * @param list JList
     */
    public ListReorderButtonPanel(final ReorderableJList list) {
        this.list = list;
        this.model = list.getModel();

        list.addListSelectionListener(this);
        up = new JButton("/\\");
        down = new JButton("\\/");

        up.addActionListener(this);
        down.addActionListener(this);

        setLayout(new MigLayout("fill"));

        add(up, "wrap");
        add(down, "");

        valueChanged(null);
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        up.setEnabled(list.getSelectedIndex() != -1);
        down.setEnabled(list.getSelectedIndex() != -1);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final Object object = list.getSelectedValue();
        final int currentIndex = list.getSelectedIndex();
        if (e.getSource() == up) {
            moveUp(object, currentIndex);
        } else {
            moveDown(object, currentIndex);
        }
    }

    /**
     * Moves an object up in the list.
     *
     * @param object Object to move
     * @param currentIndex Current index
     */
    private void moveUp(final Object object, final int currentIndex) {
        int newIndex = currentIndex - 1;
        if (newIndex < 0) {
            newIndex = model.getSize() - 1;
        }
        moveElement(object, newIndex);
    }

    /**
     * Moves an object down in the list.
     *
     * @param object Object to move
     * @param currentIndex Current index
     */
    private void moveDown(final Object object, final int currentIndex) {
        int newIndex = currentIndex + 1;
        if (newIndex > model.getSize() - 1) {
            newIndex = 0;
        }
        moveElement(object, newIndex);
    }

    /**
     * Moves an object from its current positition to a new position in a list.
     *
     * @param object Object to move
     * @param newIndex Current index
     */
    private void moveElement(final Object object, final int newIndex) {
        model.removeElement(object);
        model.add(newIndex, object);
        list.setSelectedIndex(newIndex);
    }
}

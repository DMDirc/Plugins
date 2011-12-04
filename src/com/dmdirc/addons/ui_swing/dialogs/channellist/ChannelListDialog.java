/*
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

package com.dmdirc.addons.ui_swing.dialogs.channellist;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

/**
 *
 */
public class ChannelListDialog extends StandardDialog implements
        ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Swing controller. */
    private final SwingController controller;
    /** Arguments label. */
    private JLabel argsLabel;
    /** Search arguments field. */
    private JTextField args;
    /** List panel. */
    private ChannelListPanel list;

    public ChannelListDialog(final SwingController controller) {
        super(controller.getMainFrame(), ModalityType.MODELESS);
        this.controller = controller;
        setTitle("Channel List");
        argsLabel = new JLabel("Arguments:" );
        args = new JTextField();
        list = new ChannelListPanel();
        layoutComponents();
        addListeners();

        list.setVisible(false);
    }

    private void layoutComponents() {
        setLayout(new MigLayout("fill, wmin 40%, hmin 40%, hidemode 3"));
        add(argsLabel, "");
        add(args, "wrap");
        add(list, "wrap");
        add(getLeftButton(), "split, right, sg button");
        add(getRightButton(), "right, sg button");
    }

    private void addListeners() {
        getRightButton().addActionListener(this);
        getLeftButton().addActionListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getCancelButton()) {
        } else {
            dispose();
        }
    }

}

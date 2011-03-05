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

package com.dmdirc.addons.swingdebug;

import com.dmdirc.addons.ui_swing.DMDircEventQueue;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.CheckBoxMenuItem;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.plugins.BasePlugin;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

/**
 * Swing debug plugin. Provides long running EDT task violation detection and
 * a console for System.out and System.err.
 */
public class SwingDebugPlugin extends BasePlugin implements ActionListener {

    /** Swing controller. */
    private final SwingController controller;
    /** Debug menu. */
    private JMenu debugMenu;
    /** Debug EDT menu item. */
    private JCheckBoxMenuItem debugEDT;
    /** Debug EDT menu item. */
    private JCheckBoxMenuItem showSysOut;
    /** Debug EDT menu item. */
    private JCheckBoxMenuItem showSysErr;
    /** Old System.out. */
    private PrintStream sysout;
    /** Old System.err. */
    private PrintStream syserr;
    /** System.out redirect thread. */
    private SystemStreamRedirectThread out;
    /** System.err redirect thread. */
    private SystemStreamRedirectThread err;
    /** System out frame. */
    private JDialog outFrame;
    /** System error frame. */
    private JDialog errorFrame;

    /**
     * Creates a new SwingDebugPlugin.
     *
     * @param controller The controller to add debug entries to
     */
    public SwingDebugPlugin(final SwingController controller) {
        this.controller = controller;
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        try {
            sysout = System.out;
            syserr = System.err;
            final TextLabel outTextArea = new TextLabel();
            final TextLabel errorTextArea = new TextLabel();
            outFrame = new JDialog(controller.getMainFrame());
            errorFrame = new JDialog(controller.getMainFrame());
            outFrame.setTitle("DMDirc: System.out Console");
            errorFrame.setTitle("DMDirc: System.err Console");
            outFrame.setLayout(new MigLayout("ins 0, pack, wmin 20sp, hmin 20sp"));
            errorFrame.setLayout(new MigLayout("ins 0, pack, wmin 20sp, hmin 20sp"));
            outFrame.add(new JScrollPane(outTextArea), "grow, push");
            errorFrame.add(new JScrollPane(errorTextArea), "grow, push");
            outFrame.pack();
            errorFrame.pack();
            out = new SystemStreamRedirectThread(
                    SystemStreamRedirectThread.Stream.OUT,
                    outTextArea.getDocument());
            err = new SystemStreamRedirectThread(
                    SystemStreamRedirectThread.Stream.IN,
                    errorTextArea.getDocument());
            out.start();
            err.start();
            debugMenu = new JMenu("Debug");
            debugEDT = new CheckBoxMenuItem("Check EDT task length");
            showSysOut = new CheckBoxMenuItem("Show System.out Console");
            showSysErr = new CheckBoxMenuItem("Show System.err Console");
            debugEDT.addActionListener(this);
            showSysErr.addActionListener(this);
            showSysOut.addActionListener(this);
            controller.getMainFrame().getJMenuBar().add(debugMenu);
            debugMenu.add(debugEDT);
            debugMenu.add(showSysOut);
            debugMenu.add(showSysErr);
            CloseListener.add(showSysOut.getModel(), outFrame);
            CloseListener.add(showSysErr.getModel(), outFrame);
        } catch (IOException ex) {
            onUnload();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        out.cancel();
        err.cancel();
        System.setOut(sysout);
        System.setErr(syserr);
        outFrame = null;
        errorFrame = null;
        controller.getMainFrame().getJMenuBar().remove(debugMenu);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == debugEDT) {
            if (debugEDT.getState()) {
                Toolkit.getDefaultToolkit().getSystemEventQueue().
                        push(new TracingEventQueue(this, controller));
            } else {
                Toolkit.getDefaultToolkit().getSystemEventQueue().
                        push(new DMDircEventQueue(controller));
            }
        }
        if (e.getSource() == showSysOut) {
            outFrame.setVisible(showSysOut.getState());
        }
        if (e.getSource() == showSysErr) {
            errorFrame.setVisible(showSysErr.getState());
        }
    }

}

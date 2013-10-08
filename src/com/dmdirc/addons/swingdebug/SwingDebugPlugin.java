/*
 * Copyright (c) 2006-2013 DMDirc Developers
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
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.plugins.implementations.BasePlugin;
import com.dmdirc.ui.WindowManager;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import lombok.RequiredArgsConstructor;

/**
 * Swing debug plugin. Provides long running EDT task violation detection and a
 * console for System.out and System.err.
 */
@RequiredArgsConstructor
public class SwingDebugPlugin extends BasePlugin implements ActionListener {

    /** The controller to read/write settings with. */
    private final IdentityController identityController;
    /** Swing controller. */
    private final SwingController controller;
    /** Window Management. */
    private final WindowManager windowManager;
    /** Debug menu. */
    private JMenu debugMenu;
    /** Debug EDT menu item. */
    private JCheckBoxMenuItem debugEDT;
    /** Debug EDT menu item. */
    private JCheckBoxMenuItem showSysOut;
    /** Debug EDT menu item. */
    private JCheckBoxMenuItem showSysErr;
    /** System out window. */
    private SystemStreamContainer sysoutFrame;
    /** System error window. */
    private SystemStreamContainer syserrFrame;

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
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
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
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
                        push(new TracingEventQueue(this, identityController, controller));
            } else {
                Toolkit.getDefaultToolkit().getSystemEventQueue().
                        push(new DMDircEventQueue(controller));
            }
        }
        if (e.getSource() == showSysOut) {
            if (showSysOut.isSelected()) {
                sysoutFrame = new SystemStreamContainer(SystemStreamType.Out,
                        controller.getGlobalConfig(), this);
                windowManager.addWindow(sysoutFrame);
            } else {
                sysoutFrame.close();
            }
        }

        if (e.getSource() == showSysErr) {
            if (showSysErr.isSelected()) {
                syserrFrame = new SystemStreamContainer(SystemStreamType.Error,
                        controller.getGlobalConfig(), this);
                windowManager.addWindow(syserrFrame);
            } else {
                syserrFrame.close();
            }
        }
    }

    /**
     * Notifies this plugin the specified container is closing.
     *
     * @param container Container that is closing
     */
    void windowClosing(final SystemStreamContainer container) {
        if (container == sysoutFrame) {
            showSysOut.setSelected(false);
        }
        if (container == syserrFrame) {
            showSysErr.setSelected(false);
        }
    }
}

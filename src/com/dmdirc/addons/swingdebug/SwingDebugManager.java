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

package com.dmdirc.addons.swingdebug;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.DMDircEventQueue;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.CheckBoxMenuItem;
import com.dmdirc.interfaces.FrameCloseListener;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.util.URLBuilder;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

public class SwingDebugManager implements ActionListener, FrameCloseListener {

    /** This plugin's settings domain. */
    private final String domain;
    /** The config to read settings with. */
    private final AggregateConfigProvider globalConfig;
    /** Swing controller. */
    private final SwingController controller;
    /** Window Management. */
    private final WindowManager windowManager;
    /** Swing main frame. */
    private final MainFrame mainFrame;
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
    /** URL Builder to use for frame containers. */
    private final URLBuilder urlBuilder;

    @Inject
    public SwingDebugManager(
            @PluginDomain(SwingDebugPlugin.class) final String domain,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final MainFrame mainFrame,
            final SwingController controller,
            final WindowManager windowManager,
            final URLBuilder urlBuilder) {
        this.domain = domain;
        this.globalConfig = globalConfig;
        this.controller = controller;
        this.windowManager = windowManager;
        this.urlBuilder = urlBuilder;
        this.mainFrame = mainFrame;
    }

    public void load() {
        debugMenu = new JMenu("Debug");
        debugEDT = new CheckBoxMenuItem("Check EDT task length");
        showSysOut = new CheckBoxMenuItem("Show System.out Console");
        showSysErr = new CheckBoxMenuItem("Show System.err Console");
        debugEDT.addActionListener(this);
        showSysErr.addActionListener(this);
        showSysOut.addActionListener(this);
        mainFrame.getJMenuBar().add(debugMenu);
        debugMenu.add(debugEDT);
        debugMenu.add(showSysOut);
        debugMenu.add(showSysErr);
    }

    public void unload() {
        mainFrame.getJMenuBar().remove(debugMenu);
        debugMenu = null;
        debugEDT = null;
        showSysOut = null;
        showSysErr = null;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == debugEDT) {
            if (debugEDT.getState()) {
                Toolkit.getDefaultToolkit().getSystemEventQueue().
                        push(new TracingEventQueue(domain, globalConfig, controller));
            } else {
                Toolkit.getDefaultToolkit().getSystemEventQueue().
                        push(new DMDircEventQueue(controller));
            }
        }
        if (e.getSource() == showSysOut) {
            if (showSysOut.isSelected()) {
                sysoutFrame = new SystemStreamContainer(SystemStreamType.Out, globalConfig,
                        urlBuilder);
                sysoutFrame.addCloseListener(this);
                windowManager.addWindow(sysoutFrame);
            } else {
                sysoutFrame.close();
            }
        }

        if (e.getSource() == showSysErr) {
            if (showSysErr.isSelected()) {
                syserrFrame = new SystemStreamContainer(SystemStreamType.Error, globalConfig,
                        urlBuilder);
                syserrFrame.addCloseListener(this);
                windowManager.addWindow(syserrFrame);
            } else {
                syserrFrame.close();
            }
        }
    }

    @Override
    public void windowClosing(final FrameContainer window) {
        if (window == syserrFrame) {
            showSysErr.setSelected(false);
            syserrFrame.removeCloseListener(this);
        }
        if (window == sysoutFrame) {
            showSysOut.setSelected(false);
            sysoutFrame.removeCloseListener(this);
        }
    }

}

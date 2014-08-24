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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.ClientModule.AddonConfig;
import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.dialogs.DialogKeyListener;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.logger.ErrorLevel;

import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;

import javax.inject.Inject;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.layout.PlatformDefaults;

/**
 * Initialises swing and system UI settings.
 */
public class SwingUIInitialiser {

    private final Apple apple;
    private final AggregateConfigProvider globalConfig;
    private final ConfigProvider addonConfig;
    private final DialogKeyListener dialogKeyListener;
    private final DMDircEventQueue eventQueue;
    private final DMDircMBassador eventBus;

    @Inject
    public SwingUIInitialiser(final DMDircMBassador eventBus, final Apple apple,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @AddonConfig final ConfigProvider addonConfig,
            final DialogKeyListener dialogKeyListener,
            final DMDircEventQueue eventQueue) {
        this.eventBus = eventBus;
        this.apple = apple;
        this.globalConfig = globalConfig;
        this.addonConfig = addonConfig;
        this.dialogKeyListener = dialogKeyListener;
        this.eventQueue = eventQueue;
    }

    public void load() {
        apple.load();
        setAntiAlias();
        initUISettings();
        installEventQueue();
        installKeyListener();
    }

    public void unload() {
        uninstallEventQueue();
        uninstallKeyListener();
    }

    /**
     * Make swing not use Anti Aliasing if the user doesn't want it.
     */
    private void setAntiAlias() {
        // For this to work it *HAS* to be before anything else UI related.
        final boolean aaSetting = globalConfig.getOptionBool("ui", "antialias");
        System.setProperty("awt.useSystemAAFontSettings",
                Boolean.toString(aaSetting));
        System.setProperty("swing.aatext", Boolean.toString(aaSetting));
    }

    /**
     * Initialises the global UI settings for the Swing UI.
     */
    private void initUISettings() {
        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                // This will do nothing on non OS X Systems
                if (Apple.isApple()) {
                    apple.setUISettings();
                    apple.setListener();
                }

                final Font defaultFont = new Font(Font.DIALOG, Font.TRUETYPE_FONT, 12);
                if (UIManager.getFont("TextField.font") == null) {
                    UIManager.put("TextField.font", defaultFont);
                }
                if (UIManager.getFont("TextPane.font") == null) {
                    UIManager.put("TextPane.font", defaultFont);
                }
                addonConfig.setOption("ui", "textPaneFontName",
                        UIManager.getFont("TextPane.font").getFamily());
                addonConfig.setOption("ui", "textPaneFontSize",
                        UIManager.getFont("TextPane.font").getSize());

                try {
                    UIUtilities.initUISettings();
                    UIManager.setLookAndFeel(UIUtilities.getLookAndFeel(
                            globalConfig.getOption("ui", "lookandfeel")));
                    UIUtilities.setUIFont(new Font(globalConfig.getOption("ui", "textPaneFontName"),
                            Font.PLAIN, 12));
                } catch (UnsupportedOperationException | UnsupportedLookAndFeelException |
                        IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
                    eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, ex,
                            "Unable to set UI Settings", ""));
                }

                if ("Metal".equals(UIManager.getLookAndFeel().getName())
                        || Apple.isAppleUI()) {
                    PlatformDefaults.setPlatform(PlatformDefaults.WINDOWS_XP);
                }
            }
        });
    }

    /**
     * Installs the dialog key listener.
     */
    private void installKeyListener() {
        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .addKeyEventDispatcher(dialogKeyListener);
            }
        });
    }

    /**
     * Removes the dialog key listener.
     */
    private void uninstallKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removeKeyEventDispatcher(dialogKeyListener);
    }

    /**
     * Installs the DMDirc event queue.
     */
    private void installEventQueue() {
        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                Toolkit.getDefaultToolkit().getSystemEventQueue().push(eventQueue);
            }
        });
    }

    /**
     * Removes the DMDirc event queue.
     */
    private void uninstallEventQueue() {
        eventQueue.pop();
    }

}

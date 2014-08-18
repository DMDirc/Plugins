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

import com.dmdirc.addons.ui_swing.components.renderers.FontListCellRenderer;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;

import com.google.common.eventbus.EventBus;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

/**
 * System font picking component.
 */
public class FontPicker extends JComboBox<Object> {

    /** A version number for this class. */
    private static final long serialVersionUID = -9054812588033935839L;
    /** Font family to choose from. */
    private final String fontFamily;

    /**
     * Creates a new Font picker for the specified font family.
     *
     * @param eventBus   The event bus to post errors to
     * @param fontFamily Font family
     */
    public FontPicker(final EventBus eventBus, final String fontFamily) {
        super(new DefaultComboBoxModel<>());
        this.fontFamily = fontFamily;

        setRenderer(new FontListCellRenderer(getRenderer()));
        new LoggingSwingWorker<String[], String[]>(eventBus) {

            @Override
            protected String[] doInBackground() {
                return GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getAvailableFontFamilyNames();
            }

            @Override
            protected void done() {
                try {
                    loadFonts(get());
                } catch (InterruptedException ex) {
                    //Ignore
                } catch (ExecutionException ex) {
                    eventBus.post(new UserErrorEvent(ErrorLevel.MEDIUM, ex, ex.getMessage(), ""));
                }
            }
        }.execute();
    }

    /**
     * Loads the fonts and adds them to the font picker.
     *
     * @param fonts Fonts to load
     */
    private void loadFonts(final String... fonts) {
        final int size = getFont().getSize();
        for (final String font : fonts) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    ((DefaultComboBoxModel<Object>) getModel()).addElement(new Font(
                            font, Font.PLAIN, size));
                }
            });
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setSelectedItem(new Font(fontFamily, Font.PLAIN, size));
            }
        });
    }

}

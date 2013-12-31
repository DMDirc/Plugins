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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.addons.ui_swing.BackgroundOption;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.util.URLBuilder;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.jdesktop.jxlayer.plaf.LayerUI;

/**
 * Background painting layer UI. Paints a opaque background then paints the
 * specified image onto the background layer.
 */
@Slf4j
@SuppressWarnings("unused")
public class BackgroundPainter extends LayerUI<JComponent> {

    /**
     * Domain to retrieve settings from.
     */
    @Getter(value = AccessLevel.PROTECTED)
    @NonNull
    private final String domain;

    /**
     * Key in domain to get image URL from.
     */
    @NonNull
    @Getter(value = AccessLevel.PROTECTED)
    private final String imageKey;

    /**
     * Key in domain to get image background type from.
     */
    @NonNull
    @Getter(value = AccessLevel.PROTECTED)
    private final String optionKey;

    /** The URL builder to use to find icons. */
    private final URLBuilder urlBuilder;

    /**
     * Config manager to bind to and retrieve settings from.
     */
    private final AggregateConfigProvider configManager;

    /**
     * Background image.
     */
    private Image backgroundImage;

    /**
     * Background option type.
     */
    private BackgroundOption backgroundOption;

    /**
     * Creates a new background painter.
     *
     * @param configManager Config manager to retrieve settings from
     * @param urlBuilder URL Builder
     * @param domain Domain to retrieve settings from
     * @param imageKey Key for background image
     * @param optionKey Key for background type
     */
    public BackgroundPainter(
            final AggregateConfigProvider configManager,
            final URLBuilder urlBuilder,
            final String domain, final String imageKey,
            final String optionKey) {
        this.configManager = configManager;
        this.urlBuilder = urlBuilder;
        this.domain = domain;
        this.imageKey = imageKey;
        this.optionKey = optionKey;
        configManager.getBinder().bind(this, BackgroundPainter.class);
    }

    /**
     * Called to update the value of the image URL.
     *
     * @param value New image URL
     */
    @ConfigBinding(domain = "plugin-ui_swing", key = "textpanebackground")
    public void updateImage(final String value) {
        if (value == null || value.isEmpty()) {
            backgroundImage = null;
        } else {
            new ImageLoader(urlBuilder.getUrl(value)).executeInExecutor();
        }
    }

    /**
     * Called to update the value of the background type
     *
     * @param value New background type
     */
    @ConfigBinding(domain = "plugin-ui_swing", key = "textpanebackgroundoption")
    public void updateOption(final String value) {
        try {
            backgroundOption = BackgroundOption.valueOf(value);
        } catch (IllegalArgumentException ex) {
            backgroundOption = BackgroundOption.CENTER;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(final Graphics graphics, final JComponent component) {
        final Graphics2D g2 = (Graphics2D) graphics;
        g2.setColor(component.getBackground());
        g2.fill(g2.getClipBounds());
        UIUtilities.paintBackground(g2, component.getBounds(),
                backgroundImage, backgroundOption);
        super.paint(graphics, component);
    }

    /**
     * Called to unbind this painter from its config binder.
     */
    public void unbind() {
        configManager.getBinder().unbind(this);
    }

    /**
     * Loads image URLs in the background.
     */
    @AllArgsConstructor
    private class ImageLoader extends LoggingSwingWorker<Image, Void> {

        /**
         * URL of image file to load.
         */
        private final URL imageURL;

        /**
         * {@inheritDoc}
         */
        @Override
        protected Image doInBackground() {
            try {
                log.trace("Loading image: {}", imageURL);
                if (imageURL != null) {
                    return ImageIO.read(imageURL);
                }
                return null;
            } catch (IOException ex) {
                log.trace("Background loading IOException: {}", ex.getMessage());
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void done() {
            try {
                if (isCancelled()) {
                    log.trace("Background loading cancelled.");
                    return;
                }
                log.trace("Background loading complete: {}", get());
                backgroundImage = get();
            } catch (InterruptedException ex) {
                log.debug("Interrupted whilst loading image: {}", imageURL);
            } catch (ExecutionException ex) {
                log.debug("Exception whilst loading image: {}. "
                        + "Exception message:", imageURL, ex.getMessage());
            }
        }
    }
}

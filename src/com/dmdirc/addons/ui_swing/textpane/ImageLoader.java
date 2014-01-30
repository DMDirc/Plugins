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

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import org.slf4j.LoggerFactory;

/**
 * Loads image URLs in the background.
 */
public class ImageLoader extends LoggingSwingWorker<Image, Void> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ImageLoader.class);

    /**
     * URL of image file to load.
     */
    private final URL imageURL;
    /**
     * Background painter to load image into.
     */
    private final BackgroundPainter painter;

    public ImageLoader(final URL imageURL, final BackgroundPainter painter) {
        this.imageURL = imageURL;
        this.painter = painter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Image doInBackground() {
        try {
            LOG.trace("Loading image: {}", imageURL);
            if (imageURL != null) {
                return ImageIO.read(imageURL);
            }
            return null;
        } catch (IOException ex) {
            LOG.trace("Background loading IOException: {}", ex.getMessage());
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
                LOG.trace("Background loading cancelled.");
                return;
            }
            LOG.trace("Background loading complete: {}", get());
            painter.setBackgroundImage(get());
        } catch (InterruptedException ex) {
            LOG.debug("Interrupted whilst loading image: {}", imageURL);
        } catch (ExecutionException ex) {
            LOG.debug("Exception whilst loading image: {}. " + "Exception message:", imageURL, ex.getMessage());
        }
    }
}

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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.BackgroundOption;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.util.URLBuilder;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.annotation.Nonnull;
import javax.swing.JComponent;

import org.jdesktop.jxlayer.plaf.LayerUI;

/**
 * Background painting layer UI. Paints a opaque background then paints the specified image onto the
 * background layer.
 */
public class BackgroundPainter extends LayerUI<JComponent> {

    /** A version for this class, sued for serialisation. */
    private static final long serialVersionUID = 1L;

    /**
     * Domain to retrieve settings from.
     */
    @Nonnull
    private final String domain;
    /**
     * Key in domain to get image URL from.
     */
    @Nonnull
    private final String imageKey;
    /**
     * Key in domain to get image background type from.
     */
    @Nonnull
    private final String optionKey;
    /** The URL builder to use to find icons. */
    private final URLBuilder urlBuilder;
    /**
     * Config manager to bind to and retrieve settings from.
     */
    private final AggregateConfigProvider configManager;
    /** The event bus to post errors to. */
    private final DMDircMBassador eventBus;
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
     * @param urlBuilder    URL Builder
     * @param eventBus      The event bus to post errors to
     * @param domain        Domain to retrieve settings from
     * @param imageKey      Key for background image
     * @param optionKey     Key for background type
     */
    public BackgroundPainter(
            final AggregateConfigProvider configManager,
            final URLBuilder urlBuilder,
            final DMDircMBassador eventBus,
            final String domain, final String imageKey,
            final String optionKey) {
        this.configManager = configManager;
        this.urlBuilder = urlBuilder;
        this.domain = domain;
        this.imageKey = imageKey;
        this.optionKey = optionKey;
        this.eventBus = eventBus;
        configManager.getBinder().bind(this, BackgroundPainter.class);
    }

    protected String getDomain() {
        return domain;
    }

    protected String getImageKey() {
        return imageKey;
    }

    protected String getOptionKey() {
        return optionKey;
    }

    protected void setBackgroundImage(final Image backgroundImage) {
        this.backgroundImage = backgroundImage;
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
            new ImageLoader(urlBuilder.getUrl(value), this, eventBus).execute();
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

}

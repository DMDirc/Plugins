/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.addons.ui_swing.BackgroundOption;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.binding.ConfigBinding;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.util.URLBuilder;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.plaf.LayerUI;

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
    /**
     * Key in the domain to get image opacity level.
     */
    @Nonnull
    private final String opacityKey;

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
     * Background opacity.
     */
    private float opacity;

    /**
     * Creates a new background painter.
     *
     * @param configManager Config manager to retrieve settings from
     * @param urlBuilder    URL Builder
     * @param domain        Domain to retrieve settings from
     * @param imageKey      Key for background image
     * @param optionKey     Key for background type
     * @param opacityKey    Key for the opacity
     */
    public BackgroundPainter(
            final AggregateConfigProvider configManager,
            final URLBuilder urlBuilder,
            @Nonnull final String domain, @Nonnull final String imageKey,
            @Nonnull final String optionKey, @Nonnull final String opacityKey) {
        this.configManager = configManager;
        this.urlBuilder = urlBuilder;
        this.domain = domain;
        this.imageKey = imageKey;
        this.optionKey = optionKey;
        this.opacityKey = opacityKey;
        configManager.getBinder().bind(this, BackgroundPainter.class);
    }

    @Nonnull
    protected String getDomain() {
        return domain;
    }

    @Nonnull
    protected String getImageKey() {
        return imageKey;
    }

    @Nonnull
    protected String getOptionKey() {
        return optionKey;
    }

    @Nonnull
    protected String getOpacityKey() {
        return opacityKey;
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
            new ImageLoader(urlBuilder.getUrl(value), this).execute();
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

    @ConfigBinding(domain = "plugin-ui_swing", key = "textpanebackgroundopacity")
    public void updateOpacity(final String opacity) {
        try {
            this.opacity = Float.valueOf(opacity);
            if (this.opacity < 0 || this.opacity > 1) {
                this.opacity = 1;
            }
        } catch (IllegalArgumentException ex) {
            this.opacity = 1;
        }
    }

    @Override
    public void paint(final Graphics graphics, final JComponent component) {
        final Graphics2D g2 = (Graphics2D) graphics;
        g2.setColor(component.getBackground());
        g2.fill(g2.getClipBounds());
        UIUtilities.paintBackground(g2, component.getBounds(),
                backgroundImage, backgroundOption, opacity);
        super.paint(graphics, component);
    }

    /**
     * Called to unbind this painter from its config binder.
     */
    public void unbind() {
        configManager.getBinder().unbind(this);
    }

}

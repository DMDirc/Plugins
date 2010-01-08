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

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.ui.core.util.ExtendedAttributedString;
import com.dmdirc.ui.core.util.Utils;
import com.dmdirc.ui.messages.Styliser;
import java.awt.Font;

import java.text.AttributedString;
import java.util.Arrays;

import javax.swing.UIManager;

/**
 * Represents a line of text in IRC.
 */
class Line implements ConfigChangeListener {

    private final String[] lineParts;
    private final ConfigManager config;
    private int lineHeight;
    private String fontName;

    /**
     * Creates a new line.
     *
     * @param lineParts Parts of the line
     * @param config Configuration manager for this line
     */
    public Line(final String[] lineParts, final ConfigManager config) {
        this.lineParts = lineParts;
        this.config = config;
        setCachedSettings();
        config.addChangeListener("ui", "textPaneFontSize", this);
        config.addChangeListener("ui", "textPaneFontName", this);
    }

    /**
     * Creates a new line with a specified height.
     *
     * @param lineParts Parts of the line
     * @param config Configuration manager for this line
     * @param lineHeight The height for this line
     */
    public Line(final String[] lineParts, final ConfigManager config,
            final int lineHeight) {
        this.lineParts = lineParts;
        this.config = config;
        setCachedSettings();
        config.addChangeListener("ui", "textPaneFontSize", this);
        config.addChangeListener("ui", "textPaneFontName", this);
    }

    /**
     * Returns the line parts of this line.
     *
     * @return Lines parts
     */
    public String[] getLineParts() {
        return lineParts;
    }

    /**
     * Returns the length of the specified line
     * 
     * @return Length of the line
     */
    public int getLength() {
        int length = 0;
        for (String linePart : lineParts) {
            length += linePart.length();
        }
        return length;
    }

    /**
     * Returns the height of the specified line.
     * 
     * @return Line height
     */
    public int getHeight() {
        return lineHeight;
    }

    /**
     * Returns the Line text at the specified number.
     *
     * @return Line at the specified number or null
     */
    public String getText() {
        StringBuilder lineText = new StringBuilder();
        for (String linePart : lineParts) {
            lineText.append(linePart);
        }
        return Styliser.stipControlCodes(lineText.toString());
    }

    /**
     * Returns the Line text at the specified number.
     *
     * @return Line at the specified number or null
     *
     * @since 0.6.3m1
     */
    public String getStyledText() {
        StringBuilder lineText = new StringBuilder();
        for (String linePart : lineParts) {
            lineText.append(linePart);
        }
        return lineText.toString();
    }

    /**
     * Converts a StyledDocument into an AttributedString.
     *
     * @return AttributedString representing the specified StyledDocument
     */
    public AttributedString getStyled() {
        final ExtendedAttributedString string = Utils.getAttributedString(lineParts,
                fontName, lineHeight);
        lineHeight = string.getMaxLineHeight();
        return string.getAttributedString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Line) {
            return Arrays.equals(((Line) obj).getLineParts(), getLineParts());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getLineParts().hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        setCachedSettings();
    }

    private void setCachedSettings() {
        final Font defaultFont = UIManager.getFont("TextPane.font");
        if (config.hasOptionString("ui", "textPaneFontName")) {
            fontName = config.getOption("ui", "textPaneFontName");
        } else {
            fontName = defaultFont.getName();
        }
        if (config.hasOptionString("ui", "textPaneFontSize")) {
            lineHeight = config.getOptionInt("ui", "textPaneFontSize");
        } else {
            lineHeight = defaultFont.getSize();
        }
    }
}

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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;

import java.awt.Component;

import javax.swing.JSplitPane;

/**
 * JSplit pane that snaps around its components preferred size.
 */
public class SplitPane extends JSplitPane implements ConfigChangeListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 2;
    /** use one touch expandable? */
    private boolean useOneTouchExpandable;
    /** Global config manager. */
    private final AggregateConfigProvider config;

    /** Orientation type . */
    public enum Orientation {

        /** Horizontal orientation. */
        HORIZONTAL,
        /** Vertical orientation. */
        VERTICAL

    }

    /**
     * Instantiates a new snapping split pane. Defaults to using a horizontal split, two null
     * components and snapping to the left component.
     *
     * @param manager Config manager to read values from
     */
    public SplitPane(final AggregateConfigProvider manager) {
        this(manager, Orientation.HORIZONTAL, null, null);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using a horizontal split, two null
     * components and snapping to the left component.
     *
     * @param manager     Config manager to read values from
     * @param orientation Split pane orientation
     */
    public SplitPane(final AggregateConfigProvider manager, final Orientation orientation) {
        this(manager, orientation, null, null);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using two null components.
     *
     * @param manager        Config manager to read values from
     * @param orientation    Split pane orientation <code>JSplitPane.HORIZONTAL_SPLIT</code> or
     *                       <code>JSplitPane.VERTICAL_SPLIT</code>
     * @param leftComponent  left component
     * @param rightComponent right component
     */
    public SplitPane(final AggregateConfigProvider manager, final Orientation orientation,
            final Component leftComponent, final Component rightComponent) {
        super((orientation.equals(Orientation.HORIZONTAL))
                ? HORIZONTAL_SPLIT : VERTICAL_SPLIT,
                true, leftComponent, rightComponent);

        config = manager;
        useOneTouchExpandable = config.getOptionBool(
                "ui", "useOneTouchExpandable");

        setOneTouchExpandable(useOneTouchExpandable);
        setContinuousLayout(true);

        getActionMap().setParent(null);
        getActionMap().clear();

        config.addChangeListener("ui", "useOneTouchExpandable", this);
    }

    @Override
    public void configChanged(final String domain, final String key) {
        useOneTouchExpandable = config.getOptionBool(
                "ui", "useOneTouchExpandable");

        UIUtilities.invokeLater(() -> setOneTouchExpandable(useOneTouchExpandable));
    }

}

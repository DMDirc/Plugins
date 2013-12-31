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

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.AwayStateListener;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.FrameCloseListener;

import javax.swing.JLabel;

/**
 * Simple panel to show when a user is away or not.
 */
public class AwayLabel extends JLabel implements ConfigChangeListener,
        AwayStateListener, FrameCloseListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** awayindicator string for compiler optimisation. */
    private static final String AWAY_INDICATOR = "awayindicator";
    /** Away indicator. */
    private boolean useAwayIndicator;
    /** Parent frame container. */
    private final FrameContainer container;

    /**
     * Creates a new away label for the specified container.
     *
     * @param container Parent frame container
     */
    public AwayLabel(final FrameContainer container) {
        super("(away)");

        this.container = container;

        container.getConfigManager().addChangeListener("ui", AWAY_INDICATOR,
                this);
        setVisible(false);
        useAwayIndicator = container.getConfigManager().getOptionBool("ui",
                AWAY_INDICATOR);

        if (container.getConnection() != null) {
            setVisible(container.getConnection().isAway());
            container.getConnection().addAwayStateListener(this);
        }

        container.addCloseListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        useAwayIndicator = container.getConfigManager()
                .getOptionBool("ui", AWAY_INDICATOR);
        if (!useAwayIndicator) {
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    setVisible(false);
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onAway(final String reason) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (useAwayIndicator) {
                    setVisible(true);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onBack() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (useAwayIndicator) {
                    setVisible(false);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing(final FrameContainer window) {
        if (container != null && container.getConnection() != null) {
            container.getConnection().removeAwayStateListener(this);
        }
    }
}

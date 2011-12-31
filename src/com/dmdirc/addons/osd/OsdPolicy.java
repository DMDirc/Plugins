/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
package com.dmdirc.addons.osd;

/**
 * Enumerates OSD Policies.
 *
 * @author simon
 * @since 0.6.3
 */
public enum OsdPolicy {

    /** Spawn new windows below old ones. */
    DOWN ("Place new windows below old ones", true) {
        /** {@inheritDoc} */
        @Override
        public int getYPosition(final OsdManager osdManager, final int startY) {
            int y = startY;
            for (OsdWindow window : osdManager.getWindowList()) {
                if (window.isVisible()) {
                    y = Math.max(y, window.getY() + window.getHeight() + WINDOW_GAP);
                }
            }
            return y;
        }
    },
    /** Spawn new windows above old ones. */
    UP ("Place new windows above old ones", true) {
        /** {@inheritDoc} */
        @Override
        public int getYPosition(final OsdManager osdManager, final int startY) {
            int y = startY;
            for (OsdWindow window : osdManager.getWindowList()) {
                if (window.isVisible()) {
                    y = Math.min(y, window.getY() - window.getHeight() - WINDOW_GAP);
                }
            }
            return y;
        }
    },
    /** Close old OSD windows and display the new windows. */
    CLOSE ("Close existing windows", false) {
        /** {@inheritDoc} */
        @Override
        public int getYPosition(final OsdManager osdManager, final int startY) {
            osdManager.closeAll();
            return startY;
        }
    },
    /** Place new windows on top of old windows. */
    ONTOP ("Place new windows on top of existing windows", false) {
        /** {@inheritDoc} */
        @Override
        public int getYPosition(final OsdManager osdManager, final int startY) {
            return startY;
        }
    };

    /** The spacing between the windows. */
    private static final int WINDOW_GAP = 5;

    /** Description of policy. */
    private final String description;

    /** Does policy need to change window location */
    private final boolean changesPosition;

    /**
     * Creates a new instance of OsdPolicies.
     *
     * @param description Description of the behaviour of the enum value
     */
    OsdPolicy(final String description, final boolean changesPosition) {
        this.description = description;
        this.changesPosition = changesPosition;
    }

    /**
     * Calculates the Y position for new windows according to this policy.
     * <p>
     * In order to ensure that windows are displayed at the correct position,
     * the calling party MUST ensure that the window list is not altered between
     * this method's invocation and the time at which the window is displayed.
     * If the window list is altered, multiple windows may appear on top of
     * each other instead of stacking correctly, or there may be gaps in up/down
     * policy layouts.
     *
     * @param osdManager OSD Manager we are using
     * @param startY Value of startY from the main config
     *
     * @return returns Y Value to use for new windows
     */
    public abstract int getYPosition(final OsdManager osdManager, final int startY);

    /**
     * See if this behaviour changes window position
     *
     * @return true or false depending on window behaviour
     */
    public boolean changesPosition() {
        return changesPosition;
    };

    /**
     * Return a description of what each policy does.
     *
     * @return Description of policies behaviour
     */
    public String getDescription() {
        return description;
    }
}

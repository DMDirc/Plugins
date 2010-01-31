/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes,
 * Simon Mott
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
public enum OsdPolicies {

    /** Spawn new windows below old ones. */
    DOWN ("Place new windows below old ones") {
        /** {@inheritDoc} */
        @Override
        int getYPosition(OsdManager osdManager, int y) {
            for (OsdWindow window : osdManager.getWindowList()) {
                if (window.isVisible()) {
                    y = Math.max(y, window.getY() + window.getHeight() + WINDOW_GAP);
                }
            }
            return y;
        }
        /** {@inheritDoc} */
        @Override
        boolean changesPosition() {
            return true;
        }
    },
    /** Spawn new windows above old ones. */
    UP ("Place new windows above old ones") {
        /** {@inheritDoc} */
        @Override
        int getYPosition(OsdManager osdManager, int y) {
            for (OsdWindow window : osdManager.getWindowList()) {
                if (window.isVisible()) {
                    y = Math.min(y, window.getY() - window.getHeight() - WINDOW_GAP);
                }
            }
            return y;
        }
        /** {@inheritDoc} */
        @Override
        boolean changesPosition() {
            return true;
        }
    },
    /** Close old OSD windows and display the new windows. */
    CLOSE ("Close existing windows") {
        /** {@inheritDoc} */
        @Override
        int getYPosition(OsdManager osdManager, int y) {
            osdManager.closeAll();
            return y;
        }
        /** {@inheritDoc} */
        @Override
        boolean changesPosition() {
            return false;
        }
    },
    /** Place new windows on top of old windows. */
    ONTOP ("Place new windows on top of existing windows") {
        /** {@inheritDoc} */
        @Override
        int getYPosition(OsdManager osdManager, int y) {
            return y;
        }
        /** {@inheritDoc} */
        @Override
        boolean changesPosition() {
            return false;
        }
    };

    /** The spacing between the windows. */
    private static final int WINDOW_GAP = 5;

    /** Description of policy. */
    private final String description;

    /**
     * Creates a new instance of OsdPolicies.
     *
     * @param description Description of the behaviour of the enum value
     */
    OsdPolicies(String description) {
        this.description = description;
    }

    /**
     * Calculates Y position for new windows based on enum value.
     *
     * @param osdManager OSD Manager we are using
     * @param y Value of startY from the main config
     *
     * @return returns Y Value to use for new windows
     */
    abstract int getYPosition(OsdManager osdManager, int y);

    /**
     * See if this behaviour changes window position
     *
     * @return true or false depending on window behaviour
     */
    abstract boolean changesPosition();

    /**
     * Return a description of what each policy does.
     *
     * @param policy policy to get description of
     * 
     * @return Description of policies behaviour
     */
    public String getDescription() {
        return description;
    }
}

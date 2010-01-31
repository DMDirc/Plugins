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
 * Enumerates OSD Policies
 *
 * @author simon
 * @since 0.6.3
 */
public enum OsdPolicies {

    /** Spawn new windows below old ones */
    DOWN,
    /** Spawn new windows above old ones */
    UP,
    /** Close old OSD windows and display the new windows */
    CLOSE,
    /** Place new windows on top of old windows */
    ONTOP;

    /**
     * Return a description of what each policy does.
     *
     * @param policy policy to get description of
     * @return Description of policies behaviour
     */
    public String getDescription(String policy) {
        if (OsdPolicies.valueOf(policy) == OsdPolicies.DOWN) {
            return "Place new windows below old ones";
        } else if (OsdPolicies.valueOf(policy) == OsdPolicies.UP) {
            return "Place new windows above old ones";
        } else if (OsdPolicies.valueOf(policy) == OsdPolicies.CLOSE) {
            return "Close existing windows";
        } else if (OsdPolicies.valueOf(policy) == OsdPolicies.ONTOP) {
            return "Place new windows on top of existing windows";
        } else {
            return OsdPolicies.valueOf(policy).toString();
        }
    }
}

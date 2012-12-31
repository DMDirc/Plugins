/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.sessionlock;

import com.dmdirc.interfaces.actions.ActionMetaType;
import com.dmdirc.interfaces.actions.ActionType;

/**
 * Session lock action types.
 */
public enum SessionLockActionType implements ActionType {

    /** Indicates the session is locked. */
    SESSION_LOCK("Session locked"),
    /** Indicated the session is unlocked. */
    SESSION_UNLOCK("Session unlocked");

    /** Type name. */
    private final String name;

    /**
     * Creates a new instance of this action type.
     *
     * @param name Name
     */
    SessionLockActionType(final String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public ActionMetaType getType() {
        return SessionLockActionMetaType.SESSION_EVENT;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

}

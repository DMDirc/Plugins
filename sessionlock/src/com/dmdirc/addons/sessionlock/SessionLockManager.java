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

package com.dmdirc.addons.sessionlock;

import com.dmdirc.actions.ActionManager;

import javax.inject.Inject;

import com.greboid.lock.LockAdapter;
import com.greboid.lock.LockListener;

public class SessionLockManager implements LockListener {

    /** Action manager. */
    private final ActionManager actionManager;
    /** Have we registered our actions? */
    private static boolean registered;
    /** Lock Adapter to detect session events. */
    private LockAdapter lockAdapter;

    @Inject
    public SessionLockManager(final ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    /**
     * Loads the manager, registering action types and the lock adapter.
     */
    public void load() {
        if (!registered) {
            actionManager.registerTypes(SessionLockActionType.values());
            registered = true;
        }

        lockAdapter = new LockAdapter();
        lockAdapter.addLockListener(this);
    }

    /**
     * Unloads the plugin, removing lock adapter, action types remain.
     */
    public void unload() {
        lockAdapter.removeLockListener(this);
        lockAdapter = null;
    }

    @Override
    public void locked() {
        actionManager.triggerEvent(SessionLockActionType.SESSION_LOCK, null);
    }

    @Override
    public void unlocked() {
        actionManager.triggerEvent(SessionLockActionType.SESSION_UNLOCK, null);
    }

}

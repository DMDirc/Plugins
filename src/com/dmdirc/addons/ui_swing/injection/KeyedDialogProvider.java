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

package com.dmdirc.addons.ui_swing.injection;

import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import static com.dmdirc.addons.ui_swing.SwingPreconditions.checkOnEDT;

/**
 * Provider for {@link StandardDialog} based windows that correspond to some key.
 *
 * <p>This provider will cache instances that are created until the windows are closed. Once a
 * window has been closed, the next call to {@link #get()} or {@link #displayOrRequestFocus()}
 * will result in a new instance being created.
 *
 * <p>Dialogs with different keys may be open simultaneously, and are treated independently.
 *
 * @param <K> The type of key that dialogs are associated with.
 * @param <T> The type of dialog that will be managed.
 */
public abstract class KeyedDialogProvider<K, T extends StandardDialog> {

    /** The existing instances being displayed, if any. */
    private final Map<K, T> instances = new HashMap<>();

    /**
     * Gets an instance of the dialog provided by this class.
     *
     * <p>If there is an existing instance with the same key that hasn't been closed, it will be
     * returned. Otherwise a new instance will be created and returned. New instances will not be
     * automatically be * displayed to the user - use
     * {@link #displayOrRequestFocus(java.lang.Object)} to do so.
     *
     * <p>This method <em>must</em> be called on the Event Despatch Thread.
     *
     * @param key The key associated with the dialog to get.
     * @return An instance of the dialog.
     */
    public T get(final K key) {
        checkOnEDT();
        if (!instances.containsKey(key)) {
            final T instance = getInstance(key);
            instance.addWindowListener(new Listener(key));
            instances.put(key, instance);
        }
        return instances.get(key);
    }

    /**
     * Disposes of the dialog associated with the given key, if it exists.
     *
     * <p>This method <em>must</em> be called on the Event Despatch Thread.
     *
     * @param key The key associated with the dialog to close.
     */
    public void dispose(final K key) {
        checkOnEDT();
        if (instances.containsKey(key)) {
            instances.get(key).dispose();
        }
    }

    /**
     * Ensures the dialog is visible to the user.
     *
     * <p>If no dialog currently exists with the same key, a new one will be created and displayed
     * to the user. If a dialog existed prior to this method being invoked, it will request focus
     * to bring it to the user's attention.
     *
     * <p>This method <em>must</em> be called on the Event Despatch Thread.
     *
     * @param key The key associated with the dialog to display.
     */
    public void displayOrRequestFocus(final K key) {
        checkOnEDT();
        get(key).displayOrRequestFocus();
    }

    /**
     * Returns a new instance of the dialog with the specified key.
     *
     * @param key The key to create a new dialog for.
     * @return A new instance of this provider's dialog.
     */
    protected abstract T getInstance(final K key);

    /**
     * Listens to window closing events to remove the cached instance of the dialog.
     */
    private class Listener extends WindowAdapter {

        private final K key;

        public Listener(final K key) {
            this.key = key;
        }

        @Override
        public void windowClosing(final WindowEvent e) {
            super.windowClosing(e);
            instances.remove(key);
        }

    }

}

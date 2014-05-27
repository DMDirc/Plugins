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

package com.dmdirc.addons.ui_swing.dialogs.newaliases;

import com.dmdirc.commandparser.aliases.Alias;
import com.dmdirc.interfaces.ui.AliasDialogModel;

import com.google.common.base.Optional;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;
import java.util.Collection;

/**
 * Alias manager dialog model stores state of the UI, mostly proxying to the core model.
 */
public class AliasManagerModel {

    /** Profile change support. */
    private final PropertyChangeSupport pcs;
    /** Core alias dialog mode. */
    private final AliasDialogModel model;

    public AliasManagerModel(final AliasDialogModel model) {
        this.model = model;
        pcs = new PropertyChangeSupport(this);
    }

    public void load() {
        final PropertyChangeListener[] listeners = pcs.getPropertyChangeListeners();
        for (PropertyChangeListener listener : listeners) {
            if (listener instanceof PropertyChangeListenerProxy) {
                final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
                pcs.firePropertyChange(proxy.getPropertyName(), null, null);
            }
        }
    }

    public Collection<Alias> getAliases() {
        return model.getAliases();
    }

    public Optional<Alias> getAlias(final String name) {
        return model.getAlias(name);
    }

    public void addAlias(final String name, final int minArguments, final String substitution) {
        model.addAlias(name, minArguments, substitution);
        pcs.firePropertyChange("addAlias", null, model.getAlias(name).get());
    }

    public void editAlias(final String name, final int minArguments, final String substitution) {
        final Alias oldAlias = model.getAlias(name).get();
        model.editAlias(name, minArguments, substitution);
        pcs.firePropertyChange("editAlias", oldAlias, model.getAlias(name).get());
    }

    public void renameAlias(final String oldName, final String newName) {
        final Alias oldAlias = model.getAlias(oldName).get();
        model.renameAlias(oldName, newName);
        model.setSelectedAlias(model.getAlias(newName));
        pcs.firePropertyChange("renameAlias", oldAlias, model.getAlias(newName).get());
    }

    public void removeAlias(final String name) {
        final Alias oldAlias = model.getAlias(name).get();
        model.removeAlias(name);
        pcs.firePropertyChange("deleteAlias", oldAlias, null);
    }

    public void setSelectedAlias(final Optional<Alias> alias) {
        final Optional<Alias> oldAlias = model.getSelectedAlias();
        model.setSelectedAlias(alias);
        pcs.firePropertyChange("selectedAlias", oldAlias, alias);
    }

    public Optional<Alias> getSelectedAlias() {
        return model.getSelectedAlias();
    }

    public void save() {
        model.save();
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(final String name, final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(name, listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

}

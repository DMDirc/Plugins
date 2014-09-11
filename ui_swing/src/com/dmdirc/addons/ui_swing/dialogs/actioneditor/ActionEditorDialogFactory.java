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

package com.dmdirc.addons.ui_swing.dialogs.actioneditor;

import com.dmdirc.ClientModule;
import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionFactory;
import com.dmdirc.actions.ActionSubstitutorFactory;
import com.dmdirc.addons.ui_swing.dialogs.actionsmanager.ActionsManagerDialog;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.IconManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link ActionEditorDialog}s.
 */
@Singleton
public class ActionEditorDialogFactory {

    private final IconManager iconManager;
    private final AggregateConfigProvider config;
    private final ActionSubstitutorFactory subsFactory;
    private final ActionFactory actionFactory;

    @Inject
    public ActionEditorDialogFactory(
            @ClientModule.GlobalConfig final IconManager iconManager,
            @ClientModule.GlobalConfig final AggregateConfigProvider config,
            final ActionSubstitutorFactory subsFactory,
            final ActionFactory actionFactory) {
        this.iconManager = iconManager;
        this.config = config;
        this.subsFactory = subsFactory;
        this.actionFactory = actionFactory;
    }

    public ActionEditorDialog getActionEditorDialog(
            final ActionsManagerDialog parentWindow,
            final String group) {
        return new ActionEditorDialog(iconManager, config, subsFactory, actionFactory,
                parentWindow, group);
    }

    public ActionEditorDialog getActionEditorDialog(
            final ActionsManagerDialog parentWindow,
            final Action action) {
        return new ActionEditorDialog(iconManager, config, subsFactory, actionFactory,
                parentWindow, action);
    }

}

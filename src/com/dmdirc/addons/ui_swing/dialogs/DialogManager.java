/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.dialogs;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ui.StatusBar;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.core.util.URLHandler;
import com.dmdirc.util.SimpleInjector;

import java.awt.Window;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the DMDirc dialogs, creates and disposes as required to ensure only
 * the required number exist at any one time.
 */
public class DialogManager {

    private SwingController controller;
    private Map<Class<? extends StandardDialog>, StandardDialog> dialogs;

    public DialogManager(final SwingController controller) {
        this.controller = controller;
        dialogs = Collections.synchronizedMap(
                new HashMap<Class<? extends StandardDialog>, StandardDialog>());
    }

    public <T extends StandardDialog> T showDialog(final Class<T> klass,
            final Object... params) {
        final T dialog = getDialog(klass, params);
        dialog.display();
        return dialog;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends StandardDialog> T getDialog(final Class<T> klass, final Object... params) {
        final T instance;
        if (dialogs.containsKey(klass)) {
            instance = (T) dialogs.get(klass);
        } else {
            final SimpleInjector injector = getInjector(params);
            instance = injector.createInstance(klass);

            dialogs.put(instance.getClass(), instance);
        }
        return instance;
    }

    private SimpleInjector getInjector(final Object... params) {
        final SimpleInjector injector = new SimpleInjector();

        for(Object param : params) {
            injector.addParameter(param);
        }

        injector.addParameter(SwingController.class, controller);
        injector.addParameter(IconManager.class, controller.getIconManager());
        injector.addParameter(IdentityManager.class, controller.getIdentityManager());
        injector.addParameter(ConfigManager.class, controller.getGlobalConfig());
        injector.addParameter(MainFrame.class, controller.getMainFrame());
        injector.addParameter(Window.class, controller.getMainFrame());
        injector.addParameter(SwingStatusBar.class, controller.getSwingStatusBar());
        injector.addParameter(StatusBar.class, controller.getSwingStatusBar());
        injector.addParameter(SwingWindowFactory.class, controller.getWindowFactory());
        injector.addParameter(URLHandler.class, controller.getURLHandler());
        injector.addParameter(DialogManager.class, this);
        
        return injector;
    }
    
    public void close(final Class<? extends StandardDialog> klass) {
        final StandardDialog dialog = dispose(klass);
        if (dialog != null) {
            dialog.dispose();
        }
    }
    
    public StandardDialog dispose(final Class<? extends StandardDialog> klass) {
        if (dialogs.containsKey(klass)) {
            return dialogs.remove(dialogs.get(klass));
        }
        return null;
    }

    public StandardDialog dispose(final StandardDialog dialog) {
        if (dialogs.containsKey(dialog.getClass())) {
            return dispose(dialog.getClass());
        }
        return null;
    }
}

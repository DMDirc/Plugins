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

package com.dmdirc.addons.ui_swing.dialogs.url;

import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.ui.core.util.URLHandler;

import java.awt.Window;
import java.net.URI;

import javax.inject.Inject;

import static com.dmdirc.ClientModule.GlobalConfig;
import static com.dmdirc.ClientModule.UserConfig;

/**
 * Factory for {@link URLDialogFactory}s
 */
public class URLDialogFactory {

    private final AggregateConfigProvider global;
    private final ConfigProvider config;
    private final Window parentWindow;
    private final URLHandler urlHandler;

    @Inject
    public URLDialogFactory(
            @GlobalConfig final AggregateConfigProvider global,
            @UserConfig final ConfigProvider config,
            @MainWindow final Window parentWindow,
            final URLHandler urlHandler) {
        this.global = global;
        this.config = config;
        this.parentWindow = parentWindow;
        this.urlHandler = urlHandler;
    }

    public URLDialog getURLDialog(final URI uri) {
        return new URLDialog(uri, global, config, parentWindow, urlHandler);
    }
}

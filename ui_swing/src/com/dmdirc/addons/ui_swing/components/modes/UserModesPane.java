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

package com.dmdirc.addons.ui_swing.components.modes;

import com.dmdirc.interfaces.Connection;

/** User mode panel. */
public final class UserModesPane extends ModesPane {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Parent server. */
    private final Connection connection;

    /**
     * Creates a new instance of UserModesPane.
     *
     * @param connection Parent server
     */
    public UserModesPane(final Connection connection) {
        super(connection.getWindowModel().getConfigManager(),
                connection.getWindowModel().getIconManager());

        this.connection = connection;
        initModesPanel();
    }

    @Override
    public boolean hasModeValue(final String mode) {
        return connection.getWindowModel().getConfigManager().hasOptionString("server",
                "umode" + mode);
    }

    @Override
    public String getModeValue(final String mode) {
        return connection.getWindowModel().getConfigManager().getOption("server", "umode" + mode);
    }

    @Override
    public boolean isModeEnabled(final String mode) {
        return true;
    }

    @Override
    public boolean isModeSettable(final String mode) {
        return true;
    }

    @Override
    public String getAvailableBooleanModes() {
        return connection.getParser().get().getUserModes();
    }

    @Override
    public String getOurBooleanModes() {
        return connection.getParser().get().getLocalClient().getModes();
    }

    @Override
    public String getAllParamModes() {
        return "";
    }

    @Override
    public String getParamModeValue(final String mode) {
        return "";
    }

    @Override
    public void alterMode(final boolean add, final String mode,
            final String parameter) {
        connection.getParser().get().getLocalClient().alterMode(add, mode.toCharArray()[0]);
    }

    @Override
    public void flushModes() {
        connection.getParser().get().getLocalClient().flushModes();
    }

}

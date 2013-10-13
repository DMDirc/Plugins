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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.scriptplugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Used to allow the rhino javascript to do stuff that it otherwise can't, such
 * as setting global variables, string triming and getting a char.
 */
public class JavaScriptHelper {
    /** Used to identify the JavaScriptHelper. */
    private static final String ID = "DMDIRC-JSH";

    /**
     * Used to allow scripts to know if a specific function they need is
     * available.
     */
    private static final int VERSION = 2;

    /** Hashtable for storing stuff. */
    private static final Map<String, Object> SETTINGS = new HashMap<>();

    /**
     * Method to set Stuff.
     *
     * @param setting Name of setting
     * @param value Value of setting
     */
    public void setGlobal(final String setting, final Object value) {
        if (setting.isEmpty()) {
            return;
        }
        final String key = setting.toLowerCase(Locale.getDefault());
        if (SETTINGS.containsKey(key)) {
            SETTINGS.remove(key);
        }
        if (value != null) {
            SETTINGS.put(key, value);
        }
    }

    /**
     * Method to get Stuff.
     *
     * @param setting Name of setting
     *
     * @return Value of setting
     */
    public Object getGlobal(final String setting) {
        if (setting.isEmpty()) {
            return "";
        }
        return SETTINGS.get(setting.toLowerCase(Locale.getDefault()));
    }

    /**
     * Method to trim spaces from strings.
     *
     * @param str String to trim
     *
     * @return String without spaces on the ends
     */
    public String trim(final String str) {
        return str.trim();
    }

    /**
     * Method to get a java Character object.
     *
     * @param str String to make object from
     *
     * @return Character represending first char in the string
     */
    public Character toChar(final String str) {
        return str.charAt(0);
    }

    /**
     * Get the version of JavaScriptHelper in use.
     *
     * @return JavaScriptHelper version
     */
    public int getVersion() {
        return VERSION;
    }

    /**
     * Get the ID of this JavaScriptHelper.
     * If you extend or modify this, you should change the myIDString variable.
     *
     * @return JavaScriptHelper ID
     */
    public String getID() {
        return ID;
    }
}

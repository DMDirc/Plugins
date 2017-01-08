/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.tabcompletion_mirc;

import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.input.tabstyles.TabCompletionResult;
import com.dmdirc.ui.input.tabstyles.TabCompletionStyle;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

public class MircStyle implements TabCompletionStyle {

    /** The last word that was tab completed. */
    private String lastWord;
    /** The last string we tried to tab complete. */
    private String tabString;
    /** The tab completer that we use. */
    protected final TabCompleter tabCompleter;
    /** The input window that we use. */
    protected final WindowModel window;

    /**
     * Creates a new mIRC-style tab completer.
     *
     * @param completer The tab completer this style is for
     * @param window    The window this tab style is for
     */
    public MircStyle(final TabCompleter completer, final WindowModel window) {
        this.tabCompleter = completer;
        this.window = window;
    }

    @Override
    public TabCompletionResult getResult(final String original, final int start,
            final int end, final boolean shiftPressed,
            final AdditionalTabTargets additional) {

        final String word = original.substring(start, end);
        final String target;
        if (word.equals(lastWord)) {
            // We're continuing to tab through
            final List<String> results = new ArrayList<>(tabCompleter.complete(tabString, additional));
            results.sort(String.CASE_INSENSITIVE_ORDER);
            target = results.get((results.indexOf(lastWord) + (shiftPressed ? -1: 1) + results.size()) % results.size());
        } else {
            // New tab target
            final List<String> results = new ArrayList<>(tabCompleter.complete(word, additional));

            if (results.isEmpty()) {
                Toolkit.getDefaultToolkit().beep();
                return null;
            } else {
                results.sort(String.CASE_INSENSITIVE_ORDER);

                if (!word.isEmpty()
                        && window instanceof GroupChat
                        && window.getName().startsWith(word)) {
                    target = window.getName();
                } else {
                    target = results.get(0);
                }
                tabString = word;
            }
        }

        lastWord = target;

        return new TabCompletionResult(original.substring(0, start) + target
                + original.substring(end), start + target.length());
    }

}

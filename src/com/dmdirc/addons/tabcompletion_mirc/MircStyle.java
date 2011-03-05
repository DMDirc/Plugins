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

package com.dmdirc.addons.tabcompletion_mirc;

import com.dmdirc.Channel;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.input.TabCompleterResult;
import com.dmdirc.ui.input.tabstyles.TabCompletionResult;
import com.dmdirc.ui.input.tabstyles.TabCompletionStyle;

import java.awt.Toolkit;
import java.util.Collections;

public class MircStyle implements TabCompletionStyle {

    /** The last word that was tab completed. */
    private String lastWord;

    /** The last string we tried to tab complete. */
    private String tabString;

    /** The tab completer that we use. */
    protected final TabCompleter tabCompleter;

    /** The input window that we use. */
    protected final WritableFrameContainer window;

    /**
     * Creates a new mIRC-style tab completer.
     *
     * @param completer The tab completer this style is for
     * @param window The window this tab style is for
     */
    public MircStyle(final TabCompleter completer, final WritableFrameContainer window) {
        this.tabCompleter = completer;
        this.window = window;
    }

    /** {@inheritDoc} */
    @Override
    public TabCompletionResult getResult(final String original, final int start,
            final int end, final boolean shiftPressed,
            final AdditionalTabTargets additional) {

        final String word = original.substring(start, end);
        String target = "";
        if (word.equals(lastWord)) {
            final TabCompleterResult res = tabCompleter.complete(tabString, additional);
            Collections.sort(res.getResults(), String.CASE_INSENSITIVE_ORDER);
            // We're continuing to tab through
            target = res.getResults().get((res.getResults().indexOf(lastWord) +
                    (shiftPressed ? -1 : 1) + res.getResults().size()) % res
                    .getResults().size());
        } else {
            // New tab target
            final TabCompleterResult res = tabCompleter.complete(word, additional);

            if (res.getResultCount() == 0) {
                Toolkit.getDefaultToolkit().beep();
                return null;
            } else {
                Collections.sort(res.getResults(), String.CASE_INSENSITIVE_ORDER);

                if (word.length() > 0 && window instanceof Channel
                        && ((Channel) window)
                        .getChannelInfo().getName().startsWith(word)) {
                    target = ((Channel) window).getChannelInfo().getName();
                } else {
                    target = res.getResults().get(0);
                }
                tabString = word;
            }
        }

        lastWord = target;

        return new TabCompletionResult(original.substring(0, start) + target
                + original.substring(end), start + target.length());
    }

}
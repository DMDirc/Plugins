/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.textpane;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Break iterator implementation that splits a given text against it's
 * whitespace indexes.
 */
class WhitespaceBreakIterator extends BreakIterator {

    /** Text being iterated. */
    private CharacterIterator text;
    /** Current break number. */
    private int currentBreak;
    /** Current break index. */
    private int currentIndex;
    /** Pre-compiled whitespace regex. */
    private final Pattern whitespace = Pattern.compile("\\s");
    /** Regex matches. */
    private Matcher matcher;
    /** Map of whitespace breaks to their offsets. */
    private Map<Integer, Integer> breaksToOffsets;
    /** Map of whitespace offsets to their break number. */
    private Map<Integer, Integer> offsetsToBreaks;

    /** {@inheritDoc} */
    @Override
    public int first() {
        currentBreak = text.getBeginIndex();
        currentIndex = breaksToOffsets.get(currentBreak);
        return currentIndex;
    }

    /** {@inheritDoc} */
    @Override
    public int last() {
        currentBreak = breaksToOffsets.size() - 1;
        currentIndex = breaksToOffsets.get(currentBreak);
        return currentIndex;
    }

    /** {@inheritDoc} */
    @Override
    public int next(final int n) {
        currentBreak += n;
        if (currentBreak >= breaksToOffsets.size()) {
            last();
            return BreakIterator.DONE;
        }
        if (currentBreak < 0) {
            first();
            return BreakIterator.DONE;
        }
        currentIndex = breaksToOffsets.get(currentBreak);

        return currentIndex;
    }

    /** {@inheritDoc} */
    @Override
    public int next() {
        return next(1);
    }

    /** {@inheritDoc} */
    @Override
    public int previous() {
        return next(-1);
    }

    /** {@inheritDoc} */
    @Override
    public int following(final int offset) {
        if (matcher.find(offset)) {
            if (matcher.end() == text.getEndIndex() - 1) {
                return BreakIterator.DONE;
            } else {
                currentBreak = offsetsToBreaks.get(matcher.end());
                currentIndex = matcher.end();
                return currentIndex;
            }
        } else {
            return BreakIterator.DONE;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int preceding(final int offset) {
        if (matcher.find(offset)) {
            if (matcher.end() == text.getBeginIndex()) {
                return BreakIterator.DONE;
            } else {
                currentBreak = offsetsToBreaks.get(matcher.end()) - 1;
                currentIndex = breaksToOffsets.get(currentBreak);
                return currentIndex;
            }
        } else {
            return BreakIterator.DONE;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int current() {
        return currentIndex;
    }

    /** {@inheritDoc} */
    @Override
    public CharacterIterator getText() {
        return text;
    }

    /** {@inheritDoc} */
    @Override
    public void setText(final CharacterIterator newText) {
        text = newText;
        currentBreak = -1;
        currentIndex = -1;
        breaksToOffsets = new HashMap<Integer, Integer>();
        offsetsToBreaks = new HashMap<Integer, Integer>();
        matcher = whitespace.matcher(Matcher.quoteReplacement(getString()));

        for (int i = 0; matcher.find(); i++) {
            breaksToOffsets.put(i, matcher.end());
            offsetsToBreaks.put(matcher.end(), i);
        }
    }

    /**
     * Returns the character iterator as a string.
     *
     * @return String of character iterator
     */
    private String getString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(text.current());
        while (text.getIndex() < text.getEndIndex() - 1) {
            builder.append(text.next());
        }
        return builder.toString();
    }
}

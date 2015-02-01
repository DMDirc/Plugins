/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.components;

import java.util.List;
import java.util.function.Consumer;

/**
 * {@link LoggingSwingWorker} that runs a {@link Runnable}.
 */
public class RunnableLoggingSwingWorker<T, V> extends SupplierLoggingSwingWorker<T, V> {

    /**
     * Creates a new logging swing worker.
     *
     * @param backgroundRunnable The runnable to call as the background task, off the EDT.
     */
    public RunnableLoggingSwingWorker(final Runnable backgroundRunnable) {
        this(backgroundRunnable, result -> {});
    }

    /**
     * Creates a new logging swing worker.
     *
     * @param backgroundRunnable The runnable to call as the background task, off the EDT.
     * @param doneConsumer       The consumer called when the background task is complete
     */
    public RunnableLoggingSwingWorker(final Runnable backgroundRunnable,
            final Consumer<T> doneConsumer) {
        this(backgroundRunnable, doneConsumer, chunks -> {});
    }

    /**
     * Creates a new logging swing worker.
     *
     * @param backgroundRunnable The runnable to call as the background task, off the EDT.
     * @param doneConsumer       The consumer called when the background task is complete
     * @param processConsumer    The consumer called to process results of the background task
     *                           as it progresses
     */
    public RunnableLoggingSwingWorker(final Runnable backgroundRunnable,
            final Consumer<T> doneConsumer, final Consumer<List<V>> processConsumer) {
        super(() -> { backgroundRunnable.run(); return null; } , doneConsumer, processConsumer);
    }
}

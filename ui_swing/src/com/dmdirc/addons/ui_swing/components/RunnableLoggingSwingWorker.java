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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * {@link LoggingSwingWorker} that runs a {@link Runnable}.
 */
public class RunnableLoggingSwingWorker<T, V> extends LoggingSwingWorker<T, V> {

    private final DMDircMBassador eventBus;
    private final Consumer<T> doneConsumer;
    private final Consumer<List<V>> processConsumer;
    private Supplier<T> backgroundSupplier;
    private Runnable backgroundRunnable;

    /**
     * Creates a new logging swing worker.
     *
     * @param eventBus           Event bus to post errors to.
     * @param backgroundSupplier The supplier to call as the background task, off the EDT.
     */
    public RunnableLoggingSwingWorker(final DMDircMBassador eventBus,
            final Supplier<T> backgroundSupplier) {
        this(eventBus, backgroundSupplier, result -> {});
    }

    /**
     * Creates a new logging swing worker.
     *
     * @param eventBus           Event bus to post errors to.
     * @param backgroundRunnable The runnable to call as the background task, off the EDT.
     */
    public RunnableLoggingSwingWorker(final DMDircMBassador eventBus,
            final Runnable backgroundRunnable) {
        this(eventBus, backgroundRunnable, result -> {});
    }

    /**
     * Creates a new logging swing worker.
     *
     * @param eventBus           Event bus to post errors to.
     * @param backgroundSupplier The supplier to call as the background task, off the EDT.
     * @param doneConsumer       The consumer called when the background task is complete
     */
    public RunnableLoggingSwingWorker(final DMDircMBassador eventBus,
            final Supplier<T> backgroundSupplier,
            final Consumer<T> doneConsumer) {
        this(eventBus, backgroundSupplier, doneConsumer, chunks -> {});
    }

    /**
     * Creates a new logging swing worker.
     *
     * @param eventBus           Event bus to post errors to.
     * @param backgroundRunnable The runnable to call as the background task, off the EDT.
     * @param doneConsumer       The consumer called when the background task is complete
     */
    public RunnableLoggingSwingWorker(final DMDircMBassador eventBus,
            final Runnable backgroundRunnable,
            final Consumer<T> doneConsumer) {
        this(eventBus, backgroundRunnable, doneConsumer, chunks -> {});
    }

    /**
     * Creates a new logging swing worker.
     *
     * @param eventBus           Event bus to post errors to.
     * @param backgroundSupplier The supplier to call as the background task, off the EDT.
     * @param doneConsumer       The consumer called when the background task is complete
     * @param processConsumer    The consumer called to process results of the background task
     *                           as it progresses
     */
    public RunnableLoggingSwingWorker(final DMDircMBassador eventBus,
            final Supplier<T> backgroundSupplier,
            final Consumer<T> doneConsumer,
            final Consumer<List<V>> processConsumer) {
        super(eventBus);
        this.eventBus = eventBus;
        this.backgroundSupplier = backgroundSupplier;
        this.doneConsumer = doneConsumer;
        this.processConsumer = processConsumer;
    }

    /**
     * Creates a new logging swing worker.
     *
     * @param eventBus           Event bus to post errors to.
     * @param backgroundRunnable The runnable to call as the background task, off the EDT.
     * @param doneConsumer       The consumer called when the background task is complete
     * @param processConsumer    The consumer called to process results of the background task
     *                           as it progresses
     */
    public RunnableLoggingSwingWorker(final DMDircMBassador eventBus,
            final Runnable backgroundRunnable,
            final Consumer<T> doneConsumer,
            final Consumer<List<V>> processConsumer) {
        super(eventBus);
        this.eventBus = eventBus;
        this.backgroundRunnable = backgroundRunnable;
        this.doneConsumer = doneConsumer;
        this.processConsumer = processConsumer;
    }

    @Override
    protected T doInBackground() throws Exception {
        if (backgroundSupplier == null) {
            backgroundRunnable.run();
            return null;
        } else {
            return backgroundSupplier.get();
        }
    }

    @Override
    protected void process(final List<V> chunks) {
        processConsumer.accept(chunks);
    }

    @Override
    protected void done() {
        try {
            handleDone(get());
        } catch (InterruptedException ex) {
            //Ignore
        } catch (ExecutionException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex, ex.getMessage(), ""));
        }
    }

    public void handleDone(final T value) {
        doneConsumer.accept(value);
    }
}

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

package com.dmdirc.addons.ui_swing;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executor for creating swing worker daemon threads. Workaround for bug
 * http://bugs.sun.com/view_bug.do?bug_id=6880336
 */
public final class SwingWorkerExecutor extends ThreadPoolExecutor implements
        ThreadFactory {

    /** Max number of swing worker threads. */
    private static final int MAX_WORKERS = 20;
    /** Static instance. */
    public static final SwingWorkerExecutor EXECUTOR =
            new SwingWorkerExecutor();

    /**
     * Creates a new swing worker executor.
     */
    private SwingWorkerExecutor() {
        super(1, MAX_WORKERS, 2L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>());
        setThreadFactory(this);
    }

    
    @Override
    public Thread newThread(final Runnable r) {
        final Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName("SwingWorker-" + thread.getName());
        thread.setDaemon(true);
        return thread;
    }

    /**
     * Queues the specified runnable on this executor.
     *
     * @param runnable Runnable to execute
     */
    public static void queue(final Runnable runnable) {
        EXECUTOR.execute(runnable);
    }

}

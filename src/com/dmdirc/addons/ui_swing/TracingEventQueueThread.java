/*
 * Copyright (c) 2007, Kirill Grouchnikov
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of the <ORGANIZATION> nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.dmdirc.addons.ui_swing;

import java.awt.AWTEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Event queue extention to monitor long running tasks on the EDT. Found at
 * http://today.java.net/lpt/a/433
 */
public class TracingEventQueueThread extends Thread {

    private final long thresholdDelay;
    private final Map<AWTEvent, Long> eventTimeMap;
    private ThreadMXBean threadBean;
    private boolean running = false;

    /**
     * Instantiates a new tracing thread.
     * 
     * @param thresholdDelay
     *            Length to consider a long running task
     */
    public TracingEventQueueThread(final long thresholdDelay) {
        this.thresholdDelay = thresholdDelay;
        eventTimeMap = new HashMap<AWTEvent, Long>();

        try {
            final MBeanServer mbeanServer = ManagementFactory
                    .getPlatformMBeanServer();
            final ObjectName objName = new ObjectName(
                    ManagementFactory.THREAD_MXBEAN_NAME);
            final Set<ObjectName> mbeans = mbeanServer
                    .queryNames(objName, null);
            for (final ObjectName name : mbeans) {
                threadBean = ManagementFactory.newPlatformMXBeanProxy(
                        mbeanServer, name.toString(), ThreadMXBean.class);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Marks the start time for the specified event.
     * 
     * @param event
     *            Event to monitor
     */
    public synchronized void eventDispatched(final AWTEvent event) {
        eventTimeMap.put(event, System.currentTimeMillis());
    }

    /**
     * Marks the end time for the specified event.
     * 
     * @param event
     *            Event to finish monitoring.
     */
    public synchronized void eventProcessed(final AWTEvent event) {
        checkEventTime(event, System.currentTimeMillis(), eventTimeMap
                .get(event));
        eventTimeMap.put(event, null);
    }

    private void checkEventTime(final AWTEvent event, final long currTime,
            final long startTime) {
        final long currProcessingTime = currTime - startTime;
        if (currProcessingTime >= thresholdDelay) {
            System.out.println("Event [" + event.hashCode() + "] "
                    + event.getClass().getName()
                    + " is taking too much time on EDT (" + currProcessingTime
                    + ")");

            if (threadBean != null) {
                final long[] threadIds = threadBean.getAllThreadIds();
                for (final long threadId : threadIds) {
                    final ThreadInfo threadInfo = threadBean.getThreadInfo(
                            threadId, Integer.MAX_VALUE);
                    if (threadInfo != null
                            && threadInfo.getThreadName().startsWith(
                                    "AWT-EventQueue")) {
                        System.out.println(threadInfo.getThreadName() + " / "
                                + threadInfo.getThreadState());
                        final StackTraceElement[] stack = threadInfo
                                .getStackTrace();
                        for (final StackTraceElement stackEntry : stack) {
                            System.out.println("\t" + stackEntry.getClassName()
                                    + "." + stackEntry.getMethodName() + " ["
                                    + stackEntry.getLineNumber() + "]");
                        }
                    }
                }

                final long[] deadlockedThreads = threadBean
                        .findDeadlockedThreads();
                if (deadlockedThreads != null && deadlockedThreads.length > 0) {
                    System.out.println("Deadlocked threads:");
                    for (final long threadId : deadlockedThreads) {
                        final ThreadInfo threadInfo = threadBean.getThreadInfo(
                                threadId, Integer.MAX_VALUE);
                        System.out.println(threadInfo.getThreadName() + " / "
                                + threadInfo.getThreadState());
                        final StackTraceElement[] stack = threadInfo
                                .getStackTrace();
                        for (final StackTraceElement stackEntry : stack) {
                            System.out.println("\t" + stackEntry.getClassName()
                                    + "." + stackEntry.getMethodName() + " ["
                                    + stackEntry.getLineNumber() + "]");
                        }
                    }
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        running = true;
        while (running) {
            final long currTime = System.currentTimeMillis();
            synchronized (this) {
                for (final Map.Entry<AWTEvent, Long> entry : eventTimeMap
                        .entrySet()) {
                    final AWTEvent event = entry.getKey();
                    if (entry.getValue() == null) {
                        continue;
                    }
                    final long startTime = entry.getValue();
                    checkEventTime(event, currTime, startTime);
                }
            }
            try {
                Thread.sleep(100);
            } catch (final InterruptedException ie) {
                // Ignore
            }
        }
    }

    /**
     * Cancels this thread.
     */
    public void cancel() {
        running = false;
    }
}
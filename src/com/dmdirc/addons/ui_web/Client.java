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

package com.dmdirc.addons.ui_web;

import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.addons.ui_web.uicomponents.WebWindow;
import java.util.LinkedList;
import java.util.List;
import org.mortbay.util.ajax.Continuation;

/**
 *
 * @author chris
 */
public class Client {
    
    private long lastSeenTime = System.currentTimeMillis();

    private Continuation continuation;
    
    private final String ip;
    
    private final List<Event> events = new LinkedList<Event>();

    public Client(final String ip) {
        events.add(new Event("statusbar", "Welcome to the DMDirc web interface"));
        
        this.ip = ip;
        
        final List<Window> added = new LinkedList<Window>();
        final List<Window> queued = new LinkedList<Window>(WebWindow.getWindows());

        while (!queued.isEmpty()) {
            final Window window = queued.remove(0);
            final Window parent = WindowManager.getParent(window.getContainer()).getFrame();
            
            if (parent == null) {
                events.add(new Event("newwindow", window));
                added.add(window);
            } else if (added.contains(parent)) {
                events.add(new Event("newchildwindow", new Object[]{parent, window}));
                added.add(window);
            } else {
                queued.add(window);
            }
        }
    }
    
    public String getIp() {
        return ip;
    }
    
    public long getTime() {
        return System.currentTimeMillis() - lastSeenTime;
    }

    public Object getMutex() {
        return events;
    }

    public void setContinuation(Continuation continuation) {
        this.continuation = continuation;
    }
    
    public void touch() {
        lastSeenTime = System.currentTimeMillis();
    }
    
    public void addEvent(final Event event) {
        synchronized (events) {
            events.add(event);

            if (continuation != null) {
                continuation.resume();
            }
        }
    }
    
    public List<Event> retrieveEvents() {
        synchronized (events) {
            final List<Event> res = new LinkedList<Event>(events);
            events.clear();
            return res;
        }
    }
    
    public int getEventCount() {
        return events.size();
    }

}

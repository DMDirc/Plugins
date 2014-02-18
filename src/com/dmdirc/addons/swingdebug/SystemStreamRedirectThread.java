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

package com.dmdirc.addons.swingdebug;

import com.dmdirc.ui.messages.IRCDocument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * Simple utility class to redirect System streams to the specified Document.
 */
public class SystemStreamRedirectThread implements Runnable {

    /** Is this thread running? */
    private boolean running = false;
    /** Reader to for the system stream. */
    private final BufferedReader reader;
    /** Stream identifier. */
    private final SystemStreamType stream;
    /** Document to output stream into. */
    private final IRCDocument document;
    /** Original System stream. */
    private PrintStream originalStream;

    /**
     * Constructs a new redirection thread.
     *
     * @param stream   System stream to redirect
     * @param document Document to redirect stream into
     *
     * @throws IOException On error redirecting stream
     */
    public SystemStreamRedirectThread(final SystemStreamType stream,
            final IRCDocument document) throws IOException {
        super();
        this.stream = stream;
        this.document = document;

        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        reader = new BufferedReader(new InputStreamReader(in));
        switch (stream) {
            case Out:
                originalStream = System.out;
                System.setOut(new PrintStream(out));
                break;
            case Error:
                originalStream = System.err;
                System.setErr(new PrintStream(out));
                break;
            default:
                throw new IllegalArgumentException("Unknown stream type: "
                        + stream);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                if (reader.ready()) {
                    document.addText(new String[]{reader.readLine(),});
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        //Ignore
                    }
                }
                Thread.yield();
            } catch (IOException ex) {
                running = false;
            }
        }
    }

    /**
     * Starts the thread adding text to the document.
     */
    public void start() {
        final Thread thread = new Thread(this,
                "System stream redirector (" + stream + ")");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Cancels the thread adding text to the document.
     */
    public void cancel() {
        running = false;
        switch (stream) {
            case Out:
                System.setOut(originalStream);
                break;
            case Error:
                System.setErr(originalStream);
                break;
            default:
                throw new IllegalArgumentException("Unknown stream type: "
                        + stream);
        }
    }

    /**
     * Is this thread running?
     *
     * @return true iif running
     */
    public boolean isRunning() {
        return running;
    }

}

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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.dialogs.FeedbackDialog;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.util.io.Downloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends feedback worker thread.
 */
public class SendWorker extends LoggingSwingWorker {

    /** Parent feedback dialog. */
    private FeedbackDialog dialog;
    /** Name. */
    private String name;
    /** Email. */
    private String email;
    /** Feedback. */
    private String feedback;
    /** Server name. */
    private String serverInfo;
    /** DMDirc Info. */
    private String dmdircInfo;
    /** Error/Success message. */
    private StringBuilder error;

    /**
     * Creates a new send worker to send feedback.
     *
     * @param dialog Parent feedback dialog
     * @param name Name
     * @param email Email
     * @param feedback Feedback
     */
    public SendWorker(final FeedbackDialog dialog, final String name,
            final String email, final String feedback) {
        this(dialog, name, email, feedback, "", "");
    }

    /**
     * Creates a new send worker to send feedback.
     *
     * @param dialog Parent feedback dialog
     * @param name Name
     * @param email Email
     * @param feedback Feedback
     * @param serverInfo serverInfo
     * @param dmdircInfo DMDirc info
     */
    public SendWorker(final FeedbackDialog dialog, final String name,
            final String email, final String feedback,
            final String serverInfo, final String dmdircInfo) {
        super();

        this.dialog = dialog;
        this.name = name;
        this.email = email;
        this.feedback = feedback;
        this.serverInfo = serverInfo;
        this.dmdircInfo = dmdircInfo;

        error = new StringBuilder();
    }

    /** {@inheritDoc} */
    @Override
    protected Object doInBackground() {
        final Map<String, String> postData =
                new HashMap<String, String>();

        if (!name.isEmpty()) {
            postData.put("name", name);
        }
        if (!email.isEmpty()) {
            postData.put("email", email);
        }
        if (!feedback.isEmpty()) {
            postData.put("feedback", feedback);
        }
        postData.put("version", IdentityManager.getGlobalConfig().getOption(
                "version", "version"));
        if (!serverInfo.isEmpty()) {
            postData.put("serverInfo", serverInfo);
        }
        if (!dmdircInfo.isEmpty()) {
            postData.put("dmdircInfo", dmdircInfo);
        }

        sendData(postData);

        return error;
    }

    /**
     * Sends the error data to the server appending returned information to the
     * global error variable.
     *
     * @param postData Feedback data to send
     */
    private void sendData(final Map<String, String> postData) {
        try {
            final List<String> response =
                    Downloader.getPage("http://www.dmdirc.com/feedback.php",
                    postData);
            if (response.size() >= 1) {
                for (String responseLine : response) {
                    error.append(responseLine).append("\n");
                }
            } else {
                error.append("Failure: Unknown response from the server.");
            }
        } catch (MalformedURLException ex) {
            error.append("Malformed feedback URL.");
        } catch (IOException ex) {
            error.append("Failure: ").append(ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void done() {
        super.done();
        dialog.layoutComponents2(error);
    }
}


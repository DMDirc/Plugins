/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.nma;

import com.dmdirc.util.io.Downloader;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple client for sending events to NotifyMyAndroid.
 */
@Slf4j
@RequiredArgsConstructor
public class NotifyMyAndroidClient {

    /** The base URL for the NMA API. */
    private static final String BASE_URL = "https://www.notifymyandroid.com";
    /** The method to call to send a notification. */
    private static final String NOTIFY_PATH = "/publicapi/notify";

    /** The API keys to deliver to. */
    private final Collection<String> apiKeys;

    /** The application to report ourselves as. */
    private final String application;

    /**
     * Creates a new instance of {@link NotifyMyAndroidClient} with a single
     * API key.
     *
     * @param apiKey The API key to use when connecting to NMA
     * @param application The application string to report to NMA.
     */
    public NotifyMyAndroidClient(final String apiKey, final String application) {
        this(Arrays.asList(new String[] { apiKey }), application);
    }

    /**
     * Sends a notification to NotifyMyAndroid. At present return status and
     * text is ignored.
     *
     * @param event The name of the event to send (max 1,000 chars).
     * @param description The description of the event (max 10,000 chars).
     * @throws IOException If the NMA service couldn't be reached
     */
    public void notify(final String event, final String description) throws IOException {
        final Map<String, String> arguments = new HashMap<>();
        arguments.put("apikey", getApiKeys());
        arguments.put("application", application);
        arguments.put("event", event);
        arguments.put("description", description);

        log.info("Sending notification to NMA for event '{}'", event);
        log.debug("Arguments: {}", arguments);

        final List<String> response = Downloader.getPage(BASE_URL + NOTIFY_PATH, arguments);
        log.debug("Response: {}", response);
    }

    /**
     * Returns a comma-separated list of API keys.
     *
     * @return A string representation of API keys.
     */
    private String getApiKeys() {
        final StringBuilder builder = new StringBuilder();

        for (String apiKey : apiKeys) {
            if (builder.length() > 0) {
                builder.append(',');
            }

            builder.append(apiKey);
        }

        return builder.toString();
    }

}

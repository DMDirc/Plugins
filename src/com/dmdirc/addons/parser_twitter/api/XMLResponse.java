/*
 *  Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.dmdirc.addons.parser_twitter.api;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Describes an XML formatted response from a URL connection.
 *
 * @author shane
 */
public class XMLResponse {

    /** The HttpURLConnection that was used. */
    private final HttpURLConnection request;

    /** The Document that was returned (if any). */
    private final Document document;

    /**
     * Create a new XMLResponse
     *
     * @param request Request of response
     * @param doc Document for response.
     */
    public XMLResponse(final HttpURLConnection request, final Document doc) {
        this.request = request;
        this.document = doc;
    }

    /**
     * Get the Document for this response.
     *
     * @return the Document for this response.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Get the HttpURLConnection for this response.
     *
     * @return the HttpURLConnection for this response.
     */
    public HttpURLConnection getRequest() {
        return request;
    }

    /**
     * Was this an error?
     *
     * @return True if an error element exists in the document, there is no
     *         document, or a non-200 status code was returned.
     */
    public boolean isError() {
        return !(getError().isEmpty());
    }

    /**
     * Get the contents of this error.
     * If an error element is found then the contents will be returned, else
     * a description of the status code if non-200, or "" if no error.
     *
     * @return The contents of this error.
     */
    public String getError() {
        if (document == null || request == null) {
            return "No document or request found.";
        } else {
            final String error = TwitterAPI.getElementContents(getDocumentElement(), "error", "");
            if (error.isEmpty()) {
                try {
                    if (request.getResponseCode() != 200) {
                        return "(" + request.getResponseCode() + ") " + request.getResponseMessage();
                    }
                } catch (IOException ex) {
                    return "Error obtaining response code: "+ex;
                }
            } else {
                return error;
            }
        }

        return "";
    }

    /**
     * Get the Document Element from the Document or null on error.
     *
     * @return The Document Element from the Document or null on error.
     */
    public Element getDocumentElement() {
        return (document == null) ? null : document.getDocumentElement();
    }

    /**
     * Return the elements that match the given name, or null on error.
     *
     * @param name The element name to look for.
     * @return The elements that match the given name, or null on error.
     */
    public NodeList getElementsByTagName(final String name) {
        return (document == null) ? null : document.getElementsByTagName(name);
    }

    /**
     * Get the response code from the request.
     * 
     * @return The response code, or 0 on error.
     */
    public int getResponseCode() {
        if (request != null) {
            try {
                return request.getResponseCode();
            } catch (IOException ex) { }
        }

        return 0;
    }

    /**
     * Checks to see if the response code was 200, the document is not null
     * and no error element is in the document.
     * 
     * @return true if the response code was 200, the document is not null
     * and no error element is in the document.
     */
    public boolean isGood() {
        return getResponseCode() == 200 && getDocument() != null && !isError();
    }
}

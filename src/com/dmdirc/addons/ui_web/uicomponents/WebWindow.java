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

package com.dmdirc.addons.ui_web.uicomponents;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_web.DynamicRequestHandler;
import com.dmdirc.addons.ui_web.Event;
import com.dmdirc.addons.ui_web.Message;
import com.dmdirc.addons.ui_web.WebInterfaceUI;
import com.dmdirc.interfaces.FrameCloseListener;
import com.dmdirc.interfaces.FrameInfoListener;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.messages.IRCDocumentListener;
import com.dmdirc.ui.messages.IRCTextAttribute;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * A server-side representation of a "window" in the Web UI.
 */
public class WebWindow implements Window, IRCDocumentListener,
        FrameInfoListener, FrameCloseListener {

    protected static int counter = 0;

    protected static final Map<String, WebWindow> WINDOWS
            = new HashMap<String, WebWindow>();

    protected int myID = ++counter;

    private final FrameContainer parent;

    public WebWindow(final WebInterfaceUI controller,
            final FrameContainer parent) {
        super();

        this.parent = parent;

        WINDOWS.put(getId(), this);

        parent.getDocument().addIRCDocumentListener(this);
        parent.addFrameInfoListener(this);

        if (parent.getParent() == null) {
            DynamicRequestHandler.addEvent(new Event("newwindow", this));
        } else {
            DynamicRequestHandler.addEvent(new Event("newchildwindow",
                    new Object[]{controller.getWindowManager().getWindow(
                            parent.getParent()), this}));
        }
    }

    public static Collection<WebWindow> getWindows() {
        return WINDOWS.values();
    }

    public static WebWindow getWindow(final String id) {
        return WINDOWS.get(id);
    }

    public List<String> getMessages() {
        final List<String> messages = new ArrayList<String>(getContainer()
                .getDocument().getNumLines());

        for (int i = 0; i < getContainer().getDocument().getNumLines(); i++) {
            messages.add(style(getContainer().getDocument().getStyledLine(i)));
        }

        return messages;
    }

    /** {@inheritDoc} */
    @Override
    public FrameContainer getContainer() {
        return parent;
    }

    public String getId() {
        return String.valueOf(myID);
    }

    protected String style(final AttributedCharacterIterator aci) {
        final StringBuilder builder = new StringBuilder();

        Map<AttributedCharacterIterator.Attribute, Object> map = null;
        char chr = aci.current();

        while (aci.getIndex() < aci.getEndIndex()) {
            if (!aci.getAttributes().equals(map)) {
                style(aci.getAttributes(), builder);
                map = aci.getAttributes();
            }

            builder.append(StringEscapeUtils.escapeHtml(String.valueOf(chr)));
            chr = aci.next();
        }

        return builder.toString();
    }

    protected static void style(
            final Map<AttributedCharacterIterator.Attribute, Object> map,
            final StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append("</span>");
        }

        String link = null;

        builder.append("<span style=\"");

        for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry
                : map.entrySet()) {

            if (entry.getKey().equals(TextAttribute.FOREGROUND)) {
                builder.append("color: ");
                builder.append(toColour(entry.getValue()));
                builder.append("; ");
            } else if (entry.getKey().equals(TextAttribute.BACKGROUND)) {
                builder.append("background-color: ");
                builder.append(toColour(entry.getValue()));
                builder.append("; ");
            } else if (entry.getKey().equals(TextAttribute.WEIGHT)) {
                builder.append("font-weight: bold; ");
            } else if (entry.getKey().equals(TextAttribute.FAMILY)) {
                builder.append("font-family: monospace; ");
            } else if (entry.getKey().equals(TextAttribute.POSTURE)) {
                builder.append("font-style: italic; ");
            } else if (entry.getKey().equals(TextAttribute.UNDERLINE)) {
                builder.append("text-decoration: underline; ");
            } else if (entry.getKey().equals(IRCTextAttribute.HYPERLINK)) {
                builder.append("cursor: pointer; ");
                link = "link_hyperlink('"
                        + StringEscapeUtils.escapeHtml(StringEscapeUtils
                        .escapeJavaScript((String) entry.getValue()))
                        + "');";
            } else if (entry.getKey().equals(IRCTextAttribute.CHANNEL)) {
                builder.append("cursor: pointer; ");
                link = "link_channel('"
                        + StringEscapeUtils.escapeHtml(
                        StringEscapeUtils.escapeJavaScript(
                        (String) entry.getValue()))
                        + "');";
            } else if (entry.getKey().equals(IRCTextAttribute.NICKNAME)) {
                builder.append("cursor: pointer; ");
                link = "link_query('"
                        + StringEscapeUtils.escapeHtml(
                        StringEscapeUtils.escapeJavaScript(
                        (String) entry.getValue()))
                        + "');";
            }
        }

        builder.append('"');

        if (link != null) {
            builder.append(" onClick=\"");
            builder.append(link);
            builder.append('"');
        }

        builder.append('>');
    }

    protected static String toColour(final Object object) {
        final Color colour = (Color) object;

        return "rgb(" + colour.getRed() + ", " + colour.getGreen() + ", "
                + colour.getBlue() + ")";
    }

    /** {@inheritDoc} */
    @Override
    public UIController getController() {
        //TODO FIXME
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void linesAdded(final int line, final int length, final int size) {
        for (int i = 0; i < length; i++) {
            DynamicRequestHandler.addEvent(new Event("lineadded", new Message(
                style(parent.getDocument().getStyledLine(line)), this)));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void trimmed(final int newSize, final int numTrimmed) {
        //TODO FIXME
    }

    /** {@inheritDoc} */
    @Override
    public void cleared() {
        //TODO FIXME
    }

    /** {@inheritDoc} */
    @Override
    public void repaintNeeded() {
        //TODO FIXME
    }

    /** {@inheritDoc} */
    @Override
    public void iconChanged(final FrameContainer window, final String icon) {
        //TODO FIXME
    }

    /** {@inheritDoc} */
    @Override
    public void nameChanged(final FrameContainer window, final String name) {
        //TODO FIXME
    }

    /** {@inheritDoc} */
    @Override
    public void titleChanged(final FrameContainer window, final String title) {
        //TODO FIXME
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing(final FrameContainer window) {
        DynamicRequestHandler.addEvent(new Event("closewindow", myID));
    }

    /**
     * Retrieves the title of this window.
     *
     * @return This window's title
     */
    public String getTitle() {
        return parent.getTitle();
    }

    /**
     * Retrieves the name of this window.
     *
     * @return This window's name
     */
    public String getName() {
        return parent.getName();
    }

    /**
     * Retrieves the type of this window.
     *
     * @return This window's type
     */
    public String getType() {
        // TODO: Pass icon properly instead of relying on type
        return parent.getClass().getSimpleName().toLowerCase();
    }

}

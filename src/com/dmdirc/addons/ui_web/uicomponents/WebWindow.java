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

package com.dmdirc.addons.ui_web.uicomponents;

import com.dmdirc.FrameContainer;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.ui.core.util.Utils;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.IRCTextAttribute;
import com.dmdirc.util.StringTranscoder;

import com.dmdirc.addons.ui_web.DynamicRequestHandler;
import com.dmdirc.addons.ui_web.Event;
import com.dmdirc.addons.ui_web.Message;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.beans.PropertyVetoException;
import java.nio.charset.Charset;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author chris
 */
public class WebWindow implements Window {
    
    protected static int counter = 0;
    
    protected static Map<String, WebWindow> windows = new HashMap<String, WebWindow>();
    
    protected int myID = ++counter;
    
    private final FrameContainer parent;
    
    private List<String> messages = new ArrayList<String>();
    
    private String title;
    
    public WebWindow(final FrameContainer parent) {
        super();
        
        this.parent = parent;
        
        windows.put(getId(), this);
    }
    
    public static Collection<WebWindow> getWindows() {
        return windows.values();
    }
    
    public static WebWindow getWindow(final String id) {
        return windows.get(id);
    }
    
    public List<String> getMessages() {
        return messages;
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(String messageType, Object... args) {
        if (!messageType.isEmpty()) {
            addLine(Formatter.formatMessage(parent.getConfigManager(), messageType, args), true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(StringBuffer messageType, Object... args) {
        if (messageType != null) {
            addLine(messageType.toString(), args);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(String line, boolean timestamp) {
        for (String linepart : line.split("\n")) {
            final String message = 
                    style(Formatter.formatMessage(parent.getConfigManager(), "timestamp",
                    new Date()), getConfigManager()) + style(linepart, getConfigManager());
            messages.add(message);
            DynamicRequestHandler.addEvent(new Event("lineadded", new Message(message, this)));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public ConfigManager getConfigManager() {
        return parent.getConfigManager();
    }

    /** {@inheritDoc} */
    @Override
    public FrameContainer getContainer() {
        return parent;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isVisible() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean isVisible) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return title;
    }
    
    public String getName() {
        return parent.toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isMaximum() {
        return true;
    }

    public void setMaximum(boolean b) throws PropertyVetoException {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /** {@inheritDoc} */
    @Override
    public void open() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public StringTranscoder getTranscoder() {
        return new StringTranscoder(Charset.defaultCharset());
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getType() {
        if (this instanceof WebServerWindow) {
            return "server";
        } else if (this instanceof WebChannelWindow) {
            return "channel";
        } else if (this instanceof WebQueryWindow) {
            return "query";
        } else {
            return "window";
        }
    }
    
    public String getId() {
        return String.valueOf(myID);
    }
    
    protected String style(final String input, final ConfigManager config) {
        final StringBuilder builder = new StringBuilder();
        final AttributedCharacterIterator aci = Utils.getAttributedString(parent.getStyliser(),
                new String[]{input}, "dialog", 12).getAttributedString().getIterator();
         
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
    
    protected static void style(final Map<AttributedCharacterIterator.Attribute, Object> map,
            final StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append("</span>");
        }
        
        String link = null;
                
        builder.append("<span style=\"");
        
        for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : map.entrySet()) {
        
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
                        + StringEscapeUtils.escapeHtml(
                        StringEscapeUtils.escapeJavaScript((String) entry.getValue()))
                        + "');";
            } else if (entry.getKey().equals(IRCTextAttribute.CHANNEL)) {
                builder.append("cursor: pointer; ");
                link = "link_channel('"
                        + StringEscapeUtils.escapeHtml(
                        StringEscapeUtils.escapeJavaScript((String) entry.getValue()))
                        + "');";
            } else if (entry.getKey().equals(IRCTextAttribute.NICKNAME)) {
                builder.append("cursor: pointer; ");
                link = "link_query('"
                        + StringEscapeUtils.escapeHtml(
                        StringEscapeUtils.escapeJavaScript((String) entry.getValue()))
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
    public void restore() {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void maximise() {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void toggleMaximise() {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void minimise() {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void activateFrame() {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

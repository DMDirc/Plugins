/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Channel;
import com.dmdirc.addons.ui_web.DynamicRequestHandler;
import com.dmdirc.addons.ui_web.Event;
import com.dmdirc.addons.ui_web.WebInterfaceUI;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.ui.interfaces.ChannelWindow;

import java.util.Collection;

/**
 *
 * @author chris
 */
public class WebChannelWindow extends WebInputWindow implements ChannelWindow {

    private final Channel channel;

    public WebChannelWindow(final WebInterfaceUI controller,
            final Channel channel) {
        super(controller, channel);
        this.channel = channel;
    }

    /** {@inheritDoc} */
    @Override
    public void updateNames(final Collection<ChannelClientInfo> clients) {
        updateNames();
    }

    /** {@inheritDoc} */
    @Override
    public void addName(final ChannelClientInfo client) {
        DynamicRequestHandler.addEvent(new Event("addnicklist",
                client.getClient().getNickname()));
    }

    /** {@inheritDoc} */
    @Override
    public void removeName(final ChannelClientInfo client) {
        updateNames();
    }

    /** {@inheritDoc} */
    @Override
    public void updateNames() {
        DynamicRequestHandler.addEvent(new Event("clearnicklist", null));
        for (ChannelClientInfo cci : channel.getChannelInfo()
                .getChannelClients()) {
            DynamicRequestHandler.addEvent(new Event("addnicklist",
                    cci.getClient().getNickname()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #getContainer()}
     */
    @Override
    @Deprecated
    public Channel getChannel() {
        return channel;
    }

    /** {@inheritDoc} */
    @Override
    public void redrawNicklist() {
        // Do nothing
    }

}

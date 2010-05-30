/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.parser_twitter;

import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.CallbackObject;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
import java.util.HashMap;
import java.util.Map;

/**
 * A callback object for the Twitter parser.
 *
 * @since 0.6.3m2
 * @author chris
 */
public class TwitterCallbackObject extends CallbackObject {

    /** A map of interfaces to the classes which should be instansiated for them. */
    protected static Map<Class<?>, Class<?>> IMPL_MAP = new HashMap<Class<?>, Class<?>>();

    static {
        IMPL_MAP.put(ChannelClientInfo.class, TwitterChannelClientInfo.class);
        IMPL_MAP.put(ChannelInfo.class, TwitterChannelInfo.class);
        IMPL_MAP.put(ClientInfo.class, TwitterClientInfo.class);
        IMPL_MAP.put(LocalClientInfo.class, TwitterClientInfo.class);
    }

    /**
     * Create a new TwitterCallbackObject.
     *
     * @param parser Parser that owns this object.
     * @param manager Callback Manager that owns this object.
     * @param type Type of callback.
     */
    public TwitterCallbackObject(final Parser parser, final CallbackManager<?> manager,
            final Class<? extends CallbackInterface> type) {
        super(parser, manager, type);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<?> getImplementation(final Class<?> type) {
        return IMPL_MAP.containsKey(type) ? IMPL_MAP.get(type) : type;
    }

}

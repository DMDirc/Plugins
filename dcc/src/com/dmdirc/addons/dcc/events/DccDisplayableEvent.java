/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.addons.dcc.events;

import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.DisplayPropertyMap;
import com.dmdirc.events.DisplayableEvent;

import java.util.Optional;

/**
 * Base class for displayable DCC events.
 */
public abstract class DccDisplayableEvent extends DccEvent implements DisplayableEvent {

    /** The properties associated with this event. */
    private final DisplayPropertyMap properties = new DisplayPropertyMap();

    @Override
    public <T> void setDisplayProperty(final DisplayProperty<T> property, final T value) {
        properties.put(property, value);
    }

    @Override
    public <T> Optional<T> getDisplayProperty(final DisplayProperty<T> property) {
        return properties.get(property);
    }

    @Override
    public DisplayPropertyMap getDisplayProperties() {
        return properties;
    }

}

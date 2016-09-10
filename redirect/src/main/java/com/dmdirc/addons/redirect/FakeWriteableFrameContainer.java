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

package com.dmdirc.addons.redirect;

import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.messages.BackBufferFactory;

import java.util.Collections;
import java.util.Optional;

/**
 * Implements a fake input window, which sends echoed text to the specified chat window instead.
 */
public class FakeWriteableFrameContainer extends FrameContainer {

    /** The target for this window. */
    private final WindowModel target;

    /**
     * Creates a new instance of FakeInputWindow.
     */
    public FakeWriteableFrameContainer(
            final WindowModel target,
            final EventBus eventBus,
            final BackBufferFactory backBufferFactory) {
        super(target.getIcon(), target.getName(), target.getTitle(),
                target.getConfigManager(), backBufferFactory,
                eventBus,
                Collections.<String>emptyList());
        this.target = target;
        initBackBuffer();
        setInputModel(target.getInputModel().orElse(null));
    }

    @Override
    public Optional<Connection> getConnection() {
        return target.getConnection();
    }

}

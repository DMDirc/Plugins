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

package com.dmdirc.addons.ui_swing;

import java.lang.reflect.Method;

import net.engio.mbassy.dispatch.ReflectiveHandlerInvocation;
import net.engio.mbassy.subscription.SubscriptionContext;

/**
 * Handler invocation that invokes the handler on the Swing EDT.
 *
 * <p>Handlers can be annotated as follows to ensure they are called on the EDT:
 *
 * <pre><code>@Handler(invocation = EdtHandlerInvocation.class)</code></pre>
 *
 * <p>All calls will be invoked synchronously on the EDT. To make the handler asynchronous,
 * change the delivery method:
 *
 * <pre><code>@Handler(
 *     invocation = EdtHandlerInvocation.class,
 *     delivery = Invoke.Asynchronously)</code></pre>
 */
public class EdtHandlerInvocation extends ReflectiveHandlerInvocation {

    public EdtHandlerInvocation(final SubscriptionContext context) {
        super(context);
    }

    @Override
    protected void invokeHandler(final Object message, final Object listener,
            final Method handler) {
        UIUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                EdtHandlerInvocation.super.invokeHandler(message, listener, handler);
            }
        });
    }
}

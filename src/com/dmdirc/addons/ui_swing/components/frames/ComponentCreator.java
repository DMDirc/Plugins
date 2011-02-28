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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.util.SimpleInjector;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

/**
 * Utility class to create frame components.
 */
public class ComponentCreator {

    /** Prevent instantiation. */
    private ComponentCreator() {
        //Prevent instantiation.
    }

    public static Set<JComponent> initFrameComponents(final Object frame,
            final SwingController controller,
            final FrameContainer owner) {
        final SimpleInjector injector = new SimpleInjector();
        final Set<String> names = owner.getComponents();
        final Set<JComponent> components = new HashSet<JComponent>();

        injector.addParameter(frame);
        injector.addParameter(controller);
        injector.addParameter(controller.getMainFrame());

        for (String string : names) {
            Object object;
            try {
                Class<?> clazz = null;
                if (string.equals(WindowComponent.INPUTFIELD.getIdentifier())) {
                    clazz = Class.forName("com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField");
                } else if (string.equals(WindowComponent.TEXTAREA.getIdentifier())) {
                    clazz = Class.forName("com.dmdirc.addons.ui_swing.textpane.TextPane");
                }
                object = injector.createInstance(clazz);
            } catch (ClassNotFoundException ex) {
                object = null;
            }
            if (object instanceof JComponent) {
                components.add((JComponent) object);
            }
        }

        return components;
    }
}

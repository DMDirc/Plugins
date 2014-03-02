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

package com.dmdirc.addons.ui_swing.components;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Extention of a normal JCheckboxMenuItem that stays open when clicked.
 */
public class CheckBoxMenuItem extends JCheckBoxMenuItem {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 1;
    /** Menu path to use when clicked. */
    private static MenuElement[] path;

    /**
     * Constructs a new checkbox menu item.
     *
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem()
     */
    public CheckBoxMenuItem() {
        super();
        getModel().addChangeListener(new StayOpenListener());
    }

    /**
     * Constructs a new checkbox menu item with the specified action.
     *
     * @param a Action to use
     *
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(Action)
     */
    public CheckBoxMenuItem(final Action a) {
        super(a);
        getModel().addChangeListener(new StayOpenListener());
    }

    /**
     * Constructs a new checkbox menu item with the specified action.
     *
     * @param icon Icon to use
     *
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(Icon)
     */
    public CheckBoxMenuItem(final Icon icon) {
        super(icon);
        getModel().addChangeListener(new StayOpenListener());
    }

    /**
     * Constructs a new checkbox menu item with the specified text.
     *
     * @param text Text to use
     *
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String)
     */
    public CheckBoxMenuItem(final String text) {
        super(text);
        getModel().addChangeListener(new StayOpenListener());
    }

    /**
     * Constructs a new checkbox menu item with the specified text and selected state.
     *
     * @param text     Text to use
     * @param selected Initial selection state
     *
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String, boolean)
     */
    public CheckBoxMenuItem(final String text, final boolean selected) {
        super(text, selected);
        getModel().addChangeListener(new StayOpenListener());
    }

    /**
     * Constructs a new checkbox menu item with the specified text and icon.
     *
     * @param text Text to use
     * @param icon Icon to use
     *
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String, Icon)
     */
    public CheckBoxMenuItem(final String text, final Icon icon) {
        super(text, icon);
        getModel().addChangeListener(new StayOpenListener());
    }

    /**
     * Constructs a new checkbox menu item with the specified text icon and initial selected state.
     *
     * @param text     Text to use
     * @param icon     Icon to use
     * @param selected Initial selection state
     *
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String, Icon, boolean)
     */
    public CheckBoxMenuItem(final String text, final Icon icon,
            final boolean selected) {
        super(text, icon, selected);
        getModel().addChangeListener(new StayOpenListener());
    }

    /**
     * Overridden to reopen the menu.
     *
     * @param pressTime the time to "hold down" the button, in milliseconds
     */
    @Override
    public void doClick(final int pressTime) {
        super.doClick(pressTime);
        MenuSelectionManager.defaultManager().setSelectedPath(path);
    }

    /**
     * Listener to restore the saved path when clicked.
     */
    private class StayOpenListener implements ChangeListener {

        @Override
        public void stateChanged(final ChangeEvent e) {
            if (getModel().isArmed() && isShowing()) {
                path = MenuSelectionManager.defaultManager().getSelectedPath();
            }
        }

    }

}

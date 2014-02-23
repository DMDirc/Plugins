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

package com.dmdirc.addons.ui_swing.components.durationeditor;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.DateUtils;
import com.dmdirc.util.collections.ListenerList;

import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Duration display and edit component.
 */
public class DurationDisplay extends JPanel implements ActionListener,
        DurationListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Listener list. */
    private final ListenerList listeners;
    /** Icon manager to change icon on editor window. */
    private final IconManager iconManager;
    /** Current duration. */
    private int duration;
    /** Duration label. */
    private JLabel durationLabel;
    /** Edit button. */
    private JButton button;
    /** Parent window. */
    private Window window;

    /**
     * Initialises a new duration display of 0 milliseconds.
     *
     * @param iconManager Icon manager used to change window icon
     */
    public DurationDisplay(final IconManager iconManager) {
        this(iconManager, 0);
    }

    /**
     * Instantiates a new duration display.
     *
     * @param iconManager Icon manager used to change window icon
     * @param duration Starting duration
     */
    public DurationDisplay(final IconManager iconManager, final long duration) {
        this(null, iconManager, duration);
    }

    /**
     * Initialises a new duration display showing the specified millisecond duration.
     *
     * @param window   Parent window.
     * @param iconManager Icon manager used to change window icon
     * @param duration Duration to display in milliseconds
     *
     * @since 0.6
     */
    public DurationDisplay(final Window window, final IconManager iconManager, final long duration) {
        super();

        this.window = window;
        this.iconManager = iconManager;
        this.duration = (int) (duration / 1000);
        listeners = new ListenerList();

        initComponents();
        addListeners();
        layoutComponents();
    }

    /**
     * Initialises and lays out the components.
     */
    private void initComponents() {
        button = new JButton("Edit");
        durationLabel = new JLabel();
        if (duration == 0) {
            durationLabel.setText("0 Seconds");
        } else {
            durationLabel.setText(DateUtils.formatDuration(duration));
        }

        if (UIUtilities.isWindowsUI()) {
            button.setMargin(new Insets(2, 4, 2, 4));
        } else {
            button.setMargin(new Insets(0, 2, 0, 2));
        }
    }

    /**
     * Adds listeners to the components.
     */
    private void addListeners() {
        button.addActionListener(this);
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("ins 0, fill"));

        add(durationLabel, "growx, pushx");
        add(button, "");
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final DurationEditor editor = new DurationEditor(window, iconManager, duration);
        editor.display(window);
        editor.addDurationListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void durationUpdated(final int newDuration) {
        duration = newDuration;
        if (duration == 0) {
            durationLabel.setText("0 Seconds");
        } else {
            durationLabel.setText(DateUtils.formatDuration(duration));
        }
        fireDurationListener(newDuration * 1000);
    }

    /**
     * Returns the duration of this display in milliseconds.
     *
     * @return Displayed duration in milliseconds
     */
    public long getDuration() {
        return duration * 1000;
    }

    /**
     * Adds a DurationListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addDurationListener(final DurationListener listener) {
        if (listener == null) {
            return;
        }

        listeners.add(DurationListener.class, listener);
    }

    /**
     * Removes a DurationListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeDurationListener(final DurationListener listener) {
        listeners.remove(DurationListener.class, listener);
    }

    /**
     * Fires the duration updated method on all listeners.
     *
     * @param newDuration New duration
     */
    protected void fireDurationListener(final int newDuration) {
        for (final DurationListener listener : listeners.get(DurationListener.class)) {
            listener.durationUpdated(newDuration);
        }
    }

    /**
     * Sets the Parent window.
     *
     * @param window Parent window
     */
    public void setWindow(final Window window) {
        this.window = window;
    }

}

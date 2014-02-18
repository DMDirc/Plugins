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

import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.util.collections.ListenerList;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

/**
 * Duration editor component.
 */
public class DurationEditor extends StandardDialog implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 1;
    /** How many seconds in a minute. */
    private static final int SECONDS_IN_MINUTE = 60;
    /** How many hours in a day. */
    private static final int HOURS_IN_DAY = 24;
    /** How many seconds in an hour. */
    private static final int SECONDS_IN_HOURS = 3600;
    /** How many seconds in a day. */
    private static final int SECONDS_IN_DAY = 86400;
    /** Maximum number of days in a month. */
    private static final int MAX_DAYS_IN_MONTH = 31;
    /** Days spinner. */
    private JSpinner daysSpinner;
    /** Hours spinner. */
    private JSpinner hoursSpinner;
    /** Minutes spinner. */
    private JSpinner minutesSpinner;
    /** Seconds spinner. */
    private JSpinner secondsSpinner;
    /** Listener list. */
    private final ListenerList listeners;
    /** Parent window. */
    private Window window;

    /**
     * Instantiates a new duration editor.
     *
     * @param window Parent window.
     *
     * @since 0.6
     */
    public DurationEditor(final Window window) {
        this(window, 0);
    }

    /**
     * Instantiates a new duration editor.
     *
     * @param window   Parent window.
     * @param duration Starting duration
     *
     * @since 0.6
     */
    public DurationEditor(final Window window, final long duration) {
        super(window, ModalityType.MODELESS);

        this.window = window;

        listeners = new ListenerList();

        initComponents(duration);
        addListeners();
        layoutComponents();
    }

    /**
     * Initialises the components.
     *
     * @param duration Duration to initialise to
     */
    private void initComponents(final long duration) {
        orderButtons(new JButton(), new JButton());
        daysSpinner = new JSpinner(new SpinnerNumberModel(0, 0,
                MAX_DAYS_IN_MONTH, 1));
        hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0,
                HOURS_IN_DAY, 1));
        minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0,
                SECONDS_IN_MINUTE, 1));
        secondsSpinner = new JSpinner(new SpinnerNumberModel(0, 0,
                SECONDS_IN_MINUTE, 1));

        long updatedDuration = duration;
        daysSpinner.setValue((int) duration / SECONDS_IN_DAY);
        updatedDuration = (updatedDuration % SECONDS_IN_DAY);
        hoursSpinner.setValue((int) duration / SECONDS_IN_HOURS);
        updatedDuration = (updatedDuration % SECONDS_IN_HOURS);
        minutesSpinner.setValue((int) duration / SECONDS_IN_MINUTE);
        updatedDuration = (updatedDuration % SECONDS_IN_MINUTE);
        secondsSpinner.setValue((int) duration);
    }

    /**
     * Adds the listeners.
     */
    private void addListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        setWindow(window);
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("hidemode 3, pack"));

        add(new JLabel("Days: "), "split 8");
        add(daysSpinner);
        add(new JLabel("Hours: "));
        add(hoursSpinner);
        add(new JLabel("Minutes: "));
        add(minutesSpinner);
        add(new JLabel("Seconds: "));
        add(secondsSpinner, "wrap");
        add(getLeftButton(), "split 2, sgx button, right");
        add(getRightButton(), "sgx button, right");

        setDefaultCloseOperation(DurationEditor.DISPOSE_ON_CLOSE);
    }

    /** {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            fireDurationListener(getDuration());
        }
        dispose();
    }

    /**
     * Returns the duration currently represented by this duration editor.
     *
     * @return Current duration (in seconds)
     */
    public int getDuration() {
        int duration = 0;

        duration += ((Number) secondsSpinner.getValue()).intValue();
        duration += (((Number) minutesSpinner.getValue())).intValue()
                * SECONDS_IN_MINUTE;
        duration += (((Number) hoursSpinner.getValue())).intValue()
                * SECONDS_IN_MINUTE * SECONDS_IN_MINUTE;
        duration += (((Number) daysSpinner.getValue())).intValue()
                * SECONDS_IN_MINUTE * SECONDS_IN_MINUTE * SECONDS_IN_MINUTE;

        return duration;
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

        if (window != null) {
            window.addWindowListener(new WindowAdapter() {
                /** {@inheritDoc} */
                @Override
                public void windowClosed(final WindowEvent e) {
                    dispose();
                }
            });
        }
    }

}

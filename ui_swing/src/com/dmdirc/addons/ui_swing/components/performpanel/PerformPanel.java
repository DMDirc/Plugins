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

package com.dmdirc.addons.ui_swing.components.performpanel;

import com.dmdirc.addons.ui_swing.components.inputfields.TextAreaInputField;
import com.dmdirc.commandparser.auto.AutoCommand;
import com.dmdirc.commandparser.auto.AutoCommandManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

/**
 * Creates a text area that fills whatever space it has available. This panel facilitates
 * modification of performs.
 *
 * @since 0.6.4
 */
public class PerformPanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Text area that the perform is displayed in. */
    private final JTextArea performSpace;
    /** Perform wrapper to read/write performs to. */
    private final AutoCommandManager autoCommandManager;
    /** Map of original auto commands to their modified forms. */
    private final Map<AutoCommand, AutoCommand> autoCommands = new HashMap<>();
    /** The auto command that is displayed in the text area. */
    private AutoCommand visiblePerform;

    /**
     * Creates a new instance of PerformPanel that has no knowledge of any performs at the time of
     * creation.
     *
     * By default this panel displays a blank text area.
     *
     * @param iconManager    Icon manager
     * @param config         Config to read settings from
     * @param autoCommandManager Perform wrapper to read/write performs to.
     */
    public PerformPanel(
            final IconManager iconManager,
            final ColourManagerFactory colourManagerFactory,
            final AggregateConfigProvider config,
            final AutoCommandManager autoCommandManager) {
        this(iconManager, colourManagerFactory, config, autoCommandManager,
                Collections.<AutoCommand>emptyList());
    }

    /**
     * Creates a new instance of PerformPanel and prepares the list of PerformDescriptions passed to
     * it for viewing/modification.
     *
     * By default this panel displays a blank text area.
     *
     * @param iconManager    Icon manager
     * @param config         Config to read settings from
     * @param autoCommandManager Perform wrapper to read/write performs to.
     * @param performs       Collection of PerformDescriptions to initialise
     */
    public PerformPanel(
            final IconManager iconManager,
            final ColourManagerFactory colourManagerFactory,
            final AggregateConfigProvider config,
            final AutoCommandManager autoCommandManager,
            final Collection<AutoCommand> performs) {

        this.autoCommandManager = autoCommandManager;

        performs.forEach(this::addCommand);
        setLayout(new MigLayout("ins 0, fill"));
        performSpace = new TextAreaInputField(iconManager, colourManagerFactory, config, "");
        add(new JScrollPane(performSpace), "grow, push");
    }

    /**
     * This will add a command to the internal cache to track changes.
     *
     * @param command Auto command to add
     */
    public void addCommand(final AutoCommand command) {
        autoCommands.putIfAbsent(command, command);
    }

    /**
     * Saves modifications to the provided performs.
     */
    public void savePerform() {
        if (visiblePerform != null) {
            autoCommands.put(visiblePerform,
                    createAutoCommand(visiblePerform, performSpace.getText()));
        }

        autoCommands.entrySet().parallelStream()
                .filter(e -> !e.getKey().equals(e.getValue()))
                .forEach(e -> autoCommandManager.replaceAutoCommand(e.getKey(), e.getValue()));
    }

    /**
     * Displays the specified perform to the user. Edits made to any previously displayed perform
     * are stored, but are not saved until {@link #savePerform()} is called. If the specified
     * perform is not in this panel's cache, it will be added.
     *
     * @param perform Perform to display in the text area
     */
    public void switchPerform(final AutoCommand perform) {
        if (perform != null && !autoCommands.containsKey(perform)) {
            addCommand(perform);
        }
        if (visiblePerform != null) {
            autoCommands.put(visiblePerform,
                    createAutoCommand(visiblePerform, performSpace.getText()));
        }
        if (perform == null) {
            performSpace.setText("");
        } else {
            performSpace.setText(perform.getResponse());
        }
        performSpace.setEnabled(perform != null);
        visiblePerform = perform;
    }

    /**
     * Creates a new auto command based on the existing one, but with a different response.
     *
     * @param existing The existing auto command to model on.
     * @param text The new text to use for the response.
     * @return A new AutoCommand with the same target as the existing one, and the given text.
     */
    private static AutoCommand createAutoCommand(final AutoCommand existing, final String text) {
        return AutoCommand.create(existing.getServer(), existing.getNetwork(),
                existing.getProfile(), text);
    }

}

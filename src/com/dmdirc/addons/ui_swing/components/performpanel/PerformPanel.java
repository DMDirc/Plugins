/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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


import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.actions.wrappers.PerformWrapper.PerformDescription;
import com.dmdirc.addons.ui_swing.components.TextAreaInputField;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;

/**
 * Customizable perform panels.
 *
 * @author Simon Mott
 * @since 0.6.4
 */
public class PerformPanel extends JPanel {

    /** Text area that the perform is displayed in. */
    private JTextArea performSpace;
    /** Map of performs this panel can display. */
    private Map<PerformDescription, String[]> performMap = new
            HashMap<PerformDescription, String[]>();
    /** The perform that is displayed in the text area. */
    private PerformDescription visiblePerform;

    /**
     * Creates a new instance of PerformPanel that has no knowledge of any
     * performs at the time of creation.
     *
     * By default this panel displays a blank text area.
     */
    public PerformPanel() {
        this(Collections.<PerformDescription>emptyList());
    }

    /**
     * Creates a new instance of PerformPanel and prepaires the list of
     * PerformDescriptions passed to it for viewing/modification.
     *
     * By default this panel displays a blank text area.
     *
     * @param performList Collection of PerformDescriptions to initiliase
     */
    public PerformPanel(Collection<PerformDescription> performList) {
        for (PerformDescription perform : performList) {
            addPerform(perform);
        }
        setLayout(new MigLayout("fill"));
        performSpace = new TextAreaInputField("");
        add(new JScrollPane(performSpace), "grow, push");
        visiblePerform = null;
    }

    /**
     * Adds a perform to the map of performs this PerformPanel can modify.
     *
     * @param perform PerformDescription to add
     */
    public void addPerform(PerformDescription perform) {
        performMap.put(perform, PerformWrapper.getPerformWrapper().getPerform(perform));
    }

    /**
     * Deletes a perform from the map of performs this PerformPanel can modify.
     *
     * @param perform PerformDescription to remove
     */
    public void delPerform(PerformDescription perform) {
        performMap.remove(perform);
    }

    /**
     * Saves modifications to the provided perform and updates our map.
     *
     * @param perform PerformDescription to save
     * @param text Text to save to this PerformDescription
     */
    public void savePerform() {
        performMap.put(visiblePerform, performSpace.getText().split("\n"));
        System.out.println(visiblePerform.getProfile() + " " + 
                visiblePerform.getTarget() + " " + visiblePerform.getType() +
                " saved as " +
                performSpace.getText());
        for (Entry<PerformDescription, String[]> perform : performMap.entrySet()) {
            System.out.println(perform.getKey().getProfile());
            System.out.println(perform.getKey().getTarget());
            System.out.println(perform.getKey().getType());
            System.out.println("becomes " + implode(perform.getValue()));
            PerformWrapper.getPerformWrapper().setPerform(perform.getKey(),
                    perform.getValue());
        }
    }

    /**
     * Allows switching between the PerformDescriptions that are in the
     * performMap. If a perform is provided that is not in the map then it
     * is added.
     *
     * @param perform Perform to display in the text area
     */
    public void switchPerform(PerformDescription perform) {
        if (!performMap.containsKey(perform)) {
            addPerform(perform);
        }
        if (visiblePerform != null) {
            performMap.put(visiblePerform, performSpace.getText().split("\n"));
        }
        performSpace.setText(implode(performMap.get(perform)));
        visiblePerform = perform;
    }

    /**
     * Implodes the specified string array, joining each line with a LF.
     *
     * @param lines The lines to be joined together
     * @return A string containing each element of lines, separated by a LF.
     */
    private String implode(final String[] lines) {
        final StringBuilder res = new StringBuilder();

        for (String line : lines) {
            res.append('\n');
            res.append(line);
        }

        return res.length() == 0 ? "" : res.substring(1);
    }
}

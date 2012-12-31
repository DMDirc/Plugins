/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.dialogs.about;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Background loader of licences into a list.
 */
public class LicenceLoader extends LoggingSwingWorker<Void, Void> {

    /** Tree. */
    private final JTree tree;
    /** Model to load licences into. */
    private final DefaultTreeModel model;
    /** Swing Controller */
    final SwingController controller;

    /**
     * Instantiates a new licence loader.
     *
     * @param tree Tree
     * @param model Model to load licences into
     */
    public LicenceLoader(final SwingController controller, final JTree tree, final DefaultTreeModel model) {
        super();

        this.controller = controller;
        this.tree = tree;
        this.model = model;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException on any exception in the background task
     */
    @Override
    protected Void doInBackground() throws IOException {
        final ResourceManager rm = ResourceManager.getResourceManager();
        if (rm == null) {
            Logger.userError(ErrorLevel.LOW, "Unable to load licences, "
                    + "no resource manager");
        } else {
            addCoreLicences(rm);
            for (PluginInfo pi : controller.getMain().getPluginManager()
                    .getPluginInfos()) {
                addPluginLicences(pi);
            }
        }

        return null;
    }

    private Licence createLicence(final Entry<String, InputStream> entry) {
        final String licenceString = entry.getKey().substring(entry.getKey().
                lastIndexOf('/') + 1);
        if (licenceString.length() > 1) {
            final String[] licenceStringParts = licenceString.split(" - ");
            return new Licence(licenceStringParts[1], licenceStringParts[0],
                    "<html><h1>" + licenceStringParts[1] + "</h1><p>"
                    + readInputStream(entry.getValue()).replace("\n", "<br>")
                    + "</p></html>");
        } else {
            return null;
        }
    }

    private void addCoreLicences(final ResourceManager rm) {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("DMDirc");
        final Map<String, InputStream> licences =
                new TreeMap<String, InputStream>(String.CASE_INSENSITIVE_ORDER);
        licences.putAll(rm.getResourcesStartingWithAsInputStreams(
                "com/dmdirc/licences/"));
        addLicensesToNode(licences, root);
    }

    private void addPluginLicences(final PluginInfo pi) throws IOException {
        final Map<String, InputStream> licences = pi.getLicenceStreams();

        if (licences.isEmpty()) {
            return;
        }

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(pi);
        addLicensesToNode(licences, root);
    }

    private void addLicensesToNode(final Map<String, InputStream> licences,
            final DefaultMutableTreeNode root) {
        UIUtilities.invokeAndWait(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                model.insertNodeInto(root, (DefaultMutableTreeNode) model.
                        getRoot(), model.getChildCount(model.getRoot()));
            }
        });
        for (Entry<String, InputStream> entry : licences.entrySet()) {
            final Licence licence = createLicence(entry);
            if (licence == null) {
                continue;
            }
            UIUtilities.invokeAndWait(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    model.insertNodeInto(new DefaultMutableTreeNode(licence),
                            root, model.getChildCount(root));
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void done() {
        model.nodeStructureChanged((DefaultMutableTreeNode) model.getRoot());
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        tree.setSelectionRow(0);
        super.done();
    }

    /**
     * Converts an input stream into a string.
     *
     * @param stream Stream to convert
     *
     * @return Contents of the input stream
     */
    private String readInputStream(final InputStream stream) {
        String line;
        final BufferedReader input =
                new BufferedReader(new InputStreamReader(stream));
        final StringBuilder text = new StringBuilder();

        try {
            line = input.readLine();
            while (line != null) {
                text.append(line).append("\n");
                line = input.readLine();
            }
        } catch (IOException ex) {
            //Ignore
        }

        return text.toString();
    }
}

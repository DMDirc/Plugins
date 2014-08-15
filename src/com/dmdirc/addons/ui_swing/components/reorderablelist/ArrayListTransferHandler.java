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

package com.dmdirc.addons.ui_swing.components.reorderablelist;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

/**
 * Arraylist Transfer handler.
 *
 * @param <T> Type to be transferred
 */
public final class ArrayListTransferHandler<T> extends TransferHandler {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Local Transfer flavour. */
    private DataFlavor localArrayListFlavor;
    /** Serial Transfer flavour. */
    private final DataFlavor serialArrayListFlavor;
    /** Source component. */
    private JList<T> sourceList;
    /** Dragged Indices. */
    private int[] indices;
    /** Index to add item(s). */
    private int addIndex = -1;
    /** Number of items to add. */
    private int addCount;

    public ArrayListTransferHandler() {
        try {
            localArrayListFlavor = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType + ";class=java.util.ArrayList");
        } catch (ClassNotFoundException e) {
            Logger.userError(ErrorLevel.LOW, "unable to create data flavor: " + e.getMessage());
        }
        serialArrayListFlavor = new DataFlavor(ArrayList.class, "ArrayList");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean importData(final JComponent comp, final Transferable t) {
        if (!canImport(comp, t.getTransferDataFlavors())) {
            return false;
        }

        try {
            if (hasLocalArrayListFlavor(t.getTransferDataFlavors())) {
                return doImport((JList<T>) comp, (ArrayList<T>) t.getTransferData(
                        localArrayListFlavor));
            } else if (hasSerialArrayListFlavor(t.getTransferDataFlavors())) {
                return doImport((JList<T>) comp, (ArrayList<T>) t.
                        getTransferData(serialArrayListFlavor));
            } else {
                return false;
            }
        } catch (UnsupportedFlavorException e) {
            Logger.userError(ErrorLevel.LOW, "Unsupported data flavor: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to import data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Imports the transferable data into the list.
     *
     * @param target       target list
     * @param transferList transferable list
     *
     * @return Whether the data was imported
     */
    private boolean doImport(final JList<T> target, final List<T> transferList) {
        int index = target.getSelectedIndex();
        if (sourceList.equals(target) && indices != null && index >= indices[0] - 1 && index
                <= indices[indices.length - 1]) {
            indices = null;
            return true;
        }

        final DefaultListModel<T> listModel = (DefaultListModel<T>) target.getModel();
        final int max = listModel.getSize();

        if (index < 0) {
            index = max;
        } else {
            index++;
            if (index > max) {
                index = max;
            }

        }
        addIndex = index;
        addCount = transferList.size();

        for (T aTransferList : transferList) {
            listModel.add(index++, aTransferList);
        }

        return true;
    }

    @Override
    protected void exportDone(final JComponent source, final Transferable data,
            final int action) {
        if ((action == MOVE) && (indices != null)) {
            final DefaultListModel<T> model = (DefaultListModel<T>) sourceList.getModel();

            if (addCount > 0) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] > addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }

        indices = null;
        addIndex = -1;
        addCount = 0;
    }

    /**
     * Do any of the specified flavours match the local flavour.
     *
     * @param transferFlavors Flavours to check
     *
     * @return whether the transferFlavors is supported
     */
    private boolean hasLocalArrayListFlavor(final DataFlavor[] transferFlavors) {
        if (localArrayListFlavor == null) {
            return false;
        }
        for (DataFlavor transferFlavor : transferFlavors) {
            if (transferFlavor.equals(localArrayListFlavor)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Do any of the specified flavours match the serial flavour.
     *
     * @param transferFlavors Flavours to check
     *
     * @return whether the flavour is supported
     */
    private boolean hasSerialArrayListFlavor(final DataFlavor[] transferFlavors) {
        for (DataFlavor transferFlavor : transferFlavors) {
            if (transferFlavor.equals(serialArrayListFlavor)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canImport(final JComponent comp, final DataFlavor[] transferFlavors) {
        return comp instanceof JList && ((JList) comp).getModel() instanceof DefaultListModel
                && (hasLocalArrayListFlavor(transferFlavors)
                || hasSerialArrayListFlavor(transferFlavors));
    }

    @Override
    protected Transferable createTransferable(final JComponent c) {
        if (c instanceof JList) {
            @SuppressWarnings("unchecked")
            final JList<T> list = (JList<T>) c;
            sourceList = list;
            indices = sourceList.getSelectedIndices();
            final List<T> values = sourceList.getSelectedValuesList();

            return new ListTransferable<>(values);
        }

        return null;
    }

    @Override
    public int getSourceActions(final JComponent c) {
        return COPY_OR_MOVE;
    }

}

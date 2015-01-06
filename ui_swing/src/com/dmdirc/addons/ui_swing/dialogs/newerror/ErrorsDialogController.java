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

package com.dmdirc.addons.ui_swing.dialogs.newerror;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.GenericTableModel;
import com.dmdirc.interfaces.ui.ErrorsDialogModel;
import com.dmdirc.interfaces.ui.ErrorsDialogModelListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * Controller linking the {@link ErrorsDialogModel} to the {@link ErrorsDialog}.
 */
public class ErrorsDialogController implements ErrorsDialogModelListener {

    private final ErrorsDialogModel model;
    private GenericTableModel<ProgramError> tableModel;
    private JTextField date;
    private JTextField severity;
    private JTextField reportStatus;
    private JTextArea details;
    private JButton deleteAll;
    private JButton delete;
    private JButton send;

    public ErrorsDialogController(final ErrorsDialogModel model) {
        this.model = model;
    }

    public void init(final ErrorsDialog dialog, final GenericTableModel<ProgramError> tableModel,
            final JTable table, final JTextField date, final JTextField severity,
            final JTextField reportStatus, final JTextArea details, final JButton deleteAll,
            final JButton delete, final JButton send, final JButton close) {
        this.tableModel = tableModel;
        this.date = date;
        this.severity = severity;
        this.reportStatus = reportStatus;
        this.details = details;
        this.deleteAll = deleteAll;
        this.delete = delete;
        this.send = send;
        model.addListener(this);
        model.load();
        model.getErrors().forEach(this::errorAdded);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                final int index = table.getSelectedRow();
                if (index == -1) {
                    model.setSelectedError(Optional.empty());
                } else {
                    model.setSelectedError(Optional.ofNullable(tableModel.getValue(index)));
                }
            }
        });
        deleteAll.addActionListener(e -> model.deleteAllErrors());
        delete.addActionListener(e -> model.deleteSelectedError());
        send.addActionListener(e -> model.sendSelectedError());
        close.addActionListener(e -> dialog.dispose());
    }

    @Override
    public void errorDeleted(final ProgramError error) {
        UIUtilities.invokeAndWait(() -> {
            tableModel.removeValue(error);
            deleteAll.setEnabled(model.isDeleteAllAllowed());
            checkEnabledStates();
        });
    }

    @Override
    public void errorAdded(final ProgramError error) {
        UIUtilities.invokeLater(() -> {
            tableModel.addValue(error);
            checkEnabledStates();
        });
    }

    @Override
    public void selectedErrorChanged(final Optional<ProgramError> selectedError) {
        UIUtilities.invokeLater(() -> {
            date.setText(selectedError.map(ProgramError::getDate)
                    .map(d -> new SimpleDateFormat("MMM dd hh:mm aa").format(d)).orElse(""));
            severity.setText(selectedError
                    .map(ProgramError::getLevel)
                    .map(ErrorLevel::name).orElse(""));
            reportStatus.setText(selectedError
                    .map(ProgramError::getReportStatus)
                    .map(ErrorReportStatus::name).orElse(""));
            details.setText(selectedError.map(ProgramError::getDetails).orElse(""));
            details.append(Joiner.on('\n').skipNulls()
                    .join(selectedError.map(ProgramError::getTrace).orElse(Lists.newArrayList())));
            checkEnabledStates();
        });
    }

    @Override
    public void errorStatusChanged(final ProgramError error) {
        UIUtilities.invokeLater(() -> {
            final int index = tableModel.getIndex(error);
            tableModel.fireTableRowsUpdated(index, index);
            checkEnabledStates();
        });
    }

    private void checkEnabledStates() {
        deleteAll.setEnabled(model.isDeleteAllAllowed());
        send.setEnabled(model.isSendAllowed());
        delete.setEnabled(model.isDeletedAllowed());
    }
}

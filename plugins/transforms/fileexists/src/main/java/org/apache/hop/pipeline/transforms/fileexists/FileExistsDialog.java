/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.pipeline.transforms.fileexists;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.pipeline.transform.ComponentSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FileExistsDialog extends BaseTransformDialog {
  private static final Class<?> PKG = FileExistsMeta.class; // For Translator

  private boolean gotPreviousFields = false;
  private CCombo wFileName;

  private Label wlFileType;
  private TextVar wResult;
  private TextVar wFileType;

  private Button wInclFileType;

  private Button wAddResult;

  private final FileExistsMeta input;

  public FileExistsDialog(
      Shell parent, IVariables variables, FileExistsMeta transformMeta, PipelineMeta pipelineMeta) {
    super(parent, variables, transformMeta, pipelineMeta);
    input = transformMeta;
  }

  @Override
  public String open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    PropsUi.setLook(shell);
    setShellImage(shell, input);

    ModifyListener lsMod = e -> input.setChanged();

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "FileExistsDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = PropsUi.getMargin();

    // THE BUTTONS
    wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wOk.addListener(SWT.Selection, e -> ok());
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    wCancel.addListener(SWT.Selection, e -> cancel());
    setButtonPositions(new Button[] {wOk, wCancel}, margin, null);

    // TransformName line
    wlTransformName = new Label(shell, SWT.RIGHT);
    wlTransformName.setText(BaseMessages.getString(PKG, "FileExistsDialog.TransformName.Label"));
    PropsUi.setLook(wlTransformName);
    fdlTransformName = new FormData();
    fdlTransformName.left = new FormAttachment(0, 0);
    fdlTransformName.right = new FormAttachment(middle, -margin);
    fdlTransformName.top = new FormAttachment(0, margin);
    wlTransformName.setLayoutData(fdlTransformName);
    wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wTransformName.setText(transformName);
    PropsUi.setLook(wTransformName);
    wTransformName.addModifyListener(lsMod);
    fdTransformName = new FormData();
    fdTransformName.left = new FormAttachment(middle, 0);
    fdTransformName.top = new FormAttachment(0, margin);
    fdTransformName.right = new FormAttachment(100, 0);
    wTransformName.setLayoutData(fdTransformName);

    // filename field
    Label wlFileName = new Label(shell, SWT.RIGHT);
    wlFileName.setText(BaseMessages.getString(PKG, "FileExistsDialog.FileName.Label"));
    PropsUi.setLook(wlFileName);
    FormData fdlFileName = new FormData();
    fdlFileName.left = new FormAttachment(0, 0);
    fdlFileName.right = new FormAttachment(middle, -margin);
    fdlFileName.top = new FormAttachment(wTransformName, margin);
    wlFileName.setLayoutData(fdlFileName);

    wFileName = new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
    PropsUi.setLook(wFileName);
    wFileName.addModifyListener(lsMod);
    FormData fdFileName = new FormData();
    fdFileName.left = new FormAttachment(middle, 0);
    fdFileName.top = new FormAttachment(wTransformName, margin);
    fdFileName.right = new FormAttachment(100, -margin);
    wFileName.setLayoutData(fdFileName);
    wFileName.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(FocusEvent e) {
            // Do nothing on focusLost
          }

          @Override
          public void focusGained(FocusEvent e) {
            Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            shell.setCursor(busy);
            get();
            shell.setCursor(null);
            busy.dispose();
          }
        });

    // Result fieldname ...
    Label wlResult = new Label(shell, SWT.RIGHT);
    wlResult.setText(BaseMessages.getString(PKG, "FileExistsDialog.ResultField.Label"));
    PropsUi.setLook(wlResult);
    FormData fdlResult = new FormData();
    fdlResult.left = new FormAttachment(0, 0);
    fdlResult.right = new FormAttachment(middle, -margin);
    fdlResult.top = new FormAttachment(wFileName, margin * 2);
    wlResult.setLayoutData(fdlResult);

    wResult = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wResult.setToolTipText(BaseMessages.getString(PKG, "FileExistsDialog.ResultField.Tooltip"));
    PropsUi.setLook(wResult);
    wResult.addModifyListener(lsMod);
    FormData fdResult = new FormData();
    fdResult.left = new FormAttachment(middle, 0);
    fdResult.top = new FormAttachment(wFileName, margin * 2);
    fdResult.right = new FormAttachment(100, 0);
    wResult.setLayoutData(fdResult);

    // Add filename to result filenames?
    Label wlAddResult = new Label(shell, SWT.RIGHT);
    wlAddResult.setText(BaseMessages.getString(PKG, "FileExistsDialog.AddResult.Label"));
    PropsUi.setLook(wlAddResult);
    FormData fdlAddResult = new FormData();
    fdlAddResult.left = new FormAttachment(0, 0);
    fdlAddResult.top = new FormAttachment(wResult, margin);
    fdlAddResult.right = new FormAttachment(middle, -margin);
    wlAddResult.setLayoutData(fdlAddResult);
    wAddResult = new Button(shell, SWT.CHECK);
    PropsUi.setLook(wAddResult);
    wAddResult.setToolTipText(BaseMessages.getString(PKG, "FileExistsDialog.AddResult.Tooltip"));
    FormData fdAddResult = new FormData();
    fdAddResult.left = new FormAttachment(middle, 0);
    fdAddResult.top = new FormAttachment(wlAddResult, 0, SWT.CENTER);
    wAddResult.setLayoutData(fdAddResult);
    wAddResult.addSelectionListener(new ComponentSelectionListener(input));

    // ///////////////////////////////
    // START OF Additional Fields GROUP //
    // ///////////////////////////////

    Group wAdditionalFields = new Group(shell, SWT.SHADOW_NONE);
    PropsUi.setLook(wAdditionalFields);
    wAdditionalFields.setText(
        BaseMessages.getString(PKG, "FileExistsDialog.wAdditionalFields.Label"));

    FormLayout additionalFieldsgroupLayout = new FormLayout();
    additionalFieldsgroupLayout.marginWidth = 10;
    additionalFieldsgroupLayout.marginHeight = 10;
    wAdditionalFields.setLayout(additionalFieldsgroupLayout);

    // include filetype?
    Label wlInclFileType = new Label(wAdditionalFields, SWT.RIGHT);
    wlInclFileType.setText(BaseMessages.getString(PKG, "FileExistsDialog.InclFileType.Label"));
    PropsUi.setLook(wlInclFileType);
    FormData fdlInclFileType = new FormData();
    fdlInclFileType.left = new FormAttachment(0, 0);
    fdlInclFileType.top = new FormAttachment(wResult, margin);
    fdlInclFileType.right = new FormAttachment(middle, -margin);
    wlInclFileType.setLayoutData(fdlInclFileType);
    wInclFileType = new Button(wAdditionalFields, SWT.CHECK);
    PropsUi.setLook(wInclFileType);
    wInclFileType.setToolTipText(
        BaseMessages.getString(PKG, "FileExistsDialog.InclFileType.Tooltip"));
    FormData fdInclFileType = new FormData();
    fdInclFileType.left = new FormAttachment(middle, 0);
    fdInclFileType.top = new FormAttachment(wlInclFileType, 0, SWT.CENTER);
    wInclFileType.setLayoutData(fdInclFileType);
    wInclFileType.addSelectionListener(new ComponentSelectionListener(input));

    // Enable/disable the right fields to allow a filename to be added to each row...
    wInclFileType.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            activeFileType();
          }
        });

    // FileType fieldname ...
    wlFileType = new Label(wAdditionalFields, SWT.RIGHT);
    wlFileType.setText(BaseMessages.getString(PKG, "FileExistsDialog.FileTypeField.Label"));
    PropsUi.setLook(wlFileType);
    FormData fdlFileType = new FormData();
    fdlFileType.left = new FormAttachment(wInclFileType, 2 * margin);
    fdlFileType.top = new FormAttachment(wResult, margin);
    wlFileType.setLayoutData(fdlFileType);

    wFileType = new TextVar(variables, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wFileType.setToolTipText(BaseMessages.getString(PKG, "FileExistsDialog.FileTypeField.Tooltip"));
    PropsUi.setLook(wFileType);
    wFileType.addModifyListener(lsMod);
    FormData fdFileType = new FormData();
    fdFileType.left = new FormAttachment(wlFileType, margin);
    fdFileType.top = new FormAttachment(wResult, margin);
    fdFileType.right = new FormAttachment(100, 0);
    wFileType.setLayoutData(fdFileType);

    FormData fdAdditionalFields = new FormData();
    fdAdditionalFields.left = new FormAttachment(0, margin);
    fdAdditionalFields.top = new FormAttachment(wAddResult, margin);
    fdAdditionalFields.right = new FormAttachment(100, -margin);
    fdAdditionalFields.bottom = new FormAttachment(wOk, -2 * margin);
    wAdditionalFields.setLayoutData(fdAdditionalFields);

    // ///////////////////////////////
    // END OF Additional Fields GROUP //
    // ///////////////////////////////

    getData();
    activeFileType();
    input.setChanged(changed);

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return transformName;
  }

  private void activeFileType() {
    wlFileType.setEnabled(wInclFileType.getSelection());
    wFileType.setEnabled(wInclFileType.getSelection());
  }

  /** Copy information from the meta-data input to the dialog fields. */
  public void getData() {
    if (input.getFilenamefield() != null) {
      wFileName.setText(input.getFilenamefield());
    }
    if (input.getResultfieldname() != null) {
      wResult.setText(input.getResultfieldname());
    }
    wInclFileType.setSelection(input.isIncludefiletype());
    if (input.getFiletypefieldname() != null) {
      wFileType.setText(input.getFiletypefieldname());
    }
    wAddResult.setSelection(input.isAddresultfilenames());

    wTransformName.selectAll();
    wTransformName.setFocus();
  }

  private void cancel() {
    transformName = null;
    input.setChanged(changed);
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }
    input.setFilenamefield(wFileName.getText());
    input.setResultfieldname(wResult.getText());
    input.setIncludefiletype(wInclFileType.getSelection());
    input.setFiletypefieldname(wFileType.getText());
    input.setAddresultfilenames(wAddResult.getSelection());
    transformName = wTransformName.getText(); // return value

    dispose();
  }

  private void get() {
    if (!gotPreviousFields) {
      try {
        String fieldvalue = wFileName.getText();
        wFileName.removeAll();
        IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformName);
        if (r != null) {
          wFileName.setItems(r.getFieldNames());
        }
        if (fieldvalue != null) {
          wFileName.setText(fieldvalue);
        }
        gotPreviousFields = true;
      } catch (HopException ke) {
        new ErrorDialog(
            shell,
            BaseMessages.getString(PKG, "FileExistsDialog.FailedToGetFields.DialogTitle"),
            BaseMessages.getString(PKG, "FileExistsDialog.FailedToGetFields.DialogMessage"),
            ke);
      }
    }
  }
}

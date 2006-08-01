/* 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   08.05.2006 (sieb): created
 */
package org.knime.workbench.editor2.actions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.knime.workbench.ui.KNIMEUIPlugin;

/**
 * JFace implementation of a dialog asking for a node name and its description.
 * 
 * @author Christoph Sieb, University of Konstanz
 */
public class NameDescriptionDialog extends Dialog {
    private String m_initName;

    private String m_initDescription;

    private Text m_nameField;

    private Text m_descriptionField;

    private String m_name;

    private String m_description;

    /**
     * Creates a dialog to ask for the user specified node name and the
     * description.
     * 
     * @param parent the parent shell for this dialog
     * @param nameInit the initial name for the node
     * @param descriptionInit the initial description
     */
    public NameDescriptionDialog(final Shell parent, final String nameInit,
            final String descriptionInit) {
        super(parent);
        this.setShellStyle(SWT.APPLICATION_MODAL);

        m_initName = nameInit;
        m_initDescription = descriptionInit;
    }

    /**
     * Creates the widgets of this dialog.
     * 
     * @see org.eclipse.jface.window.Window
     *      #configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(final Shell newShell) {

        super.configureShell(newShell);
        Image img = KNIMEUIPlugin.getDefault().getImageRegistry().get("knime");
        newShell.setImage(img);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;

        Composite content = new Composite(newShell, SWT.NONE);
        content.setLayout(gridLayout);

        Label nameLabel = new Label(content, SWT.RIGHT);
        nameLabel.setText("User name:");

        GridData nameData = new GridData();
        nameData.minimumWidth = 300;
        nameData.grabExcessHorizontalSpace = true;
        m_nameField = new Text(content, SWT.SINGLE);
        m_nameField.setLayoutData(nameData);
        if (m_initName == null) {
            m_nameField.setText("");
        } else {
            m_nameField.setText(m_initName);
        }

        Label descriptionLabel = new Label(content, SWT.RIGHT);
        descriptionLabel.setText("User description:");

        GridData descrData = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        descrData.minimumHeight = 200;
        descrData.minimumWidth = 300;
        descrData.grabExcessHorizontalSpace = true;
        descrData.grabExcessVerticalSpace = true;
        m_descriptionField = new Text(content, SWT.MULTI | SWT.WRAP
                | SWT.V_SCROLL);
        m_descriptionField.setLayoutData(descrData);
        //m_descriptionField.setSize(500, 300);
        if (m_initDescription == null) {
            m_descriptionField.setText("");
        } else {

            m_descriptionField.setText(m_initDescription);
        }
    }

    /**
     * Linux (GTK) hack: must explicitly invoke <code>getInitialSize()</code>.
     * 
     * @see org.eclipse.jface.window.Window#create()
     */
    @Override
    public void create() {
        super.create();
        String os = System.getProperty("os.name");
        if (os != null && os.toLowerCase().startsWith("linux")) {
            getShell().setSize(getInitialSize());
        }
    }

    /**
     * Invoked by the super class if ok is pressed. Copies the text field
     * content to member variables, so that they can be accessed afterwards.
     * (Neccessary as the widgets will be disposed)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {

        m_name = m_nameField.getText();
        m_description = m_descriptionField.getText();

        // if nothing in the name field confirm the implicit deletion
        if ((m_name == null || m_name.trim().equals(""))
                && (!(m_description == null) && !m_description.trim()
                        .equals(""))) {
            MessageBox mb = new MessageBox(Display.getDefault()
                    .getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO
                    | SWT.CANCEL);
            mb.setText("Empty name field...");
            mb.setMessage("An empty name field deletes the name AND"
                    + " the description!\n Delete?");
            if (mb.open() != SWT.YES) {
                return;
            }
        }

        setReturnCode(OK);
        close();
    }

    /**
     * @return the description input of the dialog.
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * @return the name input of the dialog
     */
    public String getName() {
        return m_name;
    }
}

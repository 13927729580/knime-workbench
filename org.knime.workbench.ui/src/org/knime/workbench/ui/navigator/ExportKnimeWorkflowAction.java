/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 * 
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
 *   20.10.2006 (sieb): created
 */
package org.knime.workbench.ui.navigator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.knime.workbench.repository.ImageRepository;
import org.knime.workbench.ui.wizards.export.WorkflowExportWizard;

/**
 * Action to invoke the knime export wizard.
 * 
 * @author Christoph Sieb, University of Konstanz
 */
public class ExportKnimeWorkflowAction extends Action {

    private static final int SIZING_WIZARD_WIDTH = 470;

    private static final int SIZING_WIZARD_HEIGHT = 550;

    /**
     * The id for this action.
     */
    public static final String ID = "KNIMEExport";

    /**
     * The workbench window; or <code>null</code> if this action has been
     * <code>dispose</code>d.
     */
    private IWorkbenchWindow m_workbenchWindow;

    /**
     * Create a new instance of this class.
     * 
     * @param window the window
     */
    public ExportKnimeWorkflowAction(final IWorkbenchWindow window) {
        super("Export KNIME workflow...");
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.m_workbenchWindow = window;
        setToolTipText("Exports a KNIME workflow to an archive");
        setId(ID); //$NON-NLS-1$
        // window.getWorkbench().getHelpSystem().setHelp(this,
        // IWorkbenchHelpContextIds.IMPORT_ACTION);
        // self-register selection listener (new for 3.0)

    }

    /**
     * Create a new instance of this class.
     * 
     * @param workbench the workbench
     * @deprecated use the constructor
     *             <code>ImportResourcesAction(IWorkbenchWindow)</code>
     */
    public ExportKnimeWorkflowAction(final IWorkbench workbench) {
        this(workbench.getActiveWorkbenchWindow());
    }

    /**
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageRepository.getImageDescriptor("icons/knimeExport.PNG");
    }

    /**
     * Invoke the Import wizards selection Wizard.
     */
    public void run() {
        if (m_workbenchWindow == null) {
            // action has been disposed
            return;
        }

        WorkflowExportWizard wizard = new WorkflowExportWizard();

        IStructuredSelection selectionToPass;
        // get the current workbench selection
        ISelection workbenchSelection =
                m_workbenchWindow.getSelectionService().getSelection();
        if (workbenchSelection instanceof IStructuredSelection) {
            selectionToPass = (IStructuredSelection)workbenchSelection;
        } else {
            selectionToPass = StructuredSelection.EMPTY;
        }

        wizard.init(m_workbenchWindow.getWorkbench(), selectionToPass);

        // wizard.setForcePreviousAndNextButtons(true);

        Shell parent = m_workbenchWindow.getShell();
        WizardDialog dialog = new WizardDialog(parent, wizard);
        dialog.create();
        dialog.getShell().setSize(
                Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x),
                SIZING_WIZARD_HEIGHT);
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
        // IWorkbenchHelpContextIds.IMPORT_WIZARD);
        dialog.open();
    }

    /*
     * (non-Javadoc) Method declared on ActionFactory.IWorkbenchAction.
     * 
     * @since 3.0
     */
    public void dispose() {
        if (m_workbenchWindow == null) {
            // action has already been disposed
            return;
        }

        m_workbenchWindow = null;
    }
}

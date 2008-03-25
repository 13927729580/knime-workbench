/*
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * -------------------------------------------------------------------
 *
 * History
 *   11.01.2007 (sieb): created
 */
package org.knime.workbench.editor2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.UIManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.DefaultNodeProgressMonitor;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.WorkflowManager;
import org.knime.core.node.workflow.WorkflowPersistor.WorkflowLoadResult;

/**
 * A runnable which is used by the {@link WorkflowEditor} to load a workflow
 * with a progress bar. NOTE: As the {@link UIManager} holds a reference to this
 * runnable an own class file is necessary sucht that all references to the
 * created workflow manager can be deleted, otherwise the manager can not be
 * deleted later and the memeory can not be freed.
 *
 * @author Christoph Sieb, University of Konstanz
 */
class LoadWorkflowRunnable extends PersistWorflowRunnable {

    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(LoadWorkflowRunnable.class);

    private WorkflowEditor m_editor;

    private File m_workflowFile;

    public LoadWorkflowRunnable(final WorkflowEditor editor, 
            final File workflowFile) {
        m_editor = editor;
        m_workflowFile = workflowFile;
    }

    public void run(final IProgressMonitor pm) {
        CheckThread checkThread = null;
        // indicates whether to create an empty workflow
        // this is done if the file is empty
        boolean createEmptyWorkflow = false;

        // set the loading canceled variable to false
        m_editor.setLoadingCanceled(false);

        try {

            // create progress monitor
            ProgressHandler progressHandler = new ProgressHandler(pm, 101,
                    "Loading workflow...");
            final DefaultNodeProgressMonitor progressMonitor 
                = new DefaultNodeProgressMonitor();
            progressMonitor.addProgressListener(progressHandler);

            checkThread = new CheckThread(pm, progressMonitor, true);

            checkThread.start();

            final WorkflowLoadResult result = WorkflowManager.load(
                    m_workflowFile, new ExecutionMonitor());
            
            m_editor.setWorkflowManager(result.getWorkflowManager());
            pm.subTask("Finished.");
            pm.done();
            
            if (result.hasErrors()) {
                m_editor.markDirty();
                Display.getDefault().asyncExec(new Runnable() {

                    public void run() {
                        
                        MessageDialog.openError(
                                new Shell(Display.getDefault().getActiveShell()),
                                "Errors during load: ",
                                result.getErrors());
                    }
                    
                });
            }

        } catch (FileNotFoundException fnfe) {
            LOGGER.fatal("File not found", fnfe);
        } catch (IOException ioe) {
            if (m_workflowFile.length() == 0) {
                LOGGER.info("New workflow created.");
                createEmptyWorkflow = true;
            } else {
                LOGGER.error("Could not load workflow from: "
                        + m_workflowFile.getName(), ioe);
            }
        } catch (InvalidSettingsException ise) {
            LOGGER.error("Could not load workflow from: "
                    + m_workflowFile.getName(), ise);
        } catch (CanceledExecutionException cee) {
            LOGGER.info("Canceled loading worflow: " 
                    + m_workflowFile.getName());
            m_editor.setWorkflowManager(null);
            m_editor.setLoadingCanceled(true);
            m_editor.setLoadingCanceledMessage(cee.getMessage());
            /*
        } catch (Exception we) {
            // the workflow exception is a collection exception
            // it is stored to show the errors in a window
//            m_editor.setWorkflowException(we);
 *
 */
        } catch (Exception e) {
            LOGGER.error("Workflow could not be loaded. " + e.getMessage(), e);
            m_editor.setWorkflowManager(null);
        } finally {
            // terminate the check thread
            checkThread.finished();
            // create empty WFM if loading failed

            if (createEmptyWorkflow) {
                // && createEmptyWorkflow.intValue() == 0) {
                m_editor.setWorkflowManager(WorkflowManager.ROOT
                        .createAndAddProject());
                m_editor.setIsDirty(false);
            }

            // IMPORTANT: Remove the reference to the file and the
            // editor!!! Otherwise the memory can not be freed later
            m_editor = null;
            m_workflowFile = null;
        }
    }
}


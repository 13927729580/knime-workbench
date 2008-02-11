/* 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *   25.05.2005 (Florian Georg): created
 */
package org.knime.workbench.editor2.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.NodeContainer;

import org.knime.workbench.editor2.ImageRepository;
import org.knime.workbench.editor2.WorkflowEditor;
import org.knime.workbench.editor2.editparts.NodeContainerEditPart;

/**
 * Action to set the user name and description of a node.
 * 
 * @author Christoph Sieb, University of Konstanz
 */
public class SetNameAndDescriptionAction extends AbstractNodeAction {
    private static final NodeLogger LOGGER =
            NodeLogger.getLogger(SetNameAndDescriptionAction.class);

    /**
     * unique ID for this action.
     */
    public static final String ID = "knime.action.setnameanddescription";

    /**
     * @param editor The workflow editor
     */
    public SetNameAndDescriptionAction(final WorkflowEditor editor) {
        super(editor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return "Node name and description";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageRepository
                .getImageDescriptor("icons/setNameDescription.PNG");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getToolTipText() {
        return "Set/view the user specified node name and a context dependent description.";
    }

    /**
     * @return <code>true</code>, if just one node part is selected.
     * 
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {

        NodeContainerEditPart[] parts = getSelectedNodeParts();

        // only if just one node part is selected
        if (parts.length != 1) {
            return false;
        }

        return true;
    }

    /**
     * Opens a dialog and collects the user name and description. After the
     * dialog is closed the new name and description are set to the node
     * container if applicable.
     * 
     * {@inheritDoc}
     */
    @Override
    public void runOnNodes(final NodeContainerEditPart[] nodeParts) {
        // if more than one node part is selected
        if (nodeParts.length != 1) {
            LOGGER.debug("Execution denied as more than one node is "
                    + "selected. Not allowed in 'Set name and "
                    + "description' action.");
            return;
        }

        final NodeContainer container = nodeParts[0].getNodeContainer();

        LOGGER.debug("Opening 'Set name and description' dialog"
                + " for one node ...");

        // open name and description dialog
        try {
            Shell editorShell = PlatformUI.getWorkbench()
                .getDisplay().getActiveShell();

        Shell parent = new Shell(editorShell, SWT.BORDER
                | SWT.TITLE | SWT.NO_TRIM | SWT.APPLICATION_MODAL);

        String initialName = "Node " + container.getID();
        if (container.getCustomName() != null) {
            initialName = container.getCustomName();
        }
        String initialDescr = "";
        if (container.getCustomDescription() != null) {
            initialDescr = container.getCustomDescription();
        }
        String dialogTitle = container.getNameWithID();
        if (container.getCustomName() != null) {
            dialogTitle += " - " + container.getCustomName();
        }
        NameDescriptionDialog dialog =
            new NameDescriptionDialog(parent, initialName,
                    initialDescr, dialogTitle);

        Point relativeLocation =  new Point(
                nodeParts[0].getFigure().getBounds().x,
                nodeParts[0].getFigure().getBounds().y);

        relativeLocation = editorShell.toDisplay(relativeLocation);
        parent.setLocation(relativeLocation);

        LOGGER.debug("custom description location: "
                + parent.getLocation().x
                + ", " + parent.getLocation().y);

        int result = dialog.open();

        // check if ok was pressed
        if (result == Window.OK) {
            // if the name or description have been changed
            // the editor must be set dirty
            String description = dialog.getDescription();
            String userName = dialog.getName();
            if (userName.trim().equals("")) {

                if (container.getCustomName() != null
                        || container.getCustomDescription() != null) {

                    // mark editor as dirty
                    getEditor().markDirty();
                }
                container.setCustomName(null);
                container.setCustomDescription(null);
            } else {
                // if name or description is different mark editor dirty
                if (!userName.equals(container.getCustomName())
                        || !description.equals(container
                                .getCustomDescription())) {
                    getEditor().markDirty();
                }

                container.setCustomName(userName);
                container.setCustomDescription(description);
            }
            }
        } catch (Throwable t) {
            LOGGER.error("trying to open description editor: ", t);
        }
        // else do nothing
    }
}

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
 *   ${date} (${user}): created
 */
package org.knime.workbench.editor2.editparts.policy;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.knime.core.node.workflow.NodeContainer;

import org.knime.workbench.editor2.commands.ChangeNodeBoundsCommand;

/**
 * Handles manual layout editing for the workflow, that is, creates the commands
 * that can change the visual constraints of the contents.
 * 
 * Only available for XYLayoutManagers, not for automatic layout
 * 
 * @author Florian Georg, University of Konstanz
 */
public class NewWorkflowXYLayoutPolicy extends XYLayoutEditPolicy {
    /**
     * {@inheritDoc}
     */
    @Override
    protected Command createAddCommand(final EditPart child,
            final Object constraint) {
        return null;
    }

    /**
     * Creates command to move / resize <code>NodeContainer</code> components
     * on the project's client area.
     * 
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy
     *      #createChangeConstraintCommand(org.eclipse.gef.EditPart,
     *      java.lang.Object)
     */
    @Override
    protected Command createChangeConstraintCommand(final EditPart child,
            final Object constraint) {

        // only rectangular constraints are supported
        if (!(constraint instanceof Rectangle)) {
            return null;
        }

        // We need a node container model object ...
        if (!(child.getModel() instanceof NodeContainer)) {
            return null;
        }

        NodeContainer container = (NodeContainer) child.getModel();

        // Create a copy of the bounds from the model, and return a command
        // that set it into the visuals
        Rectangle rect = ((Rectangle) constraint).getCopy();
        int[] newBounds = new int[] {rect.x, rect.y, rect.width, rect.height};

        ChangeNodeBoundsCommand command = new ChangeNodeBoundsCommand(
                container, newBounds);

        return command;
    }

    /**
     * @param request The request
     * @return always null
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy
     *      #getCreateCommand(org.eclipse.gef.requests.CreateRequest)
     */
    @Override
    protected Command getCreateCommand(final CreateRequest request) {
        return null;
    }

    /**
     * @param request The request
     * @return always null
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy
     *      #getDeleteDependantCommand(org.eclipse.gef.Request)
     */
    @Override
    protected Command getDeleteDependantCommand(final Request request) {
        return null;
    }
}

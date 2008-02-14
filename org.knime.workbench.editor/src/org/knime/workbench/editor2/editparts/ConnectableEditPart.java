/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   24.01.2008 (Fabian Dill): created
 */
package org.knime.workbench.editor2.editparts;

import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.WorkflowManager;
import org.knime.workbench.editor2.commands.CreateConnectionCommand;

/**
 * Interface for {@link WorkflowRootEditPart} and {@link NodeContainerEditPart}
 * to provide the underlying {@link WorkflowManager}. This is used in the 
 * {@link CreateConnectionCommand} to determine the source or target of the 
 * connection.
 *  
 * @author Fabian Dill, University of Konstanz
 */
public interface ConnectableEditPart {

    /**
     *
     * @return the underlying node container, which should be connected.
     */
    public NodeContainer getNodeContainer();

}

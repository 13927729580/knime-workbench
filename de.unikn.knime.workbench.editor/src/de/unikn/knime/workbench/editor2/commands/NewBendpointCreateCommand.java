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
 *   ${date} (${user}): created
 */
package de.unikn.knime.workbench.editor2.commands;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.ZoomManager;

import de.unikn.knime.core.node.workflow.ConnectionContainer;
import de.unikn.knime.workbench.editor2.WorkflowEditor;
import de.unikn.knime.workbench.editor2.extrainfo.ModellingConnectionExtraInfo;

/**
 * Command for creation of connection bendpoints. The bendpoints are stored in a
 * default implementation of an <code>ExtraInfo</code> object.
 * 
 * @author Florian Georg, University of Konstanz
 */
public class NewBendpointCreateCommand extends Command {
    private Point m_location;

    private int m_index;

    private ModellingConnectionExtraInfo m_extraInfo;

    private AbsoluteBendpoint m_bendpoint;

    private ConnectionContainer m_connection;
    
    private ZoomManager m_zoomManager;

    /**
     * New NewBendpointCreateCommand.
     * 
     * @param connection The connection model
     * @param index bendpoint index
     * @param location where ?
     */
    public NewBendpointCreateCommand(final ConnectionContainer connection,
            final int index, final Point location, ZoomManager zoomManager) {
        m_connection = connection;
        m_extraInfo = (ModellingConnectionExtraInfo) connection.getExtraInfo();
        if (m_extraInfo == null) {
            m_extraInfo = new ModellingConnectionExtraInfo();
        }
        m_index = index;
        m_location = location;
        
        m_zoomManager = zoomManager;
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        Point location = m_location.getCopy();
        WorkflowEditor.adaptZoom(m_zoomManager, location, true);
        
        m_bendpoint = new AbsoluteBendpoint(location);
        m_bendpoint.setLocation(location);
        m_extraInfo.addBendpoint(m_bendpoint.x, m_bendpoint.y, m_index);

        // we need this to fire some update event up
        m_connection.setExtraInfo(m_extraInfo);
    }

    /**
     * @see org.eclipse.gef.commands.Command#redo()
     */
    @Override
    public void redo() {
        Point location = m_location.getCopy();
        WorkflowEditor.adaptZoom(m_zoomManager, location, true);
        
        m_extraInfo.addBendpoint(location.x, location.y, m_index);

        // we need this to fire some update event up
        m_connection.setExtraInfo(m_extraInfo);
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        m_extraInfo.removeBendpoint(m_index);

        // we need this to fire some update event up
        m_connection.setExtraInfo(m_extraInfo);
    }
}

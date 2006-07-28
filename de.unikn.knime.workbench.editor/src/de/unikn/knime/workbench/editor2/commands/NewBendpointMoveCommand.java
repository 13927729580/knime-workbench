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
 * Command for moving an absolute bendpoint on the connection.
 * 
 * @author Florian Georg, University of Konstanz
 */
public class NewBendpointMoveCommand extends Command {
    private Point m_newLocation;

    private Point m_oldLocation;

    private int m_index;

    private ModellingConnectionExtraInfo m_extraInfo;

    //private AbsoluteBendpoint m_bendpoint;

    private ConnectionContainer m_connection;

    private ZoomManager m_zoomManager;

    /**
     * New bendpoint move command.
     * 
     * @param connection The connection model
     * @param index The bendpoint index
     * @param newLocation the new location
     */
    public NewBendpointMoveCommand(final ConnectionContainer connection,
            final int index, final Point newLocation,
            final ZoomManager zoomManager) {
        m_extraInfo = (ModellingConnectionExtraInfo)connection.getExtraInfo();
        m_connection = connection;

        m_index = index;
        m_newLocation = newLocation;
        m_zoomManager = zoomManager;
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        int[] p = m_extraInfo.getBendpoint(m_index);

        AbsoluteBendpoint bendpoint = new AbsoluteBendpoint(p[0], p[1]);
        m_oldLocation = bendpoint.getLocation();
        
        Point newLocation = m_newLocation.getCopy();
        WorkflowEditor.adaptZoom(m_zoomManager, newLocation, true);
        
        bendpoint = new AbsoluteBendpoint(newLocation);

        m_extraInfo.removeBendpoint(m_index);
        m_extraInfo.addBendpoint(bendpoint.x, bendpoint.y, m_index);

        // issue notfication
        m_connection.setExtraInfo(m_extraInfo);
    }

    /**
     * @see org.eclipse.gef.commands.Command#redo()
     */
    @Override
    public void redo() {
        m_extraInfo.removeBendpoint(m_index);
        
        Point newLocation = m_newLocation.getCopy();
        WorkflowEditor.adaptZoom(m_zoomManager, newLocation, true);
        
        m_extraInfo.addBendpoint(newLocation.x, newLocation.y, m_index);

        // issue notfication
        m_connection.setExtraInfo(m_extraInfo);

    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        Point oldLocation = m_oldLocation.getCopy();
        //WorkflowEditor.adaptZoom(m_zoomManager, oldLocation, true);

        m_extraInfo.removeBendpoint(m_index);
        m_extraInfo.addBendpoint(oldLocation.x, oldLocation.y, m_index);

        // issue notfication
        m_connection.setExtraInfo(m_extraInfo);
    }
}

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
 *   Aug 5, 2005 (georg): created
 */
package org.knime.workbench.editor2.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ModelPortObject;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodePort;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.workbench.editor2.ImageRepository;

/**
 * Action to open a port view on a specific out-port.
 *
 * @author Florian Georg, University of Konstanz
 */
public class OpenPortViewAction extends Action {
    private final NodeContainer m_nodeContainer;

    private final int m_index;

    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(OpenPortViewAction.class);

    /**
     * New action to opne view on a port.
     *
     * @param nodeContainer The node
     * @param portIndex The index of the out-port
     */
    public OpenPortViewAction(final NodeContainer nodeContainer,
            final int portIndex) {
        m_nodeContainer = nodeContainer;
        m_index = portIndex;
    }

    protected int getPortIndex() {
        return m_index;
    }

    protected NodeContainer getNodeContainer() {
        return m_nodeContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageRepository.getImageDescriptor("icons/openView.gif");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getToolTipText() {
        return "Opens a view on outport #" + m_index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {

        // the name is constructed like "Model outport <specificIndex>"
        String name;

        if (m_nodeContainer.getOutPort(m_index).getPortType().equals(
                BufferedDataTable.TYPE)) {
            name = "Data Outport " + m_index;
        } else if (m_nodeContainer.getOutPort(m_index).getPortType().equals(
                ModelPortObject.TYPE)){
            name = "Model Outport " + m_index;
        } else {
            name ="Unknown Outport " + m_index;
        }

        // the text will be displayed in the context menu
        // it consists of the specific port type and index and its description
        String description = m_nodeContainer.getOutPort(m_index).getPortName();

        return name + ": " + description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        LOGGER.debug("Open Port View " + m_nodeContainer.getName() + " (#"
                + m_index + ")");
        NodePort port = m_nodeContainer.getOutPort(m_index);
        m_nodeContainer.getOutPort(m_index).openPortView(port.getPortName());
    }
}

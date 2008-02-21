/* ------------------------------------------------------------------
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
 * If you have any quesions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   20.02.2008 (Fabian Dill): created
 */
package org.knime.workbench.editor2.model;

import org.knime.core.node.workflow.WorkflowManager;
import org.knime.workbench.editor2.extrainfo.ModellingNodeExtraInfo;

/**
 * 
 * @author Fabian Dill, University of Konstanz
 */
public class WorkflowPortBar {
    
    private final boolean m_in;
    private final WorkflowManager m_manager;
    private ModellingNodeExtraInfo m_uiInfo;
    
    public WorkflowPortBar(final WorkflowManager manager, final boolean in) {
        m_in = in;
        m_manager = manager;
    }
    
    public boolean isInPortBar() {
        return m_in;
    }
    
    public WorkflowManager getWorkflowManager() {
        return m_manager;
    }
    
    public ModellingNodeExtraInfo getUIInfo() {
        return m_uiInfo;
    }
    
    public void setUIInfo(final ModellingNodeExtraInfo uiInfo) {
        m_uiInfo = uiInfo;
    }

}

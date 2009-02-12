/* 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 *   16.03.2005 (georg): created
 */
package org.knime.workbench.repository.model;

import org.eclipse.swt.graphics.Image;

/**
 * Abstract base class of "leaf" objects (that is, objects without children).
 * 
 * @author Florian Georg, University of Konstanz
 */
public abstract class AbstractSimpleObject extends AbstractRepositoryObject
        implements ISimpleObject {
    
    private Image m_icon;
    
    private String m_categoryPath;
    
    private String m_pluginID;
    /**
     * @return Returns the pluginID.
     */
    public String getPluginID() {
        return m_pluginID;
    }

    /**
     * @param pluginID The pluginID to set.
     */
    public void setPluginID(final String pluginID) {
        m_pluginID = pluginID;
    }
    
    /**
     * @return Returns the icon.
     */
    public Image getIcon() {
        return m_icon;
    }

    /**
     * @param icon The icon to set.
     */
    public void setIcon(final Image icon) {
        m_icon = icon;
    }
    
    /**
     * @return Returns the categoryPath.
     */
    public String getCategoryPath() {
        return m_categoryPath;
    }

    /**
     * @param categoryPath The categoryPath to set.
     */
    public void setCategoryPath(final String categoryPath) {
        m_categoryPath = categoryPath;
    }
    
}

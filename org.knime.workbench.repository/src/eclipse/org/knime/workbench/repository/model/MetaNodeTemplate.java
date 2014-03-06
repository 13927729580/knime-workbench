/* This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright by 
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
 */
package org.knime.workbench.repository.model;

import org.knime.core.node.workflow.WorkflowManager;

/**
 *
 * @author Fabian Dill, University of Konstanz
 */
public class MetaNodeTemplate extends AbstractNodeTemplate {

    private final WorkflowManager m_manager;

    private String m_description;

    /**
     * Creates a new metanode template.
     *
     * @param id the (unique) id of the node template
     * @param name the name
     * @param categoryPath the absolute path of the category in which this template should be placed
     * @param contributingPlugin the contributing plug-in's ID
     * @param manager the metanode's workflow manager
     */
    public MetaNodeTemplate(final String id, final String name,
            final String categoryPath, final String contributingPlugin, final WorkflowManager manager) {
        super(id, name, contributingPlugin);
        m_manager = manager;
        setAfterID("");
        setCategoryPath(categoryPath);
    }

    /**
     * Creates a copy of the given object.
     *
     * @param copy the object to copy
     */
    protected MetaNodeTemplate(final MetaNodeTemplate copy) {
        super(copy);
        this.m_manager = copy.m_manager;
        this.m_description = copy.m_description;
    }

    /**
     * Returns the metanode's workflow manager.
     *
     * @return a workflow manager
     */
    public WorkflowManager getManager() {
        return m_manager;
    }

    @Override
    public String getCategoryPath() {
        if (super.getCategoryPath() != null) {
            return super.getCategoryPath();
        }
        return "/meta";
    }

    /**
     * Returns a description for this metanode template.
     *
     * @return a description, never <code>null</code>
     */
    public String getDescription() {
        if (m_description != null) {
            return m_description;
        }
        return m_manager.getName() + ": " + (m_manager.getCustomDescription() != null ? m_manager
                .getCustomDescription() : "");
    }

    /**
     * Sets a description for this metanode template.
     *
     * @param description a description
     */
    public void setDescription(final String description) {
        /*
         * If we have a description in the extension but no custom description
         * in the meta node -> set description also as custom description If we
         * have a custom description -> add the description found in the
         * extension.
         */
        m_description = description;
        if ((m_manager != null) && (m_manager.getCustomDescription() == null)) {
            m_manager.setCustomDescription(m_description);
        } else if ((m_manager != null) && (m_manager.getCustomDescription() != null)) {
            m_manager.setCustomDescription(m_manager.getCustomDescription()
                    + " " + m_description);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRepositoryObject deepCopy() {
        return new MetaNodeTemplate(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result =
                prime
                        * result
                        + ((m_description == null) ? 0 : m_description
                                .hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MetaNodeTemplate other = (MetaNodeTemplate)obj;
        if (m_description == null) {
            if (other.m_description != null) {
                return false;
            }
        } else if (!m_description.equals(other.m_description)) {
            return false;
        }
        return true;
    }

    /**
    *
    * {@inheritDoc}
    */
   @Override
   public String toString() {
       return m_description;
   }
}

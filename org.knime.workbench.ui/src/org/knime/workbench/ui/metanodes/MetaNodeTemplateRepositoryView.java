/* This source code, its documentation and all appendant files
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
 */
package org.knime.workbench.ui.metanodes;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.knime.core.node.workflow.NodeID;
import org.knime.core.node.workflow.WorkflowManager;

/**
 * The view displaying the meta node templates.
 * 
 * @author Fabian Dill, University of Konstanz
 */
public class MetaNodeTemplateRepositoryView extends ViewPart {

    private ListViewer m_viewer;
    private MetaNodeTemplateRepositoryManager m_manager;

    private static MetaNodeTemplateRepositoryView instance;
    
    
    /**
     * 
     * @return singleton instance with the 
     *  {@link MetaNodeTemplateRepositoryManager} loaded and set
     */
    public static MetaNodeTemplateRepositoryView getInstance() {
        if (instance == null) {
            instance = new MetaNodeTemplateRepositoryView();
        }
        return instance;
    }
    
    /**
     * Creates the {@link MetaNodeTemplateRepositoryManager} already loaded.
     */
    public MetaNodeTemplateRepositoryView() {
        m_manager = MetaNodeTemplateRepositoryManager.getInstance();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void createPartControl(final Composite parent) {
        m_viewer = new ListViewer(parent,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        
        this.getSite().setSelectionProvider(m_viewer);
        Transfer[] transfers = new Transfer[]{
                LocalSelectionTransfer.getTransfer()};
        m_viewer.addDragSupport(DND.DROP_COPY, transfers,
                new MetaNodeTemplateRepositoryDragSource(this));
        // content provider (which reads from repository)
        m_viewer.setContentProvider(new IStructuredContentProvider() {
            

            @Override
            public Object[] getElements(final Object inputElement) {
                return m_manager.getTemplates().toArray(
                        new MetaNodeTemplateRepositoryItem[m_manager
                                                           .getTemplates()
                                                           .size()]);
            }

            @Override
            public void dispose() {
                m_manager.save();
            }

            @Override
            public void inputChanged(final Viewer viewer, 
                    final Object oldInput, final Object newInput) {
                m_manager = (MetaNodeTemplateRepositoryManager)newInput;
            }
            
        });
        // set the input
        m_viewer.setInput(m_manager);

        m_viewer.setLabelProvider(new LabelProvider() {
            @Override
            public Image getImage(final Object element) {
                return super.getImage(element);
            }
            
            @Override
            public String getText(final Object element) {
                if (!(element instanceof MetaNodeTemplateRepositoryItem)) {
                    return super.getText(element);
                }
                return ((MetaNodeTemplateRepositoryItem)element).getName();
            }
        });
        instance = this;
    }
    
    /**
     * Forwards request to the 
     * {@link MetaNodeTemplateRepositoryManager#createMetaNodeTemplate(
     * String, WorkflowManager, NodeID)}.
     * 
     * @param name name of the template
     * @param source {@link WorkflowManager} to copy the meta node from
     * @param nodes the id of meta node 
     */
    public void createMetaNodeTemplate(final String name, 
            final WorkflowManager source, final NodeID[] nodes) {
        m_manager.createMetaNodeTemplate(name, source, nodes[0]);
        m_viewer.refresh();
    }
    
    /**
     * 
     * @param item item to be deleted from the 
     * {@link MetaNodeTemplateRepositoryManager}
     */
    public void deleteTemplate(final MetaNodeTemplateRepositoryItem item) {
        m_manager.removeItem(item);
        m_viewer.refresh();
    }
    
    /**
     * 
     * @param name new name of a meta node template
     * @return true if the name is already used, false otherwise
     */
    public boolean isNameUnique(final String name) {
        for (MetaNodeTemplateRepositoryItem item : m_manager.getTemplates()) {
            if (item.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFocus() {
        
    }
    
    /**
     * 
     * @return the current seleted {@link MetaNodeTemplateRepositoryItem}
     */
    public IStructuredSelection getSelection() {
        return (IStructuredSelection)m_viewer.getSelection();
    }
    
    /**
     * Saves the state of the {@link MetaNodeTemplateRepositoryManager}.
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        m_manager.save();
        super.dispose();
    }

}

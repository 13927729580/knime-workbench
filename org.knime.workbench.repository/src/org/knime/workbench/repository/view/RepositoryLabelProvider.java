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
 *   16.03.2005 (georg): created
 */
package org.knime.workbench.repository.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import org.knime.workbench.repository.model.Category;
import org.knime.workbench.repository.model.NodeTemplate;

/**
 * LabelProvider, provides Text and images for viewers that display the
 * repository model (categories, nodes, ...).
 * 
 * @author Florian Georg, University of Konstanz
 */
public class RepositoryLabelProvider extends LabelProvider {
    /**
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(final Object element) {
        if (element instanceof NodeTemplate) {
            return ((NodeTemplate) element).getName();
        }
        if (element instanceof Category) {
            return ((Category) element).getName();
        }
        return super.getText(element);
    }

    /**
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(final Object element) {
        if (element instanceof NodeTemplate) {
            return ((NodeTemplate) element).getIcon();
        }
        if (element instanceof Category) {
            return ((Category) element).getIcon();
        }

        return super.getImage(element);
    }
}

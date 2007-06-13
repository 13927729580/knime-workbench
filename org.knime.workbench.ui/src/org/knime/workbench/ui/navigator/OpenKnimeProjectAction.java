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
 *   21.07.2006 (sieb): created
 */
package org.knime.workbench.ui.navigator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Action to open a knime project.
 * 
 * @author Christoph Sieb, University of Konstanz
 */
public class OpenKnimeProjectAction extends Action {
    private KnimeResourceNavigator m_knimeResourceNavigator;

    /**
     * Creates a new action that will open editors on the then-selected knime
     * project.
     * 
     * @param knimeNavigator the navigator hosting the projects and dealing with
     *            the editor opening
     */
    public OpenKnimeProjectAction(final KnimeResourceNavigator knimeNavigator) {
        m_knimeResourceNavigator = knimeNavigator;
        IStructuredSelection selection = (StructuredSelection)m_knimeResourceNavigator
                .getViewer().getSelection();
        int numberSelections = selection.size();
        String actionText;
        if (numberSelections <= 1) {
            actionText = "Open Knime workflow";
            if (numberSelections < 1) {
                setEnabled(false);
            }
        } else {
            actionText = "Open " + numberSelections + " Knime workflows";
        }
        setText(actionText);
    }

    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    @Override
    public void run() {
        IStructuredSelection selection = (StructuredSelection)m_knimeResourceNavigator
                .getViewer().getSelection();

        m_knimeResourceNavigator.handleOpen(new OpenEvent(
                m_knimeResourceNavigator.getViewer(), selection));
    }
}

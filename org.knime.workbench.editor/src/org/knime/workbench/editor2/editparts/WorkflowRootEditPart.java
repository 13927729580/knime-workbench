/*
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
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
 * -------------------------------------------------------------------
 *
 * History
 *   26.05.2005 (Florian Georg): created
 */
package org.knime.workbench.editor2.editparts;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.swt.widgets.Shell;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.WorkflowEvent;
import org.knime.core.node.workflow.WorkflowListener;
import org.knime.core.node.workflow.WorkflowManager;
import org.knime.workbench.editor2.editparts.policy.NewWorkflowContainerEditPolicy;
import org.knime.workbench.editor2.editparts.policy.NewWorkflowXYLayoutPolicy;
import org.knime.workbench.editor2.editparts.snap.SnapToPortGeometry;
import org.knime.workbench.editor2.figures.ProgressToolTipHelper;
import org.knime.workbench.editor2.figures.WorkflowFigure;
import org.knime.workbench.editor2.figures.WorkflowLayout;

/**
 * Root controller for the <code>WorkflowManager</code> model object. Consider
 * this as the controller for the "background" of the editor. It always has a
 * <code>WorkflowManager</code> as its model object.
 * 
 * Model: {@link WorkflowManager}
 * 
 *
 * @author Florian Georg, University of Konstanz
 */
public class WorkflowRootEditPart extends AbstractWorkflowEditPart implements
        WorkflowListener, CommandStackListener, ConnectableEditPart {
    private static final NodeLogger LOGGER =
            NodeLogger.getLogger(WorkflowRootEditPart.class);

    private ProgressToolTipHelper m_toolTipHelper;

    /**
     * @return The <code>WorkflowManager</code> that is used as model for this
     *         edit part
     */
    public WorkflowManager getWorkflowManager() {
        return (WorkflowManager)getModel();
    }


    /**
     *
     * {@inheritDoc}
     */
    public NodeContainer getNodeContainer() {
        return getWorkflowManager();
    }

    /**
     * Returns the model chidlren, that is, the <code>NodeConatiner</code>s
     * that are stored in the workflow manager.
     *
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected List getModelChildren() {
        List modelChildren = new ArrayList();
        WorkflowManager wfm = getWorkflowManager();
        modelChildren.addAll(wfm.getNodeContainers());
        // create my own gui port objects to determine whether
        // they are normal ports or workflow ports

        // TODO: don't create new objects!
        // create mapping from port id to workflow port proxy
        for (int i = 0; i < wfm.getNrWorkflowIncomingPorts(); i++) {
            modelChildren.add(wfm.getInPort(i));
        }
        for (int i = 0; i < wfm.getNrWorkflowOutgoingPorts(); i++) {
            modelChildren.add(wfm.getOutPort(i));
        }
        return modelChildren;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(final Class adapter) {
        if (adapter == SnapToHelper.class) {
            List<SnapToHelper> snapStrategies = new ArrayList<SnapToHelper>();
            Boolean val =
                    (Boolean)getViewer().getProperty(
                            RulerProvider.PROPERTY_RULER_VISIBILITY);
            if (false || val != null && val.booleanValue()) {
                snapStrategies.add(new SnapToGuides(this));
            }
            val =
                    (Boolean)getViewer().getProperty(
                            SnapToPortGeometry.PROPERTY_SNAP_ENABLED);
            if (true || val != null && val.booleanValue()) {
                snapStrategies.add(new SnapToPortGeometry(this));
            }
            val =
                    (Boolean)getViewer().getProperty(
                            SnapToGrid.PROPERTY_GRID_ENABLED);
            if (false || val != null && val.booleanValue()) {
                snapStrategies.add(new SnapToGrid(this));
            }

            if (snapStrategies.size() == 0) {
                return null;
            }
            if (snapStrategies.size() == 1) {
                return snapStrategies.get(0);
            }

            SnapToHelper[] ss = new SnapToHelper[snapStrategies.size()];
            for (int i = 0; i < snapStrategies.size(); i++) {
                ss[i] = snapStrategies.get(i);
            }
            return new CompoundSnapToHelper(ss);
        }
        return super.getAdapter(adapter);
    }

    /**
     * Activate controller, register as workflow listener.
     *
     * {@inheritDoc}
     */
    @Override
    public void activate() {
        super.activate();
        LOGGER.debug("WorkflowRootEditPart activated. Figure: " + getFigure());

        // register as listener on model object
        getWorkflowManager().addListener(this);

        // add as listener on the command stack
        getViewer().getEditDomain().getCommandStack().addCommandStackListener(
                this);

    }

    /**
     * Deactivate controller.
     *
     * {@inheritDoc}
     */
    @Override
    public void deactivate() {
        super.deactivate();
        LOGGER.debug("WorkflowRootEditPart deactivated");
        for (Object o : getChildren()) {
            EditPart editPart = (EditPart)o;
            editPart.deactivate();
        }
        getWorkflowManager().removeListener(this);
        getViewer().getEditDomain().getCommandStack()
                .removeCommandStackListener(this);
    }

    /**
     * Creates the root(="background") figure and sets the appropriate lazout
     * manager.
     *
     * {@inheritDoc}
     */
    @Override
    protected IFigure createFigure() {

        WorkflowFigure backgroundFigure = new WorkflowFigure();

        LayoutManager l = new WorkflowLayout();
        backgroundFigure.setLayoutManager(l);

        return backgroundFigure;
    }

    /**
     * This installes the edit policies for the root EditPart:
     * <ul>
     * <li><code>EditPolicy.CONTAINER_ROLE</code> - this serves as a
     * container for nodes</li>
     * <li><code>EditPolicy.LAYOUT_ROLE</code> - this edit part a layout that
     * allows children to be moved</li>.
     * </ul>
     *
     * {@inheritDoc}
     */
    @Override
    protected void createEditPolicies() {

        // install the CONTAINER_ROLE
        installEditPolicy(EditPolicy.CONTAINER_ROLE,
                new NewWorkflowContainerEditPolicy());

        // install the LAYOUT_ROLE
        installEditPolicy(EditPolicy.LAYOUT_ROLE,
                new NewWorkflowXYLayoutPolicy());

    }

    /**
     * Controller is getting notified about model changes. This invokes
     * <code>refreshChildren</code> keep in sync with the model.
     *
     * {@inheritDoc}
     */
    public void workflowChanged(final WorkflowEvent event) {
        LOGGER.debug("WorkflowRoot: workflow changed, refreshing "
                + "children/connections..");

        // refreshing the children
        refreshChildren();

        // refresing connections
        refreshSourceConnections();
        refreshTargetConnections();

        // update out port (workflow in port) tooltips
        for (Object part : getChildren()) {

            if (part instanceof NodeOutPortEditPart
                    || part instanceof WorkflowInPortEditPart) {
                AbstractPortEditPart outPortPart =
                        (AbstractPortEditPart)part;
                outPortPart.rebuildTooltip();
            }
        }

        // always refresh visuals
        getFigure().revalidate();
        refreshVisuals();

    }

    /**
     * {@inheritDoc}
     */
    public void commandStackChanged(final EventObject event) {
        LOGGER.debug("WorkflowRoot: command stack changed");

    }

    /**
     * @return the tooltip helper for this workflow part
     */
    public ProgressToolTipHelper getToolTipHelper() {
        return m_toolTipHelper;
    }

    /**
     *
     * @param underlyingShell underlying shell
     */
    public void createToolTipHelper(final Shell underlyingShell) {
        // create a tooltip helper for all child figures
        ZoomManager zoomManager = (ZoomManager)(getRoot().getViewer()
                .getProperty(ZoomManager.class.toString()));
        m_toolTipHelper = new ProgressToolTipHelper(getViewer().getControl(),
                zoomManager);
        ((WorkflowFigure)getFigure()).setProgressToolTipHelper(m_toolTipHelper);
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * -------------------------------------------------------------------
 *
 * History
 *   25.05.2005 (Florian Georg): created
 */
package org.knime.workbench.editor2;

import java.util.List;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.workflow.LoopEndNode;
import org.knime.core.node.workflow.MetaNodeTemplateInformation.Role;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.SingleNodeContainer;
import org.knime.core.node.workflow.WorkflowManager;
import org.knime.workbench.editor2.actions.AbstractNodeAction;
import org.knime.workbench.editor2.actions.AddAnnotationAction;
import org.knime.workbench.editor2.actions.CancelAction;
import org.knime.workbench.editor2.actions.ChangeMetaNodeLinkAction;
import org.knime.workbench.editor2.actions.CheckUpdateMetaNodeLinkAction;
import org.knime.workbench.editor2.actions.CollapseMetaNodeAction;
import org.knime.workbench.editor2.actions.ConvertMetaNodeToSubNodeAction;
import org.knime.workbench.editor2.actions.DisconnectMetaNodeLinkAction;
import org.knime.workbench.editor2.actions.ExecuteAction;
import org.knime.workbench.editor2.actions.ExecuteAndOpenViewAction;
import org.knime.workbench.editor2.actions.ExpandMetaNodeAction;
import org.knime.workbench.editor2.actions.LockMetaNodeAction;
import org.knime.workbench.editor2.actions.MetaNodeReconfigureAction;
import org.knime.workbench.editor2.actions.OpenDialogAction;
import org.knime.workbench.editor2.actions.OpenInteractiveViewAction;
import org.knime.workbench.editor2.actions.OpenPortViewAction;
import org.knime.workbench.editor2.actions.OpenSubworkflowEditorAction;
import org.knime.workbench.editor2.actions.OpenViewAction;
import org.knime.workbench.editor2.actions.OpenWorkflowPortViewAction;
import org.knime.workbench.editor2.actions.PasteActionContextMenu;
import org.knime.workbench.editor2.actions.PauseLoopExecutionAction;
import org.knime.workbench.editor2.actions.ResetAction;
import org.knime.workbench.editor2.actions.ResumeLoopAction;
import org.knime.workbench.editor2.actions.RevealMetaNodeTemplateAction;
import org.knime.workbench.editor2.actions.SaveAsMetaNodeTemplateAction;
import org.knime.workbench.editor2.actions.SelectLoopAction;
import org.knime.workbench.editor2.actions.SetNodeDescriptionAction;
import org.knime.workbench.editor2.actions.StepLoopAction;
import org.knime.workbench.editor2.actions.ToggleFlowVarPortsAction;
import org.knime.workbench.editor2.editparts.NodeContainerEditPart;
import org.knime.workbench.editor2.editparts.WorkflowInPortBarEditPart;
import org.knime.workbench.editor2.editparts.WorkflowInPortEditPart;
import org.knime.workbench.editor2.model.WorkflowPortBar;


/**
 * Provider for the Workflow editor's context menus.
 *
 * @author Florian Georg, University of Konstanz
 * @author Christoph Sieb, University of Konstanz
 */
public class WorkflowContextMenuProvider extends ContextMenuProvider {

    private final ActionRegistry m_actionRegistry;

    private final GraphicalViewer m_viewer;

    // it's final, but the content changes each time the menu opens
    private final Point m_lastLocation = new Point(0, 0);

    /**
     * Creates a new context menu provider, that is, registers some actions from
     * the action registry.
     *
     * @param actionRegistry The action registry of the editor
     * @param viewer The graphical viewer
     */
    public WorkflowContextMenuProvider(final ActionRegistry actionRegistry,
            final GraphicalViewer viewer) {
        super(viewer);
        m_viewer = viewer;
        assert actionRegistry != null : "WorkflowContextMenuProvider "
                + "needs an action registry !";

        m_actionRegistry = actionRegistry;
        m_viewer.getControl().addMenuDetectListener(new MenuDetectListener() {
            @Override
            public void menuDetected(final MenuDetectEvent e) {
                Point pt = m_viewer.getControl().toControl(e.x, e.y);
                m_lastLocation.x = pt.x;
                m_lastLocation.y = pt.y;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildContextMenu(final IMenuManager manager) {

        final String FLOW_VAR_PORT_GRP = "Flow Variable Ports";

        // add the groups (grouped by separators) in their order first
        manager.add(new Separator(IWorkbenchActionConstants.GROUP_APP));
        manager.add(new Separator(FLOW_VAR_PORT_GRP));
        GEFActionConstants.addStandardActionGroups(manager);

        IAction action;

        action = m_actionRegistry.getAction("cut");
        manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
        ((UpdateAction)action).update();

        action = m_actionRegistry.getAction("copy");
        manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
        ((UpdateAction)action).update();

        action = m_actionRegistry.getAction(PasteActionContextMenu.ID);
        manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
        ((UpdateAction)action).update();

        action = m_actionRegistry.getAction("undo");
        manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
        ((UpdateAction)action).update();

        action = m_actionRegistry.getAction("redo");
        manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
        ((UpdateAction)action).update();

        action = m_actionRegistry.getAction("delete");
        manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
        ((UpdateAction)action).update();

        // Add (some) available actions from the registry to the context menu
        // manager

        // openDialog
        action = m_actionRegistry.getAction(OpenDialogAction.ID);
        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
        ((AbstractNodeAction)action).update();
        // execute
        action = m_actionRegistry.getAction(ExecuteAction.ID);
        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
        ((AbstractNodeAction)action).update();
        // execute and open first view
        action = m_actionRegistry.getAction(ExecuteAndOpenViewAction.ID);
        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
        ((AbstractNodeAction)action).update();
        // cancel execution
        action = m_actionRegistry.getAction(CancelAction.ID);
        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
        ((AbstractNodeAction)action).update();
        // show some menu items on LoopEndNodes only
        List parts = m_viewer.getSelectedEditParts();
        if (parts.size() == 1) {
            EditPart p = (EditPart)parts.get(0);
            if (p instanceof NodeContainerEditPart) {
                NodeContainer container =
                        (NodeContainer)((NodeContainerEditPart)p).getModel();
                if (container instanceof SingleNodeContainer) {
                    SingleNodeContainer snc = (SingleNodeContainer)container;
                    if (snc.isModelCompatibleTo(LoopEndNode.class)) {
                        // pause loop execution
                        action = m_actionRegistry.getAction(PauseLoopExecutionAction.ID);
                        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                        ((AbstractNodeAction)action).update();
                        // step loop execution
                        action = m_actionRegistry.getAction(StepLoopAction.ID);
                        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                        ((AbstractNodeAction)action).update();
                        // resume loop execution
                        action = m_actionRegistry.getAction(ResumeLoopAction.ID);
                        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                        ((AbstractNodeAction)action).update();
                    }
                }
            }
        }
        // reset
        action = m_actionRegistry.getAction(ResetAction.ID);
        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
        ((AbstractNodeAction)action).update();
        // set name and description
        action = m_actionRegistry.getAction(SetNodeDescriptionAction.ID);
        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
        ((AbstractNodeAction)action).update();
        // add workflow annotation
        action = m_actionRegistry.getAction(AddAnnotationAction.ID);
        AddAnnotationAction aaa = (AddAnnotationAction)action;
        aaa.setLocation(m_lastLocation.x, m_lastLocation.y);
        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
        ((AbstractNodeAction)action).update();

        // collapse meta nodes
        action = m_actionRegistry.getAction(CollapseMetaNodeAction.ID);
        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
        ((AbstractNodeAction)action).update();
        // expand meta nodes
        action = m_actionRegistry.getAction(ExpandMetaNodeAction.ID);
        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
        ((AbstractNodeAction)action).update();
        // convert meta node to subnode
        action = m_actionRegistry.getAction(ConvertMetaNodeToSubNodeAction.ID);
        manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
        ((AbstractNodeAction)action).update();
        // insert "select loop" if loop nodes are selected
        boolean addSelectLoop = true;
        for (Object p : parts) {
            if (!(p instanceof NodeContainerEditPart)) {
                addSelectLoop = false;
                break;
            }
            NodeContainer nc = ((NodeContainerEditPart)p).getNodeContainer();
            if (!(nc instanceof SingleNodeContainer)) {
                addSelectLoop = false;
                break;
            }
            if (!((SingleNodeContainer)nc).isMemberOfScope()) {
                addSelectLoop = false;
                break;
            }
        }
        if (addSelectLoop) {
            action = m_actionRegistry.getAction(SelectLoopAction.ID);
            manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
            ((AbstractNodeAction)action).update();
        }

        // depending on the current selection: add the actions for the port
        // views and the node views
        // also check whether this node part is a meta-node
        // if so offer the "edit meta-node" option
        // all these feature are only offered if exactly 1 part is selected
        parts = m_viewer.getSelectedEditParts();
        // by now, we only support one part...
        if (parts.size() == 1) {
            EditPart p = (EditPart)parts.get(0);
            if (p instanceof WorkflowInPortBarEditPart) {
                WorkflowInPortBarEditPart root = (WorkflowInPortBarEditPart)p;
                manager.add(new Separator("outPortViews"));
                for (Object o : p.getChildren()) {
                    EditPart child = (EditPart)o;
                    if (child instanceof WorkflowInPortEditPart
                            && ((WorkflowInPortEditPart)child).isSelected()) {
                        final WorkflowManager wm = ((WorkflowPortBar)root.getModel()).getWorkflowManager();
                        action = new OpenWorkflowPortViewAction(wm,
                            ((WorkflowInPortEditPart)child).getIndex(), wm.getNrInPorts());
                        manager.appendToGroup("outPortViews", action);
                        ((WorkflowInPortEditPart)child).setSelected(false);
                    }
                }
            }
            if (p instanceof NodeContainerEditPart) {

                NodeContainer container = null;
                container =
                        (NodeContainer)((NodeContainerEditPart)p).getModel();

                if (!(container instanceof WorkflowManager)) {
                    action = m_actionRegistry.getAction(
                            ToggleFlowVarPortsAction.ID);
                    manager.appendToGroup(FLOW_VAR_PORT_GRP, action);
                    ((AbstractNodeAction)action).update();
                }

                // add for node views option if applicable
                int numNodeViews = container.getNrViews();
                for (int i = 0; i < numNodeViews; i++) {
                    action = new OpenViewAction(container, i);
                    manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP,
                            action);
                }

                // add interactive view
                if (container.hasInteractiveView() || container.hasInteractiveWebView()) {
                    action = new OpenInteractiveViewAction(container);
                    manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                }

                if (container instanceof WorkflowManager) {
                    action = new OpenSubworkflowEditorAction((NodeContainerEditPart)p);
                    manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                    if (parts.size() == 1) {
                        if (Role.Link.equals(((WorkflowManager)container).getTemplateInformation().getRole())) {
                            action = m_actionRegistry.getAction(ChangeMetaNodeLinkAction.ID);
                            manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                            ((AbstractNodeAction)action).update();
                        } else {
                            action = m_actionRegistry.getAction(MetaNodeReconfigureAction.ID);
                            manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                            ((AbstractNodeAction)action).update();
                        }
                    }
                }

                // add port views
                manager.add(new Separator("outPortViews"));

                int numOutPorts = container.getNrOutPorts();
                for (int i = 0; i < numOutPorts; i++) {
                    if (i == 0 && !(container instanceof WorkflowManager)) {
                        // skip the implicit flow var ports on "normal" nodes
                        continue;
                    }
                    action = new OpenPortViewAction(container, i, numOutPorts);
                    manager.appendToGroup("outPortViews", action);
                }

            }
        }

        boolean addMetaNodeActions = false;
        boolean addMetaNodeLinkActions = false;
        for (Object p : parts) {
            if (p instanceof NodeContainerEditPart) {
                NodeContainer model =
                    ((NodeContainerEditPart)p).getNodeContainer();
                if (model instanceof WorkflowManager) {
                    addMetaNodeActions = true;
                    if (Role.Link.equals(((WorkflowManager)model).getTemplateInformation().getRole())) {
                        addMetaNodeLinkActions = true;
                    }
                }
            }
        }
        if (addMetaNodeActions) {
            action = m_actionRegistry.getAction(SaveAsMetaNodeTemplateAction.ID);
            manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
            ((AbstractNodeAction)action).update();
            if (addMetaNodeLinkActions) {
                action = m_actionRegistry.getAction(CheckUpdateMetaNodeLinkAction.ID);
                manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                ((AbstractNodeAction)action).update();
                action = m_actionRegistry.getAction(RevealMetaNodeTemplateAction.ID);
                manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                ((AbstractNodeAction)action).update();
                action = m_actionRegistry.getAction(DisconnectMetaNodeLinkAction.ID);
                manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                ((AbstractNodeAction)action).update();
            }

            if (Boolean.getBoolean(KNIMEConstants.PROPERTY_SHOW_METANODE_LOCK_ACTION)) {
                action = m_actionRegistry.getAction(LockMetaNodeAction.ID);
                manager.appendToGroup(IWorkbenchActionConstants.GROUP_APP, action);
                ((AbstractNodeAction)action).update();
            }

        }

        manager.updateAll(true);
    }


}

/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - GraphicalEditor GEF class (some parts taken for use here)
 *     Alex Bradley    - remainder of initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.ui;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.gef.ui.properties.UndoablePropertySheetPage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.actions.CheckedRedoAction;
import org.eclipselabs.collage.actions.CollageLayersActionRegistry;
import org.eclipselabs.collage.actions.ExportLayersAction;
import org.eclipselabs.collage.actions.ImportLayersAction;
import org.eclipselabs.collage.actions.LayerCreateAction;
import org.eclipselabs.collage.actions.LayerToggleVisibleAction;
import org.eclipselabs.collage.model.ResourceShapeList;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.parts.LayerComponentEditPolicy;
import org.eclipselabs.collage.parts.tree.CollageLayerTreeEditPart;
import org.eclipselabs.collage.parts.tree.ResourceShapeListTreeEditPart;
import org.eclipselabs.collage.parts.tree.ShapeTreeEditPart;
import org.eclipselabs.collage.parts.tree.ShapesTreeEditPartFactory;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Tree viewer for collage layers.
 * @author hudsonr
 * @author Alex Bradley
 */
public class CollageLayersTreeView extends ViewPart implements CommandStackListener, ISelectionChangedListener, MouseListener {
	public static final String VIEW_ID = "org.eclipselabs.collage.collageLayers";
	
	private SelectionAction visibleAction;
	private Action addAction;
		
	private List<String> selectionActions = new ArrayList<String>();
	private List<String> stackActions = new ArrayList<String>();

	private TreeViewer treeViewer;
	private EditDomain editDomain = new EditDomain();

	@Override
	public void createPartControl(Composite parent) {
		treeViewer = new TreeViewer();
		treeViewer.createControl(parent);
		editDomain.addViewer(treeViewer);
		treeViewer.setEditPartFactory(new ShapesTreeEditPartFactory());
		treeViewer.setContents(CollageActivator.getDefault().getDefaultCollageRoot());
		// TODO Use a SelectionSynchronizer?
		getSite().setSelectionProvider(treeViewer);
		treeViewer.addSelectionChangedListener(this);
		treeViewer.getControl().addMouseListener(this);
		
		createToolbar();
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getCommandStack().addCommandStackListener(this);
		initializeActionRegistry();
	}


	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}
	
	@Override
	public void dispose() {
		getCommandStack().removeCommandStackListener(this);
//		getSite().getWorkbenchWindow().getSelectionService()
//				.removeSelectionListener(this);
		getEditDomain().setActiveTool(null);
		getActionRegistry().dispose();
		super.dispose();
	}

	/**
	 * Returns the adapter for the specified key.
	 * 
	 * <P>
	 * <EM>IMPORTANT</EM> certain requests, such as the property sheet, may be
	 * made before or after {@link #createPartControl(Composite)} is called. The
	 * order is unspecified by the Workbench.
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
		if (type == IPropertySheetPage.class) {
			return new UndoablePropertySheetPage(getCommandStack(),
					getActionRegistry().getAction(ActionFactory.UNDO.getId()),
					getActionRegistry().getAction(ActionFactory.REDO.getId()));
		}
		if (type == TreeViewer.class)
			return treeViewer;
		if (type == CommandStack.class)
			return getCommandStack();
		if (type == ActionRegistry.class)
			return getActionRegistry();
		if (type == EditPart.class && treeViewer != null)
			return treeViewer.getContents();
		if (type == IFigure.class && treeViewer != null)
			return ((GraphicalEditPart) treeViewer.getRootEditPart())
					.getFigure();
		return super.getAdapter(type);
	}

	/**
	 * Lazily creates and returns the action registry.
	 * 
	 * @return the action registry
	 */
	protected ActionRegistry getActionRegistry() {
		return CollageLayersActionRegistry.getDefault();
	}

	/**
	 * Returns the command stack.
	 * 
	 * @return the command stack
	 */
	protected CommandStack getCommandStack() {
		return getEditDomain().getCommandStack();
	}

	/**
	 * Returns the edit domain.
	 * 
	 * @return the edit domain
	 */
	protected EditDomain getEditDomain() {
		return editDomain;
	}

	/**
	 * Returns the list of <em>IDs</em> of Actions that are dependent on changes
	 * in the workbench's {@link ISelectionService}. The associated Actions can
	 * be found in the action registry. Such actions should implement the
	 * {@link UpdateAction} interface so that they can be updated in response to
	 * selection changes.
	 * 
	 * @see #updateActions(List)
	 * @return the list of selection-dependent action IDs
	 */
	protected List<String> getSelectionActions() {
		return selectionActions;
	}

	/**
	 * Returns the list of <em>IDs</em> of Actions that are dependant on the
	 * CommmandStack's state. The associated Actions can be found in the action
	 * registry. These actions should implement the {@link UpdateAction}
	 * interface so that they can be updated in response to command stack
	 * changes. An example is the "undo" action.
	 * 
	 * @return the list of stack-dependant action IDs
	 */
	protected List<String> getStackActions() {
		return stackActions;
	}

	/**
	 * Initializes the ActionRegistry. This registry may be used by
	 * {@link ActionBarContributor ActionBarContributors} and/or
	 * {@link ContextMenuProvider ContextMenuProviders}.
	 * <P>
	 * This method may be called on Editor creation, or lazily the first time
	 * {@link #getActionRegistry()} is called.
	 */
	protected void initializeActionRegistry() {
		createActions();
		installGlobalActions();
		updateActions(stackActions);
	}

	/**
	 * Creates actions for this editor. Subclasses should override this method
	 * to create and register actions with the {@link ActionRegistry}.
	 */
	protected void createActions() {
		ActionRegistry registry = getActionRegistry();
		IAction action;

		action = new UndoAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new CheckedRedoAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new SelectAllAction(this);
		registry.registerAction(action);

		action = new DeleteAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new ExportLayersAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new ImportLayersAction(this);
		registry.registerAction(action);
		
		// Not in registry - don't register as global actions
        visibleAction = new LayerToggleVisibleAction(this);
		addAction = new LayerCreateAction(this);
	}
	
	protected void installGlobalActions () {
		IActionBars actionBars = getViewSite().getActionBars();
		Iterator<?> iter = getActionRegistry().getActions();
		while (iter.hasNext()) {
			IAction action = (IAction)iter.next();
			actionBars.setGlobalActionHandler(action.getId(), action);
		}
		actionBars.updateActionBars();
	}

	private void createToolbar() {
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(getActionRegistry().getAction(ActionFactory.IMPORT.getId()));
        mgr.add(getActionRegistry().getAction(ActionFactory.EXPORT.getId()));
        mgr.add(new Separator());
        mgr.add(visibleAction);
        mgr.add(addAction);
        mgr.add(getActionRegistry().getAction(ActionFactory.DELETE.getId()));
	}

	/**
	 * When the command stack changes, the actions interested in the command
	 * stack are updated.
	 * 
	 * @param event
	 *            the change event
	 */
	@Override
	public void commandStackChanged(EventObject event) {
		updateActions(stackActions);
		updateActions(selectionActions);
		visibleAction.update();
		CollageUIRegistry.getDefault().updateAllUndoRedo();
	}
	
	/**
	 * A convenience method for updating a set of actions defined by the given
	 * List of action IDs. The actions are found by looking up the ID in the
	 * {@link #getActionRegistry() action registry}. If the corresponding action
	 * is an {@link UpdateAction}, it will have its <code>update()</code> method
	 * called.
	 * 
	 * @param actionIds
	 *            the list of IDs to update
	 */
	protected void updateActions(List<String> actionIds) {
		ActionRegistry registry = getActionRegistry();
		Iterator<String> iter = actionIds.iterator();
		while (iter.hasNext()) {
			IAction action = registry.getAction(iter.next());
			if (action instanceof UpdateAction)
				((UpdateAction) action).update();
		}
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updateActions(selectionActions);
		visibleAction.update();
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// Double clicking an item opens an editor for it (if applicable).
		TreeItem item = ((Tree)treeViewer.getControl()).getItem(new Point(e.x, e.y));
		if (item != null) {
			Object obj = item.getData();
			if (obj != null) {
				try {
					if (obj instanceof ResourceShapeListTreeEditPart) {
						((ResourceShapeListTreeEditPart)obj).getCastedModel().getResource().openInEditor();
					} else if (obj instanceof ShapeTreeEditPart) {
						Shape shape = ((ShapeTreeEditPart)obj).getCastedModel();
						IEditorPart editor = ((ResourceShapeList)shape.getParent()).getResource().openInEditor();
						if (editor != null) {
							shape.getBoundaries().showInEditor(editor);
						}
					}
				} catch (CoreException e1) {
					CollageUtilities.showError(CollageActivator.PLUGIN_NAME, "Opening editor for selected item failed. See stack trace for details.");
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	public void mouseDown(MouseEvent e) { }

	@Override
	public void mouseUp(MouseEvent e) {	
		// Clicking on a layer's "eye icon" area toggles its visibility.
		Point clickPoint = new Point(e.x, e.y);
		TreeItem item = ((Tree)treeViewer.getControl()).getItem(clickPoint);
		if (item != null) {
			Object obj = item.getData();
			if (obj != null && obj instanceof CollageLayerTreeEditPart) {
				CollageLayerTreeEditPart editPart = (CollageLayerTreeEditPart)obj;
				if (item.getImageBounds(0).contains(clickPoint)) {
					Command command = editPart.getCommand(new Request(LayerComponentEditPolicy.REQ_TOGGLE_VISIBLE));
					if (command != null && command.canExecute()) {
						// Turn off direct editing for this click.
						editPart.setDirectEditEnabled(false);
						editDomain.getCommandStack().execute(command);
						return;
					}
				}
				// This click didn't toggle visibility, so allow it to trigger direct editing.
				editPart.setDirectEditEnabled(true);
			}
		}
	}
}

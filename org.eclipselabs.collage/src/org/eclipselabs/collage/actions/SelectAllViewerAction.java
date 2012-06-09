/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Bradley    - adapted for use without IWorkbenchPart
 *******************************************************************************/
package org.eclipselabs.collage.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionFactory;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.internal.GEFMessages;

/**
 * An action which selects all edit parts in the supplied {@link GraphicalViewer}.
 * Adapted from {@link org.eclipse.gef.ui.actions.SelectAllAction}; unfortunately,
 * {@code SelectAllAction} relied too strongly on having an {@code IWorkbenchPart}
 * that could be adapted to a {@link GraphicalViewer}, so it was easier to clone and
 * alter than to adapt through inheritance.
 */
@SuppressWarnings("restriction")
public class SelectAllViewerAction extends Action {

	private GraphicalViewer viewer;

	/**
	 * Constructs a <code>SelectAllAction</code> and associates it with the
	 * given part.
	 * 
	 * @param part
	 *            The workbench part associated with this SelectAllAction
	 */
	public SelectAllViewerAction(GraphicalViewer viewer) {
		this.viewer = viewer;
		setText(GEFMessages.SelectAllAction_Label);
		setToolTipText(GEFMessages.SelectAllAction_Tooltip);
		setId(ActionFactory.SELECT_ALL.getId());
	}

	/**
	 * Selects all edit parts in the active workbench part.
	 */
	public void run() {
		if (viewer != null) {
			viewer.setSelection(new StructuredSelection(
					getSelectableEditParts(viewer)));
		}
	}

	/**
	 * Retrieves edit parts which can be selected
	 * 
	 * @param viewer
	 *            from which the edit parts are to be retrieved
	 * @return list of selectable EditParts
	 * @since 3.5
	 */
	private static List<EditPart> getSelectableEditParts(GraphicalViewer viewer) {
		List<EditPart> selectableChildren = new ArrayList<EditPart>();
		findSelectableChildren(viewer.getContents(), selectableChildren);
		return selectableChildren;
	}
	
	/**
	 * Helper function which searches an edit part's tree of children for selectable children.
	 * @param editPart Edit part to search.
	 * @param selectableChildren Output list to fill with selectable children.
	 */
	private static void findSelectableChildren (EditPart editPart, List<EditPart> selectableChildren) {
		for (Iterator<?> iter = editPart.getChildren().iterator(); iter.hasNext();) {
			Object child = iter.next();
			if (child instanceof EditPart) {
				EditPart childPart = (EditPart) child;
				if (childPart.isSelectable() == true) {
					selectableChildren.add(childPart);
				}
				findSelectableChildren(childPart, selectableChildren);
			}
		}
	}
}

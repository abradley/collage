/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.actions;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.parts.LayerComponentEditPolicy;

/**
 * Action to toggle the visibility of a Collage layer.
 * @author Alex Bradley
 */
public class LayerToggleVisibleAction extends SelectionAction {
	public LayerToggleVisibleAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		Command cmd = createToggleCommand(getSelectedObjects());
		return (cmd != null && cmd.canExecute());
	}

	protected Command createToggleCommand(List<?> objects) {
		// Based on similar method in org.eclipse.gef.ui.actions.DeleteAction
		if (objects.isEmpty())
			return null;
		if (!(objects.get(0) instanceof EditPart))
			return null;

		GroupRequest toggleReq = new GroupRequest(LayerComponentEditPolicy.REQ_TOGGLE_VISIBLE);
		toggleReq.setEditParts(objects);

		CompoundCommand compoundCmd = new CompoundCommand("show/hide layers");
		for (int i = 0; i < objects.size(); i++) {
			EditPart object = (EditPart) objects.get(i);
			Command cmd = object.getCommand(toggleReq);
			if (cmd != null)
				compoundCmd.add(cmd);
		}

		return compoundCmd;
	}
	
	@Override
	protected void init() {
		super.init();
		setText("Show/Hide Layer(s)");
		setImageDescriptor(CollageActivator.getImageDescriptor(CollageActivator.LAYER_VISIBLE_ICON));
		setEnabled(false);
	}

	@Override
	public void run() {
		execute(createToggleCommand(getSelectedObjects()));
	}
}

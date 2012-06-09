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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.model.CollageLayer;

/**
 * Action to create a Collage layer. 
 * @author Alex Bradley
 */
public class LayerCreateAction extends WorkbenchPartAction {
	public LayerCreateAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		Command cmd = makeCreateCommand();
		return (cmd != null && cmd.canExecute());
	}

	private Command makeCreateCommand () {
		Object obj = getWorkbenchPart().getAdapter(EditPart.class);
		if (obj instanceof EditPart) {
			CreateRequest request = new CreateRequest();
			request.setFactory(new SimpleFactory(CollageLayer.class));
			return ((EditPart)obj).getCommand(request);
		}
		return null;
	}
	
	@Override
	protected void init() {
		super.init();
		setText("New Layer");
		setImageDescriptor(CollageActivator.getImageDescriptor(CollageActivator.LAYER_NEW_ICON));
		setEnabled(false);
	}

	@Override
	public void run() {
		execute(makeCreateCommand());
	}
}

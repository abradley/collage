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
 *     Alex Bradley    - adapted for use with Collage tree viewer
 *******************************************************************************/
package org.eclipselabs.collage.parts.tree;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipselabs.collage.model.CollageLayer;
import org.eclipselabs.collage.model.commands.LayerRenameCommand;

/**
 * Direct edit policy for a Collage layer.
 * @author IBM Corporation
 * @author Alex Bradley
 */
public class CollageLayerDirectEditPolicy extends TreeDirectEditPolicy {
	@Override
	protected Command getDirectEditCommand(DirectEditRequest edit) {
		String labelText = (String) edit.getCellEditor().getValue();
		CollageLayerTreeEditPart editPart = (CollageLayerTreeEditPart)getHost();
		return new LayerRenameCommand((CollageLayer)editPart.getModel(), labelText);
	}

	@Override
	protected void showCurrentEditValue(DirectEditRequest request) {
		String value = (String) request.getCellEditor().getValue();
		CollageLayerTreeEditPart editPart = (CollageLayerTreeEditPart)getHost();
		((TreeItem)editPart.getWidget()).setText(value);
	}
}

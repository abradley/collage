/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.text.tools;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipselabs.collage.colourpicker.tools.ColouredShapeCreationTool;
import org.eclipselabs.collage.text.model.TextNoteShape;
import org.eclipselabs.collage.text.parts.TextNoteEditPart;

/**
 * Create a text note shape.
 * @author Alex Bradley
 */
public class TextShapeCreationTool extends ColouredShapeCreationTool {
	private static final Dimension MINIMUM_SIZE = new Dimension(100, 70);

	public TextShapeCreationTool () {
		super(new SimpleFactory(TextNoteShape.class));
		setUnloadWhenFinished(true);
	}
	
	@Override
	protected void performCreation(int button) {
		super.performCreation(button);
		EditPartViewer viewer = getCurrentViewer();
		Object model = getCreateRequest().getNewObject();
		if (model == null || viewer == null) {
			return;
		}
		Object editpart = viewer.getEditPartRegistry().get(model);
		if (editpart instanceof TextNoteEditPart) {
			((TextNoteEditPart)editpart).performRequest(new DirectEditRequest());
		}
	}

	@Override
	protected void updateTargetRequest() {
		super.updateTargetRequest();
		CreateRequest req = getCreateRequest();
		if (isInState(STATE_DRAG_IN_PROGRESS) && MINIMUM_SIZE.contains(req.getSize())) {
			req.setSize(MINIMUM_SIZE);
		}
	}
}

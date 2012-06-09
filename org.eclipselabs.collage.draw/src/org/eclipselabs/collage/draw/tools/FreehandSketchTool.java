/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.draw.tools;

import org.eclipse.gef.Request;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipselabs.collage.draw.DrawActivator;
import org.eclipselabs.collage.draw.model.FreehandSketchShape;
import org.eclipselabs.collage.draw.requests.FreehandSketchCreateRequest;

/**
 * Creation tool for freehand sketches.
 * @author Alex Bradley
 */
public class FreehandSketchTool extends VariableLineWidthShapeCreationTool {
	public FreehandSketchTool () {
		super(new SimpleFactory(FreehandSketchShape.class));
		
		setDefaultCursor(DrawActivator.getDefault().getPencilCursor());
	}
	
	@Override
	protected Request createTargetRequest() {
		FreehandSketchCreateRequest request = new FreehandSketchCreateRequest();
		request.setFactory(getFactory());
		return request;
	}

	@Override
	protected String getDebugName() {
		return "Freehand Sketching Tool";
	}

	@Override
	protected void updateTargetRequest() {
		if (isInState(STATE_DRAG_IN_PROGRESS)) {
			FreehandSketchCreateRequest createRequest = getSketchCreateRequest();
			createRequest.addPoint(getLocation());
			createRequest.setSnapToEnabled(false);
			enforceConstraintsForSizeOnDropCreate(createRequest);
		} else {
			super.updateTargetRequest();
		}
	}
	
	private FreehandSketchCreateRequest getSketchCreateRequest () {
		return (FreehandSketchCreateRequest)getCreateRequest();
	}
}

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
package org.eclipselabs.collage.draw.tools;

import org.eclipse.gef.requests.CreationFactory;
import org.eclipselabs.collage.colourpicker.tools.ColouredShapeCreationTool;
import org.eclipselabs.collage.draw.DrawActivator;
import org.eclipselabs.collage.draw.model.VariableLineWidthShape;

/**
 * Tool for creating shapes with a variable line width. Sets line width based on Collage UI property value
 * when user clicks to begin creating the shape.
 * @author Alex Bradley
 */
public class VariableLineWidthShapeCreationTool extends
		ColouredShapeCreationTool {
	public VariableLineWidthShapeCreationTool() {
		super();
	}

	public VariableLineWidthShapeCreationTool(CreationFactory aFactory) {
		super(aFactory);
	}

	@Override
	protected boolean handleButtonDown(int button) {
		((VariableLineWidthShape)getCreateRequest().getNewObject()).setLineWidth(DrawActivator.getLineWidth(getCollageUI()));
		return super.handleButtonDown(button);
	}
}

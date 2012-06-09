/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.draw.tools;

import org.eclipse.gef.requests.SimpleFactory;
import org.eclipselabs.collage.draw.model.EllipticalShape;

/**
 * Create an ellipse.
 * @author Alex Bradley
 */
public class EllipticalShapeCreationTool extends
		VariableLineWidthShapeCreationTool {
	public EllipticalShapeCreationTool () {
		super(new SimpleFactory(EllipticalShape.class));
	}
}

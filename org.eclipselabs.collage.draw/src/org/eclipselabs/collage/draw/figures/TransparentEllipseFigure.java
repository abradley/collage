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
package org.eclipselabs.collage.draw.figures;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;

/**
 * An ellipse without fill.
 * @author Alex Bradley
 */
public class TransparentEllipseFigure extends Ellipse {
	@Override
	protected void fillShape(Graphics graphics) {
		// Don't fill
	}
}

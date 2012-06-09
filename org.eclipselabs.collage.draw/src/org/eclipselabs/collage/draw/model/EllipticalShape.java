/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation
 *    Alex Bradley    - adapted for use in Collage draw plugin
 *******************************************************************************/
package org.eclipselabs.collage.draw.model;

import javax.xml.bind.annotation.XmlType;

import org.eclipse.swt.graphics.Image;
import org.eclipselabs.collage.draw.DrawActivator;

/**
 * An elliptical shape.
 * 
 * @author Alex Bradley
 * @author Elias Volanakis
 */
@XmlType(name="ellipse")
public class EllipticalShape extends VariableLineWidthShape {
	@Override
	public Image getIcon() {
		return DrawActivator.getImage(DrawActivator.ELLIPSE_ICON);
	}

	@Override
	public String toString() {
		return "Ellipse (" + getShapeBoundariesDescription() + ")";
	}
}

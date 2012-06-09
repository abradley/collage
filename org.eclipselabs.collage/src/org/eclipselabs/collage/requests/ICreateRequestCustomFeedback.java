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
package org.eclipselabs.collage.requests;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;

/**
 * CreateRequest that provides custom graphical feedback during creation.
 * @author Alex Bradley
 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy
 * @see org.eclipselabs.collage.parts.ResourceShapeListEditPart#ShapesXYLayoutEditPolicy
 */
public interface ICreateRequestCustomFeedback {
	/**
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#createSizeOnDropFeedback
	 */
	public IFigure createSizeOnDropFeedback();
	
	/**
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getSizeOnDropFeedback
	 */
	public IFigure updateSizeOnDropFeedback(IFigure feedback);
	
	/**
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreationFeedbackOffset
	 */
	public Insets getCreationFeedbackOffset();
}

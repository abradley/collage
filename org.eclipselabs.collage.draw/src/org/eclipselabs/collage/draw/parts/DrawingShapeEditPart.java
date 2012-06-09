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
package org.eclipselabs.collage.draw.parts;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineShape;
import org.eclipse.swt.SWT;
import org.eclipselabs.collage.colourpicker.model.ColouredShape;
import org.eclipselabs.collage.draw.figures.PaddedPolylineShape;
import org.eclipselabs.collage.draw.figures.TransparentEllipseFigure;
import org.eclipselabs.collage.draw.figures.TransparentRectangleFigure;
import org.eclipselabs.collage.draw.model.EllipticalShape;
import org.eclipselabs.collage.draw.model.FreehandSketchShape;
import org.eclipselabs.collage.draw.model.RectangularShape;
import org.eclipselabs.collage.draw.model.VariableLineWidthShape;
import org.eclipselabs.collage.parts.ShapeEditPart;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * EditPart for draw plugin's shapes.
 * @author Alex Bradley
 */
public class DrawingShapeEditPart extends ShapeEditPart {
	@Override
	protected VariableLineWidthShape getCastedModel() {
		return (VariableLineWidthShape)getModel();
	}

	@Override
	protected IFigure createFigureForModel() {
		IFigure result;
		int lineWidth = getCastedModel().getLineWidth();
		if (getModel() instanceof EllipticalShape) {
			TransparentEllipseFigure fig = new TransparentEllipseFigure();
			fig.setLineWidth(lineWidth);
			fig.setAntialias(SWT.ON);
			result = fig;
		} else if (getModel() instanceof RectangularShape) {
			TransparentRectangleFigure fig = new TransparentRectangleFigure();
			fig.setLineWidth(lineWidth);
			result = fig;
		} else if (getModel() instanceof FreehandSketchShape) {
			PolylineShape polyline = new PaddedPolylineShape(true);
			polyline.setLineWidth(lineWidth);
			polyline.setAntialias(SWT.ON);
			polyline.setPoints(((FreehandSketchShape)getModel()).getPoints());
			result = polyline;
		} else {
			return super.createFigureForModel();
		}
		result.setForegroundColor(CollageUtilities.getColor(getCastedModel().getColour()));
		return result;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		String prop = evt.getPropertyName();

		if (ColouredShape.COLOUR_PROP.equals(prop)) {
			getFigure().setForegroundColor(CollageUtilities.getColor(getCastedModel().getColour()));
		}
		
		if (VariableLineWidthShape.LINE_WIDTH_PROP.equals(prop)) {
			IFigure fig = getFigure();
			if (fig instanceof org.eclipse.draw2d.Shape) {
				((org.eclipse.draw2d.Shape)fig).setLineWidth(getCastedModel().getLineWidth());
			}
			if (getModel() instanceof FreehandSketchShape) {
				refreshVisuals();
			}
		}
	}
}

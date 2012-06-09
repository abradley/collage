/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.draw.requests;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineShape;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipselabs.collage.colourpicker.model.ColouredShape;
import org.eclipselabs.collage.draw.figures.PaddedPolylineShape;
import org.eclipselabs.collage.draw.model.FreehandSketchShape;
import org.eclipselabs.collage.draw.model.VariableLineWidthShape;
import org.eclipselabs.collage.requests.ICreateRequestCustomFeedback;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Creation request for a freehand sketch. The sketch-in-progress is stored in a {@link PointList}
 * containing points in their original "absolute" canvas coordinates; {@code getPoints()} will return
 * the point list translated into "relative" coordinates (i.e., location = (0, 0)).   
 * @author Alex Bradley
 */
public class FreehandSketchCreateRequest extends CreateRequest implements ICreateRequestCustomFeedback {
	private PointList points = new PointList();
	
	/**
	 * Indicate the location of this request if no points have been entered.
	 */
	private Point fallbackLocation = new Point(0, 0);
	
	protected RGB getColour() {
		return ((ColouredShape)getNewObject()).getColour();
	}
	
	protected int getLineWidth() {
		return ((VariableLineWidthShape)getNewObject()).getLineWidth();
	}
	
	/**
	 * Return the point list for this sketch translated to "relative" coordinates.
	 * @return point list translated into "relative" coordinates (i.e., location = (0, 0))
	 */
	public PointList getPoints() {
		PointList copy = points.getCopy();
		Rectangle bounds = copy.getBounds();
		copy.translate(bounds.getLocation().negate());
		return copy;
	}
	
	/**
	 * Add a single point to this sketch.
	 * @param point
	 */
	public void addPoint (Point point) {
		points.addPoint(point);
	}
	
	@Override
	public Point getLocation() {
		if (points.size() == 0) {
			return fallbackLocation.getCopy();
		} else {
			return getBounds().getLocation();
		}
	}
	
	@Override
	public Dimension getSize() {
		if (points.size() == 0) {
			return null;
		} else {
			return getBounds().getSize();
		}
	}
	
	@Override
	public void setLocation(Point location) {
		if (points.size() == 0) {
			fallbackLocation = location;
		}
	}
	
	@Override
	public void setSize(Dimension size) {
		if (size == null) {
			points.removeAllPoints();
		}
	}
	
	@Override
	public Object getNewObject() {
		Object newObject = super.getNewObject();
		if (newObject instanceof FreehandSketchShape) {
			((FreehandSketchShape)newObject).setPoints(getPoints());
		}
		return newObject;
	}

	private Rectangle getBounds () {
		if (points.size() == 1) {
			return new Rectangle(points.getFirstPoint().x, points.getFirstPoint().y, getLineWidth(), getLineWidth());
		}
		return points.getBounds().getCopy();
	}

	@Override
	public IFigure createSizeOnDropFeedback() {
		PolylineShape result = new PaddedPolylineShape(false);
		result.setPoints(getPoints());
		result.setForegroundColor(CollageUtilities.getColor(getColour()));
		result.setLineWidth(getLineWidth());
		result.setAntialias(SWT.ON);
		return result;
	}

	@Override
	public IFigure updateSizeOnDropFeedback(IFigure feedback) {
		if (feedback != null && feedback instanceof PolylineShape) {
			((PolylineShape)feedback).setPoints(getPoints());
		}
		return feedback;
	}

	@Override
	public Insets getCreationFeedbackOffset() {
		return new Insets();
	}
}

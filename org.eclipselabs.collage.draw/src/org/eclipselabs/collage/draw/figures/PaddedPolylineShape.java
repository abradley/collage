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
package org.eclipselabs.collage.draw.figures;

import java.util.Arrays;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolylineShape;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * <p>A {@link PolylineShape} with the following extensions:</p>
 * <ul>
 * <li>bounds padded to accommodate the width of the line drawn</li> 
 * <li>support for scaling on resize</li>
 * <li>support for lines with only one point</li>
 * </ul>
 * @author Alex Bradley
 */
public class PaddedPolylineShape extends PolylineShape {
	/** True iff the polyline should scale on resize. */
	private final boolean scalingOn;
	
	/** Cached copy of the result of {@link #getPoints()}. */
	private PointList cachedPoints;
	/** Cached copy of scaled points. */
	private PointList cachedScaledPoints;
	/** Cached copy of the result of {@link #getBounds()}. */
	private Rectangle cachedBounds;
	/** Cached copy of padded bounds. */
	private Rectangle cachedExpandedBounds;
	
	/**
	 * Create a padded polyline shape.
	 * @param scalingOn True if resizing the bounds should scale the polyline.
	 */
	public PaddedPolylineShape(boolean scalingOn) {
		super();
		this.scalingOn = scalingOn;
	}
	
	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.pushState();
		int expand = getExpansion();
		graphics.translate(getLocation().translate(expand, expand));
		if (getPoints().size() == 1) {
			graphics.setBackgroundColor(graphics.getForegroundColor());
			graphics.fillOval(bounds.getTranslated(bounds.getTopLeft().negate()));
		} else {
			graphics.drawPolyline(scalingOn ? getScaledPoints() : getPoints());
		}
		graphics.popState();
	}

	/**
	 * @return Amount by which to pad bounds.
	 */
	private int getExpansion () {
		// Chosen based on Polyline.getBounds()
		return (int) (getLineWidthFloat() / 2.0f);
	}
	
	private PointList getScaledPoints () {
		checkForUpdates();
		return cachedScaledPoints;
	}
	
	@Override
	public Rectangle getBounds() {
		checkForUpdates();
		return cachedExpandedBounds;
	}
	
	private void checkForUpdates () {
		Rectangle origBounds = super.getBounds();
		if (cachedBounds == null || !cachedBounds.equals(origBounds) ||
		    (scalingOn && (cachedPoints == null || !Arrays.equals(getPoints().toIntArray(), cachedPoints.toIntArray())))) {
			// Update cached bounds.
			cachedBounds = origBounds.getCopy();
			int expand = getExpansion();
			cachedExpandedBounds = cachedBounds.getExpanded(expand, expand);

			if (scalingOn) {
				// Update cached points.
				cachedPoints = getPoints().getCopy();
				cachedScaledPoints = scalePointList(cachedPoints.getCopy(), origBounds.getSize());
			}
		}
	}
	
	/**
	 * Scale a sketch (point list) to the given size.
	 * @param pointList List of points in the sketch <b>(may be modified by this method)</b>
	 * @param newSize Size to which sketch should be scaled
	 */
	private static PointList scalePointList (PointList pointList, Dimension newSize) {
		Dimension origSize = pointList.getBounds().getSize();
		
		if (!origSize.equals(newSize)) {
			double widthFactor = (origSize.width() == 0) ? 1.0 : (newSize.preciseWidth() / origSize.preciseWidth());
			double heightFactor = (origSize.height() == 0) ? 1.0 : (newSize.preciseHeight() / origSize.preciseHeight());

			// PointList represents a list of points as an int array [x1, y1, x2, y2, ..., xn, yn]
			// toIntArray returns the actual array by reference so we can modify it directly.
			int[] points = pointList.toIntArray();
			for (int i = 0; i < points.length; i++) {
				points[i] = (int) Math.floor(points[i] * ( (i%2 == 0) ? widthFactor : heightFactor ) );
			}
		}
		
		return pointList;
	}
}

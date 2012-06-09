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
package org.eclipselabs.collage.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.text.ITextViewer;
import org.eclipselabs.collage.util.CollageUtilities;
import org.eclipselabs.collage.xml.adapters.PointAdapter;

/**
 * Specifies a point in a text editor canvas relative to the top left pixel of a file line.
 * 
 * @author Alex Bradley
 */
public class FileLinePoint {
	private int line = 1;
	private Point offsets = new Point(0, 0);

	/**
	 * Create a FileLinePoint with line = 1 and offsets = (0, 0). 
	 */
	public FileLinePoint () { }
	
	/**
	 * Create a new file-line relative point.
	 * @param line 1-based line in a file (if value supplied is < 1, line will be set to 1)
	 * @param offsets {@link Point} giving x and y offsets from the top left pixel of the file line
	 *  (the x and y values should be >= 0; negative values will be clamped to 0)
	 */
	public FileLinePoint(int line, Point offsets) {
		setLine(line);
		setOffsets(offsets);
	}
	
	/**
	 * Create a new file-line relative point based on a GEF {@link Point} in a Collage drawing area installed
	 * on an {@link ITextViewer}.
	 * @param viewer Text viewer on which Collage UI is installed
	 * @param point Point in Collage drawing area in current state of text viewer
	 */
	public FileLinePoint (ITextViewer viewer, Point point) {
		int newLine = CollageUtilities.getDocumentLineAtPoint(viewer, point);
		setLine(newLine);
		setOffsets(point.getTranslated(0, -CollageUtilities.getCurrentTopPixelForDocumentLine(viewer, newLine)));
	}

	/**
	 * Get a copy of this object that can be safely modified without changing the original.
	 * @return Copy of this file-line relative point.
	 */
	public FileLinePoint getCopy () {
		return new FileLinePoint(line, offsets.getCopy());
	}
	
	/**
	 * Get the file line number
	 * @return 1-based line number
	 */
	@XmlAttribute
	public int getLine() {
		return line;
	}
	
	/**
	 * Set the file line number.
	 * @param line 1-based line in a file (if value supplied is < 1, line will be set to 1)
	 */
	public void setLine(int line) {
		this.line = Math.max(1, line);
	}
	
	/**
	 * Get the x and y offsets that show where in the file line this point is located.
	 * @return non-negative offsets as a {@link Point}
	 */
	@XmlAttribute
	@XmlJavaTypeAdapter(PointAdapter.class)
	public Point getOffsets() {
		return offsets.getCopy();
	}
	
	/**
	 * Set the x and y offsets that show where in the file line this point is located.
	 * @param offsets {@link Point} giving x and y offsets from the top left pixel of the file line
	 *  (the x and y values should be >= 0; negative values will be clamped to 0)
	 */
	public void setOffsets(Point offsets) {
		this.offsets = new Point(Math.max(0, offsets.x()), Math.max(0, offsets.y()));
	}
	
	/**
	 * Convert this file-line relative point to a point in the canvas of a given text viewer (with origin
	 * at the top left corner of the first line in the text viewer's document.) The y-value of the resulting
	 * point depends on the current projection state of the text viewer. 
	 * @param textViewer text viewer for which to compute a corresponding point
	 * @return point in {@code textViewer} corresponding to this file-line relative point
	 */
	public Point toTextViewerPoint (ITextViewer textViewer) {
		return new Point(offsets.x(), offsets.y() + CollageUtilities.getCurrentTopPixelForDocumentLine(textViewer, line));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + line;
		result = prime * result + ((offsets == null) ? 0 : offsets.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || !(obj instanceof FileLinePoint))
			return false;
		FileLinePoint other = (FileLinePoint) obj;
		return (line == other.line && offsets.equals(other.offsets));
	}

	@Override
	public String toString() {
		return "FileLinePoint [line=" + line + ", offsets=" + CollageUtilities.pointToString(offsets)
				+ "]";
	}
}

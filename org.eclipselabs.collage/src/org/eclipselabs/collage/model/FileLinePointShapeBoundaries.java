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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Shape boundary specified by top left and bottom right {@link FileLinePoint}s.
 * @author Alex Bradley
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class FileLinePointShapeBoundaries extends ShapeBoundaries {
	/**
	 * Check that new value of boundary property is of the expected type and doesn't result in
	 * nonsensical bounds.
	 */
	private class BoundaryValidator implements ICellEditorValidator {
		private final String property;
		
		BoundaryValidator (String property) {
			this.property = property;
		}
		
		@Override
		public String isValid(Object value) {
			FileLinePoint newTopLeft = topLeft.getCopy();
			FileLinePoint newBottomRight = bottomRight.getCopy();
			
			try {
				if (TOP_LINE_PROP.equals(property)) {
					newTopLeft.setLine(CollageUtilities.getPositiveInteger((String)value));
				} else if (TOP_OFFSET_PROP.equals(property)) {
					newTopLeft.setOffsets(CollageUtilities.stringToPoint((String)value));
				} else if (BOTTOM_LINE_PROP.equals(property)) {
					newBottomRight.setLine(CollageUtilities.getPositiveInteger((String)value));
				} else if (BOTTOM_OFFSET_PROP.equals(property)) {
					newBottomRight.setOffsets(CollageUtilities.stringToPoint((String)value));
				}

				if (newTopLeft.getLine() > newBottomRight.getLine()) {
					return "Top line must be less than or equal to bottom line";
				}
				if (newTopLeft.getOffsets().x() >= newBottomRight.getOffsets().x()) {
					return "Left x-offset must be less than right x-offset"; 
				}
				if (newTopLeft.getLine() == newBottomRight.getLine() && 
						newTopLeft.getOffsets().y() >= newBottomRight.getOffsets().y()) {
					return "Top y-offset must be less than bottom y-offset";
				}
				
				return null; // Valid
			} catch (IllegalArgumentException e) {
				return e.getMessage();
			}
		}
	}
	
	/** Top line property (used by property descriptor) */
	private static final String TOP_LINE_PROP = "Shape.topLine";
	/** Top offset property (used by property descriptor) */
	private static final String TOP_OFFSET_PROP = "Shape.topOffset";
	/** Bottom line property (used by property descriptor) */
	private static final String BOTTOM_LINE_PROP = "Shape.bottomLine";
	/** Bottom offset property (used by property descriptor) */
	private static final String BOTTOM_OFFSET_PROP = "Shape.bottomOffset";
	
	private transient final IPropertyDescriptor[] DESCRIPTORS = new IPropertyDescriptor[] {
		makePropertyDescriptor(TOP_LINE_PROP, "Top line", ""),
		makePropertyDescriptor(TOP_OFFSET_PROP, "Top left corner", 
				"Top left corner of shape (specified relative to top left corner of top line)"),
		makePropertyDescriptor(BOTTOM_LINE_PROP, "Bottom line", ""),
		makePropertyDescriptor(BOTTOM_OFFSET_PROP, "Bottom right corner", 
				"Bottom right corner of shape (specified relative to top left corner of bottom line)")};
	
	/** Top left corner of shape. */
	private FileLinePoint topLeft = new FileLinePoint(1, new Point(0, 0));
	/** Bottom right corner of shape. */
	private FileLinePoint bottomRight = new FileLinePoint(1, new Point(10, 10));

	@Override
	public ShapeBoundaries getCopy() {
		FileLinePointShapeBoundaries copy = new FileLinePointShapeBoundaries();
		copy.topLeft = topLeft.getCopy();
		copy.bottomRight = bottomRight.getCopy();
		return copy;
	}

	@Override
	public Rectangle toGEFConstraint(ITextViewer viewer) {
		Point topLeftPoint = topLeft.toTextViewerPoint(viewer);
		Point bottomRightPoint = bottomRight.toTextViewerPoint(viewer);
		// Note: avoiding Rectangle(Point, Point), as the rectangle it creates is 1 pixel too large
		// in both directions. See also: https://bugs.eclipse.org/bugs/show_bug.cgi?id=175163
		return new Rectangle(topLeftPoint, bottomRightPoint.getDifference(topLeftPoint));
	}

	@Override
	public void setFromGEFConstraint(ITextViewer viewer, Rectangle bounds) {
		topLeft = new FileLinePoint(viewer, bounds.getTopLeft());
		bottomRight = new FileLinePoint(viewer, bounds.getBottomRight());
	}
	
	@Override
	public ShapeBoundaries handleDocumentChange(DocumentChange change) {
		if (change.getStartLine() <= topLeft.getLine() && bottomRight.getLine() < change.getOldLastLine()
				&& change.getDocumentEvent().getText().isEmpty()) {
			return null; // delete shape
		}
		
		if (change.getLineDelta() != 0 && change.getStartLine() < bottomRight.getLine()) {
			int newTopLine = transformLine(topLeft.getLine(), change);
			int newBottomLine = transformLine(bottomRight.getLine(), change);

			// Special case: moving up shapes when some of the text underneath them has been deleted.
			// This adjustment to the top line gives better-looking results.
			if (change.getStartLine() == change.getNewLastLine() && newTopLine == change.getStartLine() &&
					topLeft.getLine() < change.getOldLastLine()) {
				newTopLine--;
			}

			if (newTopLine != topLeft.getLine() || newBottomLine != bottomRight.getLine()) {
				FileLinePointShapeBoundaries newBounds = new FileLinePointShapeBoundaries();
				newBounds.setTopLeft(new FileLinePoint(newTopLine, topLeft.getOffsets().getCopy()));
				newBounds.setBottomRight(new FileLinePoint(newBottomLine, bottomRight.getOffsets().getCopy()));
				return newBounds;
			}
		}
		
		return this;
	}

	/**
	 * <p>Transform a line number based on a document change specification:</p>
	 * <ul>
	 * <li>If the line is before or on (&le;) the first line of the change, it is returned unchanged.</li>
	 * <li>If the line is after the first line of the change and before (&lt;) the old last line of the
	 * change, it is transformed as follows:
	 * <blockquote>
	 * line&prime; = change.startLine + (line &minus; change.startLine) &times; (change.newLastLine &minus; change.startLine) / max</i>(1, change.oldLastLine &minus; change.startLine) 
	 * </blockquote></li>
	 * <li>If the line is after (&ge;) the old last line of the change, it is shifted by the line delta
	 * of the change (newLastLine &minus; oldLastLine).</li>
	 * </ul>
	 * @param line A 1-based document line.
	 * @param change A document change specification.
	 * @return Line number transformed based on the document change specification.
	 */
	protected static int transformLine (int line, DocumentChange change) {
		if (line <= change.getStartLine()) {
			return line;
		} else if (line < change.getOldLastLine()) {
			int relLine = line - change.getStartLine();
			int newRelLine = (int) (relLine * (1.0 * change.getNewLastLine() - change.getStartLine()) 
					/ Math.max(1, change.getOldLastLine() - change.getStartLine()));
			
			return change.getStartLine() + newRelLine;
		} else {
			return line + change.getLineDelta();
		}
	}
	
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return DESCRIPTORS;
	}

	@Override
	public boolean hasProperty(Object propertyId) {
		for (IPropertyDescriptor desc : getPropertyDescriptors()) {
			if (desc.getId().equals(propertyId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getPropertyValue(Object propertyId) {
		if (TOP_LINE_PROP.equals(propertyId)) {
			return Integer.toString(topLeft.getLine());
		}
		if (BOTTOM_LINE_PROP.equals(propertyId)) {
			return Integer.toString(bottomRight.getLine());
		}
		if (TOP_OFFSET_PROP.equals(propertyId)) {
			return CollageUtilities.pointToString(topLeft.getOffsets());
		}
		if (BOTTOM_OFFSET_PROP.equals(propertyId)) {
			return CollageUtilities.pointToString(bottomRight.getOffsets());
		}
		throw new IllegalArgumentException();
	}

	@Override
	public void setPropertyValue(Object propertyId, Object value) {
		if (TOP_LINE_PROP.equals(propertyId)) {
			int topLine = Integer.parseInt((String)value);
			setTopLeft(new FileLinePoint(topLine, topLeft.getOffsets()));
		} else if (BOTTOM_LINE_PROP.equals(propertyId)) {
			int bottomLine = Integer.parseInt((String)value);
			setBottomRight(new FileLinePoint(bottomLine, bottomRight.getOffsets()));
		} else if (TOP_OFFSET_PROP.equals(propertyId)) {
			Point offsets = CollageUtilities.stringToPoint((String)value);
			setTopLeft(new FileLinePoint(topLeft.getLine(), offsets));
		} else if (BOTTOM_OFFSET_PROP.equals(propertyId)) {
			Point offsets = CollageUtilities.stringToPoint((String)value);
			setBottomRight(new FileLinePoint(bottomRight.getLine(), offsets));
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void showInEditor(IEditorPart editor) throws CoreException {
		Object adapted = editor.getAdapter(IRewriteTarget.class);
		if (adapted != null && adapted instanceof IRewriteTarget && editor instanceof ITextEditor) {
			IRewriteTarget target = (IRewriteTarget)adapted;
			try {
				int offset = target.getDocument().getLineOffset(topLeft.getLine() - 1);
				((ITextEditor)editor).selectAndReveal(offset, 0);
			} catch (BadLocationException e) {
				throw new CoreException(new Status(IStatus.ERROR, CollageActivator.PLUGIN_ID, "Unable to find document offset for element", e));
			}
		}
	}

	@Override
	public String getDescription() {
		int startLine = topLeft.getLine();
		int endLine = bottomRight.getLine();
		if (startLine == endLine) {
			return "line " + startLine;
		} else {
			return "lines " + startLine + "-" + endLine;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof FileLinePointShapeBoundaries) {
			FileLinePointShapeBoundaries other = (FileLinePointShapeBoundaries)obj;
			return (topLeft.equals(other.topLeft) && bottomRight.equals(other.bottomRight));
		}
		return false;
	}

	/**
	 * Set the top left corner of this shape. (Assumes that new value has passed validation through 
	 * {@link BoundaryValidator}.)
	 * @param newTopLeft new top left corner for shape boundaries, expressed as point relative to line
	 */
	private void setTopLeft(FileLinePoint newTopLeft) {
		this.topLeft = newTopLeft;
	}

	/**
	 * Set the bottom right corner of this shape. (Assumes that new value has passed validation through 
	 * {@link BoundaryValidator}.)
	 * @param newBottomRight new bottom right corner for shape boundaries, expressed as point relative to line
	 */
	private void setBottomRight(FileLinePoint newBottomRight) {
		this.bottomRight = newBottomRight;
	}

	private IPropertyDescriptor makePropertyDescriptor(String id, String displayName, String description) {
		PropertyDescriptor descriptor = new TextPropertyDescriptor(id, displayName); 
		descriptor.setValidator(new BoundaryValidator(id)); 
		descriptor.setDescription(description);
		descriptor.setCategory(Shape.CATEGORY_LOCATION_SIZE);
		return descriptor;
	}
}

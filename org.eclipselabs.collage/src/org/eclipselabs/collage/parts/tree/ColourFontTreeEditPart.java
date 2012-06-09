/*******************************************************************************
 * Copyright (c) 2011 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley    - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.parts.tree;

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Extension of {@link AbstractTreeEditPart}. Allows colours and fonts to be provided for
 * tree items by overriding methods {@link #getForegroundColour()}, {@link #getBackgroundColour()}, 
 * and {@link #getFont()}. 
 * @author Alex Bradley
 */
public abstract class ColourFontTreeEditPart extends AbstractTreeEditPart {
	public ColourFontTreeEditPart() {
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		setWidgetForegroundColour(getForegroundColour());
		setWidgetBackgroundColour(getBackgroundColour());
		setWidgetFont(getFont());
	}

	/**
	 * Override this method to return the foreground colour for this {@link EditPart}'s
	 * widget. This method is called from {@link #refreshVisuals()}.
	 * 
	 * @return the foreground colour for the {@link TreeItem}
	 */
	protected Color getForegroundColour () {
		return null;
	}

	/**
	 * Override this method to return the background colour for this {@link EditPart}'s
	 * widget. This method is called from {@link #refreshVisuals()}.
	 * 
	 * @return the background colour for the {@link TreeItem}
	 */
	protected Color getBackgroundColour () {
		return null;
	}

	/**
	 * Override this method to return the font for this {@link EditPart}'s
	 * widget. This method is called from {@link #refreshVisuals()}.
	 * 
	 * @return the font for the {@link TreeItem}
	 */
	protected Font getFont () {
		return null;
	}

	protected final void setWidgetForegroundColour (Color color) {
		if (checkTreeItem())
			((TreeItem) getWidget()).setForeground(color);
	}
	
	protected final void setWidgetBackgroundColour (Color color) {
		if (checkTreeItem())
			((TreeItem) getWidget()).setBackground(color);
	}
	
	protected final void setWidgetFont (Font font) {
		if (checkTreeItem())
			((TreeItem) getWidget()).setFont(font);
	}
}

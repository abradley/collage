/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Bradley    - adaptation for Collage plugin
 *******************************************************************************/
package org.eclipselabs.collage.text.edit;

import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.viewers.CellEditor;

import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipselabs.collage.text.figures.TextNoteFigure;


final public class LabelCellEditorLocator implements CellEditorLocator {

	private TextNoteFigure textNote;

	public LabelCellEditorLocator(TextNoteFigure stickyNote) {
		setLabel(stickyNote);
	}

	public void relocate(CellEditor celleditor) {
		Text text = (Text) celleditor.getControl();
		Rectangle rect = textNote.getEditableArea();
		textNote.translateToAbsolute(rect);
		org.eclipse.swt.graphics.Rectangle trim = text.computeTrim(0, 0, 0, 0);
		rect.translate(trim.x, trim.y);
		rect.width += trim.width;
		rect.height += trim.height;
		
		if (text.getVerticalBar() != null) {
			rect.width -= text.getVerticalBar().getSize().x + 5;
		}
		
		text.setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Returns the textNote figure.
	 */
	protected TextNoteFigure getLabel() {
		return textNote;
	}

	/**
	 * Sets the Sticky note figure.
	 * 
	 * @param textNote
	 *            The textNote to set
	 */
	protected void setLabel(TextNoteFigure stickyNote) {
		this.textNote = stickyNote;
	}

}
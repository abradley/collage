/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Bradley    - adapted for use with Collage
 *******************************************************************************/
package org.eclipselabs.collage.parts.tree;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.jface.viewers.CellEditor;

import org.eclipse.gef.tools.CellEditorLocator;

public final class LayerNameCellEditorLocator implements CellEditorLocator {

	private final TreeItem treeItem;

	public LayerNameCellEditorLocator(TreeItem treeItem) {
		this.treeItem = treeItem;
	}

	@Override
	public void relocate(CellEditor celleditor) {
		Text text = (Text) celleditor.getControl();
		// At the moment, this will result in bounds that stretch to the right side
		// of the TreeViewer. See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=300147
		Rectangle textBounds = treeItem.getTextBounds(0);
		// Shrinking the top and bottom gives the effect of a "border" around the
		// cell editor (due to the selection highlight underneath.) Under Linux GTK, 
		// the following yields a 1-pixel border at the top and bottom.
		text.setBounds(new Rectangle(textBounds.x, textBounds.y + 1, textBounds.width, textBounds.height - 3));
	}
}
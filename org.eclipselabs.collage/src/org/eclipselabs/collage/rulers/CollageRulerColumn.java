/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley    - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.rulers;

import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn;
import org.eclipselabs.collage.ui.CollageUI;

/**
 * Ruler contribution that installs a Collage UI on a text editor.
 * @author Alex Bradley
 */
public class CollageRulerColumn extends AbstractContributedRulerColumn {
	private Canvas canvas = null;
	
	private CollageUI collageUI;
	
	@Override
	public Control createControl(CompositeRuler parentRuler,
			Composite parentControl) {
		canvas = new Canvas(parentControl, SWT.NO_BACKGROUND);
		
		collageUI = new CollageUI();
		collageUI.install(getEditor(), parentRuler.getTextViewer());
		
		return canvas;
	}
	
	@Override
	public void columnRemoved() {
		collageUI.uninstall();
	}
	
	@Override
	public Control getControl() {
		return canvas;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public void redraw() {
	}
	
	@Override
	public void setModel(IAnnotationModel model) {
	}

	@Override
	public void setFont(Font font) {
	}
}

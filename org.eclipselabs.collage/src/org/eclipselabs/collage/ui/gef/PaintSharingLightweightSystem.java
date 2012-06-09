/*******************************************************************************
 * Copyright (c) 2011 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.ui.gef;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * This specialization of LightweightSystem tries to be "friendly" to other users
 * of its painting GC by saving some painting parameters before it paints and restoring
 * them after it's done painting.
 * @author Alex Bradley
 */
public class PaintSharingLightweightSystem extends LightweightSystem {
	private int savedLineWidth = 1;
	private Color savedBackgroundColor = null;
	private Color savedForegroundColor = null;
	private Rectangle savedClipping = null;
	
	@Override
	public void paint(GC gc) {
		savePaintParameters(gc);
		
		super.paint(gc);
		
		restorePaintParameters(gc);
	}

	protected void savePaintParameters (GC gc) {
		savedLineWidth = gc.getLineWidth();
		savedBackgroundColor = gc.getBackground();
		savedForegroundColor = gc.getForeground();
		savedClipping = gc.isClipped() ? gc.getClipping() : null;
	}
	
	protected void restorePaintParameters (GC gc) {
		gc.setLineWidth(savedLineWidth);
		
		if (colorAvailable(savedBackgroundColor)) {
			gc.setBackground(savedBackgroundColor);
		}
		
		if (colorAvailable(savedForegroundColor)) {
			gc.setForeground(savedForegroundColor);
		}
		
		gc.setClipping(savedClipping);
	}
	
	private static boolean colorAvailable (Color color) {
		return color != null && !color.isDisposed();
	}
}

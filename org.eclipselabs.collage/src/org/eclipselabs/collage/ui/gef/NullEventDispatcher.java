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
package org.eclipselabs.collage.ui.gef;

import org.eclipse.draw2d.EventDispatcher;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.widgets.Control;

/**
 * Event dispatcher that drops all events.
 * @see CollageDomainEventDispatcher#dispatchKeyTraversed
 * @author Alex Bradley
 */
public class NullEventDispatcher extends EventDispatcher {
	private static final NullEventDispatcher SINGLETON = new NullEventDispatcher();
	
	private NullEventDispatcher () { }
	
	public static EventDispatcher getInstance () {
		return SINGLETON;
	}
	
	@Override
	public void dispatchFocusGained(FocusEvent e) { }

	@Override
	public void dispatchFocusLost(FocusEvent e) { }

	@Override
	public void dispatchKeyPressed(KeyEvent e) { }

	@Override
	public void dispatchKeyReleased(KeyEvent e) { }

	@Override
	public void dispatchKeyTraversed(TraverseEvent e) {
		// Prevent interference with StyledText's tab handling. See note in 
		// CollageDomainEventDispatcher#dispatchKeyTraversed.
		e.doit = false;
	}

	@Override
	public void dispatchMouseDoubleClicked(MouseEvent me) { }

	@Override
	public void dispatchMouseEntered(MouseEvent e) { }

	@Override
	public void dispatchMouseExited(MouseEvent e) { }

	@Override
	public void dispatchMouseHover(MouseEvent me) { }

	@Override
	public void dispatchMouseMoved(MouseEvent me) { }

	@Override
	public void dispatchMousePressed(MouseEvent me) { }

	@Override
	public void dispatchMouseReleased(MouseEvent me) { }

	@Override
	protected AccessibilityDispatcher getAccessibilityDispatcher() {
		return null;
	}

	@Override
	public IFigure getFocusOwner() {
		return null;
	}

	@Override
	public boolean isCaptured() {
		return false;
	}

	@Override
	protected void releaseCapture() { }

	@Override
	public void requestFocus(IFigure fig) { }

	@Override
	public void requestRemoveFocus(IFigure fig) { }

	@Override
	protected void setCapture(IFigure figure) { }

	@Override
	public void setControl(Control control) { }

	@Override
	public void setRoot(IFigure figure) { }

	@Override
	protected void updateCursor() { }
}

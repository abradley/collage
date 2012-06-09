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

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.parts.DomainEventDispatcher;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipselabs.collage.ui.CollageUI;

/**
 * <p>Specialized domain event dispatcher for use with Collage {@link TransparentGraphicalViewer}.
 * It is different from the standard {@link DomainEventDispatcher} in two ways:</p>
 * <ul>
 * <li>It handles presses of the Delete key directly by calling Collage's delete action, since normal workbench
 * key press handling fails to call our globally registered delete action when Delete is pressed. (I suspect this
 * is due to some combination of "out of order key" handling in 
 * {@code org.eclipse.ui.internal.keys.WorkbenchKeyboard.filterKeySequenceBindings(Event)} and our interference with
 * {@link StyledText}'s normal key handling.)</li>  
 * <li>It sets {@code doit} to {@code false} on traverse events to prevent interference with {@link StyledText}'s 
 * handling of the tab key (traversal events.)</li>
 * </ul>
 * 
 * <p><b>WARNING:</b> This approach must be considered kludgy, as {@link DomainEventDispatcher}'s documentation
 * states that it is "not intended to be used or subclassed by clients."</p>
 * @author Alex Bradley
 */
public class CollageDomainEventDispatcher extends DomainEventDispatcher {
	public CollageDomainEventDispatcher(EditDomain d, EditPartViewer v) {
		super(d, v);
	}

	@Override
	public void dispatchKeyPressed(KeyEvent e) {
		if (e.keyCode == SWT.DEL) {
			// The DEL key should normally be handled by the workbench and cause a registered delete action to fire. 
			// However, it appears to get lost in org.eclipse.ui.internal.keys.WorkbenchKeyboard#filterKeySequenceBindings 
			// due to special handling for "out of order keys" (ESC and DEL). So we look out for DEL here and fire
			// our own DeleteAction.
			EditPartViewer epViewer = getViewer();
			if (epViewer instanceof TransparentGraphicalViewer) {
				CollageUI collageUI = ((TransparentGraphicalViewer)epViewer).getCollageUI();
				if (collageUI.editingEnabled()) {
					IAction action = collageUI.getDeleteAction();
					if (action != null) {
						action.run();
					}
					e.doit = false;
				}
			}
		} else {
			super.dispatchKeyPressed(e);
		}
	}

	@Override
	public void dispatchKeyTraversed(TraverseEvent e) {
		// LightweightSystem.EventHandler#keyTraversed forces e.doit to true, which interferes with
		// StyledText's handling of the tab key. We can't replace the EventHandler directly, so we use this
		// EventDispatcher to set e.doit back to false.
		super.dispatchKeyTraversed(e);
		e.doit = false;
	}
}

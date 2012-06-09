/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley    - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.actions;

import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * A redo action that does a more thorough check for command redoability than the GEF
 * command stack's {@code canRedo()} method.
 * @author Alex Bradley
 */
public class CheckedRedoAction extends RedoAction {
	public CheckedRedoAction(IEditorPart editor) {
		super(editor);
	}

	public CheckedRedoAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		return CollageUtilities.canReallyRedo(getCommandStack());
	}
}

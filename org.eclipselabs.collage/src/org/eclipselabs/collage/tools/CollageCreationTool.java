/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.tools;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipselabs.collage.ui.CollageUI;
import org.eclipselabs.collage.ui.gef.TransparentGraphicalViewer;

/**
 * Adaptation of a {@link CreationTool} to suit our UI. The Collage Creation Tool
 * differs from the standard Creation Tool in that
 * <ul>
 * <li>it does <b>not</b> unload by default after one creation;</li>
 * <li>it does <b>not</b> automatically select the just-created object after creation; and</li>
 * <li>it avoids the "drag threshold" delay between the {@code STATE_DRAG} and
 * {@code STATE_DRAG_IN_PROGRESS} states, moving to {@code STATE_DRAG_IN_PROGRESS} immediately
 * (this got dragging working on my tablet)</li>
 * </ul>
 * 
 * @author Alex Bradley
 */
public class CollageCreationTool extends CreationTool {
	public CollageCreationTool() {
		super();
		setUnloadWhenFinished(false);
	}

	public CollageCreationTool(CreationFactory aFactory) {
		super(aFactory);
		setUnloadWhenFinished(false);
	}

	@Override
	protected void performCreation(int button) {
		EditPartViewer viewer = getCurrentViewer();
		executeCurrentCommand();
		
		if (viewer != null) { 
			viewer.flush();
		}
	}

	@Override
	protected boolean handleButtonDown(int button) {
		super.handleButtonDown(button);
		// Skip right to drag-in-progress, don't wait for threshold
		stateTransition(STATE_DRAG, STATE_DRAG_IN_PROGRESS);
		super.handleDragInProgress();
		return true;
	}

	@Override
	protected boolean handleDragStarted() {
		return false;
	}

	@Override
	protected boolean handleDragInProgress() {
		return false;
	}

	@Override
	protected boolean handleDrag() {
		return super.handleDragInProgress();
	}

	/**
	 * Get the current Collage UI for this tool.
	 * @return Collage UI instance
	 */
	protected CollageUI getCollageUI () {
		return ((TransparentGraphicalViewer)getCurrentViewer()).getCollageUI();
	}
}

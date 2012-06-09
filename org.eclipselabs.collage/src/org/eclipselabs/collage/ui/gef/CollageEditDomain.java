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
package org.eclipselabs.collage.ui.gef;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.Tool;
import org.eclipselabs.collage.tools.ToolChangeListener;

/**
 * An {@link EditDomain} that notifies listeners when the active tool changes.
 * @author Alex Bradley
 */
public class CollageEditDomain extends EditDomain {
	private List<ToolChangeListener> listeners = new ArrayList<ToolChangeListener>();
	private Object listenersLock = new Object();
	
	@Override
	public void setActiveTool(Tool tool) {
		Tool oldTool = getActiveTool();
		super.setActiveTool(tool);
		fireToolChange(oldTool, tool);
	}

	/**
	 * Add a listener that will be notified when the active tool changes.
	 * @param listener A tool change listener.
	 */
	public void addToolChangeListener (ToolChangeListener listener) {
		synchronized (listenersLock) {
			listeners.add(listener);
		}
	}
	
	private void fireToolChange (Tool oldTool, Tool newTool) {
		// This method can get called in the middle of the constructor before field initialization is complete.
		if (listenersLock == null) {
			return;
		}
		
		synchronized (listenersLock) {
			for (ToolChangeListener listener : listeners) {
				listener.toolChange(oldTool, newTool);
			}
		}
	}
}

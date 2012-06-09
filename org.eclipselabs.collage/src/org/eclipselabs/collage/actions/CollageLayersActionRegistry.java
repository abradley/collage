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

import java.util.Iterator;

import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.UpdateAction;

/**
 * Singleton action registry used for the layers view and properties view.
 * @author Alex Bradley
 */
public final class CollageLayersActionRegistry extends ActionRegistry implements CommandStackEventListener {
	private static final CollageLayersActionRegistry INSTANCE = new CollageLayersActionRegistry();
	
	private CollageLayersActionRegistry () { 
		super();
	}
	
	public static CollageLayersActionRegistry getDefault () {
		return INSTANCE;
	}
	
	@Override
	public void stackChanged(CommandStackEvent event) {
		if (event.isPostChangeEvent()) {
			updateActions();
		}
	}
	
	private void updateActions () {
		Iterator<?> iter = getActions(); 
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof UpdateAction) {
				((UpdateAction)obj).update();
			}
		}
	}
}

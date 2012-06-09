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
package org.eclipselabs.collage.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton registry for all active Collage UI instances.
 * @author Alex Bradley
 */
public final class CollageUIRegistry {
	private static final CollageUIRegistry INSTANCE = new CollageUIRegistry();
	
	private List<CollageUI> uiList = new ArrayList<CollageUI>();
	
	private CollageUIRegistry () { }
	
	public static CollageUIRegistry getDefault () {
		return INSTANCE;
	}
	
	public void addCollageUI (CollageUI ui) {
		uiList.add(ui);
	}
	
	public void removeCollageUI (CollageUI ui) {
		uiList.remove(ui);
	}
	
	public void updateAllUndoRedo () {
		for (CollageUI ui : uiList) {
			ui.updateUndoRedo();
		}
	}
}

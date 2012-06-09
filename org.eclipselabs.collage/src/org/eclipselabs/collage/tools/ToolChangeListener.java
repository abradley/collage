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
package org.eclipselabs.collage.tools;

import java.util.EventListener;

import org.eclipse.gef.Tool;
import org.eclipselabs.collage.ui.gef.CollageEditDomain;

/**
 * Listen for activation of GEF tools in a {@link CollageEditDomain}.
 * @author Alex Bradley
 */
public interface ToolChangeListener extends EventListener {
	/**
	 * Called when active tool has changed.
	 * @param oldTool Previously active tool.
	 * @param newTool Newly activated tool.
	 */
	public void toolChange (Tool oldTool, Tool newTool);
}

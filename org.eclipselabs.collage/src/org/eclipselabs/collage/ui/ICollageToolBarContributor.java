/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial API
 *******************************************************************************/
package org.eclipselabs.collage.ui;

import org.eclipse.swt.widgets.ToolBar;

/**
 * Interface for contributing arbitrary widgets to the Collage tool bar.
 * @author Alex Bradley
 */
public interface ICollageToolBarContributor {
	/**
	 * @param toolBar SWT tool bar added to an editor by Collage
	 * @param collageUI Collage UI instance
	 */
	public void addContribution (ToolBar toolBar, CollageUI collageUI);
}

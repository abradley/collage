/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.actions;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Scheduling rule for a file outside the workspace.
 * @author Alex Bradley
 */
public class FilesystemSchedulingRule implements ISchedulingRule {
	private File file;
	
	public FilesystemSchedulingRule(File file) {
		Assert.isNotNull(file);
		this.file = file;
	}
	
	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		return (rule instanceof FilesystemSchedulingRule && file.equals(((FilesystemSchedulingRule)rule).file));
	}
	
}
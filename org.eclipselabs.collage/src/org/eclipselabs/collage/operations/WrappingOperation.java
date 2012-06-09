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
package org.eclipselabs.collage.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.commands.CommandStack;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Eclipse undoable operation that wraps a GEF command just executed by a GEF command stack.
 * @author Alex Bradley
 */
public class WrappingOperation extends AbstractOperation {
	private CommandStack commandStack;
	
	/**
	 * Create the wrapping operation.
	 * @param label Label for operation.
	 * @param context Eclipse undo context in which this operation should be added.
	 * @param commandStack GEF command stack to wrap.
	 */
	public WrappingOperation(String label, IUndoContext context, CommandStack commandStack) {
		super(label);
		this.commandStack = commandStack;
		addContext(context);
	}

	public CommandStack getCommandStack () {
		return commandStack;
	}
	
	@Override
	public boolean canExecute() {
		return commandStack.canRedo();
	}

	@Override
	public boolean canRedo() {
		return CollageUtilities.canReallyRedo(commandStack);
	}

	@Override
	public boolean canUndo() {
		return commandStack.canUndo();
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (monitor != null) {
			monitor.beginTask("Undoing " + getLabel(), 10);
		}
		commandStack.undo();
		if (monitor != null) {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (monitor != null) {
			monitor.beginTask("Redoing " + getLabel(), 10);
		}
		commandStack.redo();
		if (monitor != null) {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}
}

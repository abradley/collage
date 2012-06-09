/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.model.commands;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.swt.widgets.Display;

/**
 * A GEF compound command that repositions shapes after the user has changed the text in the editor.
 * When undone, it will attempt to undo the previous change (i.e., the text change) as well. (Cascading
 * redo handling is performed by {@link org.eclipselabs.collage.parts.ResourceShapeListEditPart#historyNotification(org.eclipse.core.commands.operations.OperationHistoryEvent) ResourceShapeListEditPart.historyNotification}.) 
 * @author Alex Bradley
 */
public class HandleDocumentChangeCommand extends CompoundCommand {
	private static final String HANDLE_DOCUMENT_CHANGE_DESCRIPTION = "text editing";

	private final IUndoContext context;
	
	/**
	 * Create a document change handler command.
	 * @param context Eclipse undo context in which this command will execute.
	 */
	public HandleDocumentChangeCommand(IUndoContext context) {
		super(HANDLE_DOCUMENT_CHANGE_DESCRIPTION);
		this.context = context;
	}

	@Override
	public void undo() {
		super.undo();
		
		// Queue this to be run by the UI thread later; we may be in the middle of another undo operation right now
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// Try to undo the previous operation (i.e., a text edit, or a chain of HandleDocumentChangeCommands
				// followed by a text edit) which led to this adjustment.
				IOperationHistory history = OperationHistoryFactory.getOperationHistory();
				if (history.canUndo(context)) {
					try {
						history.undo(context, null, null);
					} catch (ExecutionException e) {
						// Ignore
					}
				}
			}
		});
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation (GEF logic example)
 *     Alex Bradley    - adapted for use in Collage
 *******************************************************************************/
package org.eclipselabs.collage.text.edit;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipselabs.collage.text.commands.TextNoteEditCommand;
import org.eclipselabs.collage.text.figures.TextNoteFigure;
import org.eclipselabs.collage.text.model.TextNoteShape;
import org.eclipselabs.collage.text.parts.TextNoteEditPart;

/**
 * Direct edit policy for a text note.
 * @author IBM Corporation
 * @author Alex Bradley
 */
public class TextNoteDirectEditPolicy extends DirectEditPolicy {
	@Override
	protected Command getDirectEditCommand(DirectEditRequest edit) {
		String text = (String) edit.getCellEditor().getValue();
		TextNoteEditPart textNote = (TextNoteEditPart) getHost();
		TextNoteEditCommand command = new TextNoteEditCommand(
				(TextNoteShape) textNote.getModel(), text);
		return command;
	}

	@Override
	protected void showCurrentEditValue(DirectEditRequest request) {
		String value = (String) request.getCellEditor().getValue();
		((TextNoteFigure) getHostFigure()).setText(value);
		// hack to prevent async layout from placing the cell editor twice.
		getHostFigure().getUpdateManager().performUpdate();
	}
}

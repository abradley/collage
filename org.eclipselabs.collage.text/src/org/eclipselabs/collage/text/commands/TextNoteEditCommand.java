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
package org.eclipselabs.collage.text.commands;

import org.eclipse.gef.commands.Command;
import org.eclipselabs.collage.text.model.TextNoteShape;

/**
 * Command to change the text in a text note.
 * @author Alex Bradley
 * @author IBM Corporation
 */
public class TextNoteEditCommand extends Command {
	private String newText, oldText;
	private TextNoteShape note;

	public TextNoteEditCommand(TextNoteShape note, String text) {
		setLabel("text note editing");
		
		this.note = note;
		if (text != null)
			newText = text;
		else
			newText = "";
	}

	@Override
	public void execute() {
		oldText = note.getText();
		note.setText(newText);
	}

	@Override
	public void undo() {
		note.setText(oldText);
	}
}

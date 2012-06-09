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
package org.eclipselabs.collage.model;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

/**
 * <p>A change to an {@link IDocument}. Wraps a {@link DocumentEvent} and includes the computed
 * start line, old last line, and new last line of the change. <strong>Note that all lines returned
 * by methods of this class are 1-based, unlike the 0-based lines returned by methods of 
 * {@link IDocument}.</strong></p> 
 * @author Alex Bradley
 */
public class DocumentChange {
	private final int startLine;
	private final int oldLastLine;
	private final int newLastLine;
	private final DocumentEvent documentEvent;
	
	/**
	 * Create a new document change.
	 * @param oldLastLine Old last line of the change (1-based).
	 * @param documentEvent Wrapped {@link DocumentEvent}.
	 * @throws BadLocationException if computation of start line or new last line fails
	 */
	public DocumentChange(DocumentEvent documentEvent, int oldLastLine) throws BadLocationException {
		this.documentEvent = documentEvent;
		this.oldLastLine = oldLastLine;
		
		// Compute 1-based start line and new last line
		this.startLine = documentEvent.getDocument().getLineOfOffset(documentEvent.getOffset()) + 1;
		this.newLastLine = documentEvent.getDocument().getLineOfOffset(documentEvent.getOffset() + documentEvent.getText().length()) + 1;
	}
	
	/**
	 * Get the {@link DocumentEvent} wrapped by this change specification.
	 * @return The wrapped {@link DocumentEvent}
	 */
	public DocumentEvent getDocumentEvent() {
		return documentEvent;
	}
	
	/**
	 * Get the first line affected by this change.
	 * @return Start line
	 */
	public int getStartLine () {
		return startLine;
	}
	
	/**
	 * Get the last line affected by this change in the pre-change state of the document.
	 * @return Old last line
	 */
	public int getOldLastLine () {
		return oldLastLine;
	}

	/**
	 * Get the last line affected by this change in the post-change state of the document.
	 * @return New last line
	 */
	public int getNewLastLine () {
		return newLastLine;
	}
	
	/**
	 * Get the difference in the number of lines in the document caused by this change.
	 * @return Change in number of document lines
	 */
	public int getLineDelta () {
		return newLastLine - oldLastLine;
	}
}

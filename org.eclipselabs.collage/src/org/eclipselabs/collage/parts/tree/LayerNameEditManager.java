/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Bradley    - adapted for use with Collage tree view
 *******************************************************************************/
package org.eclipselabs.collage.parts.tree;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TextCellEditor;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.gef.TreeEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;


/**
 * <p>Adaptation of {@link DirectEditManager}. Uses a TreeEditPart instead of a GraphicalEditPart.
 * Does not attempt to draw border around cell editor. Specialized to manage editing Collage 
 * layer names.</p>
 * 
 * <p>Original description for {@link DirectEditManager}: Manages the direct edit operation by 
 * creating and maintaining the {@link org.eclipse.jface.viewers.CellEditor} and executing the resulting
 * command if the cell editor value has changed.</p>
 * @author Alex Bradley
 * @author IBM Corporation
 */
public class LayerNameEditManager {

	private EditPartListener editPartListener;
	private ICellEditorListener cellEditorListener;
	private boolean showingFeedback;
	private boolean dirty;
	private DirectEditRequest request;
	private CellEditorLocator locator;
	private TreeEditPart source;
	private CellEditor ce;
	private boolean committing = false;
	private Object feature;

	/**
	 * Constructs a new LayerNameEditManager for the given source edit part.
	 * The cell editor will be placed using the given CellEditorLocator.
	 * 
	 * @param source
	 *            the source edit part
	 * @param locator
	 *            the locator
	 */
	public LayerNameEditManager(TreeEditPart source,
			CellEditorLocator locator) {
		this.source = source;
		this.locator = locator;
	}

	/**
	 * Creates the cell editor on the given composite.
	 * 
	 * @param composite
	 *            the composite to create the cell editor on
	 * @return the newly created cell editor
	 */
	protected CellEditor createCellEditorOn(Composite composite) {
		return new TextCellEditor(composite, SWT.SINGLE);
	}

	/**
	 * Initializes the cell editor. Subclasses [in the original DirectEditManager implementation]
	 * should implement this to set the initial text and add things such as {@link VerifyListener
	 * VerifyListeners}, if needed.
	 */
	protected void initCellEditor() {
		// update text
		CollageLayerTreeEditPart editPart = (CollageLayerTreeEditPart)getEditPart();
		getCellEditor().setValue(editPart.getText());
		getCellEditor().getControl().setFont(editPart.getFont());
	}

	/**
	 * Cleanup is done here. Any feedback is erased and listeners unhooked. If
	 * the cell editor is not <code>null</code>, it will be
	 * {@link CellEditor#deactivate() deativated}, {@link CellEditor#dispose()
	 * disposed}, and set to <code>null</code>.
	 */
	protected void bringDown() {
		eraseFeedback();
		unhookListeners();
		if (getCellEditor() != null) {
			getCellEditor().deactivate();
			getCellEditor().dispose();
			setCellEditor(null);
		}
		request = null;
		dirty = false;
	}

	/**
	 * Commits the current value of the cell editor by getting a {@link Command}
	 * from the source edit part and executing it via the {@link CommandStack}.
	 * Finally, {@link #bringDown()} is called to perform and necessary cleanup.
	 */
	protected void commit() {
		if (committing)
			return;
		committing = true;
		try {
			eraseFeedback();
			if (isDirty()) {
				CommandStack stack = getEditPart().getViewer().getEditDomain()
						.getCommandStack();
				stack.execute(getEditPart().getCommand(getDirectEditRequest()));
			}
		} finally {
			bringDown();
			committing = false;
		}
	}

	/**
	 * Creates and returns the DirectEditRequest.
	 * 
	 * @return the direct edit request
	 */
	protected DirectEditRequest createDirectEditRequest() {
		DirectEditRequest req = new DirectEditRequest();
		req.setCellEditor(getCellEditor());
		req.setDirectEditFeature(getDirectEditFeature());
		return req;
	}

	/**
	 * Asks the source edit part to erase source feedback.
	 */
	protected void eraseFeedback() {
		if (showingFeedback) {
			getEditPart().eraseSourceFeedback(getDirectEditRequest());
			showingFeedback = false;
		}
	}

	/**
	 * Returns the cell editor.
	 * 
	 * @return the cell editor
	 */
	protected CellEditor getCellEditor() {
		return ce;
	}

	private Control getControl() {
		return ce.getControl();
	}

	/**
	 * @return <code>Object</code> that can be used if the EditPart supports
	 *         direct editing of multiple features, this parameter can be used
	 *         to discriminate among them.
	 * @since 3.2
	 */
	protected Object getDirectEditFeature() {
		return feature;
	}

	/**
	 * Returns the direct edit request, creating it if needed.
	 * 
	 * @return the direct edit request
	 */
	protected DirectEditRequest getDirectEditRequest() {
		if (request == null)
			request = createDirectEditRequest();
		return request;
	}

	/**
	 * Returns the source edit part.
	 * 
	 * @return the source edit part
	 */
	protected TreeEditPart getEditPart() {
		return source;
	}

	protected CellEditorLocator getLocator() {
		return locator;
	}

	protected void handleValueChanged() {
		setDirty(true);
		showFeedback();
		placeCellEditor();
	}

	protected void hookListeners() {
		cellEditorListener = new ICellEditorListener() {
			@Override
			public void applyEditorValue() {
				commit();
			}

			@Override
			public void cancelEditor() {
				bringDown();
			}

			@Override
			public void editorValueChanged(boolean old, boolean newState) {
				handleValueChanged();
			}
		};
		getCellEditor().addListener(cellEditorListener);

		editPartListener = new EditPartListener.Stub() {
			@Override
			public void partDeactivated(EditPart editpart) {
				bringDown();
			}
		};
		getEditPart().addEditPartListener(editPartListener);
	}

	/**
	 * Returns <code>true</code> if the cell editor's value has been changed.
	 * 
	 * @return <code>true</code> if the cell editor is dirty
	 */
	protected boolean isDirty() {
		return dirty;
	}

	private void placeCellEditor() {
		getLocator().relocate(getCellEditor());
	}

	/**
	 * Sets the cell editor to the given editor.
	 * 
	 * @param editor
	 *            the cell editor
	 */
	protected void setCellEditor(CellEditor editor) {
		ce = editor;
		if (ce == null)
			return;
		hookListeners();
	}

	/**
	 * Sets the dirty property.
	 * 
	 * @param value
	 *            the dirty property
	 */
	protected void setDirty(boolean value) {
		dirty = value;
	}

	/**
	 * Sets the source edit part.
	 * 
	 * @param source
	 *            the source edit part
	 */
	protected void setEditPart(TreeEditPart source) {
		this.source = source;
		// source.addEditPartListener();
	}

	/**
	 * Sets the CellEditorLocator used to place the cell editor in the correct
	 * location.
	 * 
	 * @param locator
	 *            the locator
	 */
	public void setLocator(CellEditorLocator locator) {
		this.locator = locator;
	}

	/**
	 * Shows the cell editor when direct edit is started. Calls
	 * {@link #initCellEditor()}, {@link CellEditor#activate()}, and
	 * {@link #showFeedback()}.
	 */
	public void show() {
		if (getCellEditor() != null)
			return;
		Composite composite = (Composite) source.getViewer().getControl();
		setCellEditor(createCellEditorOn(composite));
		if (getCellEditor() == null)
			return;
		initCellEditor();
		getCellEditor().activate();
		placeCellEditor();
		getControl().setVisible(true);
		getCellEditor().setFocus();
		showFeedback();
	}

	/**
	 * Asks the source edit part to show source feedback.
	 */
	public void showFeedback() {
		showingFeedback = true;
		getEditPart().showSourceFeedback(getDirectEditRequest());
	}

	/**
	 * Unhooks listeners. Called from {@link #bringDown()}.
	 */
	protected void unhookListeners() {
		getEditPart().removeEditPartListener(editPartListener);
		editPartListener = null;

		if (getCellEditor() == null)
			return;
		getCellEditor().removeListener(cellEditorListener);
		cellEditorListener = null;
	}
}
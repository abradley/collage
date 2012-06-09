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
 *     Alex Bradley    - adaptation for Collage text plugin
 *******************************************************************************/
package org.eclipselabs.collage.text.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;

import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;

import org.eclipse.draw2d.IFigure;

import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.parts.ShapeEditPart;
import org.eclipselabs.collage.parts.TextViewerRootEditPart;
import org.eclipselabs.collage.text.edit.LabelCellEditorLocator;
import org.eclipselabs.collage.text.edit.TextNoteDirectEditPolicy;
import org.eclipselabs.collage.text.edit.TextNoteEditManager;
import org.eclipselabs.collage.text.figures.TextNoteFigure;
import org.eclipselabs.collage.text.model.TextNoteShape;
import org.eclipselabs.collage.ui.CollageUI;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Edit part for a text note.
 * @author Alex Bradley
 * @author IBM Corporation
 */
public class TextNoteEditPart extends ShapeEditPart {
	private PropertyChangeListener editingOffListener;
	private Object listenerLock = new Object();
	
	protected AccessibleEditPart createAccessible() {
		return new AccessibleGraphicalEditPart() {
			@Override
			public void getValue(AccessibleControlEvent e) {
				e.result = getTextNoteShape().getText();
			}

			@Override
			public void getName(AccessibleEvent e) {
				e.result = "Text Note";
			}
		};
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
				new TextNoteDirectEditPolicy());
	}

	@Override
	protected IFigure createFigure() {
		TextNoteFigure label = new TextNoteFigure();
		TextNoteShape model = (TextNoteShape)getModel();
		label.setForegroundColor(CollageUtilities.getColor(model.getColour()));
		label.setText(model.getText());
		label.setTopLabelText(formatModelMetadata(model));
		model.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				TextNoteShape model = (TextNoteShape)getModel();
				TextNoteFigure label = (TextNoteFigure)getFigure();
				label.setTopLabelText(formatModelMetadata(model));
				label.setForegroundColor(CollageUtilities.getColor(model.getColour()));
			}
		});
		return label;
	}
	
	private static String formatModelMetadata (Shape model) {
		return model.getCreator() + " " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(model.getModificationDate());
	}

	private TextNoteShape getTextNoteShape() {
		return (TextNoteShape) getModel();
	}

	private void performDirectEdit() {
		final TextNoteEditManager manager = new TextNoteEditManager(this, new LabelCellEditorLocator(
				(TextNoteFigure) getFigure()));
		manager.show();
		final CollageUI collageUI = ((TextViewerRootEditPart)this.getRoot()).getCollageUI();
		synchronized (listenerLock) {
			editingOffListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getNewValue() instanceof Boolean && !(Boolean)evt.getNewValue()) {
						manager.finishEditing();
					}
				}
			};
			collageUI.addPropertyChangeListener(CollageUI.EDIT_ENABLED_PROPERTY_ID, editingOffListener);
		}
	}
	
	public void finishDirectEdit () {
		final CollageUI collageUI = ((TextViewerRootEditPart)this.getRoot()).getCollageUI();
		synchronized (listenerLock) {
			collageUI.removePropertyChangeListener(CollageUI.EDIT_ENABLED_PROPERTY_ID, editingOffListener);
			editingOffListener = null;
		}
	}

	@Override
	public void performRequest(Request request) {
		if (RequestConstants.REQ_DIRECT_EDIT.equals(request.getType()))
			performDirectEdit();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equalsIgnoreCase("labelContents")) {
			refreshVisuals();
		} else {
			if (TextNoteShape.TEXT_PROP.equals(evt.getPropertyName()) && getModel() instanceof TextNoteShape) {
				IFigure fig = getFigure();
				if (fig instanceof TextNoteFigure) {
					((TextNoteFigure)fig).setText(((TextNoteShape)getModel()).getText());
				}
			}

			super.propertyChange(evt);
		}
	}

	@Override
	protected void refreshVisuals() {
		((TextNoteFigure) getFigure()).setText(getTextNoteShape()
				.getText());
		super.refreshVisuals();
	}

}

/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation (GEF shapes example)
 *    Alex Bradley    - adapted for use in Collage
 *    IBM Corporation - case choices from DocumentUndoManager$HistoryListener
 *******************************************************************************/
package org.eclipselabs.collage.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipselabs.collage.model.DocumentChange;
import org.eclipselabs.collage.model.ModelElement;
import org.eclipselabs.collage.model.ResourceShapeList;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.model.ShapeBoundaries;
import org.eclipselabs.collage.model.commands.HandleDocumentChangeCommand;
import org.eclipselabs.collage.model.commands.ShapeCreateCommand;
import org.eclipselabs.collage.model.commands.ShapeSetConstraintCommand;
import org.eclipselabs.collage.operations.WrappingOperation;
import org.eclipselabs.collage.requests.ICreateRequestCustomFeedback;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * EditPart for a {@link ResourceShapeList} instance.
 * <p>
 * This edit part serves as the main diagram container in which everything else
 * is contained. Also responsible for the container's layout (the way
 * the container rearranges its contents) and the container's capabilities (edit
 * policies).
 * </p>
 * <p>
 * This edit part must implement the PropertyChangeListener interface, so it can
 * be notified of property changes in the corresponding model element.
 * </p>
 * 
 * @author Elias Volanakis
 * @author Alex Bradley
 */
class ResourceShapeListEditPart extends AbstractGraphicalEditPart implements
		PropertyChangeListener, IAnnotationModelListener, IProjectionListener, IOperationHistoryListener, DisposeListener {
	private ProjectionAnnotationModel currentProjectionAnnotationModel = null;

	private final IDocumentListener documentListener = new IDocumentListener() {
		private int currentOldEndLine = -1;
		
		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			try {
				// Pick up the old end line before the document changes. We save this and add it to the DocumentChange 
				// later so we don't need to analyze the actual inserted string (e.g., trying to count newlines.)
				currentOldEndLine = event.getDocument().getLineOfOffset(event.getOffset() + event.getLength()) + 1;
			} catch (BadLocationException e) {
				// Give up.
			}
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			if (currentOldEndLine != -1) {
				IUndoContext undoContext = CollageUtilities.getTextViewerUndoContext(getTextViewer());
				if (undoContext != null) {
					try {
						DocumentChange documentChange = new DocumentChange(event, currentOldEndLine);
						final CompoundCommand compoundCommand = new HandleDocumentChangeCommand(undoContext);
						for (Shape shape : getModelChildren()) {
							compoundCommand.add(shape.handleDocumentChange(documentChange));
						}
						if (!compoundCommand.isEmpty()) {
							// We are already in the UI thread during this method. However, if we ran the command right now,
							// the projection model might be in an illegal state when the GEF edit parts attempted to refresh
							// visuals. So we queue this up and let the UI thread get to it when document change processing has
							// finished.
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									getRoot().getViewer().getEditDomain().getCommandStack().execute(compoundCommand);
								}
							});
						}
					} catch (BadLocationException e) {
						// Give up.
					}
				}
			}

			currentOldEndLine = -1;
		}
	};

	/**
	 * Upon activation, attach to the model element as a property change
	 * listener.
	 */
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			((ModelElement) getModel()).addPropertyChangeListener(this);
			setProjectionEnabledListening(true);
			setProjectionChangeListening(true);
			setOperationHistoryListening(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		// disallows the removal of this edit part from its parent
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new RootComponentEditPolicy());
		// handles constraint changes (e.g. moving and/or resizing) of model
		// elements
		// and creation of new model elements
		installEditPolicy(EditPolicy.LAYOUT_ROLE,
				new ShapesXYLayoutEditPolicy((TextViewerRootEditPart)this.getRoot()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		Figure f = new FreeformLayer();
		f.setBorder(new MarginBorder(3));
		f.setLayoutManager(new FreeformLayout());

		return f;
	}

	/**
	 * Upon deactivation, detach from the model element as a property change
	 * listener.
	 */
	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			((ModelElement) getModel()).removePropertyChangeListener(this);
			setProjectionEnabledListening(false);
			setProjectionChangeListening(false);
			setOperationHistoryListening(false);
		}
	}

	ResourceShapeList getCastedModel() {
		return (ResourceShapeList) getModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	@Override
	protected List<Shape> getModelChildren() {
		return getCastedModel().getShapes(); // return a list of shapes
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		// these properties are fired when Shapes are added into or removed from
		// the ShapeDiagram instance and must cause a call of refreshChildren()
		// to update the diagram's contents.
		if (ResourceShapeList.CHILD_ADDED_PROP.equals(prop)
				|| ResourceShapeList.CHILD_REMOVED_PROP.equals(prop)) {
			refreshChildren();
		}
	}

	public boolean isActiveLayer () {
		return ((CollageContentEditPart)getParent()).getActiveLayerEditPart() == this;
	}
	
	/**
	 * EditPolicy for the Figure used by this edit part. Children of
	 * XYLayoutEditPolicy can be used in Figures with XYLayout.
	 * 
	 * @author Elias Volanakis
	 */
	private static class ShapesXYLayoutEditPolicy extends XYLayoutEditPolicy {
		private final TextViewerRootEditPart rootEditPart;
		
		ShapesXYLayoutEditPolicy (TextViewerRootEditPart rootEditPart) {
			this.rootEditPart = rootEditPart;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see ConstrainedLayoutEditPolicy#createChangeConstraintCommand(
		 * ChangeBoundsRequest, EditPart, Object)
		 */
		@Override
		protected Command createChangeConstraintCommand(
				ChangeBoundsRequest request, EditPart child, Object constraint) {
			if (child instanceof ShapeEditPart
					&& constraint instanceof Rectangle) {
				// return a command that can move and/or resize a Shape
				Rectangle constraintCopy = ((Rectangle)constraint).getCopy();
				
				Shape shape = (Shape)child.getModel();
				ShapeBoundaries bounds = shape.getBoundaries();
				bounds.setFromGEFConstraint(rootEditPart.getTextViewer(), constraintCopy);
				
				return new ShapeSetConstraintCommand((Shape)child.getModel(), request, bounds);
			}
			return super.createChangeConstraintCommand(request, child,
					constraint);
		}
		
		@Override
		protected Command createChangeConstraintCommand(EditPart child,	Object constraint) {
			return null;
		}
		
		@Override
		protected IFigure createSizeOnDropFeedback(CreateRequest createRequest) {
			if (createRequest instanceof ICreateRequestCustomFeedback) {
				IFigure result = ((ICreateRequestCustomFeedback)createRequest).createSizeOnDropFeedback();
				addFeedback(result);
				return result;
			}
			return super.createSizeOnDropFeedback(createRequest);
		}

		@Override
		protected IFigure getSizeOnDropFeedback(CreateRequest createRequest) {
			IFigure feedback = super.getSizeOnDropFeedback(createRequest);
			if (createRequest instanceof ICreateRequestCustomFeedback) {
				return ((ICreateRequestCustomFeedback)createRequest).updateSizeOnDropFeedback(feedback);
			}
			return feedback;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see LayoutEditPolicy#getCreateCommand(CreateRequest)
		 */
		@Override
		protected Command getCreateCommand(CreateRequest request) {
			Object childClass = request.getNewObjectType();
			if (childClass instanceof Class<?> && Shape.class.isAssignableFrom((Class<?>)childClass)) {
				// return a command that can add a Shape to a ShapesDiagram
				Shape newShape = (Shape) request.getNewObject();
				
				Rectangle constraint = (Rectangle) getConstraintFor(request);
				
				ShapeBoundaries bounds = newShape.getBoundaries();
				bounds.setFromGEFConstraint(rootEditPart.getTextViewer(), constraint);
				
				return new ShapeCreateCommand(newShape,	(ResourceShapeList) getHost().getModel(), bounds);
			}
			return null;
		}

		@Override			
		protected Insets getCreationFeedbackOffset(CreateRequest createRequest) {
			if (createRequest instanceof ICreateRequestCustomFeedback) {
				return ((ICreateRequestCustomFeedback)createRequest).getCreationFeedbackOffset();
			}
			return super.getCreationFeedbackOffset(createRequest);
		}
	}

	@Override
	protected void refreshVisuals() {
		Iterator<?> iter = this.getChildren().iterator();
		while (iter.hasNext()) {
			Object part = iter.next();
			if (part instanceof ShapeEditPart) {
				((ShapeEditPart)part).refreshVisuals();
			}
		}

		ITextViewer textViewer = ((TextViewerRootEditPart)this.getRoot()).getTextViewer();
		StyledText textWidget = textViewer.getTextWidget();
		if (textWidget != null && !textWidget.isDisposed()) {
			textWidget.redraw();
		}
	}

	private void setOperationHistoryListening (boolean enabled) {
		if (enabled) {
			OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(this);
		} else {
			OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(this);
		}
		setDocumentHistoryListening(enabled);
	}
	
	private void setDocumentHistoryListening (boolean enabled) {
		ITextViewer textViewer = getTextViewer();
		if (textViewer != null) {
			IDocument document = textViewer.getDocument();
			if (document != null) {
				if (enabled) {
					document.addDocumentListener(documentListener);
				} else {
					document.removeDocumentListener(documentListener);				
				}
			}
		}
	}
	
	private void setProjectionEnabledListening (boolean enabled) {
		ProjectionViewer projectionViewer = getProjectionViewer();
		if (projectionViewer != null) {
			if (enabled) {
				projectionViewer.addProjectionListener(this);
			} else {
				projectionViewer.removeProjectionListener(this);				
			}
		}
	}
	
	@Override
	public void projectionEnabled() {
		setProjectionChangeListening(true);
		refreshVisuals();
	}

	@Override
	public void projectionDisabled() {
		setProjectionChangeListening(false);
		refreshVisuals();
	}
	
	private void setProjectionChangeListening (boolean enabled) {
		ProjectionViewer projectionViewer = getProjectionViewer();
		if (projectionViewer != null) {
			ProjectionAnnotationModel projectionAnnotationModel = projectionViewer.getProjectionAnnotationModel();
			if (enabled) {
				StyledText textWidget = projectionViewer.getTextWidget();
				if (textWidget != null) {
					if (projectionAnnotationModel != null) {
						projectionAnnotationModel.addAnnotationModelListener(this);
						currentProjectionAnnotationModel = projectionAnnotationModel;
					}
					
					textWidget.addDisposeListener(this);
				}
			} else {
				if (currentProjectionAnnotationModel != null) {
					currentProjectionAnnotationModel.removeAnnotationModelListener(this);
				}
				if (projectionAnnotationModel != null && currentProjectionAnnotationModel != projectionAnnotationModel) {
					projectionAnnotationModel.removeAnnotationModelListener(this);
				}
				
				StyledText textWidget = projectionViewer.getTextWidget();
				if (textWidget != null && !textWidget.isDisposed()) {
					textWidget.removeDisposeListener(this);
				}
			}
		}
	}

	@Override
	public void modelChanged(IAnnotationModel model) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				refreshVisuals();		
			}
		});
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		// cf. org.eclipse.text.undo.DocumentUndoManager$HistoryListener
		IUndoableOperation op = event.getOperation();
		final IUndoContext context = CollageUtilities.getTextViewerUndoContext(getTextViewer());
		if (context != null) {
			switch (event.getEventType()) {
			case OperationHistoryEvent.ABOUT_TO_UNDO:
			case OperationHistoryEvent.ABOUT_TO_REDO:
				if (op.hasContext(context)) {
					setDocumentHistoryListening(false);
				}
				break;
			case OperationHistoryEvent.UNDONE:
			case OperationHistoryEvent.REDONE:
			case OperationHistoryEvent.OPERATION_NOT_OK:
				if (op.hasContext(context)) {
					if (event.getEventType() == OperationHistoryEvent.REDONE) {
						final IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
						IUndoableOperation nextOp = operationHistory.getRedoOperation(context);
						if (nextOp instanceof WrappingOperation) {
							Command command = ((WrappingOperation)nextOp).getCommandStack().getRedoCommand();
							if (command != null && command instanceof HandleDocumentChangeCommand) {
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										if (operationHistory.canRedo(context)) {
											try {
												operationHistory.redo(context, null, null);
											} catch (ExecutionException e) {
												// Ignore.
											}
										}
									}
								});
							}
						}
					} else if (event.getEventType() == OperationHistoryEvent.UNDONE &&
							!(op instanceof WrappingOperation)) {
						refreshVisuals();
					}
					setDocumentHistoryListening(true);
				}
				break;
			}
		}
	}

	private ITextViewer getTextViewer () {
		TextViewerRootEditPart root = (TextViewerRootEditPart)getRoot();
		if (root != null) {
			return root.getTextViewer();
		}
		return null;
	}
	
	private ProjectionViewer getProjectionViewer () {
		ITextViewer textViewer = getTextViewer();
		if (textViewer != null && textViewer instanceof ProjectionViewer) {
			return (ProjectionViewer)textViewer;
		}
		return null;
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		setProjectionEnabledListening(false);
		setProjectionChangeListening(false);		
		setOperationHistoryListening(false);
	}
}
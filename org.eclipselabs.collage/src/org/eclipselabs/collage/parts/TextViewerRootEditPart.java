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
 *     Alex Bradley    - adaptation for use on TextViewer (StyledText)
 *******************************************************************************/
package org.eclipselabs.collage.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.AutoexposeHelper;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.gef.editparts.GuideLayer;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.SimpleRootEditPart;
import org.eclipse.gef.tools.MarqueeDragTracker;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipselabs.collage.model.resourceid.ResourceIdentifier;
import org.eclipselabs.collage.ui.CollageUI;

/**
 * A RootEditPart that can be used with {@link ITextViewer}s.
 * Adapted from {@link org.eclipse.gef.editparts.ScalableRootEditPart ScalableRootEditPart}.
 * @author Alex Bradley
 * @author Eric Bordeau
 */
public class TextViewerRootEditPart extends SimpleRootEditPart implements
		LayerConstants, LayerManager {

	class FeedbackLayer extends Layer {
		FeedbackLayer() {
			setEnabled(false);
		}

		@Override
		public Dimension getPreferredSize(int wHint, int hHint) {
			Rectangle rect = new Rectangle();
			for (int i = 0; i < getChildren().size(); i++)
				rect.union(((IFigure) getChildren().get(i)).getBounds());
			return rect.getSize();
		}

	}

	private LayeredPane innerLayers;
    private LayeredPane printableLayers;
	private PropertyChangeListener gridListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String property = evt.getPropertyName();
			if (property.equals(SnapToGrid.PROPERTY_GRID_ORIGIN)
					|| property.equals(SnapToGrid.PROPERTY_GRID_SPACING)
					|| property.equals(SnapToGrid.PROPERTY_GRID_VISIBLE))
				refreshGridLayer();
		}
	};

	private final CollageUI collageUI;
	private final ITextViewer textViewer;
	private final ResourceIdentifier resourceIdentification;
	
	/**
	 * Constructor for TextViewerRootEditPart
	 */
	public TextViewerRootEditPart(CollageUI ui, ITextViewer viewer, ResourceIdentifier resourceIdentification) {
		this.collageUI = ui;
		this.textViewer = viewer;
		this.resourceIdentification = resourceIdentification;
	}

	public CollageUI getCollageUI () {
		return collageUI;
	}
	
	public ITextViewer getTextViewer() {
		return textViewer;
	}
	
	public ResourceIdentifier getResourceIdentification() {
		return resourceIdentification;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		final Viewport viewport = createViewport();
		final StyledText textWidget = textViewer.getTextWidget();

		innerLayers = new LayeredPane(); 
		createLayers(innerLayers);

		viewport.setContents(innerLayers);
		
		if (textWidget != null) {
			refreshSizeAndScroll(viewport);
			
			textWidget.addControlListener(new ControlAdapter () {
				@Override
				public void controlResized(ControlEvent e) {
					refreshSizeAndScroll(viewport);
				}
			});
			
			// Note that scrolling by itself can alter the size of the StyledText's scrollable area
			// (the horizontal scroll bar can expand when longer lines scroll into view) so we must
			// refresh size in our handling of scroll events.
			
			textWidget.getHorizontalBar().addSelectionListener(new SelectionAdapter () {
				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshSizeAndScroll(viewport);
				}
				
			});
			
			textViewer.addViewportListener(new IViewportListener() {
				@Override
				public void viewportChanged(int verticalOffset) {
					refreshSizeAndScroll(viewport);
				}
			});
		}

		return viewport;
	}

	private void refreshSizeAndScroll (Viewport viewport) {
		StyledText textWidget = textViewer.getTextWidget();

		if (textWidget != null && !textWidget.isDisposed()) {
			// Work around weird behaviour of scrollbar getMaximum() method - if scrollable area fully exposed
			// in given dimension getMaximum() returns 1
			int width = Math.max(textWidget.getHorizontalBar().getMaximum(), textWidget.getClientArea().width);
			int height = Math.max(textWidget.getVerticalBar().getMaximum(), textWidget.getClientArea().height);
			innerLayers.setPreferredSize(width, height);
			
			viewport.getHorizontalRangeModel().setValue(textWidget.getHorizontalPixel());
			viewport.getVerticalRangeModel().setValue(textWidget.getTopPixel());
			
			// appears to be unnecessary
			// textWidget.redraw();

//			System.out.format("Viewport scrolled to (%d, %d)\n", textWidget.getTopPixel(), textWidget.getHorizontalPixel());
//			CollageUtilities.printAllBounds(viewport);
		}
	}
	
	/**
	 * Creates a {@link GridLayer grid}. Sub-classes can override this method to
	 * customize the appearance of the grid. The grid layer should be the first
	 * layer (i.e., beneath the primary layer) if it is not to cover up parts on
	 * the primary layer. In that case, the primary layer should be transparent
	 * so that the grid is visible.
	 * 
	 * @return the newly created GridLayer
	 */
	protected GridLayer createGridLayer() {
		return new GridLayer();
	}

	/**
	 * Creates the top-most set of layers on the given layered pane
	 * 
	 * @param layeredPane
	 *            the parent for the created layers
	 */
	protected void createLayers(LayeredPane layeredPane) {
//		layeredPane.add(getScaledLayers(), SCALABLE_LAYERS);
		// substituted in here
		layeredPane.add(getPrintableLayers(), PRINTABLE_LAYERS);
		
		layeredPane.add(new Layer() {
			@Override
			public Dimension getPreferredSize(int wHint, int hHint) {
				return new Dimension();
			}
		}, HANDLE_LAYER);
		layeredPane.add(new FeedbackLayer(), FEEDBACK_LAYER);
		layeredPane.add(new GuideLayer(), GUIDE_LAYER);
	}

	/**
	 * Creates a layered pane and the layers that should be printed.
	 * 
	 * @see org.eclipse.gef.print.PrintGraphicalViewerOperation
	 * @return a new LayeredPane containing the printable layers
	 */
	protected LayeredPane createPrintableLayers() {
		LayeredPane pane = new LayeredPane();

		Layer layer = new Layer();
		layer.setLayoutManager(new StackLayout());
		pane.add(layer, PRIMARY_LAYER);

		layer = new ConnectionLayer();
		layer.setPreferredSize(new Dimension(5, 5));
		pane.add(layer, CONNECTION_LAYER);

		return pane;
	}

	/**
	 * Constructs the viewport that will be used to contain all of the layers.
	 * 
	 * @return a new Viewport
	 */
	protected Viewport createViewport() {
		Viewport viewport = new Viewport(true);
		return viewport;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
		if (key == AutoexposeHelper.class)
			return null;
		    // We don't want autoexpose for now - it was firing inappropriately when the mouse hovered
		    // near view boundary
		    // return new ViewportAutoexposeHelper(this);
		return super.getAdapter(key);
	}

	/**
	 * The contents' Figure will be added to the PRIMARY_LAYER.
	 * 
	 * @see org.eclipse.gef.GraphicalEditPart#getContentPane()
	 */
	@Override
	public IFigure getContentPane() {
		return getLayer(PRIMARY_LAYER);
	}

	/**
	 * Should not be called, but returns a MarqeeDragTracker for good measure.
	 * 
	 * @see org.eclipse.gef.EditPart#getDragTracker(org.eclipse.gef.Request)
	 */
	@Override
	public DragTracker getDragTracker(Request req) {
		/*
		 * The root will only be asked for a drag tracker if for some reason the
		 * contents EditPart is not selectable or has a non-opaque figure.
		 */
		return new MarqueeDragTracker();
	}

	/**
	 * Returns the layer indicated by the key. Searches all layered panes.
	 * 
	 * @see LayerManager#getLayer(Object)
	 */
	@Override
	public IFigure getLayer(Object key) {
		if (innerLayers == null)
			return null;
		IFigure layer = printableLayers.getLayer(key);
		if (layer != null)
			return layer;
		return innerLayers.getLayer(key);
	}

	/**
	 * The root editpart does not have a real model. The LayerManager ID is
	 * returned so that this editpart gets registered using that key.
	 * 
	 * @see org.eclipse.gef.EditPart#getModel()
	 */
	@Override
	public Object getModel() {
		return LayerManager.ID;
	}

	/**
	 * Returns the LayeredPane that should be used during printing. This layer
	 * will be identified using {@link LayerConstants#PRINTABLE_LAYERS}.
	 * 
	 * @return the layered pane containing all printable content
	 */
	protected LayeredPane getPrintableLayers() {
		if (printableLayers == null)
			printableLayers = createPrintableLayers();
		return printableLayers;
	}

	/**
	 * Updates the {@link GridLayer grid} based on properties set on the
	 * {@link #getViewer() graphical viewer}:
	 * {@link SnapToGrid#PROPERTY_GRID_VISIBLE},
	 * {@link SnapToGrid#PROPERTY_GRID_SPACING}, and
	 * {@link SnapToGrid#PROPERTY_GRID_ORIGIN}.
	 * <p>
	 * This method is invoked initially when the GridLayer is created, and when
	 * any of the above-mentioned properties are changed on the viewer.
	 */
	protected void refreshGridLayer() {
		boolean visible = false;
		GridLayer grid = (GridLayer) getLayer(GRID_LAYER);
		Boolean val = (Boolean) getViewer().getProperty(
				SnapToGrid.PROPERTY_GRID_VISIBLE);
		if (val != null)
			visible = val.booleanValue();
		grid.setOrigin((Point) getViewer().getProperty(
				SnapToGrid.PROPERTY_GRID_ORIGIN));
		grid.setSpacing((Dimension) getViewer().getProperty(
				SnapToGrid.PROPERTY_GRID_SPACING));
		grid.setVisible(visible);
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#register()
	 */
	@Override
	protected void register() {
		super.register();
		if (getLayer(GRID_LAYER) != null) {
			getViewer().addPropertyChangeListener(gridListener);
			refreshGridLayer();
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#unregister()
	 */
	@Override
	protected void unregister() {
		getViewer().removePropertyChangeListener(gridListener);
		super.unregister();
	}
}


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
package org.eclipselabs.collage.draw.toolbar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipselabs.collage.colourpicker.ColourPickerExtension;
import org.eclipselabs.collage.draw.DrawActivator;
import org.eclipselabs.collage.ui.CollageUI;
import org.eclipselabs.collage.ui.ICollageToolBarContributor;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Contribute line width widget to the Collage toolbar.
 * @author Alex Bradley
 */
public class LineWidthToolBarContribution implements ICollageToolBarContributor {
	private static final String LINE_WIDTH_SPINNER_TOOLTIP = "Line width";
	private static final String LINE_WIDTH_BUTTON_TOOLTIP = "Set line width";

	private Image lineWidthImage = null;
	private ToolItem lineWidthButton = null;
	private Spinner lineWidthSpinner = null;

	@Override
	public void addContribution(ToolBar toolBar, final CollageUI collageUI) {
		lineWidthButton = new ToolItem(toolBar, SWT.CHECK);
		lineWidthButton.setToolTipText(LINE_WIDTH_BUTTON_TOOLTIP);
		lineWidthButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				lineWidthSpinner.setVisible(lineWidthButton.getSelection());
			}
		});
		updateLineWidthImage(ColourPickerExtension.getDrawingColour(collageUI), DrawActivator.getLineWidth(collageUI));
		
		ToolItem sep = new ToolItem(toolBar, SWT.SEPARATOR);
		lineWidthSpinner = new Spinner(toolBar, SWT.BORDER);
		lineWidthSpinner.setVisible(false);
		lineWidthSpinner.setToolTipText(LINE_WIDTH_SPINNER_TOOLTIP);
		lineWidthSpinner.setMinimum(1);
		lineWidthSpinner.setMaximum(30);
		lineWidthSpinner.setSelection(DrawActivator.getLineWidth(collageUI));
		lineWidthSpinner.setIncrement(1);
		lineWidthSpinner.setPageIncrement(3);
		lineWidthSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				collageUI.setProperty(DrawActivator.LINE_WIDTH_PROPERTY_ID, lineWidthSpinner.getSelection());
				updateLineWidthImage(ColourPickerExtension.getDrawingColour(collageUI), DrawActivator.getLineWidth(collageUI));
			}
		});
		lineWidthSpinner.pack();
		sep.setWidth(lineWidthSpinner.getSize().x);
		sep.setControl(lineWidthSpinner);
		
		collageUI.addPropertyChangeListener(CollageUI.EDIT_ENABLED_PROPERTY_ID, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue() instanceof Boolean && !(Boolean)evt.getNewValue()) {
					lineWidthButton.setSelection(false);
					lineWidthSpinner.setVisible(false);
				}
			}
		});
		
		collageUI.addPropertyChangeListener(CollageUI.TOOLBAR_DISPOSED_PROPERTY_ID, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue() instanceof Boolean && (Boolean)evt.getNewValue()) {
					if (lineWidthImage != null)
						lineWidthImage.dispose();
				}
			}
		});
		
		collageUI.addPropertyChangeListener(ColourPickerExtension.COLOUR_PROPERTY_ID, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateLineWidthImage(ColourPickerExtension.getDrawingColour(collageUI), DrawActivator.getLineWidth(collageUI));
			}
		});
	}

	private void updateLineWidthImage (RGB newColour, int newLineWidth) {
		if (lineWidthButton == null || lineWidthButton.isDisposed())
			return;
		
		Image oldLineWidthImage = lineWidthImage;

		RGB drawColour = newColour;
		RGB bgColour = drawColour.equals(CollageUtilities.RGB_WHITE) ? CollageUtilities.RGB_BLACK : CollageUtilities.RGB_WHITE;
		PaletteData paletteData = new PaletteData(new RGB[] {bgColour, drawColour});
		ImageData imageData = new ImageData(CollageUI.TOOLBAR_IMAGE_SIZE, CollageUI.TOOLBAR_IMAGE_SIZE, 1, paletteData);
		imageData.transparentPixel = 0;
		drawLineWidthImageData(imageData, newLineWidth);
		
		lineWidthImage = new Image(Display.getCurrent(), imageData);
		lineWidthButton.setImage(lineWidthImage);
		
		if (oldLineWidthImage != null) {
			oldLineWidthImage.dispose();
		}
	}
	
	private static void drawLineWidthImageData (ImageData imageData, int lineWidth) {
		int topY = Math.max(0, (CollageUI.TOOLBAR_IMAGE_SIZE / 2) - (lineWidth / 2) - 1);
		int bottomY = Math.min(CollageUI.TOOLBAR_IMAGE_SIZE, topY + lineWidth);
		for (int y = topY; y < bottomY; y++) {
			for (int x = 0; x < CollageUI.TOOLBAR_IMAGE_SIZE; x++) {
				imageData.setPixel(x, y, 1);
			}		
		}
	}
}

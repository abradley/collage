/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.colourpicker.toolbar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipselabs.collage.colourpicker.ColourPickerExtension;
import org.eclipselabs.collage.ui.CollageUI;
import org.eclipselabs.collage.ui.ICollageToolBarContributor;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Contribute the colour picker to the Collage toolbar.
 * @author Alex Bradley
 */
public class ColourPickerToolBarContributor implements
		ICollageToolBarContributor {
	private static final String COLOUR_DIALOG_TITLE = "Collage Drawing Colour";
	private static final String COLOUR_BUTTON_TOOLTIP = "Set drawing colour";
	
	private ToolItem colourPicker;
	private Image colourPickerImage;
	
	@Override
	public void addContribution(final ToolBar toolBar, final CollageUI collageUI) {
		colourPicker = new ToolItem(toolBar, SWT.PUSH);
		colourPicker.setToolTipText(COLOUR_BUTTON_TOOLTIP);
		colourPickerImage = new Image(toolBar.getDisplay(), CollageUI.TOOLBAR_IMAGE_SIZE, CollageUI.TOOLBAR_IMAGE_SIZE);
		updateColourPickerImage(ColourPickerExtension.getDrawingColour(collageUI));
		colourPicker.setImage(colourPickerImage);
		colourPicker.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// cf. http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/ColorDialogExample.htm
				ColorDialog colorDialog = new ColorDialog(toolBar.getShell());
				colorDialog.setText(COLOUR_DIALOG_TITLE);
				colorDialog.setRGB(ColourPickerExtension.getDrawingColour(collageUI));
				RGB newColour = colorDialog.open();
				if (newColour == null)
					return;
				
				setDrawingColour(collageUI, newColour);
				updateColourPickerImage(newColour);
				colourPicker.setImage(colourPickerImage);
			}
		});
		
		collageUI.addPropertyChangeListener(CollageUI.TOOLBAR_DISPOSED_PROPERTY_ID, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue() instanceof Boolean && (Boolean)evt.getNewValue()) {
					if (colourPickerImage != null)
						colourPickerImage.dispose();
				}
			}
		});
	}
	
	private static void setDrawingColour (CollageUI collageUI, RGB value) {
		collageUI.setProperty(ColourPickerExtension.COLOUR_PROPERTY_ID, value);
	}
	
	/**
	 * Redraw the colour picker image in a new colour.
	 * @param newColour New colour to use.
	 */
	private void updateColourPickerImage (RGB newColour) {
		GC gc = new GC(colourPickerImage);
		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
		gc.setBackground(CollageUtilities.getColor(newColour));
		gc.fillRectangle(0, 0, 15, 15);
		gc.drawRectangle(0, 0, 15, 15);
		gc.dispose();
	}
}

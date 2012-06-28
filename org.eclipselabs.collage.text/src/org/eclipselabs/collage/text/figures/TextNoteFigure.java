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
package org.eclipselabs.collage.text.figures;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.ParagraphTextLayout;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.swt.widgets.Display;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * A rounded rectangle figure containing a label at the top and, below it, an 
 * embedded TextFlow within a FlowPage that contains text.
 * @author Alex Bradley
 * @author IBM Corporation
 */
public class TextNoteFigure extends RoundedRectangle {
	private Label topLabel;
	
	private FlowPage flowPage;
	
	/** The inner TextFlow **/
	private TextFlow textFlow;

	private static final Dimension CORNER_DIMENSIONS = new Dimension(10, 10);
	private static final int BORDER_LINE_WIDTH = 2;
	private static final int DEFAULT_BORDER_WIDTH = 7;
	
	/**
	 * Creates a new TextNoteFigure with a default border size.
	 */
	public TextNoteFigure() {
		this(DEFAULT_BORDER_WIDTH);
	}

	/**
	 * Creates a new TextNoteFigure with a MarginBorder that is the given size and
	 * a FlowPage containing a TextFlow with the style WORD_WRAP_SOFT.
	 * 
	 * @param borderSize
	 *            the size of the MarginBorder
	 */
	public TextNoteFigure(int borderSize) {
		setCornerDimensions(CORNER_DIMENSIONS);
		setLineWidth(BORDER_LINE_WIDTH);
		setFont(Display.getCurrent().getSystemFont());
		setBorder(new MarginBorder(borderSize));
		flowPage = new FlowPage();

		topLabel = new Label();
		topLabel.setFont(CollageUtilities.getFontRegistry().getBoldSystemFontAtSize(8));
		
		textFlow = new TextFlow();
		textFlow.setForegroundColor(ColorConstants.black);

		textFlow.setLayoutManager(new ParagraphTextLayout(textFlow,
				ParagraphTextLayout.WORD_WRAP_SOFT));

		flowPage.add(textFlow);

		BorderLayout layout = new BorderLayout();
		setLayoutManager(layout);
		add(topLabel);
		add(flowPage);
		layout.setVerticalSpacing(3);
		layout.setConstraint(topLabel, BorderLayout.TOP);
		layout.setConstraint(flowPage, BorderLayout.CENTER);
	}

	/**
	 * Returns the text inside the TextFlow.
	 * 
	 * @return the text flow inside the text.
	 */
	public String getText() {
		return textFlow.getText();
	}

	/**
	 * Sets the text of the TextFlow to the given value.
	 * 
	 * @param newText
	 *            the new text value.
	 */
	public void setText(String newText) {
		textFlow.setText(newText);
	}

	/**
	 * Set the text for the label above the text box.
	 * @param newText New text for the label.
	 */
	public void setTopLabelText(String newText) {
		topLabel.setText(newText);
	}
	
	/**
	 * Get the boundaries of the editable text area (in parent coordinates).
	 * @return Rectangle giving the boundaries of the editable text area.
	 */
	public Rectangle getEditableArea () {
		Rectangle area = flowPage.getClientArea();
		flowPage.translateToParent(area);
		return area;
	}
}

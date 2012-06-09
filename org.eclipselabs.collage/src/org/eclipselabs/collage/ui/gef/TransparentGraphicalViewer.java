/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.ui.gef;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.ui.parts.DomainEventDispatcher;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.swt.custom.StyledText;
import org.eclipselabs.collage.ui.CollageUI;

/**
 * Graphical edit part viewer that can be installed on a {@link StyledText}.
 * @author Alex Bradley
 */
public class TransparentGraphicalViewer extends GraphicalViewerImpl {
	/**
	 * Zero-size invisible figure.
	 */
	private static class NullFigure extends Figure {
		public NullFigure () {
			setBounds(new Rectangle(0, 0, 0, 0));
			setOpaque(false);
			setVisible(false);
			setEnabled(false);
		}
	}

	private final CollageUI collageUI;
	
	public TransparentGraphicalViewer(CollageUI collageUI) {
		super();
		
		this.collageUI = collageUI;
	}
	
	private DomainEventDispatcher collageEventDispatcher = null;
	
	@Override
	protected LightweightSystem createLightweightSystem() {
		return new PaintSharingLightweightSystem();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected DomainEventDispatcher getEventDispatcher() {
		if (collageEventDispatcher != null) {
			return collageEventDispatcher;
		}
		return super.getEventDispatcher();
	}

	public CollageUI getCollageUI() {
		return collageUI;
	}

	@Override
	public void setEditDomain(EditDomain domain) {
		// Provide a specialized domain event dispatcher to work around two key handling issues (see documentation
		// of CollageDomainEventDispatcher for details.)
		// The super call will have the unfortunate effect of setting a DomainEventDispatcher which will be
		// immediately replaced with ours; however, we need the superclass's call to its superclass setEditDomain
		// method.
		super.setEditDomain(domain);
		getLightweightSystem().setEventDispatcher(
				collageEventDispatcher = new CollageDomainEventDispatcher(domain, this));
	}

	@Override
	protected void hookControl() {
		super.hookControl();
		getLightweightSystem().getRootFigure().setOpaque(false);
	}

	@Override
	protected void unhookControl() {
		super.unhookControl();
		getLightweightSystem().setEventDispatcher(NullEventDispatcher.getInstance());
		getLightweightSystem().setContents(new NullFigure());
	}
}

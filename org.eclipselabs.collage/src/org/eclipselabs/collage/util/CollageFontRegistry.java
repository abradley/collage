/*******************************************************************************
 * Copyright (c) 2011 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipselabs.collage.util;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

/**
 * Plugin font registry with some extra utility functions
 * @author Alex Bradley
 */
public class CollageFontRegistry extends FontRegistry {
	private static final String SYS_FONT = "system font";
	
	public CollageFontRegistry () {
		super();
		put(SYS_FONT, Display.getCurrent().getSystemFont().getFontData());
	}
	
	public Font getSystemFontAtSize (int size) {
		final String key = SYS_FONT + " " + size;
		if (!hasValueFor(key)) {
			put(key, getDescriptor(SYS_FONT).setHeight(size).getFontData());
		}
		return get(key);
	}

	public Font getBoldSystemFontAtSize (int size) {
		final String key = SYS_FONT + " " + size;
		if (!hasValueFor(key)) {
			put(key, getDescriptor(SYS_FONT).setStyle(SWT.BOLD).setHeight(size).getFontData());
		}
		return get(key);
	}

	public Font getBoldSystemFont () {
		return getBold(SYS_FONT);
	}
}

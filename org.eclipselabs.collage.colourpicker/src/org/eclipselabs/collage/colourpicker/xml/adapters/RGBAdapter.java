/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.colourpicker.xml.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.swt.graphics.RGB;

/**
 * Convert a SWT {@link RGB} colour description to and from a six-digit hexadecimal
 * representation. 
 * @author Alex Bradley
 */
public class RGBAdapter extends XmlAdapter<String, RGB> {
	@Override
	public RGB unmarshal(String v) throws Exception {
		if (v.length() == 6) {
			int i = Integer.parseInt(v, 16);
			return new RGB(i >> 16, (i & 0xff00) >> 8, i & 0xff);
		}
		throw new IllegalArgumentException("Invalid format for RGB string: " + v);
	}

	@Override
	public String marshal(RGB v) throws Exception {
		return String.format("%02x%02x%02x", v.red, v.green, v.blue);
	}
}

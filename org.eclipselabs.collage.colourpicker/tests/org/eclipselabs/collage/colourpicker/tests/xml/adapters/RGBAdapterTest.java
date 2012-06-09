/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.colourpicker.tests.xml.adapters;

import org.eclipse.swt.graphics.RGB;
import org.eclipselabs.collage.colourpicker.xml.adapters.RGBAdapter;
import org.junit.Test;
import static junit.framework.Assert.*;

/**
 * Tests for RGB <-> String adapter.
 * @author Alex Bradley
 */
public class RGBAdapterTest {
	private static final String[] BAD_RGB_STRINGS = {"00ae9g", "0ff", "0f0f0f0", "#fbfac6", "", "nonsense"};
	
	@Test
	public void testMarshal () throws Exception {
		RGBAdapter adapter = new RGBAdapter();
		assertEquals(adapter.marshal(new RGB(255, 0, 0)), "ff0000");
		assertEquals(adapter.marshal(new RGB(0, 255, 0)), "00ff00");
		assertEquals(adapter.marshal(new RGB(0, 0, 255)), "0000ff");
		assertEquals(adapter.marshal(new RGB(128, 128, 255)), "8080ff");
		assertEquals(adapter.marshal(new RGB(45, 189, 252)), "2dbdfc");
	}
	
	@Test
	public void testUnmarshal () throws Exception {
		RGBAdapter adapter = new RGBAdapter();
		assertEquals(new RGB(255, 0, 0), adapter.unmarshal("ff0000"));
		assertEquals(new RGB(0, 255, 0), adapter.unmarshal("00ff00"));
		assertEquals(new RGB(0, 0, 255), adapter.unmarshal("0000ff"));
		assertEquals(new RGB(128, 128, 255), adapter.unmarshal("8080ff"));		
		assertEquals(new RGB(45, 189, 252), adapter.unmarshal("2dbdfc"));
		
		for (String s : BAD_RGB_STRINGS) {
			try {
				adapter.unmarshal(s); fail(s + " should not have been accepted");
			} catch (Exception e) {}
		}		
	}
}

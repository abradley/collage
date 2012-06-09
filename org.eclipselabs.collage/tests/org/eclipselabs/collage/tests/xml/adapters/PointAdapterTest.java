/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.tests.xml.adapters;

import org.eclipse.draw2d.geometry.Point;
import org.eclipselabs.collage.xml.adapters.PointAdapter;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Test the Point XML adapter.
 * @author Alex Bradley
 */
public class PointAdapterTest {
	@Test
	public void testMarshal () throws Exception {
		PointAdapter adapter = new PointAdapter();
		assertEquals("0,0", adapter.marshal(new Point(0,0)));
		assertEquals("5,10", adapter.marshal(new Point(5,10)));
		assertEquals("1211,312", adapter.marshal(new Point(1211,312)));
	}
	
	@Test
	public void testUnmarshal () throws Exception {
		PointAdapter adapter = new PointAdapter();
		assertEquals(adapter.unmarshal("0,0"), new Point(0,0));
		assertEquals(adapter.unmarshal("5,10"), new Point(5,10));
		assertEquals(adapter.unmarshal("1211,312"), new Point(1211,312));
	}	
}

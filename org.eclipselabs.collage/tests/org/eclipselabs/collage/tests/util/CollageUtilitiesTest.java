/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.tests.util;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipselabs.collage.util.CollageUtilities;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Tests for Collage utilities.
 * @author Alex Bradley
 */
public class CollageUtilitiesTest {
	private static final String[] INVALID_POS_INTS = {"0", "-1", "-57", "abc", "5i"};
	
	private static final Integer[] A1 = { 0, 4, 2, 9, 8 };
	private static final Integer[] A2 = { 3, 23, 1, 6, 18 };
	private static final Integer[] A1A2 = { 0, 4, 2, 9, 8, 3, 23, 1, 6, 18 };
	private static final Integer[] A1_15 = { 0, 4, 2, 9, 8, 15 };
	private static final Integer[] EMPTY = {};
	
	@Test
	public void testGetPositiveInteger () {
		assertEquals(CollageUtilities.getPositiveInteger("1"), 1);
		assertEquals(CollageUtilities.getPositiveInteger("1234"), 1234);
		for (String str : INVALID_POS_INTS) {
			try {
				CollageUtilities.getPositiveInteger(str);
				fail("getPositiveInteger should not have accepted " + str);
			} catch (IllegalArgumentException e) { }
		}
	}
	
	@Test
	public void testArrayConcat () {
		assertTrue(Arrays.equals(CollageUtilities.arrayConcat(A1, A2), A1A2));
		assertTrue(Arrays.equals(CollageUtilities.arrayConcat(A1, EMPTY), A1));
		assertTrue(Arrays.equals(CollageUtilities.arrayConcat(EMPTY, A1), A1));
	}

	@Test
	public void testArrayAppend () {
		assertTrue(Arrays.equals(CollageUtilities.arrayAppend(A1, 15), A1_15));
	}

	@Test
	public void testJoin () {
		assertEquals("", CollageUtilities.join(Collections.emptyList(), ""));
		assertEquals("", CollageUtilities.join(Collections.emptyList(), "+"));
		assertEquals("foo,foo,foo,foo,foo", CollageUtilities.join(Collections.nCopies(5, "foo"), ","));
		assertEquals("2+2+2+2+2+2", CollageUtilities.join(Collections.nCopies(6, 2), "+"));
	}
	
	@Test
	public void testCopyRGB () {
		RGB rgb = new RGB(123, 45, 67);
		RGB copy = CollageUtilities.copyRGB(rgb);
		rgb.blue = 51;
		assertEquals(copy.blue, 67);
		assertFalse(rgb.equals(copy));
	}
	
	@Test
	public void testPointToString () {
		assertEquals(CollageUtilities.pointToString(new Point(23, 45)), "(23, 45)");
		assertEquals(CollageUtilities.pointToString(new Point(-105, 0)), "(-105, 0)");
	}
	
	private static String[] INVALID_POINTS = {"(a, b)", "(-10, 20)", "(-4, -5)", "(2i, 3)", "foobar"};
	
	@Test
	public void testStringToPoint () {
		assertEquals(CollageUtilities.stringToPoint("(122, 331)"), new Point(122, 331));
		assertEquals(CollageUtilities.stringToPoint("(122,331)"), new Point(122, 331));
		assertEquals(CollageUtilities.stringToPoint(" ( 122 , 331 ) "), new Point(122, 331));
		assertEquals(CollageUtilities.stringToPoint("(82,      121)   "), new Point(82, 121));
		assertEquals(CollageUtilities.stringToPoint("(0,0)"), new Point(0, 0));
		assertEquals(CollageUtilities.stringToPoint("(0,5)"), new Point(0, 5));
		for (String str : INVALID_POINTS) {
			try {
				CollageUtilities.stringToPoint(str);
				fail("stringToPoint should not have accepted " + str);
			} catch (IllegalArgumentException e) { }
		}
	}
}

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
package org.eclipselabs.collage.draw.tests.xml.adapters;

import java.util.Arrays;

import org.eclipse.draw2d.geometry.PointList;
import org.eclipselabs.collage.draw.xml.adapters.PointListAdapter;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Test the {@link PointList} serialization methods.
 * @author Alex Bradley
 */
public class PointListAdapterTest {
	private static int[][] TEST_ARRAYS = {
		{73, 86, 88, 88, 36, 76, 70, 55, 14, 32, 64, 78, 63, 94, 47, 84, 50, 9, 57, 1},
		{68, 33, 11, 27, 44, 40, 41, 62, 92, 92, 20, 11, 76, 98, 87, 12, 67, 13, 75, 81}
	};
	
	@Test
	public void testMarshalUnmarshal () throws Exception {
		for (int[] array : TEST_ARRAYS) {
			PointListAdapter adapter = new PointListAdapter();
			PointList points = new PointList(array);
			assertTrue(Arrays.equals(array, adapter.unmarshal(adapter.marshal(points)).toIntArray()));
		}
	}
	
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipselabs.collage.model.PluginDependency;
import org.eclipselabs.collage.util.CollageExtensions;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Tests for extensions utility functions - currently, just tests for dependency merger.
 * @author Alex Bradley
 */
public class CollageExtensionsTest {
	private List<String> warnings = new ArrayList<String>();
	
	private static final List<PluginDependency> EMPTY_DEPS = Collections.emptyList();
	private static final List<PluginDependency> DEPS1 = Arrays.asList(new PluginDependency("foo", "0.1.0"),
			new PluginDependency("bar", "0.2.0"),
			new PluginDependency("baz", "0.1.0"));
	private static final List<PluginDependency> HIGHER_THAN_DEPS1 = Arrays.asList(new PluginDependency("foo", "0.2.0"),
			new PluginDependency("bar", "0.3.0"),
			new PluginDependency("baz", "0.4.0"));
	private static final List<PluginDependency> BIGGER_THAN_DEPS1 = Arrays.asList(new PluginDependency("foo", "0.1.0"),
			new PluginDependency("bar", "0.2.0"),
			new PluginDependency("baz", "0.1.0"),
			new PluginDependency("lambda", "1.2.5"),
			new PluginDependency("mu", "3.0"));
	private static final List<PluginDependency> HIGHER_BIGGER_THAN_DEPS1 = Arrays.asList(new PluginDependency("foo", "0.2.0"),
			new PluginDependency("bar", "0.3.0"),
			new PluginDependency("baz", "0.4.0"),
			new PluginDependency("lambda", "1.2.5"),
			new PluginDependency("mu", "3.0"));
	
	private List<String> getClearedWarnings () {
		warnings.clear();
		return warnings;
	}
	
	@Test
	public void testMergeDependenciesBasic () throws CoreException {
		assertEquals(DEPS1, CollageExtensions.mergeDependencies(DEPS1, DEPS1, getClearedWarnings()));
		assertTrue(warnings.isEmpty());
		
		assertEquals(DEPS1, CollageExtensions.mergeDependencies(DEPS1, EMPTY_DEPS, getClearedWarnings()));
		assertTrue(warnings.isEmpty());

		assertEquals(DEPS1, CollageExtensions.mergeDependencies(DEPS1, null, getClearedWarnings()));
		assertTrue(warnings.isEmpty());
		
		assertEquals(DEPS1, CollageExtensions.mergeDependencies(EMPTY_DEPS, DEPS1, getClearedWarnings()));
		assertEquals(3, warnings.size());
	}
	
	@Test
	public void testMergeDependenciesHigher () throws CoreException {
		assertEquals(HIGHER_THAN_DEPS1, CollageExtensions.mergeDependencies(HIGHER_THAN_DEPS1, DEPS1, getClearedWarnings()));
		assertTrue(warnings.isEmpty());

		try {
			CollageExtensions.mergeDependencies(DEPS1, HIGHER_THAN_DEPS1, getClearedWarnings());
			fail("Should have thrown exception");
		} catch (CoreException e) { }
	}

	@Test
	public void testMergeDependenciesBigger () throws CoreException {
		assertEquals(BIGGER_THAN_DEPS1, CollageExtensions.mergeDependencies(BIGGER_THAN_DEPS1, DEPS1, getClearedWarnings()));
		assertTrue(warnings.isEmpty());
		
		assertEquals(BIGGER_THAN_DEPS1, CollageExtensions.mergeDependencies(DEPS1, BIGGER_THAN_DEPS1, getClearedWarnings()));
		assertEquals(2, warnings.size());
	}
		
	@Test
	public void testMergeDependenciesHigherBigger () throws CoreException {
		try {
			CollageExtensions.mergeDependencies(BIGGER_THAN_DEPS1, HIGHER_THAN_DEPS1, getClearedWarnings());
			fail("Should have thrown exception");
		} catch (CoreException e) { }
		
		assertEquals(HIGHER_BIGGER_THAN_DEPS1, CollageExtensions.mergeDependencies(HIGHER_THAN_DEPS1, BIGGER_THAN_DEPS1, getClearedWarnings()));
		assertEquals(2, warnings.size());
	}
}

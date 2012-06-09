/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley    - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.Tool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.model.ModelElement;
import org.eclipselabs.collage.model.PluginDependency;
import org.eclipselabs.collage.ui.CollageUI;
import org.eclipselabs.collage.ui.ICollageToolBarContributor;
import org.osgi.framework.Version;

/**
 * Methods for accessing contributions made through our extension points.
 * @author Alex Bradley
 */
public final class CollageExtensions {
	public static final String EXTENSION_ID_TOOLS = "tools";
	public static final String EXTENSION_PROP_TOOLS_NAME = "name";
	public static final String EXTENSION_PROP_TOOLS_ICON = "icon";
	public static final String EXTENSION_PROP_TOOLS_CLASS = "class";
	
	public static final String EXTENSION_ID_TOOLBAR = "toolbar";
	public static final String EXTENSION_PROP_TOOLBAR_CONTRIBUTION_NAME = "name";
	public static final String EXTENSION_PROP_TOOLBAR_CONTRIBUTION_CLASS = "class";
	public static final String EXTENSION_PROP_TOOLBAR_CONTRIBUTION_POSITION = "position";
	
	public static final String EXTENSION_ID_MODEL = "model";
	public static final String EXTENSION_ELEMENT_MODEL_SHAPE = "shape";
	public static final String EXTENSION_ELEMENT_MODEL_VERSION = "modelVersion";
	public static final String EXTENSION_PROP_MODEL_CLASS = "modelClass";
	public static final String EXTENSION_PROP_MODEL_CONTROLLER_CLASS = "controllerClass";
	public static final String EXTENSION_PROP_MODEL_TREE_CONTROLLER_CLASS = "treeControllerClass";
	public static final String EXTENSION_PROP_MODEL_VERSION_VALUE = "value";
	
	private static final String WARNING_MISSING_MODEL_PLUGIN = "Stored data depends on plugin %s, which is not installed.";
	private static final String WARNING_NEWER_PLUGIN_MODEL_VERSION = "Version of plugin %s that exported stored data (%s) exceeds current version (%s)."; 
	
	/**
	 * Add {@link ToolItem}s for GEF tools contributed by extensions.
	 * @param toolBar Collage tool bar.
	 * @param collageUI Collage UI instance.
	 */
	public static void setupExtensionTools (ToolBar toolBar, final CollageUI collageUI) {
		IConfigurationElement[] confElts = Platform.getExtensionRegistry().getConfigurationElementsFor(CollageActivator.PLUGIN_ID, EXTENSION_ID_TOOLS);
		
		for (IConfigurationElement e : confElts) {
			String name = e.getAttribute(EXTENSION_PROP_TOOLS_NAME);
			String icon = e.getAttribute(EXTENSION_PROP_TOOLS_ICON);
			if (name != null && icon != null) {
				try {
					Object toolObj = e.createExecutableExtension(EXTENSION_PROP_TOOLS_CLASS);
					if (toolObj instanceof Tool) {
						final Tool tool = (Tool)toolObj;
						final ToolItem button = new ToolItem(toolBar, SWT.RADIO);
						collageUI.registerTool(tool.getClass(), button);
						button.setImage(CollageActivator.getImage(e.getContributor().getName(), icon));
						button.setToolTipText(name);
						button.addSelectionListener(new SelectionAdapter () {
							@Override
							public void widgetSelected(SelectionEvent e) {
								if (button.getSelection()) {
									collageUI.setActiveTool(tool);
								}
							}			
						});	

						continue;
					}
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
			}
			
			CollageUtilities.showWarning(CollageActivator.PLUGIN_NAME, 
					String.format("Unable to load tool %s from plugin %s.",	name, e.getContributor().getName()));
		}
	}
	
	/**
	 * Add widget contributions to toolbar (arbitrary {@link ToolItem}s provided by extensions through the
	 * {@link ICollageToolBarContributor} interface.)
	 * @param toolBar Collage tool bar.
	 * @param collageUI Collage UI instance.
	 */
	public static void setupExtensionToolbarWidgets (ToolBar toolBar, CollageUI collageUI) {
		IConfigurationElement[] confElts = Platform.getExtensionRegistry().getConfigurationElementsFor(CollageActivator.PLUGIN_ID, EXTENSION_ID_TOOLBAR);
		SortedMap<Integer, List<ICollageToolBarContributor>> contribsMap = new TreeMap<Integer, List<ICollageToolBarContributor>>();
		for (IConfigurationElement e : confElts) {
			String name = "(unknown)";
			try {
				String nameAttr = e.getAttribute(EXTENSION_PROP_TOOLBAR_CONTRIBUTION_NAME);
				if (nameAttr != null) {
					name = nameAttr;
				}
				int position = 0;
				String positionAttr = e.getAttribute(EXTENSION_PROP_TOOLBAR_CONTRIBUTION_POSITION);
				if (positionAttr != null) {
					try {
						position = Integer.parseInt(positionAttr);
					} catch (NumberFormatException ex) { 
						// Ignore.
					}
				}
				
				Object obj = e.createExecutableExtension(EXTENSION_PROP_TOOLBAR_CONTRIBUTION_CLASS);
				if (obj instanceof ICollageToolBarContributor) {
					if (!contribsMap.containsKey(position)) {
						contribsMap.put(position, new ArrayList<ICollageToolBarContributor>());
					}
					contribsMap.get(position).add((ICollageToolBarContributor)obj);
				} else {
					CollageUtilities.showError(CollageActivator.PLUGIN_NAME, "Failed to load toolbar contribution " + name);					
				}
			} catch (CoreException ex) {
				CollageUtilities.showError(CollageActivator.PLUGIN_NAME, "Failed to load toolbar contribution " + name);
			}
		}
		
		for (List<ICollageToolBarContributor> contribs : contribsMap.values()) {
			for (ICollageToolBarContributor contrib : contribs) {
				contrib.addContribution(toolBar, collageUI);
			}
		}
	}

	/**
	 * Get model classes provided by extensions.
	 * @return Collection of model classes
	 */
	public static Collection<Class<?>> getExtensionModelClasses () {
		return Collections.unmodifiableSet(getExtensionModelControllerMap(false).keySet());
	}

	/**
	 * Get an instance of a controller (edit part) for a given model class provided by an extension.
	 * @param modelClass Model class for which to look up a controller.
	 * @param getTreeController True to provide a tree edit part, false to provide a regular edit part.
	 * @return New instance of controller, or {@code null} if one could not be obtained
	 */
	public static Object getControllerForExtensionModelClass (Class<?> modelClass, boolean getTreeController) {
		Class<?> controllerClass = getExtensionModelControllerMap(getTreeController).get(modelClass);
		if (controllerClass != null) {
			try {
				return controllerClass.newInstance();
			} catch (InstantiationException e) {
				CollageUtilities.showError(CollageActivator.PLUGIN_NAME, 
						String.format("Unable to create new instance of controller class %s. See stack trace for details", 
								controllerClass.getName()));
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				CollageUtilities.showError(CollageActivator.PLUGIN_NAME, 
						String.format("Unable to create new instance of controller class %s. See stack trace for details", 
								controllerClass.getName()));
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Get the names and versions of plugins upon which our model depends
	 * @return List containing a {@link PluginDependency} object for each dependency
	 */
	public static List<PluginDependency> getModelPluginDependencies () {
		List<PluginDependency> dependencies = new ArrayList<PluginDependency>();
		
		// Start with Collage itself
		dependencies.add(new PluginDependency(CollageActivator.PLUGIN_ID, ModelElement.COLLAGE_MODEL_VERSION.toString()));
		
		IConfigurationElement[] confElts = Platform.getExtensionRegistry().getConfigurationElementsFor(CollageActivator.PLUGIN_ID, EXTENSION_ID_MODEL);
		for (IConfigurationElement e : confElts) {
			if (e.getName().equals(EXTENSION_ELEMENT_MODEL_VERSION)) {
				String version = e.getAttribute(EXTENSION_PROP_MODEL_VERSION_VALUE);
				dependencies.add(new PluginDependency(e.getContributor().getName(), version));
			}
		}
		return dependencies;
	}

	/**
	 * Get mappings between model element classes and controller classes (edit parts) provided by extensions.
	 * @param useTreeControllers True to use tree edit parts for map values, false to use regular edit parts.
	 * @return Map with keys being model element classes and values being corresponding controllers (edit parts) 
	 */
	public static Map<Class<?>, Class<?>> getExtensionModelControllerMap (boolean useTreeControllers) {
		String controllerClassAttrib = useTreeControllers ? EXTENSION_PROP_MODEL_TREE_CONTROLLER_CLASS
				                                          : EXTENSION_PROP_MODEL_CONTROLLER_CLASS;
		IConfigurationElement[] confElts = Platform.getExtensionRegistry().getConfigurationElementsFor(CollageActivator.PLUGIN_ID, EXTENSION_ID_MODEL);
		Map<Class<?>, Class<?>> classes = new HashMap<Class<?>,Class<?>>();
		for (IConfigurationElement e : confElts) {
			if (e.getName().equals(EXTENSION_ELEMENT_MODEL_SHAPE)) {
				String modelClassName = e.getAttribute(EXTENSION_PROP_MODEL_CLASS);
				String controllerClassName = e.getAttribute(controllerClassAttrib);

				if (controllerClassName != null) {
					try {
						Class<?> modelClass = e.createExecutableExtension(EXTENSION_PROP_MODEL_CLASS).getClass();
						Class<?> controllerClass = e.createExecutableExtension(controllerClassAttrib).getClass();
						classes.put(modelClass, controllerClass);
					} catch (CoreException e1) {
						CollageUtilities.showError(CollageActivator.PLUGIN_NAME, 
								String.format("Unable to load contributed model/controller pair (%s, %s) from extension %s. See stack trace for details.",
										modelClassName, controllerClassName, e.getContributor().getName()));
						e1.printStackTrace();
					}
				}
			}
		}
		
		return Collections.unmodifiableMap(classes);
	}
	
	/**
	 * Get mappings between model element classes and the IDs of the extensions that define them.
	 */
	public static Map<Class<?>, String> getModelExtensionMap () {
		IConfigurationElement[] confElts = Platform.getExtensionRegistry().getConfigurationElementsFor(CollageActivator.PLUGIN_ID, EXTENSION_ID_MODEL);
		Map<Class<?>, String> classExtensions = new HashMap<Class<?>, String>();
		for (IConfigurationElement e : confElts) {
			if (e.getName().equals(EXTENSION_ELEMENT_MODEL_SHAPE)) {
				try {
					Class<?> modelClass = e.createExecutableExtension(EXTENSION_PROP_MODEL_CLASS).getClass();
					classExtensions.put(modelClass, e.getContributor().getName());
				} catch (CoreException e1) {
					// Ignore.
				}
			}
		}
		
		return Collections.unmodifiableMap(classExtensions);
	}

	
	/**
	 * <p>Given a list of current model dependencies for this plugin and a list of loaded model dependencies 
	 * from a previously saved file, return a list that contains all the dependencies from both input lists.</p>
	 * <ul>
	 * <li>If a dependency occurs in the saved model but not in the current model, a warning will be issued (the
	 * plugin as currently configured doesn't support model elements in the loaded file.)</li>
	 * <li>If a dependency in the saved model has a version less than or equal to the current version, the output
	 * list will contain a dependency with the current version.</li>
	 * <li>If a dependency in the saved model has a version greater than the current version, an exception will
	 * be thrown (a current model cannot be assumed to support a future version of itself.)</li>
	 * </ul> 
	 * @param currentDeps Current set of dependencies for the plugin.
	 * @param loadedDeps Set of dependencies loaded from a previously saved file.
	 * @param warnings List to which warnings should be added
	 * @return Merged dependencies
	 * @throws CoreException if any plugins used in the saved model have versions greater than the versions
	 * of corresponding currently installed plugins
	 */
	public static Collection<PluginDependency> mergeDependencies (Collection<PluginDependency> currentDeps, 
			Collection<PluginDependency> loadedDeps, List<String> warnings) throws CoreException {
		if (loadedDeps == null || loadedDeps.isEmpty()) {
			return currentDeps;
		}
		List<String> errors = new ArrayList<String>();
		ArrayList<PluginDependency> mergedDependencies = new ArrayList<PluginDependency>(currentDeps);
		for (PluginDependency loadedDep : loadedDeps) {
			boolean found = false;
			for (int i = 0; i < currentDeps.size(); i++) {
				PluginDependency curDep = mergedDependencies.get(i);
				if (loadedDep.getPluginID().equals(curDep.getPluginID())) {
					found = true;
					Version loadedVersion = new Version(loadedDep.getVersion());
					Version curVersion = new Version(curDep.getVersion());
					if (loadedVersion.compareTo(curVersion) > 0) {
						// loaded > current is not allowed
						errors.add(String.format(WARNING_NEWER_PLUGIN_MODEL_VERSION,
								curDep.getPluginID(), loadedDep.getVersion(), curDep.getVersion()));
					}
					break;
				}
			}
			if (!found) {
				warnings.add(String.format(WARNING_MISSING_MODEL_PLUGIN,
						loadedDep.getPluginID()));
				loadedDep.setMissing(true);
				mergedDependencies.add(loadedDep);
			}
		}
		
		if (!errors.isEmpty()) {
			String errorMsg = CollageUtilities.join(errors, "\n") + "\nPlease upgrade to the latest " +
					((errors.size() == 1) ? "version of this plugin." : "versions of these plugins.");
			throw new CoreException(new Status(IStatus.ERROR, CollageActivator.PLUGIN_ID, errorMsg));
		}
		
		return mergedDependencies;
	}
}

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
package org.eclipselabs.collage.actions;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.model.CollageRoot;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Action for importing Collage layers.
 * @author Alex Bradley
 */
public class ImportLayersAction extends WorkbenchPartAction {
	private File openFile = null;
	
	public ImportLayersAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}

	@Override
	protected void init() {
		super.init();
		setId(ActionFactory.IMPORT.getId());
		setText("Import Layer(s)");
		setImageDescriptor(CollageActivator.getImageDescriptor(CollageActivator.IMPORT_ICON));
		setEnabled(false);
	}

	@Override
	public void run() {
		openFile = CollageUtilities.askForFile(openFile, getWorkbenchPart().getSite().getShell(), true);
				
		if (openFile != null) { 
			if (openFile.canRead()) {
				Job job = new Job("Import Collage layers from " + openFile.getName()) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							CollageRoot imported = CollageRoot.loadFrom(openFile);
							
							List<String> warnings = imported.getDependencyWarnings();
							if (!warnings.isEmpty()) {
								CollageUtilities.showWarning(CollageActivator.PLUGIN_NAME, 
										String.format("Collage data imported with the following warning%s:%n%s",
												(warnings.size() == 1) ? "" : "s",
												CollageUtilities.join(warnings, "\n")));
							}

							Object obj = getWorkbenchPart().getAdapter(EditPart.class);
							if (obj instanceof EditPart) {
								CreateRequest request = new CreateRequest();
								request.setFactory(new CollageRootCreationFactory(imported));
								final Command command = ((EditPart)obj).getCommand(request);
								if (command != null && command.canExecute()) {
									Display.getDefault().asyncExec(new Runnable() {
										@Override
										public void run() {
											execute(command);
										}
									});
								} else {
									return new Status(Status.ERROR, CollageActivator.PLUGIN_ID,
											"Internal error while attempting to import Collage data: Unable to obtain GEF command for import operation.");
								}
							}
						} catch (CoreException e) {
							return e.getStatus();
						}
						return Status.OK_STATUS;
					}
				};
				IFile workspaceFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(openFile.getAbsolutePath()));
				if (workspaceFile != null && workspaceFile.exists()) {
					job.setRule(workspaceFile);
				} else {
					job.setRule(new FilesystemSchedulingRule(openFile));
				}
				job.schedule();
			} else {
				CollageUtilities.showError("Collage Import Failed", String.format("File '%s' cannot be read.", openFile.getName()));
			}
		}
	}
	
	private static class CollageRootCreationFactory implements CreationFactory {
		private final CollageRoot collage;
		
		public CollageRootCreationFactory(CollageRoot collage) {
			this.collage = collage;
		}
		
		@Override
		public Object getNewObject() {
			return collage;
		}

		@Override
		public Object getObjectType() {
			return CollageRoot.class;
		}
	}
}

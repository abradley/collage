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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.model.CollageLayer;
import org.eclipselabs.collage.model.CollageRoot;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Action for exporting Collage layers.
 * @author Alex Bradley
 */
public class ExportLayersAction extends SelectionAction {
	private static class SaveRunnable implements Runnable {
		private List<CollageLayer> layers;
		private OutputStream out;
		private IStatus status = Status.OK_STATUS;
		
		public SaveRunnable(List<CollageLayer> layers, OutputStream out) {
			this.layers = layers;
			this.out = out;
		}
		
		@Override
		public void run() {
			try {
				(new CollageRoot(layers)).saveTo(out);
				out.close();
			} catch (Exception e) {
				status = new Status(IStatus.ERROR, CollageActivator.PLUGIN_ID, "Export failed", e); 
			}
		}
	}
	
	private File saveFile = null;
	
	public ExportLayersAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		return getLayersFromSelection() != null;
	}

	@Override
	protected void init() {
		super.init();
		setId(ActionFactory.EXPORT.getId());
		setText("Export Layer(s)");
		setImageDescriptor(CollageActivator.getImageDescriptor(CollageActivator.EXPORT_ICON));
		setEnabled(false);
	}

	@Override
	public void run() {
		final List<CollageLayer> layers = getLayersFromSelection();
		if (layers == null) {
			// shouldn't happen because of calculateEnabled()
			CollageUtilities.showError(CollageActivator.PLUGIN_NAME, "Please select only layers to export.");
		}
		
		saveFile = CollageUtilities.askForFile(saveFile, getWorkbenchPart().getSite().getShell(), false);
				
		if (saveFile != null) { 
			if (saveFile.exists()) {
				if (!MessageDialog.openQuestion(getWorkbenchPart().getSite().getShell(), 
						"Confirm Export", String.format("File %s already exists. Overwrite?", saveFile.getName()))) {
					return;
				}
			}
			final IFile workspaceFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(saveFile.getAbsolutePath()));
			final String jobName = "Exporting Collage layers to " + saveFile.getName();
			if (workspaceFile != null) {
				IResource schedRule = workspaceFile;
				while (schedRule != null /* should never happen */ && !schedRule.exists()) {
					schedRule = schedRule.getParent();
				}
				
				final IResource existingParent = schedRule;
				Job job = new Job(jobName) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						PipedInputStream inputStream = new PipedInputStream();
						PipedOutputStream outputStream;
						try {
							outputStream = new PipedOutputStream(inputStream);
						} catch (IOException e) {
							return new Status(IStatus.ERROR, CollageActivator.PLUGIN_ID, "Failed to initialize piped output stream", e);
						}

						SaveRunnable saveRunnable = new SaveRunnable(layers, outputStream);
						Thread saveThread = new Thread(saveRunnable);
						saveThread.start();
						try {
							if (workspaceFile.exists()) {
								workspaceFile.setContents(inputStream, true, false, monitor);
							} else {
								if (!workspaceFile.getParent().exists()) {
									// User might have created new directory through file dialog, so try refreshing.
									monitor.beginTask("Setting file contents", 10);
									existingParent.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 2));
									workspaceFile.create(inputStream, true, new SubProgressMonitor(monitor, 8));
									monitor.done();
								} else {
									workspaceFile.create(inputStream, true, monitor);
								}
							}
						} catch (CoreException e) {
							return e.getStatus();
						}
						try {
							saveThread.join();
							return saveRunnable.status;
						} catch (InterruptedException e) {
							return new Status(IStatus.ERROR, CollageActivator.PLUGIN_ID, "Operation interruped", e);
						}
					}
				};
				job.setRule(schedRule);
				job.schedule();
			} else {
				Job job = new Job(jobName) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask(jobName, 10);
						try {
							(new CollageRoot(layers)).saveTo(saveFile);
							return Status.OK_STATUS;
						} catch (JAXBException e) {
							return new Status(IStatus.ERROR, CollageActivator.PLUGIN_ID, "Export failed", e); 
						} finally {
							monitor.done();
						}
					}
				};
				job.setRule(new FilesystemSchedulingRule(saveFile));
				job.schedule();
			}
		}
	}
		
	private List<CollageLayer> getLayersFromSelection () {
		List<CollageLayer> layers = new ArrayList<CollageLayer>(); 
		for (Object obj : getSelectedObjects()) {
			if (obj instanceof EditPart) {
				Object model = ((EditPart)obj).getModel();
				if (model instanceof CollageLayer) {
					layers.add((CollageLayer)model);
				} else {
					return null;
				}
			}
		}
		if (layers.isEmpty()) {
			return null;
		}
		return layers;
	}
}

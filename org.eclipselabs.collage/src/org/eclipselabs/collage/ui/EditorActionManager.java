/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley    - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle.Control;

import org.eclipse.core.runtime.Assert;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * As Collage enters different states, we need to provide new handlers for some of the
 * standard Eclipse actions (e.g., delete, select all...) in the active editor and in the action bars for
 * its editor site. We centralize action handler remapping in this singleton class, which internally
 * uses a command stack to ensure that remapped actions are subsequently restored to their previous
 * states in a predictable order.
 * @author Alex Bradley
 */
public final class EditorActionManager {
	private static final EditorActionManager SINGLETON = new EditorActionManager();
	
	private CommandStack stack = new CommandStack();
	
	private EditorActionManager () {}
	
	public static EditorActionManager getDefault () {
		return SINGLETON;
	}
	
	/**
	 * Uninstall all actions. The editor should have its original complement of action handlers restored
	 * after this method is called.
	 */
	public synchronized void uninstallAllActions () {
		while (stack.canUndo()) {
			stack.undo();
		}
	}
	
	/**
	 * If there are any actions provided by a given source component at the top of the stack,
	 * uninstall them until an action from a different source is at the top of the stack or the
	 * stack is empty.
	 * @param ID of source component.
	 */
	public synchronized void uninstallActions (String source) {
		while (stack.canUndo()) {
			Command command = stack.getUndoCommand();
			if (source.equals(command.getLabel())) {
				stack.undo();
			} else {
				return;
			}
		}
	}
	
	/**
	 * Install the given actions into the editor site's action bars. The stack command for this installation
	 * will be marked with the given source ID.
	 * @param source ID of source component.
	 * @param actions Actions to install
	 */
	public synchronized void installGlobalActions (String source, List<IAction> actions) {
		stack.execute(new InstallGlobalActionsCommand(source, actions));
	}

	/**
	 * Install the given actions into the editor. The stack command for this installation
	 * will be marked with the given source ID.
	 * @param source ID of source component.
	 * @param actions Actions to install
	 */
	public synchronized void installEditorActions (String source, List<IAction> actions) {
		stack.execute(new InstallEditorActionsCommand(source, actions));
	}

	/**
	 * Block the given actions into the editor (i.e., set the handlers for {@code actionIds} to
	 * {@code null}.) The stack command for this blocking will be marked with the given source ID.
	 * @param source ID of source component.
	 * @param actionIds Action IDs to block.
	 */
	public synchronized void blockEditorActions (String source, String[] actionIds) {
		stack.execute(new InstallEditorActionsCommand(source, actionIds));
	}

	private static class OldNewActionPair {
		private IAction oldAction;
		private final IAction newAction;
		private final String id;

		public OldNewActionPair(String id, IAction newAction) {
			Assert.isNotNull(id);
			this.id = id;
			this.newAction = newAction;
		}
		
		public OldNewActionPair(IAction newAction) {
			this(newAction.getId(), newAction);
		}

		public IAction getOldAction() {
			return oldAction;
		}

		public void setOldAction(IAction oldAction) {
			this.oldAction = oldAction;
		}

		public IAction getNewAction() {
			return newAction;
		}

		public String getId() {
			return id;
		}
	}

	private static abstract class InstallActionsCommand extends Command {
		private List<OldNewActionPair> actionPairs = new ArrayList<OldNewActionPair>();
		
		public InstallActionsCommand (String source, List<IAction> actions) {
			setLabel(source);
			for (IAction action : actions) {
				actionPairs.add(new OldNewActionPair(action));
			}
			setupActionContainer();
		}

		public InstallActionsCommand (String source, String[] actionIds) {
			setLabel(source);
			for (String actionId : actionIds) {
				actionPairs.add(new OldNewActionPair(actionId, null));
			}
			setupActionContainer();
		}

		abstract protected void setupActionContainer ();
		abstract protected IAction getActionHandler (String id);
		abstract protected void setActionHandler (String id, IAction action);
		
		@Override
		public void execute() {
			for (OldNewActionPair pair : actionPairs) {
				pair.setOldAction(getActionHandler(pair.getId()));
			}
			redo();
		}
		
		@Override
		public void redo() {
			for (OldNewActionPair pair : actionPairs) {
				setActionHandler(pair.getId(), pair.getNewAction());
			}
		}

		@Override
		public void undo() {
			for (OldNewActionPair pair : actionPairs) {
				setActionHandler(pair.getId(), pair.getOldAction());
			}
		}
		
	}

	private static class InstallGlobalActionsCommand extends InstallActionsCommand {
		IActionBars actionBars;
		
		public InstallGlobalActionsCommand (String source, List<IAction> actions) {
			super(source, actions);
		}
		
		@Override
		protected void setupActionContainer () {
			try {
				actionBars = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().getActiveEditor().getEditorSite().getActionBars();
			} catch (NullPointerException e) {
				// The above chain of calls failed somewhere and we didn't get any ActionBars.
				// Command will not execute. We shouldn't reach this point - when this command
				// is created we should always be in the UI thread and have an active editor.
				e.printStackTrace();
			}
		}
		
		@Override
		public boolean canExecute() {
			return actionBars != null;
		}

		@Override
		protected IAction getActionHandler(String id) {
			return actionBars.getGlobalActionHandler(id);
		}

		@Override
		protected void setActionHandler(String id, IAction action) {
			actionBars.setGlobalActionHandler(id, action);
		}

		@Override
		public void redo() {
			super.redo();
			actionBars.updateActionBars();
		}

		@Override
		public void undo() {
			super.undo();
			actionBars.updateActionBars();
		}
	}
	
	private static class InstallEditorActionsCommand extends InstallActionsCommand {
		ITextEditor textEditor;
		
		public InstallEditorActionsCommand (String source, List<IAction> actions) {
			super(source, actions);
		}

		/**
		 * When this constructor is used, the command will have the effect of blocking the given
		 * action IDs (the actions will be uninstalled on redo and reinstalled on undo.)
		 * @param actionIds Action IDs to block.
		 */
		public InstallEditorActionsCommand (String source, String[] actionIds) {
			super(source, actionIds);
		}

		@Override
		protected void setupActionContainer () {
			try {
				IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().getActiveEditor();
				if (editorPart instanceof ITextEditor) {
					textEditor = (ITextEditor)editorPart;
				}
			} catch (NullPointerException e) {
				// The above chain of calls failed somewhere and we didn't get any editor.
				// Command will not execute. We shouldn't reach this point - when this command
				// is created we should always be in the UI thread and have an active editor.
				e.printStackTrace();
			}
		}
		
		@Override
		public boolean canExecute() {
			// The second check tries to avoid using the editor when disposed.
			return textEditor != null && textEditor.getAdapter(Control.class) != null;
		}

		@Override
		public boolean canUndo() {
			// The second check tries to avoid using the editor when disposed.
			return textEditor != null && textEditor.getAdapter(Control.class) != null;
		}

		@Override
		protected IAction getActionHandler(String id) {
			return textEditor.getAction(id);
		}

		@Override
		protected void setActionHandler(String id, IAction action) {
			textEditor.setAction(id, action);
		}
	}
}

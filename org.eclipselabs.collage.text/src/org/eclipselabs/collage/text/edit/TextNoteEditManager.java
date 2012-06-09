/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation (GEF logic example)
 *     Alex Bradley    - adapted for use in Collage
 *******************************************************************************/
package org.eclipselabs.collage.text.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipselabs.collage.text.figures.TextNoteFigure;
import org.eclipselabs.collage.text.parts.TextNoteEditPart;
import org.eclipselabs.collage.ui.EditorActionManager;

/**
 * Manage editing of a text note.
 * Adapted from LogicLabelEditManager in the GEF logic designer example.
 * @author Alex Bradley
 * @author IBM Corporation
 */
public class TextNoteEditManager extends DirectEditManager {
	private static final ActionFactory[] OVERRIDDEN_ACTIONS = {
		ActionFactory.COPY,
		ActionFactory.PASTE,
		ActionFactory.DELETE,
		ActionFactory.SELECT_ALL,
		ActionFactory.CUT, 
		ActionFactory.FIND,
		ActionFactory.UNDO,
		ActionFactory.REDO
	};
	private static final String[] OVERRIDDEN_TEXTEDITOR_ACTION_IDS = {
		ITextEditorActionDefinitionIds.LINE_START,
		ITextEditorActionDefinitionIds.LINE_END
	};
	private static final Map<String, String> ACTION_DEFINITION_IDS_MAP;
	static {
		HashMap<String, String> actionDefinitionIds = new HashMap<String, String>();
		for (ActionFactory actionFactory : OVERRIDDEN_ACTIONS) {
			actionDefinitionIds.put(actionFactory.getId(), actionFactory.getCommandId());
		}
		ACTION_DEFINITION_IDS_MAP = Collections.unmodifiableMap(actionDefinitionIds);
	}
	
	private static final String ACTION_SOURCE_ID = TextNoteEditManager.class.getName();
	
	private static class DummyCollectorActionBars implements IActionBars {
		private List<IAction> actions = new ArrayList<IAction>();
		@Override
		public void clearGlobalActionHandlers() {
			actions.clear();
		}

		@Override
		public IAction getGlobalActionHandler(String actionId) {
			// Not actually expected to be called
			for (IAction action : actions) {
				if (actionId.equals(action.getId()))
					return action;
			}
			return null;
		}

		@Override
		public IMenuManager getMenuManager() {
			return null;
		}

		@Override
		public IServiceLocator getServiceLocator() {
			return null;
		}

		@Override
		public IStatusLineManager getStatusLineManager() {
			return null;
		}

		@Override
		public IToolBarManager getToolBarManager() {
			return null;
		}

		@Override
		public void setGlobalActionHandler(String actionId, IAction handler) {
			if (!actionId.equals(handler.getId())) {
				handler.setId(actionId);
			}
			handler.setActionDefinitionId(ACTION_DEFINITION_IDS_MAP.get(actionId));
			actions.add(handler);
		}

		@Override
		public void updateActionBars() {}
		
		public List<IAction> getActions () {
			return Collections.unmodifiableList(actions);
		}
	}
	
	private CellEditorActionHandler actionHandler;
	
	public TextNoteEditManager(GraphicalEditPart source,
			CellEditorLocator locator) {
		super(source, null, locator);
	}

	public void finishEditing () {
		commit();
	}	

	@Override
	protected void bringDown() {
		if (actionHandler != null) {
			actionHandler.dispose();
			actionHandler = null;
		}
		EditorActionManager.getDefault().uninstallActions(ACTION_SOURCE_ID);
		((TextNoteEditPart)this.getEditPart()).finishDirectEdit();

		super.bringDown();
	}

	@Override
	protected CellEditor createCellEditorOn(Composite composite) {
		return new TextCellEditor(composite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
	}

	@Override
	protected void initCellEditor() {
		// update text
		TextNoteFigure textNote = (TextNoteFigure) getEditPart()
				.getFigure();
		getCellEditor().setValue(textNote.getText());
		getCellEditor().getControl().setFont(textNote.getFont());

		DummyCollectorActionBars collector = new DummyCollectorActionBars();
		actionHandler = new CellEditorActionHandler(collector);
		EditorActionManager.getDefault().installGlobalActions(ACTION_SOURCE_ID, collector.getActions());
		EditorActionManager.getDefault().installEditorActions(ACTION_SOURCE_ID, collector.getActions());
		EditorActionManager.getDefault().blockEditorActions(ACTION_SOURCE_ID, OVERRIDDEN_TEXTEDITOR_ACTION_IDS);
		actionHandler.addCellEditor(getCellEditor());
	}
}
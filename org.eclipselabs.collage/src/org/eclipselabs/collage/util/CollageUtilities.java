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
package org.eclipselabs.collage.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipselabs.collage.CollageActivator;

/**
 * Utility methods for Collage plugin.
 * @author Alex Bradley
 */
public class CollageUtilities {
	public static final Pattern POINT_PATTERN = Pattern.compile("\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\s*");
	
	public static final RGB RGB_BLACK = new RGB(0, 0, 0);
	public static final RGB RGB_WHITE = new RGB(255, 255, 255);
	
	private static final String FILE_IMPORT_DIALOG_TITLE = "Import Collage Layers";
	private static final String FILE_EXPORT_DIALOG_TITLE = "Export Collage Layers";
	private static final String[] FILTER_EXTENSIONS = {"*.xcl"};
	private static final String[] FILTER_NAMES = {"XML Collage Files (*.xcl)"};
	
	/**
	 * Attempt to parse a string as a positive integer.
	 * @param value String to parse
	 * @return Positive integer parsed from string
	 * @throws IllegalArgumentException if string is not a positive number
	 */
	public static int getPositiveInteger (String value) throws IllegalArgumentException {
		try {
			int result = Integer.parseInt(value);
			if (result > 0) {
				return result;
			} else {
				throw new IllegalArgumentException("Value must be positive");
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Not a number");
		}
	}
	
	/**
	 * Concatenate two arrays.
	 * @param first First array
	 * @param second Second array
	 * @return Array of length {@code (first.length + second.length)} consisting of the elements of {@code first} 
	 * followed by the elements of {@code second}.
	 */
	public static <T> T[] arrayConcat (T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	/**
	 * Append one element to an array.
	 * @param array First array
	 * @param element Element to append
	 * @return Array of length {@code (array.length + 1)} consisting of the elements of {@code array} 
	 * followed by {@code element}.
	 */
	public static <T> T[] arrayAppend (T[] array, T element) {
		T[] result = Arrays.copyOf(array, array.length + 1);
		result[array.length] = element;
		return result;
	}	
	
	/**
	 * Join list elements into a string.
	 * @param list List of items.
	 * @param separator String separator.
	 * @return Items in list concatenated into string with {@code separator} between each item.
	 */
	public static <T> String join (List<T> list, String separator) {
		StringBuffer result = new StringBuffer();
		Iterator<T> iter = list.iterator();
		while (iter.hasNext()) {
			result.append(iter.next());
			if (iter.hasNext()) {
				result.append(separator);
			}
		}
		return result.toString();
	}
	
	/**
	 * Debugging method:
	 * Print the bounds of all figures in the given IFigure's tree, starting from the root.
	 * @param figure IFigure for which to print all related bounds.
	 */
	public static void printAllBounds (IFigure figure) {
		IFigure topParent = figure;
		while (topParent.getParent() != null) {
			topParent = topParent.getParent();
		}
		
		printFigureBounds(topParent, 0);
		System.out.println();
	}
	
	private static void printFigureBounds (IFigure figure, int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print(' ');
		}
		System.out.println(figure.getClass().getName() + " bounds = " + figure.getBounds());
		for (Object child : figure.getChildren()) {
			printFigureBounds((IFigure)child, depth+1);
		}
	}
	
	/**
	 * Copy an RGB instance.
	 * @param rgb RGB to copy
	 * @return Copy of passed rgb
	 */
	public static RGB copyRGB (RGB rgb) {
		return new RGB(rgb.red, rgb.green, rgb.blue);
	}
	
	/**
	 * Convert a {@link Point} to a string.
	 * @param point Point to convert.
	 * @return String representing the given point.
	 */
	public static String pointToString (Point point) {
		return "(" + point.x() + ", " + point.y() + ")";
	}
	
	/**
	 * Convert a string to a {@link Point}.
	 * @param string String in format matching {@link CollageUtilities#POINT_PATTERN} (basically "(x,y)", possibly
	 * with more whitespace)
	 * @return {@link Point} corresponding to value specified by string
	 * @throws IllegalArgumentException if string does not match required format
	 */
	public static Point stringToPoint (String string) {
		Matcher matcher = POINT_PATTERN.matcher(string);
		if (matcher.matches()) {
			int x = Integer.parseInt(matcher.group(1));
			int y = Integer.parseInt(matcher.group(2));
			return new Point(x, y);
		}
		throw new IllegalArgumentException("String must be in format (x, y), where x and y are non-negative integers");
	}

	/**
	 * Get the undo context for a text viewer.
	 * @param viewer Text viewer.
	 * @return {@link IUndoContext} for {@code viewer}, or {@code null} if none could be found.
	 */
	public static IUndoContext getTextViewerUndoContext (ITextViewer viewer) {
		if (viewer != null && viewer instanceof ITextViewerExtension6) {
			IUndoManager undoManager = ((ITextViewerExtension6)viewer).getUndoManager();
			if (undoManager != null && undoManager instanceof IUndoManagerExtension) { 
				IUndoManagerExtension extension = (IUndoManagerExtension)undoManager;
				return extension.getUndoContext();
			}
		}
		return null;
	}
	
	/**
	 * {@link CommandStack#canRedo} just checks if there's a redo command on the stack; it doesn't
	 * actually check whether the command is redoable. This "can really redo" utility method returns
	 * true iff {@code stack} has a redo command available which can actually be executed.
	 * @param stack GEF command stack.
	 * @return {@code} true if {@code stack} has a redo command available which can actually be executed,
	 * {@code false} otherwise.
	 */
	public static boolean canReallyRedo (CommandStack stack) {
		if (stack.canRedo()) {
			Command command = stack.getRedoCommand();
			return command != null && command.canExecute();
		}
		return false;
	}
	
	/**
	 * Given a point in the given text viewer's text widget, find the (1-based) line in the text viewer's
	 * document in which that point is located in the current state of the projection model. 
	 * @param textViewer a text viewer (possibly supporting projection through {@link ITextViewerExtension5})
	 * @param point a point in the text widget's canvas, using a coordinate system with origin located at the top
	 * left pixel of the first (0th) line in the text widget
	 * @return 1-based line in {@code textViewer}'s document corresponding to {@code p}
	 */
	public static int getDocumentLineAtPoint (ITextViewer textViewer, Point point) {
		StyledText textWidget = textViewer.getTextWidget();
		Assert.isLegal(textWidget != null && !textWidget.isDisposed());
		int widgetLine = textWidget.getLineIndex(point.y() - textWidget.getTopPixel());
		if (textViewer instanceof ITextViewerExtension5) {
			// Convert 0-based line to 1-based line
			return ((ITextViewerExtension5)textViewer).widgetLine2ModelLine(widgetLine) + 1;
		}
		// Convert 0-based line to 1-based line
		return widgetLine + 1;
	}
	
	/**
	 * Given a (1-based) line number in the given text viewer's document, find the top pixel (y-offset) for that
	 * line in the text viewer's text widget in the current state of the projection model.
	 * @param textViewer a text viewer (possibly supporting projection through {@link ITextViewerExtension5})
	 * @param lineNumber 1-based line number in {@code textViewer}'s document
	 * @return Top pixel for {@code line} in {@code textViewer}'s widget, using a coordinate system with origin 
	 * located at the top left pixel of the first (0th) line in the text widget
	 */
	public static int getCurrentTopPixelForDocumentLine (ITextViewer textViewer, int lineNumber) {
		StyledText textWidget = textViewer.getTextWidget();
		Assert.isLegal(textWidget != null && !textWidget.isDisposed());
		
		// Convert 1-based line to 0-based line
		lineNumber--;
		
		int widgetLineNumber;
		if (textViewer instanceof ITextViewerExtension5) {
			widgetLineNumber = -1;
			while (widgetLineNumber == -1 && lineNumber >= 0) {
				// If the document line isn't currently in view, try to find a previous visible line
				widgetLineNumber = ((ITextViewerExtension5)textViewer).modelLine2WidgetLine(lineNumber);
				lineNumber--;
			}
		} else {
			widgetLineNumber = lineNumber;
		}
		
		return textWidget.getTopPixel() + textWidget.getLinePixel(widgetLineNumber);
	}
	
	/**
	 * Show an error dialog.
	 * @param title Dialog title
	 * @param msg Error message to display
	 */
	public static void showError (final String title, final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, msg);
			}
		});
	}
	
	/**
	 * Show a warning dialog.
	 * @param title Dialog title
	 * @param msg Warning message to display
	 */
	public static void showWarning (final String title, final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, msg);
			}
		});
	}
	
	/**
	 * Get a {@link Color} from common registry for an {@link RGB} colour specification.
	 * @param rgb RGB description of {@link Color} to retrieve
	 * @return {@link Color} requested
	 */
	public static Color getColor (RGB rgb) {
		ColorRegistry reg = CollageActivator.getDefault().getColorRegistry();
		if (!reg.hasValueFor(rgb.toString())) {
			reg.put(rgb.toString(), rgb);
		}
		return reg.get(rgb.toString());
	}

	public static CollageFontRegistry getFontRegistry () {
		return CollageActivator.getDefault().getFontRegistry();
	}
	
    /**
     * Helper to open a file chooser dialog for importing or exporting Collage layers.
     * [Adapted from {@link org.eclipse.jface.preference.FileFieldEditor}.]
     * @param startingFile the file to open the dialog on ({@code null} to open dialog
     * in system default location.)
     * @param parentShell {@link Shell} on which to create the file dialog.
     * @param isImport Pass {@code true} to open an import dialog, {@code false} to open an
     * export dialog.
     * @return File The File the user selected or <code>null</code> if they
     * do not.
     */
    public static File askForFile (File startingFile, Shell parentShell, boolean isImport) {
        FileDialog dialog = new FileDialog(parentShell, (isImport ? SWT.OPEN : SWT.SAVE) | SWT.SHEET);
        if (startingFile != null && startingFile.getParent() != null) {
        	dialog.setFilterPath(startingFile.getParent());
			dialog.setFileName(startingFile.getName());
		}
        dialog.setFilterExtensions(FILTER_EXTENSIONS);
        dialog.setFilterNames(FILTER_NAMES);
        dialog.setText(isImport ? FILE_IMPORT_DIALOG_TITLE : FILE_EXPORT_DIALOG_TITLE);
        String file = dialog.open();
        if (file != null) {
            file = file.trim();
            if (file.length() > 0) {
				return new File(file);
			}
        }

        return null;
    }
    
    /**
     * Copy file {@code source} to {@code destination}.
     * @param source Source file
     * @param destination Destination file
     * @throws IOException
     */
    public static void fileCopy (File source, File destination) throws IOException {
    	if (destination.exists()) {
    		throw new IllegalArgumentException("Cannot overwrite existing file");
    	}
    	
    	// cf. http://www.javalobby.org/java/forums/t17036.html
    	FileChannel sourceChannel = null, destChannel = null;
    	try {
    		sourceChannel = new FileInputStream(source).getChannel();
    		destChannel = new FileOutputStream(destination).getChannel();
    		destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
    	} finally {
    		if (sourceChannel != null) {
    			sourceChannel.close();
    		}
    		if (destChannel != null) {
    			destChannel.close();
    		}    		
    	}
    }
}

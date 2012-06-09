/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley    - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.model.resourceid;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * A Java class file in a JAR.
 * @author Alex Bradley
 */
@XmlType(name="javaClassFileResource")
@XmlAccessorType(XmlAccessType.FIELD)
public class JavaClassFileIdentifier extends ResourceIdentifier {
	private class ClassFileSearchRequestor extends SearchRequestor {
		private IJavaElement element;
		
		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			if (element == null) {
				Object obj = match.getElement();
				if (obj instanceof IJavaElement && jarName.equals(((IJavaElement)obj).getPath().lastSegment())) {
					element = (IJavaElement)obj;
				}
			}
		}
		
		public IJavaElement getElement () {
			return element;
		}
	}

	@XmlAttribute
	protected String className;
	@XmlAttribute
	protected String jarName;
	
	private transient IJavaElement foundElement;
	private transient Object foundElementLock = new Object();

	// for serializer compatibility
	JavaClassFileIdentifier () { }
	
	JavaClassFileIdentifier(String shortName, String className, String jarName) {
		super(shortName);
		this.className = className;
		this.jarName = jarName;
	}	
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((jarName == null) ? 0 : jarName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaClassFileIdentifier other = (JavaClassFileIdentifier) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (jarName == null) {
			if (other.jarName != null)
				return false;
		} else if (!jarName.equals(other.jarName))
			return false;
		return true;
	}
	
	@Override
	public IEditorPart openInEditor() throws CoreException {
		synchronized (foundElementLock) {
			if (foundElement == null) {
				SearchEngine engine = new SearchEngine();
				SearchPattern pattern = SearchPattern.createPattern(className, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, 
						SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
				ClassFileSearchRequestor requestor = new ClassFileSearchRequestor();
				engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, 
						SearchEngine.createWorkspaceScope(), requestor, new NullProgressMonitor());
				foundElement = requestor.getElement();
			}
			if (foundElement != null) {
				return JavaUI.openInEditor(foundElement);
			}
		}
		CollageUtilities.showError(CollageActivator.PLUGIN_NAME, "Unable to open an editor for " + shortName + ".");
		return null;
	}
}

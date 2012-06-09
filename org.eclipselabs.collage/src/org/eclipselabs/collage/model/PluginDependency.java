/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * A dependency on a particular version of a plugin.
 * @author Alex Bradley
 */
@XmlType(name="depends")
@XmlAccessorType(XmlAccessType.FIELD)
public class PluginDependency {
	@XmlAttribute
	private String pluginID;
	@XmlAttribute
	private String version;

	private transient boolean missing = false;
	
	public PluginDependency () { }
	
	public PluginDependency(String pluginID, String version) {
		this.pluginID = pluginID;
		this.version = version;
	}
	
	public String getPluginID() {
		return pluginID;
	}
	
	public void setPluginID(String pluginID) {
		this.pluginID = pluginID;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public boolean isMissing() {
		return missing;
	}

	public void setMissing(boolean missing) {
		this.missing = missing;
	}

	@Override
	public String toString() {
		return "PluginDependency [pluginID=" + pluginID + ", version="
				+ version + ", missing=" + missing + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((pluginID == null) ? 0 : pluginID.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof PluginDependency) {
			PluginDependency other = (PluginDependency) obj;
			if (pluginID == null) {
				if (other.pluginID != null) {
					return false;
				}
			} else if (!pluginID.equals(other.pluginID)) {
				return false;
			}
			if (version == null) {
				if (other.version != null) {
					return false;
				}
			} else if (!version.equals(other.version)) {
				return false;
			}
			return true;
		}
		return false;
	}
}

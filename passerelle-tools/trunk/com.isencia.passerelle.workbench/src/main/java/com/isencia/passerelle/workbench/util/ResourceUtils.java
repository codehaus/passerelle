package com.isencia.passerelle.workbench.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

public class ResourceUtils {

	/**
	 * 
	 * @param parameter
	 * @return IResource
	 */
	public static IResource getResource(ptolemy.kernel.util.Settable parameter) {
		String     path  = parameter.getValueAsString();
		if (path.startsWith("\"")) {
			path = path.substring(1);
		}
		if (path.endsWith("\"")) {
			path = path.substring(0,path.length()-1);
		}
		final IWorkspace space = ResourcesPlugin.getWorkspace();
		final IResource  ret   = space.getRoot().findMember(path);
		return ret;
	}

}

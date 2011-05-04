package com.isencia.passerelle.workbench.model.ui.launch.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;

import com.isencia.passerelle.workbench.model.ui.launch.LaunchShortcut;

public class LaunchUtils {
	
	public static String getModelFileName(ILaunchConfiguration configuration) throws CoreException {
		String modelFileName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(
			configuration.getAttribute(LaunchShortcut.CONFIG_ATTRIBUTE_ABSOLUTE_MODEL_FILENAME, ""));
		
		String path = Path.fromPortableString(modelFileName).toOSString();
		path = path.replace('\\', '/');
		return path;
	}

	/**
	 * Get the location of the target containing the plug-ins
	 * 
	 * @return path to the OsgiInstallArea
	 */
	public static IPath findOsgiInstallArea() {
		IPluginModelBase model = PluginRegistry.findModel("org.eclipse.osgi");
		String installLocation = model.getInstallLocation();
		IPath path = new Path(installLocation);
		path = path.removeLastSegments(1);
		if( "plugins".equalsIgnoreCase(path.lastSegment()))
			path = path.removeLastSegments(1);
		return path;
	}

	/**
	 * Get the location of the workspace which contains the data
	 * 
	 * @return path to the OsgiInstanceArea
	 */
	public static IPath findOsgiInstanceArea() {
		IPluginModelBase model = PluginRegistry.findModel("org.eclipse.osgi");
		String installLocation = model.getInstallLocation();
		IPath path = new Path(installLocation);
		path = path.removeLastSegments(1);
		if( "plugins".equalsIgnoreCase(path.lastSegment()))
			path = path.removeLastSegments(1);
		path = path.append("workspace");
		return path;
	}

}

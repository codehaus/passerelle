package com.isencia.passerelle.workbench.model.ui.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaunchShortcut implements ILaunchShortcut {
	
	public final static String CONFIG_ATTRIBUTE_MODEL_FILENAME = "modelfilename";
	public final static String CONFIG_ATTRIBUTE_ABSOLUTE_MODEL_FILENAME = "absolute_modelfilename";

	private final static Logger logger = LoggerFactory.getLogger(LaunchShortcut.class);

	public Logger getLogger() {
		return logger;
	}

	/**
	 * @see ILaunchShortcut#launch(IEditorPart, String)
	 */
	public void launch(IEditorPart editor, String mode) {
		IFile element = null;
		IEditorInput input = editor.getEditorInput();
		element = (IFile) input.getAdapter(IFile.class);
		if (element != null) {
			searchAndLaunch(element, mode);
		}
	}

	/**
	 * @see ILaunchShortcut#launch(ISelection, String)
	 */
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			searchAndLaunch(((IStructuredSelection) selection).getFirstElement(), mode);
		}
	}

	protected void searchAndLaunch(Object search, String mode) {
		if (search instanceof IFile) {
			IFile file = (IFile) search;
			IPath fullPath = file.getFullPath();
			String fileName = fullPath.lastSegment();
			getLogger().debug("Launching : "+fileName);
			ILaunchConfiguration config = createConfiguration(fileName, file.getLocation()
				.toPortableString());
			DebugUITools.launch(config, mode);
		}
	}

	protected ILaunchConfiguration createConfiguration(String fileName, String modelFileName) {

		ILaunchConfigurationWorkingCopy launchConfiguration = null;
		ILaunchConfiguration existingConfiguration = null;
		ILaunchConfiguration workingConfiguration = null;
		try {
			ILaunchConfigurationType configType = getPasserelleLaunchConfigType();
			 ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
             for (int i = 0; i < configs.length; i++) {
                 ILaunchConfiguration configuration = configs[i];
                 if (configuration.getName().equals(fileName)) {
                	 existingConfiguration = configuration;
                	 break;
                 }
             }
            if( existingConfiguration != null ) {
    			launchConfiguration = configType.newInstance(null, fileName);
            } else {
    			launchConfiguration = configType.newInstance(null, DebugPlugin.getDefault()
    					.getLaunchManager().generateUniqueLaunchConfigurationNameFrom(fileName));
            }
			launchConfiguration.setAttribute(CONFIG_ATTRIBUTE_MODEL_FILENAME, fileName);
			launchConfiguration.setAttribute(CONFIG_ATTRIBUTE_ABSOLUTE_MODEL_FILENAME, modelFileName);
			// TODO Check this
//			initializeJavaProject(ClassHelper.getContext(), launchConfiguration);
			initializeJavaProject(null, launchConfiguration);
			workingConfiguration = launchConfiguration.doSave();
		} catch (CoreException ce) {
			getLogger().error("Error creating passerelle launchconfig",ce);
		}
		return workingConfiguration;
	}

	/**
	 * Returns the local java launch config type
	 * 
	 * @return ILaunchConfigurationType
	 */
	protected ILaunchConfigurationType getPasserelleLaunchConfigType() {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		return lm.getLaunchConfigurationType("com.isencia.passerelle.workbench.model.ui.launch.launchConfigurationType");
	}

	protected void initializeJavaProject(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		if (javaElement == null) {
			return;
		}
		IJavaProject javaProject = javaElement.getJavaProject();
		String name = null;
		if (javaProject != null && javaProject.exists()) {
			name = javaProject.getElementName();
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
	}

}

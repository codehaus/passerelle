package com.isencia.passerelle.workbench.model.ui.launch;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.launcher.BundleLauncherHelper;
import org.eclipse.pde.internal.ui.launcher.EclipsePluginValidationOperation;
import org.eclipse.pde.internal.ui.launcher.LaunchArgumentsHelper;
import org.eclipse.pde.internal.ui.launcher.LaunchConfigurationHelper;
import org.eclipse.pde.internal.ui.launcher.LaunchPluginValidator;
import org.eclipse.pde.internal.ui.launcher.LauncherUtils;
import org.eclipse.pde.internal.ui.launcher.VMHelper;
import org.eclipse.pde.ui.launcher.IPDELauncherConstants;
import org.eclipse.pde.ui.launcher.PDESourcePathProvider;
import org.osgi.framework.Version;

import com.isencia.passerelle.workbench.model.launch.ModelLauncher;
import com.isencia.passerelle.workbench.model.launch.ModelRunner;
import com.isencia.passerelle.workbench.model.ui.utils.LaunchUtils;

public class LaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {
	
	public final static String APPLICATION_NAME = "com.isencia.passerelle.workbench.model.launch"; 

	/**
	 * To avoid duplicating variable substitution (and duplicate prompts)
	 * this variable will store the substituted workspace location.
	 */
	private String workspaceLocation;

	protected File configDir = null;

	// key is bundle ID, value is a model
	private Map<String, IPluginModelBase> bundles;

	// key is a model, value is startLevel:autoStart
	private Map pluginModels;

	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public synchronized void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(MessageFormat.format("{0}...", new String[]{configuration.getName()}), 5); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		try {
				mode = ILaunchManager.RUN_MODE;

			try {
				preLaunchCheck(configuration, launch, new SubProgressMonitor(monitor, 2));
			} catch (CoreException e) {
				if (e.getStatus().getSeverity() == IStatus.CANCEL) {
					monitor.setCanceled(true);
					return;
				}
				throw e;
			}
			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

			IVMRunner runner= getVMRunner(configuration, mode);

			File workingDir = verifyWorkingDirectory(configuration);
			String workingDirName = null;
			if (workingDir != null) {
				workingDirName= workingDir.getAbsolutePath();
			}

			// Environment variables
			String[] environment= getEnvironment(configuration);

			ArrayList<String> vmArguments= new ArrayList<String>();
			ArrayList<String> programArguments= new ArrayList<String>();
			collectExecutionArguments(configuration, vmArguments, programArguments);

			// VM-specific attributes
			Map vmAttributesMap= getVMSpecificAttributesMap(configuration);

			// Create VM config
			
			// Classpath
			ArrayList<String> classPathList = new ArrayList<String>();
			for (Map.Entry<String, IPluginModelBase> bundleEntry : bundles.entrySet()) {
				IPluginModelBase pluginModelBase = bundleEntry.getValue();
				classPathList.add(pluginModelBase.getInstallLocation());
			}
			
			VMRunnerConfiguration runConfig= new VMRunnerConfiguration(ModelLauncher.class.getName(), classPathList.toArray(new String[] {}));
			
			runConfig.setVMArguments((String[]) vmArguments.toArray(new String[vmArguments.size()]));
			runConfig.setProgramArguments((String[]) programArguments.toArray(new String[programArguments.size()]));
			runConfig.setEnvironment(environment);
			runConfig.setWorkingDirectory(workingDirName);
			runConfig.setVMSpecificAttributesMap(vmAttributesMap);

			// Bootpath
			runConfig.setBootClassPath(getBootpath(configuration));

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

			// done the verification phase
			monitor.worked(1);

			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);

			// Launch the configuration - 1 unit of work
			runner.run(runConfig, launch, monitor);

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}
			
		} finally {
			monitor.done();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getVMRunner(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	public IVMRunner getVMRunner(ILaunchConfiguration configuration, String mode) throws CoreException {
		IVMInstall launcher = VMHelper.createLauncher(configuration);
		return launcher.getVMRunner(mode);
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.launcher.JUnitBaseLaunchConfiguration#abort(java.lang.String, java.lang.Throwable, int)
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, IPDEUIConstants.PLUGIN_ID, code, message, exception));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate#collectExecutionArguments(org.eclipse.debug.core.ILaunchConfiguration, java.util.List, java.util.List)
	 */
	@SuppressWarnings("unchecked")
	protected void collectExecutionArguments(ILaunchConfiguration configuration, List/*String*/vmArguments, List/*String*/programArgs) throws CoreException {
		// add program & VM arguments provided by getProgramArguments and getVMArguments
		String pgmArgs= getProgramArguments(configuration);
		String vmArgs= getVMArguments(configuration);
		ExecutionArguments execArgs= new ExecutionArguments(vmArgs, pgmArgs);
		vmArguments.addAll(Arrays.asList(execArgs.getVMArgumentsArray()));
		programArgs.addAll(Arrays.asList(execArgs.getProgramArgumentsArray()));

		programArgs.add("-clean"); //$NON-NLS-1$

		// Create the platform configuration for the runtime workbench
		String productID = LaunchConfigurationHelper.getProductID(configuration);
//		LaunchConfigurationHelper.createConfigIniFile(configuration, productID, fAllBundles, fModels, getConfigurationDirectory(configuration));
//		LaunchConfigurationHelper.createConfigIniFile(configuration, productID, fAllBundles,  getConfigurationDirectory(configuration));
		String brandingId = LaunchConfigurationHelper.getContributingPlugin(productID);
		TargetPlatform.createPlatformConfiguration(getConfigurationDirectory(configuration), (IPluginModelBase[]) bundles.values().toArray(new IPluginModelBase[bundles.size()]), brandingId != null ? (IPluginModelBase) bundles.get(brandingId) : null);
		TargetPlatformHelper.checkPluginPropertiesConsistency(bundles, getConfigurationDirectory(configuration));

		programArgs.add("-configuration"); //$NON-NLS-1$
		programArgs.add("file:" + new Path(getConfigurationDirectory(configuration).getPath()).addTrailingSeparator().toString()); //$NON-NLS-1$

		// Specify the output folder names
//		programArgs.add("-dev"); //$NON-NLS-1$
//		programArgs.add(ClasspathHelper.getDevEntriesProperties(getConfigurationDirectory(configuration).toString() + "/dev.properties", fAllBundles)); //$NON-NLS-1$

		// necessary for PDE to know how to load plugins when target platform = host platform
		// see PluginPathFinder.getPluginPaths()
		IPluginModelBase base = findPlugin(PDECore.PLUGIN_ID);
		if (base != null && VersionUtil.compareMacroMinorMicro(base.getBundleDescription().getVersion(), new Version("3.3.1")) < 0) //$NON-NLS-1$
			programArgs.add("-pdelaunch"); //$NON-NLS-1$				

		// Create the .options file if tracing is turned on
		if (configuration.getAttribute(IPDELauncherConstants.TRACING, false) && !IPDELauncherConstants.TRACING_NONE.equals(configuration.getAttribute(IPDELauncherConstants.TRACING_CHECKED, (String) null))) {
			programArgs.add("-debug"); //$NON-NLS-1$
			String path = getConfigurationDirectory(configuration).getPath() + IPath.SEPARATOR + ".options"; //$NON-NLS-1$
			programArgs.add(LaunchArgumentsHelper.getTracingFileArgument(configuration, path));
		}

		// add the program args specified by the user
		String[] userArgs = LaunchArgumentsHelper.getUserProgramArgumentArray(configuration);
		for (int i = 0; i < userArgs.length; i++) {
			// be forgiving if people have tracing turned on and forgot
			// to remove the -debug from the program args field.
			if (userArgs[i].equals("-debug") && programArgs.contains("-debug")) //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			programArgs.add(userArgs[i]);
		}

		if (!configuration.getAttribute(IPDEUIConstants.APPEND_ARGS_EXPLICITLY, false)) {
			if (!programArgs.contains("-os")) { //$NON-NLS-1$
				programArgs.add("-os"); //$NON-NLS-1$
				programArgs.add(TargetPlatform.getOS());
			}
			if (!programArgs.contains("-ws")) { //$NON-NLS-1$
				programArgs.add("-ws"); //$NON-NLS-1$
				programArgs.add(TargetPlatform.getWS());
			}
			if (!programArgs.contains("-arch")) { //$NON-NLS-1$
				programArgs.add("-arch"); //$NON-NLS-1$
				programArgs.add(TargetPlatform.getOSArch());
			}
		}
	}

	private IPluginModelBase findPlugin(String id) throws CoreException {
		IPluginModelBase model = PluginRegistry.findModel(id);
		if (model == null)
			model = PDECore.getDefault().findPluginInHost(id);
		if (model == null)
			abort(NLS.bind(PDEUIMessages.JUnitLaunchConfiguration_error_missingPlugin, id), null, IStatus.OK);
		return model;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getProgramArguments(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		return LaunchArgumentsHelper.getUserProgramArguments(configuration);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getVMArguments(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		String vmArgs = LaunchArgumentsHelper.getUserVMArguments(configuration);
		
		// Specify the location of the runtime workbench
		if (workspaceLocation == null || workspaceLocation.length()==0) {
			workspaceLocation = LaunchArgumentsHelper.getWorkspaceLocation(configuration);
		}
		// Specifies the directory to the data workspace(Equivalent to -data in Eclipse)
		if (workspaceLocation.length() > 0) {
			vmArgs = concatArg(vmArgs, "-Dosgi.instance.area="+workspaceLocation); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			vmArgs = concatArg(vmArgs, "-Dosgi.instance.area="+LaunchUtils.findOsgiInstanceArea()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Specifies the location of the target containing the plug-ins
		vmArgs = concatArg(vmArgs, "-Dosgi.install.area="+LaunchUtils.findOsgiInstallArea()); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Add all extra bundles needed to run a model as framework extensions
		String[] requiredPlugins = getRequiredPLugins();
		StringBuffer frameworkExtentions = new StringBuffer();
		for (String pluginId : requiredPlugins) {
			IPluginModelBase modelBase = PluginRegistry.findModel(pluginId);
			if( modelBase == null )
				continue;
			if( frameworkExtentions.length() > 0 ) {
				frameworkExtentions.append(",");
			}
			frameworkExtentions.append("file:/"+modelBase.getInstallLocation().replace(File.separatorChar, '/'));
		}
		
		vmArgs = concatArg(vmArgs, "-Dosgi.framework.extensions="+frameworkExtentions); //$NON-NLS-1$ //$NON-NLS-2$
		
		
        vmArgs = concatArg(vmArgs, "-Dosgi.framework.useSystemProperties=false");
        vmArgs = concatArg(vmArgs, "-Declipse.application=" + APPLICATION_NAME); 
		vmArgs = concatArg(vmArgs, "-Dosgi.frameworkParentClassloader=current"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Specify the Model to launch
		String modelFileName = LaunchUtils.getModelFileName(configuration);
		if( modelFileName != null ) {
			vmArgs = concatArg(vmArgs, "-Dmodel="+modelFileName); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return vmArgs;
	}

	/**
	 * Returns the result of concatenating the given argument to the
	 * specified vmArgs.
	 * 
	 * @param vmArgs existing VM arguments
	 * @param arg argument to concatenate
	 * @return result of concatenation
	 */
	private String concatArg(String vmArgs, String arg) {
		if (vmArgs.length() > 0 && !vmArgs.endsWith(" ")) //$NON-NLS-1$
			vmArgs = vmArgs.concat(" "); //$NON-NLS-1$
		return vmArgs.concat(arg);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getEnvironment(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public String[] getEnvironment(ILaunchConfiguration configuration) throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getWorkingDirectory(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public File getWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		return LaunchArgumentsHelper.getWorkingDirectory(configuration);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getVMSpecificAttributesMap(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public Map<String,String> getVMSpecificAttributesMap(ILaunchConfiguration configuration) throws CoreException {
		return LaunchArgumentsHelper.getVMSpecificAttributesMap(configuration);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#setDefaultSourceLocator(org.eclipse.debug.core.ILaunch, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	protected void setDefaultSourceLocator(ILaunch launch, ILaunchConfiguration configuration) throws CoreException {
		ILaunchConfigurationWorkingCopy wc = null;
		if (configuration.isWorkingCopy()) {
			wc = (ILaunchConfigurationWorkingCopy) configuration;
		} else {
			wc = configuration.getWorkingCopy();
		}
		String id = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, (String) null);
		if (!PDESourcePathProvider.ID.equals(id)) {
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, PDESourcePathProvider.ID);
			wc.doSave();
		}

		manageLaunch(launch);
	}

	/**
	 * Returns the location of the configuration area
	 * 
	 * @param configuration
	 * 				the launch configuration
	 * @return a directory where the configuration area is located
	 */
	protected File getConfigurationDirectory(ILaunchConfiguration configuration) {
		if (configDir == null)
			configDir = LaunchConfigurationHelper.getConfigurationArea(configuration);
		return configDir;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getBuildOrder(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		return computeBuildOrder(LaunchPluginValidator.getAffectedProjects(configuration));
	}

	/**
	 * Adds a listener to the launch to be notified at interesting launch lifecycle
	 * events such as when the launch terminates.
	 * 
	 * @param launch
	 * 			the launch 			
	 */
	protected void manageLaunch(ILaunch launch) {
		PDEPlugin.getDefault().getLaunchListener().manage(launch);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void preLaunchCheck(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		workspaceLocation = null;
		pluginModels = BundleLauncherHelper.getMergedBundleMap(configuration, false);
		bundles = new HashMap(pluginModels.size());
		Iterator iter = pluginModels.keySet().iterator();
		while (iter.hasNext()) {
			IPluginModelBase model = (IPluginModelBase) iter.next();
			bundles.put(model.getPluginBase().getId(), model);
		}

		boolean autoValidate = configuration.getAttribute(IPDELauncherConstants.AUTOMATIC_VALIDATE, false);
		monitor.beginTask("", autoValidate ? 3 : 4); //$NON-NLS-1$
		if (autoValidate)
			validatePluginDependencies(configuration, new SubProgressMonitor(monitor, 1));
		validateProjectDependencies(configuration, new SubProgressMonitor(monitor, 1));
		clear(configuration, new SubProgressMonitor(monitor, 1));
		launch.setAttribute(IPDELauncherConstants.CONFIG_LOCATION, getConfigurationDirectory(configuration).toString());
	}

	/**
	 * Clears the workspace prior to launching if the workspace exists and the option to 
	 * clear it is turned on.  Also clears the configuration area if that option is chosen.
	 * 
	 * @param configuration
	 * 			the launch configuration
	 * @param monitor
	 * 			the progress monitor
	 * @throws CoreException
	 * 			if unable to retrieve launch attribute values
	 * @since 3.3
	 */
	protected void clear(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		if (workspaceLocation == null) {
			workspaceLocation = LaunchArgumentsHelper.getWorkspaceLocation(configuration);
		}
		// Clear workspace and prompt, if necessary
		if (!LauncherUtils.clearWorkspace(configuration, workspaceLocation, new SubProgressMonitor(monitor, 1))) {
			monitor.setCanceled(true);
			return;
		}

		// clear config area, if necessary
		if (configuration.getAttribute(IPDELauncherConstants.CONFIG_CLEAR_AREA, false))
			CoreUtility.deleteContent(getConfigurationDirectory(configuration));
	}

	/**
	 * Checks if the Automated Management of Dependencies option is turned on.
	 * If so, it makes aure all manifests are updated with the correct dependencies.
	 * 
	 * @param configuration
	 * 			the launch configuration
	 * @param monitor
	 * 			a progress monitor
	 */
	protected void validateProjectDependencies(ILaunchConfiguration configuration, IProgressMonitor monitor) {
		LauncherUtils.validateProjectDependencies(configuration, monitor);
	}

	/**
	 * Validates inter-bundle dependencies automatically prior to launching
	 * if that option is turned on.
	 * 
	 * @param configuration
	 * 			the launch configuration
	 * @param monitor
	 * 			a progress monitor
	 */
	protected void validatePluginDependencies(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		EclipsePluginValidationOperation op = new EclipsePluginValidationOperation(configuration);
		LaunchPluginValidator.runValidationOperation(op, monitor);
	}
	
	
	protected String[] getRequiredPLugins() {
		
		return new String[] {
				"com.isencia.passerelle.workbench.logging.production",
				"com.isencia.passerelle.workbench.logging.development",
				"com.isencia.passerelle.workbench.model",
				"com.isencia.passerelle.actor",
				"com.isencia.passerelle.engine",
				"com.isencia.passerelle.engine.api",
				"be.isencia.passerelle.commons",
				"be.isencia.passerelle.ume",
				"ptolemy.domains.and.actors",
				"ptolemy.gui",
				"diva"				
		};
	}
}

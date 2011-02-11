package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowLaunchConfiguration extends LaunchConfiguration {

	private static Logger logger = LoggerFactory.getLogger(WorkflowLaunchConfiguration.class);
	
	private Map<String,String> systemProperties;

	protected WorkflowLaunchConfiguration(IFile file) {
		super(file);
	}

	public void addSystemProperty(final String name) {
		
        final String value = System.getProperty(name);
        if (value!=null) {
        	if (this.systemProperties==null) systemProperties= new HashMap<String,String>(7);
        	systemProperties.put(name, value);
        }
	}
	
	protected void setPropertes(ILaunch launch) {
		
		final String vmArgs = launch.getAttribute("org.eclipse.jdt.launching.VM_ARGUMENTS");
		logger.debug("Launch vmArgs is: "+vmArgs);
		final StringBuilder buf = new StringBuilder();
		if (vmArgs!=null) buf.append(vmArgs);
		if (systemProperties!=null) for (String name : systemProperties.keySet()) {
			buf.append("-D");
			buf.append(name);
			buf.append("=");
			buf.append(systemProperties.get(name));
			buf.append(" ");
		}
		logger.debug("Changed vmArgs are: "+buf.toString());
		launch.setAttribute(vmArgs, buf.toString());
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.debug.core.ILaunchConfiguration#launch(java.lang.String, org.eclipse.core.runtime.IProgressMonitor, boolean, boolean)
     */
    public ILaunch launch(String mode, IProgressMonitor monitor, boolean build, boolean register) throws CoreException {
    	
    	if (monitor == null) {
			monitor = new NullProgressMonitor();
    	}
    	/* Setup progress monitor
    	 * - Prepare delegate (0)
    	 * - Pre-launch check (1)
    	 * - [Build before launch (7)]					if build
    	 * - [Incremental build before launch (3)]		if build
    	 * - Final launch validation (1)
    	 * - Initialize source locator (1)
    	 * - Launch delegate (10) */
    	if (build) {
    		monitor.beginTask("", 23); //$NON-NLS-1$
    	}
    	else {
    		monitor.beginTask("", 13); //$NON-NLS-1$
    	}
		monitor.subTask(DebugCoreMessages.LaunchConfiguration_9);
    	try {
			// bug 28245 - force the delegate to load in case it is interested in launch notifications
	    	Set modes = getModes();
	    	modes.add(mode);
	    	ILaunchDelegate[] delegates = getType().getDelegates(modes);
	    	ILaunchConfigurationDelegate delegate = null;
	    	if (delegates.length == 1) {
	    		delegate = delegates[0].getDelegate();
	    	} else if (delegates.length == 0) {
	    		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(promptStatus);
	    		if (handler != null) {
	    			handler.handleStatus(delegateNotAvailable, new Object[] {this, mode});
	    		}
	    		IStatus status = new Status(IStatus.CANCEL, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, DebugCoreMessages.LaunchConfiguration_11, null); 
	    		throw new CoreException(status);
	    	} else {
	    		ILaunchDelegate del = getPreferredDelegate(modes);
	    		if(del == null) {
	    			del = getType().getPreferredDelegate(modes);
	    		}
	    		if(del == null) {
	    			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(promptStatus);
	    			IStatus status = null;
	    			if (handler != null) {
	    				status = (IStatus) handler.handleStatus(duplicateDelegates, new Object[] {this, mode});
	    			}
					if(status != null && status.isOK()) {
						del = getPreferredDelegate(modes);
						if(del == null) {
							del = getType().getPreferredDelegate(modes);
						}
						if(del != null) {
							delegate = del.getDelegate();
						}
						else {
							status = new Status(IStatus.CANCEL, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, DebugCoreMessages.LaunchConfiguration_13, null); 
				    		throw new CoreException(status);
						}
					}
					else {
						status = new Status(IStatus.CANCEL, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, DebugCoreMessages.LaunchConfiguration_13, null); 
			    		throw new CoreException(status);
					}
	    		}
	    		else {
	    			delegate = del.getDelegate();
	    		}
	    	}
	    	
			ILaunchConfigurationDelegate2 delegate2 = null;
			if (delegate instanceof ILaunchConfigurationDelegate2) {
				delegate2 = (ILaunchConfigurationDelegate2) delegate;
			}
			// allow the delegate to provide a launch implementation
			ILaunch launch = null;
			if (delegate2 != null) {
				launch = delegate2.getLaunch(this, mode);
			}
			if (launch == null) {
				launch = new Launch(this, mode, null);
			} else {
				// ensure the launch mode is valid
				if (!mode.equals(launch.getLaunchMode())) {
					IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, 
							MessageFormat.format(DebugCoreMessages.LaunchConfiguration_14, new String[]{mode, launch.getLaunchMode()}), null); 
					throw new CoreException(status);
				}
			}
			launch.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, Long.toString(System.currentTimeMillis()));
			boolean captureOutput = getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
			if(!captureOutput) {
			    launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, "false"); //$NON-NLS-1$
			} else {
			    launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, null);
			}
			launch.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, getLaunchManager().getEncoding(this));
			
		// perform initial pre-launch sanity checks
			monitor.subTask(DebugCoreMessages.LaunchConfiguration_8);
			
			if (delegate2 != null) {
				if (!(delegate2.preLaunchCheck(this, mode, new SubProgressMonitor(monitor, 1)))) {
					return launch;
				}
			}
			else {
				monitor.worked(1); /* No pre-launch-check */
			}
		// perform pre-launch build
			if (build) {
				IProgressMonitor buildMonitor = new SubProgressMonitor(monitor, 10, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				buildMonitor.beginTask(DebugCoreMessages.LaunchConfiguration_7, 10);			
				buildMonitor.subTask(DebugCoreMessages.LaunchConfiguration_6);
				if (delegate2 != null) {
					build = delegate2.buildForLaunch(this, mode, new SubProgressMonitor(buildMonitor, 7));
				}
				if (build) {
					buildMonitor.subTask(DebugCoreMessages.LaunchConfiguration_5);
					ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new SubProgressMonitor(buildMonitor, 3));				
				}
				else {
					buildMonitor.worked(3); /* No incremental build required */
				}
			}
		// final validation
			monitor.subTask(DebugCoreMessages.LaunchConfiguration_4);
			if (delegate2 != null) {
				if (!(delegate2.finalLaunchCheck(this, mode, new SubProgressMonitor(monitor, 1)))) {
					return launch;
				}
			}
			else {
				monitor.worked(1); /* No validation */
			}
			
			setPropertes(launch);
			if (register) {
			    getLaunchManager().addLaunch(launch);
			}
			
			try {
				//initialize the source locator
				monitor.subTask(DebugCoreMessages.LaunchConfiguration_3);
				initializeSourceLocator(launch);
				monitor.worked(1);

				/* Launch the delegate */
				monitor.subTask(DebugCoreMessages.LaunchConfiguration_2);
				delegate.launch(this, mode,	launch,	new SubProgressMonitor(monitor, 10));
			} catch (CoreException e) {
				// if there was an exception, and the launch is empty, remove it
				if (!launch.hasChildren()) {
					getLaunchManager().removeLaunch(launch);
				}
				throw e;
			} catch (RuntimeException e) {
				// if there was a runtime exception, and the launch is empty, remove it
				if (!launch.hasChildren()) {
					getLaunchManager().removeLaunch(launch);
				}
				throw e;
			}
			if (monitor.isCanceled()) {
				getLaunchManager().removeLaunch(launch);
			}
			return launch;
    	}
    	finally {
   			monitor.done();
    	}
    }

}

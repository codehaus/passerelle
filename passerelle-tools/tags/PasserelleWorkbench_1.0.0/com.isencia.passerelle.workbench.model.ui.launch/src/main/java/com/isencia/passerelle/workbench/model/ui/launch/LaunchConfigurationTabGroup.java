package com.isencia.passerelle.workbench.model.ui.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.pde.ui.launcher.ConfigurationTab;
import org.eclipse.pde.ui.launcher.PluginsTab;
import org.eclipse.pde.ui.launcher.TracingTab;


public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
//		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] { new PasserelleModelLaunchConfigurationTab(),
//				new JavaArgumentsTab(), new JavaJRETab(), new JavaClasspathTab(), new EnvironmentTab(), new CommonTab() };
//		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
//				new JavaArgumentsTab(), new JavaJRETab(), new JavaClasspathTab(), new EnvironmentTab(), new CommonTab() };
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {new JavaArgumentsTab(), new PluginsTab(false), new ConfigurationTab(true), new TracingTab(), new EnvironmentTab(), new CommonTab()};

		setTabs(tabs);
	}

}

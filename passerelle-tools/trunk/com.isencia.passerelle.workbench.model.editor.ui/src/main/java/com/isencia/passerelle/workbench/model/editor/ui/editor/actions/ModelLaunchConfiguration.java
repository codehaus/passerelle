package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;

public class ModelLaunchConfiguration extends PlatformObject implements ILaunchConfiguration {

	@Override
	public boolean contentsEqual(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ILaunchConfigurationWorkingCopy copy(String name) throws CoreException {
		return null;
	}

	@Override
	public void delete() throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean getAttribute(String attributeName, boolean defaultValue) throws CoreException {
		return false;
	}

	@Override
	public int getAttribute(String attributeName, int defaultValue)
			throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List getAttribute(String attributeName, List defaultValue)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set getAttribute(String attributeName, Set defaultValue)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getAttribute(String attributeName, Map defaultValue)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttribute(String attributeName, String defaultValue)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getAttributes() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCategory() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFile getFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResource[] getMappedResources() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMemento() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set getModes() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchDelegate getPreferredDelegate(Set modes) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfigurationType getType() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfigurationWorkingCopy getWorkingCopy()
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasAttribute(String attributeName) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLocal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMigrationCandidate() throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isWorkingCopy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ILaunch launch(String mode, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunch launch(String mode, IProgressMonitor monitor, boolean build)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunch launch(String mode, IProgressMonitor monitor, boolean build,
			boolean register) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void migrate() throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean supportsMode(String mode) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

}

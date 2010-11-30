package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;


import java.net.URL;

import javax.management.MBeanServerConnection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;
import com.isencia.passerelle.workbench.model.launch.ModelRunner;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

@SuppressWarnings("restriction")
public class RunAction extends ExecutionAction implements IEditorActionDelegate {

	private static final Logger logger = LoggerFactory.getLogger(RunAction.class);
	
	public RunAction() {
		super();
		setId(getClass().getName());
		setText("Run the workflow from start to end until finished.");
		setImageDescriptor(Activator.getImageDescriptor("/icons/run_workflow.gif"));
	}

	@Override
	public void run() {
		run(this);
	}
	
	@Override
	public void run(IAction action) {
	    
		try {
			final IFile config = getModelRunner();
			EclipseUtils.getPage().saveAllEditors(true);
			
			// Make sure that the current editor is the selected resource.
			final IResource sel = getSelectedResource();
			if (sel instanceof IFile)  EclipseUtils.openEditor((IFile)sel);
			
			if (System.getProperty("eclipse.debug.session")!=null) {
				final ModelRunner runner = new ModelRunner();
				runner.runModel(sel.getLocation().toOSString());
			} else {
				ILaunchConfiguration configuration = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(config);
				DebugUITools.launch(configuration, ILaunchManager.RUN_MODE);
			}
			
			addRefreshListener();
			
		} catch (Exception e) {
			logger.error("Cannot read configuration", e);
		}

	}
	
	public boolean isEnabled() {
		try { 
			final MBeanServerConnection server = RemoteManagerAgent.getServerConnection();
			return server.getObjectInstance(RemoteManagerAgent.REMOTE_MANAGER)==null;
		} catch (Throwable e) {
			return true;
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}
	
	public IFile getModelRunner() throws Exception {
		
	    // if the bundle is not ready then there is no image
        Bundle bundle = Activator.getDefault().getBundle();

        // look for the image (this will check both the plugin and fragment folders
        URL fullPathString = BundleUtility.find(bundle, "ModelRunner.txt");
        final IResource sel= getSelectedResource();
        final IFile   file = sel.getProject().getFile("WorkflowConfiguration.launch");
        if (file.exists()) return file;
        file.create(fullPathString.openStream(), true, null);
        file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
        return file;
 	}

	private IResource getSelectedResource() {
		final ISelection sel = EclipseUtils.getPage().getSelection();
		if (sel!=null && sel instanceof IStructuredSelection) {
			final IStructuredSelection str = (IStructuredSelection)sel;
			final Object res = str.getFirstElement();
			if (res instanceof IFile) {
				final IFile file = (IFile)res;
				if (file.getName().toLowerCase().endsWith(".moml")) return file;
			}
		} 
		final IFile file =  EclipseUtils.getIFile(EclipseUtils.getPage().getActiveEditor().getEditorInput());
		if (file.getName().toLowerCase().endsWith(".moml")) return file;
		return null;
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub

	}

}

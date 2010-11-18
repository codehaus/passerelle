package com.isencia.passerelle.workbench.model.editor.ui.dnd;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.palette.ClassTypeFactory;
import com.isencia.passerelle.workbench.model.ui.command.CreateComponentCommand;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public class FileTransferDropTargetListener  extends AbstractTransferDropTargetListener {

   private IDropClassFactory dropFactory;
   
   private static final Logger logger = LoggerFactory.getLogger(FileTransferDropTargetListener.class);
	
   public FileTransferDropTargetListener(EditPartViewer viewer, Transfer xfer) {
      super(viewer, xfer);
      
      final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("com.isencia.passerelle.workbench.model.editor.ui.dropFactory");
      if (configs!=null && configs.length>=1) {
    	  try {
			dropFactory = (IDropClassFactory)configs[0].createExecutableExtension("class");
		} catch (CoreException e) {
			logger.error("Cannot create class from "+configs[0], e);
	        dropFactory = new DefaultDropClassFactory();
		}
      } else {
          dropFactory = new DefaultDropClassFactory();
      }
   }

   public FileTransferDropTargetListener(EditPartViewer viewer) {
      this(viewer, FileTransfer.getInstance());
   }

   protected void updateTargetRequest() {
	   CreateRequest req = ((CreateRequest)getTargetRequest());
	   if (req == null) return;
	   req.setLocation(getDropLocation());
   }
   
   protected Request createTargetRequest() {
	   CreateRequest request = new CreateRequest();
	   
	   final Class<? extends NamedObj> clazz = dropFactory.getClassForPath(getFilePath());
	   if (clazz == null) return null;

	   final ClassTypeFactory factory = new ClassTypeFactory(clazz, getFileName());
	   request.setFactory(factory);
	   return request;
   }

   /**
    * Gets the file path relative to the workspace.
    * This makes the node work when the workspace is exported.
    * @return
    */
   private String getFilePath() {
	   DropTargetEvent event = getCurrentEvent();
	   if (event!=null&&event.data!=null) {
	       final String fullPath = ((String[])event.data)[0];
	       final String workspace= ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
	       return fullPath.substring(workspace.length()+1, fullPath.length()).replace('\\', '/');
	   } else {
		   final ISelection sel = EclipseUtils.getPage().getSelection();
			if (!(sel instanceof IStructuredSelection)) return null;
			
			final IStructuredSelection ss = (IStructuredSelection) sel;
			final Object          element = ss.getFirstElement();
			if (element instanceof IResource) {
				final IResource res = (IResource)element;
				final String fullPath = res.getRawLocation().toOSString();
				final String workspace= ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
				return fullPath.substring(workspace.length()+1, fullPath.length()).replace('\\', '/');
			}
	   }
	   
	   return null;
   }
   private String getFileName() {
	   final String filePath = getFilePath();
	   if (filePath==null) return null;
	   return (new Path(filePath)).lastSegment();
   }

	/**
	 * Returns the current command from the target EditPart.
	 * 
	 * @return The current command from the target EditPart
	 */
	protected Command getCommand() {
		final Command command = getTargetEditPart().getCommand(getTargetRequest());
		if (command instanceof CreateComponentCommand) {
			// We attempt to send the file parameter over if there is one
			// in the actor we are adding.
			final CreateComponentCommand cmd = (CreateComponentCommand)command;
			cmd.addConfiguratbleParameterValue(FileParameter.class, getFilePath());
		}
		return command;
	}


   protected void handleDragOver() {
	   getCurrentEvent().detail = DND.DROP_COPY;
	   super.handleDragOver();
   }
}
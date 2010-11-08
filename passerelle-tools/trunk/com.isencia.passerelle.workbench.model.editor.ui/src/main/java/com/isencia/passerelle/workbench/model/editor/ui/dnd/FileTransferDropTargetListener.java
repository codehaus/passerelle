package com.isencia.passerelle.workbench.model.editor.ui.dnd;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.gef.requests.CreateRequest;
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

public class FileTransferDropTargetListener  extends AbstractTransferDropTargetListener {

   private String            filePath, fileName;
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
	   ((CreateRequest)getTargetRequest()).setLocation(getDropLocation());
   }
   
   protected Request createTargetRequest() {
	   CreateRequest request = new CreateRequest();
	   
	   final Class<? extends NamedObj> clazz = dropFactory.getClassForPath(filePath);
	   if (clazz == null) return null;
	   
	   final ClassTypeFactory factory = new ClassTypeFactory(clazz, fileName);
	   request.setFactory(factory);
	   return request;
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
			cmd.addConfiguratbleParameterValue(FileParameter.class, filePath);
		}
		return command;
	}


   protected void handleDragOver() {
	   getCurrentEvent().detail = DND.DROP_COPY;
	   super.handleDragOver();
   }
   
   protected void handleDrop() {
	   
	   DropTargetEvent event = getCurrentEvent();
	   this.filePath = ((String[])event.data)[0];
	   this.fileName = (new File(filePath)).getName();
	   super.handleDrop();
	   
	}
}
package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>ActorEditPolicy</code> creates commands for connection initiation and completion. It uses the default
 * feedback provided by the <code><b>GraphicalNodeEditPolicy</b></code> base class
 *
 * @author Dirk Jacobs
 *
 */
public class CommentEditPolicy extends GraphicalNodeEditPolicy {

	private final static Logger logger = LoggerFactory.getLogger(CommentEditPolicy.class);	

	public CommentEditPolicy() {
		super();
	}

	public Logger getLogger() {
		return logger;
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCreateCommand(org.eclipse.gef.requests.CreateConnectionRequest)
	 */
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCompleteCommand(org.eclipse.gef.requests.CreateConnectionRequest)
	 */
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
//		ConnectionCommand command = new ConnectionCommand();
//		Channel channel = ((ChannelEditPart) request.getConnectionEditPart()).getChannel();
//		command.setChannel(channel);
//		command.setOldTarget(channel.getTarget());
//		ConnectionAnchor anchor = getActorEditPart().getTargetConnectionAnchor(request);
//		InputPort port = (InputPort) getActorEditPart().getTargetPort(anchor);
//		command.setTarget(port);
//
//		return command;
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
//		ConnectionCommand command = new ConnectionCommand();
//		Channel channel = ((ChannelEditPart) request.getConnectionEditPart()).getChannel();
//		command.setChannel(channel);
//		command.setOldSource(channel.getSource());
//		ConnectionAnchor anchor = getActorEditPart().getSourceConnectionAnchor(request);
//		OutputPort port = (OutputPort) getActorEditPart().getSourcePort(anchor);
//		command.setSource(port);
//
//		return command;
		return null;
	}

	public boolean understandsRequest(Request req) {
//		if (FlowAction.SHOW_PARAMETER.equals(req.getType())) {
//			return true;
//		} else if (FlowAction.CHANGE_COLOR.equals(req.getType())) {
//			return true;
//		} else if (FlowAction.CHANGE_ICON.equals(req.getType())) {
//			return true;
//		} else if (FlowAction.RENAME.equals(req.getType())) {
//			return true;
//		}
		return super.understandsRequest(req);
	}

	public Command getCommand(Request request) {
//		if (FlowAction.SHOW_PARAMETER.equals(request.getType())) {
//			return createShowParameterCommand((FlowRequest) request);
//		} else if (FlowAction.CHANGE_COLOR.equals(request.getType())) {
//			return createChangeColorCommand((FlowRequest) request);
//		} else if (FlowAction.CHANGE_ICON.equals(request.getType())) {
//			return createChangeIconCommand((FlowRequest) request);
//		} else if (FlowAction.RENAME.equals(request.getType())) {
//			return createRenameCommand((FlowRequest) request);
//		}
//
		return super.getCommand(request);
	}

/*	protected Command createShowParameterCommand(FlowRequest request) {
		ParameterCommand cmd = new ParameterCommand();
		cmd.setChild(getFlowNode());
		cmd.setClassLoader(buildClassLoader());
		return cmd;
	}

	protected Command createChangeColorCommand(FlowRequest request) {
		ChangeColorCommand cmd = new ChangeColorCommand();
		cmd.setChild(getFlowNode());
		return cmd;
	}

	protected Command createChangeIconCommand(FlowRequest request) {
		ChangeIconCommand cmd = new ChangeIconCommand();
		cmd.setChild(getFlowNode());
		return cmd;
	}

	protected Command createRenameCommand(FlowRequest request) {
		RenameCommand cmd = new RenameCommand();
		cmd.setChild(getFlowNode());
		return cmd;
	}
*/

/*	protected ClassLoader buildClassLoader() {
		FlowEditPart flowEditPart = (FlowEditPart) getActorEditPart().getParent();
		IResource resource = flowEditPart.getResource();
		ClassLoader classLoader = null;
		try {
			classLoader = ClassHelper.getURLClassLoader(resource);
		} catch (Exception e) {

		}
		return classLoader;
	}
*/}

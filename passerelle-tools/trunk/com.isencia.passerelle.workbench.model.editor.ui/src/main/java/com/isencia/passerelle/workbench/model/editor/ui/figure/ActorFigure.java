package com.isencia.passerelle.workbench.model.editor.ui.figure;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.workbench.model.editor.ui.IBody;

public class ActorFigure extends AbstractNodeFigure {

	public final static Color ACTOR_BACKGROUND_COLOR = ColorConstants.gray;

	private IFigure body = null;
	private InputPorts inputPorts = null;
	private OutputPorts outputPorts = null;
	private HashMap<String, PortFigure> inputPortMap = new HashMap<String, PortFigure>();
	private HashMap<String, PortFigure> outputPortMap = new HashMap<String, PortFigure>();

	public ActorFigure(String name, Image image, Clickable clickable) {
		super(name);

		add(new CompositeFigure(image, clickable));
	}

	public ActorFigure(String name, Image image) {
		super(name);

		add(new CompositeFigure(image, null));
	}

	protected IFigure generateBody(Image image, Clickable clickable) {
		Body body = new Body();
		body.setBorder(new LineBorder());
		body.initImage(image);
		body.initClickable(clickable);
		return (body);
	}

	private class CompositeFigure extends Figure {

		public CompositeFigure(Image image, Clickable clickable) {
			ToolbarLayout layout = new ToolbarLayout();
			layout.setVertical(false);
			setLayoutManager(layout);
			setOpaque(false);

			inputPorts = new InputPorts();
			add(inputPorts);
			body = generateBody(image, clickable);
			add(body);

			outputPorts = new OutputPorts();
			add(outputPorts);
		}

		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize(-1, -1);
		}

		@Override
		public Dimension getPreferredSize(int hint, int hint2) {
			Vector<ConnectionAnchor> targetConnectionAnchors = getTargetConnectionAnchors();
			Vector<ConnectionAnchor> sourceConnectionAnchors = getSourceConnectionAnchors();
			int maxAnchorCount = Math.max(targetConnectionAnchors.size(),
					sourceConnectionAnchors.size());
			if (maxAnchorCount > 0) {
				int height = maxAnchorCount
						* ANCHOR_HEIGTH
						+ (maxAnchorCount > 1 ? ((maxAnchorCount - 1) * ANCHOR_SPACING)
								: 0) + (2 * ANCHOR_MARGIN);
				if (height < MIN_HEIGHT)
					height = MIN_HEIGHT;
				return new Dimension(DEFAULT_WIDTH, height);
			} else
				return super.getMinimumSize();
		}

	}

	private class Ports extends Figure {
		public Ports() {
			ToolbarLayout layout = new ToolbarLayout();
			layout.setVertical(true);
			layout.setSpacing(ANCHOR_SPACING);
			setLayoutManager(layout);
			setOpaque(false);
			setBorder(new PortsBorder());
		}
	}

	private class PortsBorder extends AbstractBorder {

		@Override
		public Insets getInsets(IFigure ifigure) {
			return new Insets(ANCHOR_MARGIN, 0, 0, 0);
		}

		@Override
		public void paint(IFigure ifigure, Graphics g, Insets insets) {
		}

	}

	private class InputPorts extends Ports {
		public InputPorts() {
			super();
		}
	}

	private class OutputPorts extends Ports {
		public OutputPorts() {
			super();
		}

	}

	private class Body extends RectangleFigure implements IBody {
		/**
		 * @param s
		 */
		public Body() {
			BorderLayout layout = new BorderLayout();
			setLayoutManager(layout);

			setBackgroundColor(ACTOR_BACKGROUND_COLOR);
			setOpaque(true);
		}

		public void initImage(Image image) {
			if (image != null) {
				ImageFigure imageFigure = new ImageFigure(image);
				imageFigure.setAlignment(PositionConstants.WEST);
				imageFigure.setBorder(new MarginBorder(5, 5, 0, 0));
				add(imageFigure, BorderLayout.TOP);
			}
		}

		public void initClickable(Clickable clickable) {
			if (clickable != null) {
				add(clickable, BorderLayout.BOTTOM);
			}
		}

		protected void fillShape(Graphics graphics) {
			graphics.pushState();
			graphics.setForegroundColor(ColorConstants.white);
			graphics.setBackgroundColor(getBackgroundColor());
			graphics.fillGradient(getBounds(), true);
			graphics.popState();
		}

		public Dimension getPreferredSize(int wHint, int hHint) {
			Dimension size = getParent().getSize().getCopy();
			return size;
		}

	}

	public void setBackgroundColor(Color c) {
		if (body != null) {
			body.setBackgroundColor(c);
		}
	}

	public Color getBackgroundColor() {
		if (body != null) {
			return body.getBackgroundColor();
		}
		return super.getBackgroundColor();
	}

	/**
	 * Add an input port and its anchor
	 * 
	 * @param portName
	 *            unique name to refer to the port
	 * @param displayname
	 */
	public PortFigure addInput(String portName, String displayName) {
		InputPortFigure inputPortFigure = new InputPortFigure(portName);
		inputPortFigure.setToolTip(new Label(displayName));
		int x = 0;
		int y = 0;
		inputPortFigure.setLocation(new Point(x, y));
		if (inputPorts != null) {
			inputPorts.add(inputPortFigure);
			inputPortMap.put(portName, inputPortFigure);
		}
		// TODO update Anchor with correct attributes
		FixedConnectionAnchor anchor = new FixedConnectionAnchor(
				inputPortFigure);
		anchor.offsetV = ANCHOR_HEIGTH / 2;
		getTargetConnectionAnchors().add(anchor);
		connectionAnchors.put(portName, anchor);
		return inputPortFigure;
	}
	
	public void removeInput(String portName) {
		PortFigure figure = inputPortMap.get(portName);
		if (figure != null){
			inputPorts.remove(figure);
			ConnectionAnchor anchor = getConnectionAnchor(portName);
			if (anchor!=null){
				getTargetConnectionAnchors().remove(anchor);
			}
			
		}
	}
	public void removeOutput(String portName) {
		PortFigure figure = outputPortMap.get(portName);
		if (figure != null){
			outputPorts.remove(figure);
			ConnectionAnchor anchor = getConnectionAnchor(portName);
			if (anchor!=null){
				getSourceConnectionAnchors().remove(anchor);
			}
			
		}
	}

	/**
	 * Add an output port and its anchor
	 * 
	 * @param portName
	 *            unique name to refer to the port
	 * @param displayname
	 */
	public PortFigure addOutput(String portName, String displayName) {
		OutputPortFigure outputPortFigure = new OutputPortFigure(portName);
		outputPortFigure.setToolTip(new Label(displayName));
		int x = 0;
		int y = 0;
		outputPortFigure.setLocation(new Point(x, y));
		if (outputPorts != null) {
			outputPorts.add(outputPortFigure);
			outputPortMap.put(portName, outputPortFigure);
		}
		FixedConnectionAnchor anchor = new FixedConnectionAnchor(
				outputPortFigure);
		anchor.offsetV = ANCHOR_HEIGTH / 2;
		anchor.offsetH = ANCHOR_WIDTH - 1;
		getSourceConnectionAnchors().add(anchor);
		connectionAnchors.put(portName, anchor);
		return outputPortFigure;
	}

	public PortFigure getInputPort(String name) {
		return inputPortMap.get(name);
	}

	public PortFigure getOutputPort(String name) {
		return outputPortMap.get(name);
	}
}

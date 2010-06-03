package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public abstract class CompoundIOFigure extends AbstractBaseFigure {

	public final static int PORT_WIDTH = 120;
	public final static Dimension PORT_SIZE = new Dimension(PORT_WIDTH,
			DEFAULT_HEIGHT);
	private Body body = null;

	public CompoundIOFigure(String name) {
		super(name);
		body = new Body();
		ImageDescriptor icon = getImageDescriptor();
		if (icon != null) {
			body.initImage(icon.createImage());
		}
		add(body);

		setBackgroundColor(getBackgroundColor());
	}
	protected abstract ImageDescriptor getImageDescriptor();
	protected abstract Color getBackGroundcolor();
	private class Body extends RectangleFigure {
		ImageFigure imageFigure;

		public Body() {
			ToolbarLayout layout = new ToolbarLayout();
			layout.setVertical(true);
			setLayoutManager(layout);
			setOpaque(true);
		}

		private void initImage(Image image) {
			if (image != null) {
				imageFigure = new ImageFigure(image);
				imageFigure.setAlignment(PositionConstants.WEST);
				imageFigure.setBorder(new MarginBorder(5, 5, 10, 10));
				add(imageFigure);
			}
		}

		public Image getImage() {
			if (imageFigure == null)
				return null;
			return imageFigure.getImage();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.draw2d.IFigure#getPreferredSize(int, int)
		 */
		public Dimension getPreferredSize(int wHint, int hHint) {
			Dimension preferredSize = getParent().getSize().getCopy();
			return preferredSize;
		}

		public void setImage(Image image) {
			imageFigure.setImage(image);
		}

		protected void fillShape(Graphics graphics) {
			graphics.pushState();
			graphics.setForegroundColor(ColorConstants.white);
			graphics.setBackgroundColor(getBackgroundColor());
			graphics.fillGradient(getBounds(), false);
			graphics.popState();
		}

	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		return PORT_SIZE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Figure#layout()
	 */
	public void validate() {
		LayoutManager layout = getLayoutManager();
		layout.setConstraint(body, new Rectangle(0, 0, -1, -1));
		super.validate();
	}



}

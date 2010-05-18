package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class CommentFigure extends AbstractBaseFigure {

	public final static int COMMENT_WIDTH = 120;
	public final static int COMMENT_HEIGHT = 40;
	public final static Dimension COMMENT_SIZE = new Dimension(COMMENT_WIDTH,
			COMMENT_HEIGHT);
	public final static Color COMMENT_BACKGROUND_COLOR = new Color(null, 255, 255,
			0);

	private Body body = null;

	public CommentFigure(String name, Image image, Clickable clickable) {
		super(name);

		add(new CompositeFigure(image, clickable));
	}

	public CommentFigure(String name, Image image) {
		super(name);

		add(new CompositeFigure(image, null));
	}

	private class CompositeFigure extends Figure {

		public CompositeFigure(Image image, Clickable clickable) {
			ToolbarLayout layout = new ToolbarLayout();
			layout.setVertical(false);
			setLayoutManager(layout);
			setOpaque(false);
			nameLabel.setBorder(new MarginBorder(5,5,10,10));
			nameLabel.setIcon(image);
			body = new Body();
			body.initImage(image);
			body.initClickable(clickable);
			add(body);
			IFigure parent = getParent();
			if (parent != null)
				setPreferredSize(parent.getSize().getCopy());
			setBackgroundColor(COMMENT_BACKGROUND_COLOR);
		}

	}

	private class Body extends RectangleFigure {
		ImageFigure imageFigure;

		public Body() {
			BorderLayout layout = new BorderLayout();
			setLayoutManager(layout);
			setBackgroundColor(COMMENT_BACKGROUND_COLOR);
			setOpaque(true);
			IFigure parent = getParent();
			if (parent != null)
				setPreferredSize(parent.getSize().getCopy());

		}

		private void initImage(Image image) {
			if (image != null) {
				imageFigure = new ImageFigure(image);
				imageFigure.setAlignment(PositionConstants.WEST);
				imageFigure.setBorder(new MarginBorder(5, 5, 0, 0));
				add(imageFigure);
			}
		}

		private void initClickable(Clickable clickable) {
			if (clickable != null) {
				add(clickable, BorderLayout.BOTTOM);
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

}

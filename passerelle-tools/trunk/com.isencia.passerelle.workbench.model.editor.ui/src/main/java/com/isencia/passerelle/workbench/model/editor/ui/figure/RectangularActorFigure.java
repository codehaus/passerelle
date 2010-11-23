package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.workbench.model.editor.ui.IBody;

public class RectangularActorFigure extends ActorFigure {

	public RectangularActorFigure(String name, Image image, Clickable[] clickables) {
		super(name, image, clickables);
	}

	protected IFigure generateBody(Image image, Clickable[] clickables) {
		Body body = new Body();
		body.setBorder(new LineBorder());
		body.initImage(image);
		for (Clickable clickable : clickables)
			body.initClickable(clickable);
		return (body);
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
				// ImageFigure propertiesFgure = new
				// ImageFigure(IMAGE_DESCRIPTOR_PROPERTIES.createImage());
				// propertiesFgure.setAlignment(PositionConstants.EAST);
				//			
				// propertiesFgure.setBorder(new MarginBorder(5, 0, 0, 5));
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
}

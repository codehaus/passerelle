package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;

import com.isencia.passerelle.workbench.model.editor.ui.INameable;

public class AbstractBaseFigure extends Figure implements INameable {

	public final static int DEFAULT_WIDTH  = 62;
	public final static int DEFAULT_HEIGHT = 60;
	public final static int MIN_HEIGHT     = 60;
	public static Dimension DEFAULT_SIZE   = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	
	public final static Color DEFAULT_BACKGROUND_COLOR = ColorConstants.lightGray;

	public final static Color DEFAULT_FOREGROUND_COLOR = ColorConstants.gray;
	
	public final static Color LABEL_BACKGROUND_COLOR = new Color(null, 0, 0, 204);
	
	protected Label nameLabel = new Label();

	public AbstractBaseFigure(String name) {
		this(name, true);

	}

	public AbstractBaseFigure(String name, boolean withLabel) {
		ToolbarLayout layout = new ToolbarLayout();
		layout.setVertical(true);
		layout.setSpacing(2);
		setLayoutManager(layout);
		setBackgroundColor(getBackgtoundColor());
		if (withLabel) {
			nameLabel.setText(name);
			nameLabel.setOpaque(true);
			add(nameLabel);
		}
		setOpaque(false);

	}

	protected Color getBackgtoundColor() {
		return DEFAULT_BACKGROUND_COLOR;
	}

	public String getName() {
		return this.nameLabel.getText();
	}

	public void setName(String name) {
		this.nameLabel.setText(name);
	}

}

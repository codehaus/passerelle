package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;

public class AbstractBaseFigure extends Figure {

	public final static int DEFAULT_WIDTH = 60;
	public final static int DEFAULT_HEIGHT = 60;
	public final static int MIN_HEIGHT = 60;
	public final static Dimension DEFAULT_SIZE = new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT);
	public final static Color DEFAULT_BACKGROUND_COLOR = ColorConstants.gray;
//	public final static Color DEFAULT_BACKGROUND_COLOR = new Color(null,252,233,79);
	public final static Color DEFAULT_FOREGROUND_COLOR = new Color(null,0,0,0);
	
    private Label nameLabel = new Label();

	
	public AbstractBaseFigure(String name) {
		ToolbarLayout layout = new ToolbarLayout();
		layout.setVertical(true);
		layout.setSpacing(2);
		setLayoutManager(layout);
		setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
        nameLabel.setText(name);
        add(nameLabel);
		setOpaque(false);
	}
	
	public String getName() {
		return this.nameLabel.getText();
	}
	
	public void setName(String name) {
		this.nameLabel.setText(name);
	}

	
}

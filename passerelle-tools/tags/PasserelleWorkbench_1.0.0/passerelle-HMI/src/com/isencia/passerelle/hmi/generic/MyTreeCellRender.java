package com.isencia.passerelle.hmi.generic;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;


/**
 * 
 * @author ADIOUF
 *
 */
public class MyTreeCellRender implements TreeCellRenderer {
	private JCheckBox checkBox = new JCheckBox();
	private Color selectionForeground;
	private Color selectionBackground;
	private Color textForeground;
	private Color textBackground;

	protected JCheckBox getCheckBox() {
		return checkBox;
	}

	public MyTreeCellRender() {
		Boolean booleanValue = (Boolean) UIManager
				.get("Tree.drawsFocusBorderAroundIcon");
		checkBox.setFocusPainted((booleanValue != null)
				&& (booleanValue.booleanValue()));

		selectionForeground = UIManager.getColor("Tree.selectionForeground");
		selectionBackground = UIManager.getColor("Tree.selectionBackground");
		textForeground = UIManager.getColor("Tree.textForeground");
		textBackground = UIManager.getColor("Tree.textBackground");
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		String stringValue = tree.convertValueToText(value, selected, expanded,
				leaf, row, false);
		checkBox.setText(stringValue);
		checkBox.setSelected(false);
		checkBox.setEnabled(tree.isEnabled());

		if (selected) {
			checkBox.setForeground(selectionForeground);
			checkBox.setBackground(selectionBackground);
		} else {
			checkBox.setForeground(textForeground);
			checkBox.setBackground(textBackground);
		}

		if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
			Object userObject = ((DefaultMutableTreeNode) value)
					.getUserObject();

			if (userObject instanceof TreeDataObject) {
				TreeDataObject tn = (TreeDataObject) userObject;
				checkBox.setText(tn.getName());
				if (!tn.isCheckable()) {
					checkBox.setSelected(false);
					checkBox.setEnabled(false);
				} else {
					checkBox.setEnabled(true);
					switch (tn.getStatus()) {
					case TreeDataObject.FULLY_NOT_SELECTED:
						checkBox.setSelected(false);
						break;
					case TreeDataObject.FULLY_SELECTED:
						checkBox.setSelected(true);
						break;
					case TreeDataObject.PARTIAL_SELECTED:
						checkBox.setSelected(true);
						checkBox.setForeground(Color.gray);
						break;
					}
				}
			}
		}
		return checkBox;
	}
}
package com.isencia.passerelle.hmi.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTree;

/**
 * 
 * @author ADIOUF
 *
 */
public class MyTreeCellEditor extends AbstractCellEditor implements
		TreeCellEditor, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MyTreeCellRender renderer = new MyTreeCellRender();
	private JXTree tree;
	private Object value;
	private Integer maxLeafSelect;

	HashMap<String, String> labels = new HashMap<String, String>();

	private JPanel myPanel;
	private JTextField myText;
	private JLabel myLabel = new JLabel(" New Label ");
	String key;

	public MyTreeCellEditor(JXTree tree, Integer maxLeafSelect) {

		myPanel = new JPanel();
		myText = new JTextField(10);
		myText.addActionListener(this);
		myPanel.setLayout(new BorderLayout());
		myPanel.add(BorderLayout.CENTER, myLabel);
		myPanel.add(BorderLayout.EAST, myText);

		this.tree = tree;
		this.maxLeafSelect = maxLeafSelect;

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stateChange();
			}
		};

		renderer.getCheckBox().addActionListener(actionListener);
	}

	public void stateChange() {
		if (value != null) {
			DefaultMutableTreeNode tn = (((DefaultMutableTreeNode) value));
			TreeDataObject cbtn = (TreeDataObject) (tn.getUserObject());
			if (cbtn.isCheckable()) {
				if (((tn.getChildCount() > 0) && (countSelectedLeaf(
						(DefaultMutableTreeNode) tn.getRoot()).compareTo(
						maxLeafSelect) < 0))
						|| (!renderer.getCheckBox().isSelected())) {

					// Update node
					if (renderer.getCheckBox().isSelected()) {
						cbtn.setStatus(TreeDataObject.FULLY_SELECTED);
					} else {
						cbtn.setStatus(TreeDataObject.FULLY_NOT_SELECTED);
					}

					if (countSelectedLeaf((DefaultMutableTreeNode) tn.getRoot())
							.compareTo(maxLeafSelect) > 0) {
						renderer.getCheckBox().setSelected(false);
						cbtn.setStatus(TreeDataObject.FULLY_NOT_SELECTED);
					}

					// update these son
					if (tn.getChildCount() > 0) {
						checkChild(tn);
					}

					// update parents
					if (tn.getParent() != null) {
						checkParent((DefaultMutableTreeNode) tn.getParent());
					}
				} else if (((tn.getChildCount() == 0) && (countSelectedLeaf(
						(DefaultMutableTreeNode) tn.getRoot()).compareTo(
						maxLeafSelect) < 0))
						|| (!renderer.getCheckBox().isSelected())) {
					//Update node
					if (renderer.getCheckBox().isSelected()) {
						cbtn.setStatus(TreeDataObject.FULLY_SELECTED);
					} else {
						cbtn.setStatus(TreeDataObject.FULLY_NOT_SELECTED);
					}

					if (countSelectedLeaf((DefaultMutableTreeNode) tn.getRoot())
							.compareTo(maxLeafSelect) > 0) {
						renderer.getCheckBox().setSelected(false);
						cbtn.setStatus(TreeDataObject.FULLY_NOT_SELECTED);
					}

					// update parents
					if (tn.getParent() != null) {
						checkParent((DefaultMutableTreeNode) tn.getParent());
					}
				} else {
					renderer.getCheckBox().setSelected(false);
				}

				MyTreeCellEditor.this.tree.repaint();
			}
		}
		
		myText.setBackground(Color.white);
		myText.setFocusable(true);
	}

	public Object getCellEditorValue() {
		return this.value;
	}

	public boolean isCellEditable(EventObject event) {
		return true;
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row) {

		Component node = renderer.getTreeCellRendererComponent(tree, value,
				true, expanded, leaf, row, false);

		myPanel.add(BorderLayout.WEST, node);

		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode ob = (DefaultMutableTreeNode) value;
			myText.setText(labels.get(ob.getUserObject().toString()));
			key = ob.getUserObject().toString();
		}

		this.value = value;

		// return node;
		return myPanel;
	}

	/**
	 * update the status of the son of a node (recursive)
	 * 
	 * @param tn
	 */
	private void checkChild(DefaultMutableTreeNode tn) {
		int childCount = tn.getChildCount();

		for (int i = 0; i < childCount; i++) {
			TreeDataObject userObject = (TreeDataObject) ((DefaultMutableTreeNode) tn
					.getChildAt(i)).getUserObject();

			if (renderer.getCheckBox().isSelected()) {
				if (userObject.isCheckable()) {
					if (countSelectedLeaf((DefaultMutableTreeNode) tn.getRoot())
							.compareTo(maxLeafSelect) < 0) {
						userObject.setStatus(TreeDataObject.FULLY_SELECTED);
						TreePath treePath = new TreePath(tn.getPath());
						MyTreeCellEditor.this.tree.fireTreeExpanded(treePath);
					} else {
						break;
					}
				}
			} else {
				userObject.setStatus(TreeDataObject.FULLY_NOT_SELECTED);
				TreePath treePath = new TreePath(tn.getPath());
				MyTreeCellEditor.this.tree.fireTreeCollapsed(treePath);
			}
			if (tn.getChildAt(i).getChildCount() > 0) {
				checkChild((DefaultMutableTreeNode) tn.getChildAt(i));
			}
		}
	}

	/**
	 * update the status of the parents of a node (recursive) 
	 * 
	 * @param tn
	 */
	private void checkParent(DefaultMutableTreeNode tn) {
		int childCount = tn.getChildCount();

		TreeDataObject userObject = (TreeDataObject) tn.getUserObject();

		int nbCheck = 0;
		int nbCheckP = 0;

		for (int i = 0; i < childCount; i++) {
			TreeDataObject userObjectChild = (TreeDataObject) ((DefaultMutableTreeNode) tn
					.getChildAt(i)).getUserObject();
			switch (userObjectChild.getStatus()) {
			case TreeDataObject.FULLY_NOT_SELECTED:
				break;
			case TreeDataObject.FULLY_SELECTED:
				nbCheck++;
				break;
			case TreeDataObject.PARTIAL_SELECTED:
				nbCheckP++;
				break;
			}
		}

		if (nbCheck == childCount) {
			userObject.setStatus(TreeDataObject.FULLY_SELECTED);
		} else {
			if ((nbCheck == 0) && (nbCheckP == 0)) {
				userObject.setStatus(TreeDataObject.FULLY_NOT_SELECTED);
			} else {
				userObject.setStatus(TreeDataObject.PARTIAL_SELECTED);
			}
		}

		if (tn.getParent() != null) {
			checkParent((DefaultMutableTreeNode) tn.getParent());
		}
	}

	/**
	 * counts the number of leaf selected from a node
	 * 
	 * @param tn
	 */
	private Integer countSelectedLeaf(DefaultMutableTreeNode tn) {
		return new Integer(getSelectedLeafList(tn).size());
	}

	/**
	 * return list leaves selected from a node
	 * 
	 * @param node
	 * @return
	 */
	public List<Long> getSelectedLeafList(DefaultMutableTreeNode node) {
		List<Long> list = new ArrayList<Long>();

		for (int i = 0; i < node.getChildCount(); i++) {
			List<Long> tmp = getSelectedLeafList((DefaultMutableTreeNode) node
					.getChildAt(i));
			list.addAll(tmp);
		}

		if ((((TreeDataObject) node.getUserObject()).getStatus() == TreeDataObject.FULLY_SELECTED)
				&& (node.getChildCount() == 0)) {
			list.add(((TreeDataObject) node.getUserObject()).getId());
		}
		return list;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == myText) {
			labels.put(key, myText.getText());
			myText.setBackground(Color.gray);
			myText.setFocusable(false);
			tree.expandAll();
			//renderer.getCheckBox().setSelected(true);
			
			// this.stopCellEditing();
		} else {
			this.stopCellEditing();
		}
	}

	public HashMap<String, String> getLabels() {
		return labels;
	}

	public void setLabels(HashMap<String, String> labels) {
		this.labels = labels;
	}

	public void focusGained(FocusEvent e) {

	}
}
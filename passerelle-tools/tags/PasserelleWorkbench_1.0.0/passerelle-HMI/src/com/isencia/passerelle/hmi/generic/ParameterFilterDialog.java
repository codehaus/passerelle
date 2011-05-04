/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.hmi.ModelUtils;
import com.isencia.passerelle.hmi.definition.FieldMapping;
import com.isencia.passerelle.hmi.definition.HMIDefinition;
import com.isencia.passerelle.hmi.definition.Model;
import com.isencia.passerelle.hmi.form.CheckableComponent;
import com.isencia.passerelle.hmi.form.HMIComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The pop-up dialog to set which parameters should be shown in the HMI and to
 * define optional alias labels for them.
 * 
 * @author erwin dl
 */
@SuppressWarnings("serial")
public class ParameterFilterDialog extends JDialog {

	CompositeActor model;
	HMIDefinition modelParameterFilterConfig;
	List<Component> filters = new ArrayList<Component>();

	CheckTree checkTree;
	// DefaultMutableTreeNode root = new DefaultMutableTreeNode(new
	// TreeDataObject("Filter", true));
	HashMap<String, String> labels = new HashMap<String, String>();

	// DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
	// DefaultMutableTreeNode ChildNode = new DefaultMutableTreeNode();
	int index = 0;
	int paramCount = 0;

	public ParameterFilterDialog(final JFrame owner,
			final CompositeActor model,
			final HMIDefinition modelParameterFilterConfig) {
		super(owner, "Filter parameters", true);

		this.model = model;
		this.modelParameterFilterConfig = modelParameterFilterConfig;
		getContentPane().setLayout(new BorderLayout());

		if (model != null) {
			final Box titleBox = new Box(BoxLayout.Y_AXIS);
			titleBox.add(Box.createVerticalStrut(5));
			titleBox
					.add(new JLabel(
							"  Please indicate which parameters you want to show in the UI  "));
			titleBox.add(Box.createVerticalStrut(5));
			// getContentPane().add(titleBox, BorderLayout.NORTH);
			final FormLayout formLayout = new FormLayout(
					"min(100dlu;pref),5dlu,pref,2dlu", ""); // add rows
			// dynamically
			final DefaultFormBuilder builder = new DefaultFormBuilder(
					formLayout);
			builder.setComponentFactory(HMIComponentFactory.getInstance());
			builder.setDefaultDialogBorder();
			// final JScrollPane formScrollPane = new JScrollPane(builder
			// .getPanel());
			builder.appendTitle("Parameters");
			builder.appendTitle("Aliases for UI labels");

			final JButton selectAll = new JButton("Select All");
			selectAll.addActionListener(new ParameterFilterChecker(true));
			final JButton deSelectAll = new JButton("Deselect All");
			deSelectAll.addActionListener(new ParameterFilterChecker(false));
			builder.append(selectAll);
			builder.append(deSelectAll);

			// buttons
			final JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());
			final JButton okButton = new JButton("Ok");
			okButton.addActionListener(new ParameterFilterSaver());
			final JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					dispose();
				}
			});
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);

			// add
			final JPanel colapseButtonPanel = new JPanel();
			colapseButtonPanel.setLayout(new BorderLayout());
			final JButton expandAll = new JButton("Expand All");
			expandAll.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					checkTree.getTree().expandAll();
				}
			});
			final JButton colapseAll = new JButton("Colapse All");
			colapseAll.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					for (int i = 0; i < checkTree.getTree().getRowCount(); i++) {
						checkTree.getTree().collapseRow(i + 1);
					}
				}
			});
			final JPanel colapseButtonPanel1 = new JPanel();
			colapseButtonPanel1.add(expandAll);
			colapseButtonPanel1.add(colapseAll);

			colapseButtonPanel.add(titleBox, BorderLayout.NORTH);
			colapseButtonPanel.add(colapseButtonPanel1, BorderLayout.WEST);
			getContentPane().add(colapseButtonPanel, BorderLayout.NORTH);

			// Add the checkTree
			checkTree = new CheckTree("Model", true, true, Integer.MAX_VALUE,
					model, modelParameterFilterConfig);
			labels = checkTree.getLabels();
			getContentPane().add(checkTree, BorderLayout.CENTER);

			final int verticalSize = Math.min(200 + checkTree.getFilters()
					.size() * 50, 600);
			setSize(400, verticalSize);
		}
		setLocationRelativeTo(owner);
	}

	public HMIDefinition getModelParameterFilterConfig() {
		return modelParameterFilterConfig;
	}

	class ParameterFilterSaver implements ActionListener {

		public void actionPerformed(final ActionEvent e) {
			if (modelParameterFilterConfig == null) {
				modelParameterFilterConfig = new HMIDefinition();
			}
			Model modelConfig = modelParameterFilterConfig.getModel(model
					.getName());
			if (modelConfig == null) {
				modelParameterFilterConfig.addModel(model.getName(), new Model(
						null, new FieldMapping()));
				modelConfig = modelParameterFilterConfig.getModel(model
						.getName());
			}

			FieldMapping fieldConfig = modelConfig.getFieldMapping();
			if (fieldConfig == null) {
				modelConfig.setFieldMapping(new FieldMapping());
				fieldConfig = modelConfig.getFieldMapping();
			}

			// need to make sure we clear previous field settings
			fieldConfig.getFieldMappings().clear();

			final List<String> listSelected = checkTree
					.getSelectedLeafList(checkTree.getRoot());
			// TODO add checkTree
			filters = checkTree.getFilters();
			labels = checkTree.getLabels();
			// final List<String> listLabel = checkTree.getLabelList(checkTree
			// .getRoot());

			// TODO add checkTree
			for (final Object element : filters) {
				final CheckableComponent filter = (CheckableComponent) element;
				filter.setChecked(false);
			}

			for (final Object element : filters) {
				final CheckableComponent filter = (CheckableComponent) element;

				for (int i = 0; i < listSelected.size(); i++) {
					if (filter.getSubject() instanceof Parameter) {
						final Parameter p = (Parameter) filter.getSubject();

						final String filterValue = index + " " + p.getName();
						((JTextField) filter.getCheckedComponent())
								.setText(labels.get(filterValue));

						if (filterValue.equals(listSelected.get(i))) {
							filter.setChecked(true);
							break;
						}
					} else if (filter.getSubject() instanceof TextAttribute) {
						final TextAttribute p = (TextAttribute) filter
								.getSubject();

						final String filterValue = index + " " + p.getName();
						((JTextField) filter.getCheckedComponent())
								.setText(labels.get(filterValue));

						if (filterValue.equals(listSelected.get(i))) {
							filter.setChecked(true);
							break;
						}
					}
				}
				index++;
			}

			// Refrech Parameters list
			for (final Object element : filters) {
				final CheckableComponent filter = (CheckableComponent) element;
				if (filter.getSubject() instanceof Parameter) {
					final Parameter p = (Parameter) filter.getSubject();
					final boolean showIt = filter.isChecked();
					String alias = ((JTextField) filter.getCheckedComponent())
							.getText();
					if (alias == null || alias.trim().length() == 0) {
						alias = p.getName();
					} else {
						alias = alias.trim();
					}

					final String paramName = ModelUtils
							.getFullNameButWithoutModelName(model, p);

					if (showIt) {
						fieldConfig.addFieldMapping(paramName, alias);
					}
				} else if (filter.getSubject() instanceof TextAttribute) {
					final TextAttribute p = (TextAttribute) filter.getSubject();
					final boolean showIt = filter.isChecked();
					String alias = ((JTextField) filter.getCheckedComponent())
							.getText();
					if (alias == null || alias.trim().length() == 0) {
						alias = p.getName();
					} else {
						alias = alias.trim();
					}

					final String paramName = ModelUtils
							.getFullNameButWithoutModelName(model, p);
					if (showIt) {
						fieldConfig.addFieldMapping(paramName, alias);
					}
				}
			}
			dispose();
		}
	}

	class ParameterFilterChecker implements ActionListener {
		private boolean check = false;

		public ParameterFilterChecker(final boolean check) {
			super();
			this.check = check;
		}

		public void actionPerformed(final ActionEvent e) {
			// TODO add checkTree
			filters = checkTree.getFilters();
			for (final Object element : filters) {
				final CheckableComponent filter = (CheckableComponent) element;
				filter.setChecked(check);
			}
			ParameterFilterDialog.this.validate();
		}
	}
}

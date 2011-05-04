/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.specific;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.isencia.passerelle.actor.gui.binding.ParameterToWidgetBinder;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.action.SaveAction;
import com.isencia.passerelle.hmi.binding.ParameterToTextFieldBinder;
import com.isencia.passerelle.hmi.form.FormEntry;
import com.isencia.passerelle.hmi.form.FormField;
import com.isencia.passerelle.hmi.specific.binding.ParameterToTableColumnBinder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class HMITest extends HMIBase {
	private static final String HMI_MODELS_DEF_FILE = "hmi_def.xml";

	private static final String COMMAND_FIELD = "commandField";

	private static final String VALUE_FIELD = "constantValueField";

	private static final String LOOPCOUNT_FIELD = "loopCountField";

	private static final String DELAYTIME_FIELD = "delayTimeField";

	private final static Log logger = LogFactory.getLog(HMITest.class);

	JFrame frame;

	private MyTableModel myTableModel;

	private JTable table;

	private JScrollPane tableScrollPane;

	private MyTableModel myTableModelTemperature;

	private JTable temperatureTable;

	private JScrollPane temperatureTableScrollPane;

	private JButton addTemperatureButton;

	private JButton deleteTemperatureButton;

	private JButton addScanLineButton;

	private JButton deleteScanLineButton;

	private JButton goButton;

	private JButton loadScanButton;

	private JButton saveScanButton;

	private JButton saveScanAsButton;

	private JTextField rockingCurveTextField;

	private JTextField saveFileTextField;

	public HMITest() throws IOException {
		super(SPECIFIC, HMI_MODELS_DEF_FILE, false);
	}

	/**
     *
     */
	@Override
	protected Component initUI(final String title) {
		frame = new JFrame(title);
		frame.setLocation(600, 300);
		frame.setSize(500, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				logger.info("HMITest closing...");
			}
		});
		frame.setJMenuBar(createDefaultMenu());

		frame.show();

		return frame;
	}

	/**
	 * @param frame
	 */
	@Override
	protected void showModelForm(final String modelKey) {
		clearModelForms();
		if ("HelloWorld".equals(modelKey)) {
			frame.getContentPane().setLayout(new BorderLayout());
			final Box formBox = Box.createVerticalBox();
			final FormField valueField = new FormField();
			valueField.setEditable(true);
			formBox
					.add(new FormEntry(
							new JLabel(VALUE_FIELD),
							(FormField) registerBinding(
									VALUE_FIELD,
									valueField,
									(ParameterToWidgetBinder) new ParameterToTextFieldBinder())));
			frame.getContentPane().add(formBox, BorderLayout.NORTH);
		} else if ("LaunchNotepad".equals(modelKey)) {
			frame.getContentPane().setLayout(new BorderLayout());
			final Box formBox = Box.createVerticalBox();
			final FormField cmdField = new FormField();
			cmdField.setEditable(true);
			formBox
					.add(new FormEntry(
							new JLabel(COMMAND_FIELD),
							(FormField) registerBinding(
									COMMAND_FIELD,
									cmdField,
									(ParameterToWidgetBinder) new ParameterToTextFieldBinder())));
			frame.getContentPane().add(formBox, BorderLayout.NORTH);
		} else if ("test1_samba".equals(modelKey)) {
			frame.getContentPane().add(getEditSequencePanelRM4());
		} else if ("test-loop".equals(modelKey)) {
			frame.getContentPane().setLayout(new BorderLayout());
			final Box formBox = Box.createVerticalBox();
			final FormField valueField = new FormField();
			valueField.setEditable(true);
			formBox
					.add(new FormEntry(
							new JLabel(VALUE_FIELD),
							(FormField) registerBinding(
									VALUE_FIELD,
									valueField,
									(ParameterToWidgetBinder) new ParameterToTextFieldBinder())));

			final FormField delayField = new FormField();
			delayField.setEditable(true);
			formBox
					.add(new FormEntry(
							new JLabel(DELAYTIME_FIELD),
							(FormField) registerBinding(
									DELAYTIME_FIELD,
									delayField,
									(ParameterToWidgetBinder) new ParameterToTextFieldBinder())));

			final FormField loopCountField = new FormField();
			loopCountField.setEditable(true);
			formBox
					.add(new FormEntry(
							new JLabel(LOOPCOUNT_FIELD),
							(FormField) registerBinding(
									LOOPCOUNT_FIELD,
									loopCountField,
									(ParameterToWidgetBinder) new ParameterToTextFieldBinder())));

			frame.getContentPane().add(formBox, BorderLayout.NORTH);
		}
		frame.show();
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		logger.info("HMITest starting...");

		try {
			// UIManager.setLookAndFeel(Options
			// .getCrossPlatformLookAndFeelClassName());

			new HMITest().init();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void clearModelForms() {
		frame.getContentPane().removeAll();
	}

	public JPanel getEditSequencePanelRM4() {
		final JPanel editSequencePanelRM4 = new JPanel(new BorderLayout());

		final JPanel rockingCurvePanel = getRockingCurvePanel();
		// JPanel startOptionPanel = getStartOptionPanel();
		final JPanel tableScrollPanel = getTableScrollPane();
		final JPanel temperaturePanel = getTemperaturePanel();
		final JPanel buttonPanel = getButtonPanel();

		final JPanel topPanel = new JPanel(new GridLayout());
		topPanel.add("West", rockingCurvePanel);
		// topPanel.add("Center", startOptionPanel);

		final JPanel lowPanel = new JPanel(new GridLayout());
		lowPanel.add("West", temperaturePanel);
		lowPanel.add("Center", buttonPanel);

		editSequencePanelRM4.add("North", topPanel);
		editSequencePanelRM4.add("Center", tableScrollPanel);
		editSequencePanelRM4.add("South", lowPanel);

		final TitledBorder borderTitle = BorderFactory
				.createTitledBorder("Edit sequence with temperature and positioner");
		editSequencePanelRM4.setBorder(borderTitle);

		return editSequencePanelRM4;
	}

	private JPanel getButtonPanel() {
		addScanLineButton = new JButton("Add scan line");
		final AddRowCommand addRowCommand = new AddRowCommand(myTableModel);
		addScanLineButton.addActionListener(addRowCommand);
		//
		deleteScanLineButton = new JButton("Delete scan line");
		final DeleteRowCommand deleteRowCommand = new DeleteRowCommand(table,
				myTableModel);
		deleteScanLineButton.addActionListener(deleteRowCommand);
		//
		goButton = new JButton("GO");
		//
		loadScanButton = new JButton("Load sequence");
		// LoadSequenceCommand loadSequenceCommand = new
		// LoadSequenceCommand(contextData);
		// loadScanButton.addActionListener(loadSequenceCommand);
		//
		saveScanButton = new JButton("Save sequence");
		//
		// //SaveRockingCommand saveRockingCommand = new SaveRockingCommand();
		// saveScanButton.addActionListener(new ModelSaver());
		saveScanButton.setAction(new SaveAction(this));
		//
		saveScanAsButton = new JButton("Save sequence as");
		// SaveSequenceCommand saveSequenceCommand = new
		// SaveSequenceCommand(contextData);
		// saveScanAsButton.addActionListener(saveSequenceCommand);
		//
		//
		saveFileTextField = new JTextField(getCurrentModel().getName());
		//
		final FormLayout layout = new FormLayout("p, 3dlu, p, 3dlu, p", // cols
				"pref, 3dlu, p, 9dlu, p, 3dlu, p, 3dlu, p"); // rows

		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.
		layout.setColumnGroups(new int[][] { { 1, 3, 5 } });
		//
		// // Create a builder that assists in adding components to the
		// container.
		// // Wrap the panel with a standardized border.
		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();

		// Obtain a reusable constraints object to place components in the grid.
		final CellConstraints cc = new CellConstraints();

		// Fill the grid with components; the builder offers to create
		// frequently used components, e.g. separators and labels.

		// Add a titled separator to cell (1, 1) that spans 7 columns.

		builder.addSeparator("Commands", cc.xyw(1, 1, 5));
		builder.add(addScanLineButton, cc.xyw(1, 3, 1));
		builder.add(deleteScanLineButton, cc.xyw(3, 3, 1));
		builder.add(goButton, cc.xyw(5, 3, 1));

		builder.addSeparator("File", cc.xyw(1, 5, 5));
		builder.add(loadScanButton, cc.xyw(1, 7, 1));
		builder.add(saveScanButton, cc.xyw(3, 7, 1));
		builder.add(saveScanAsButton, cc.xyw(5, 7, 1));
		builder.add(saveFileTextField, cc.xyw(1, 9, 5));

		return builder.getPanel();
	}

	private JPanel getRockingCurvePanel() {
		// Initialization
		rockingCurveTextField = new JTextField("100");
		registerBinding("rockingCurveField", rockingCurveTextField,
				(ParameterToWidgetBinder) new ParameterToTextFieldBinder());

		// Create panel
		final FormLayout layout = new FormLayout("p, 3dlu, p", // cols
				"pref, 3dlu, p"); // rows

		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.
		layout.setColumnGroups(new int[][] { { 1, 3 } });

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();

		// Obtain a reusable constraints object to place components in the grid.
		final CellConstraints cc = new CellConstraints();

		builder.addSeparator("Rocking curve", cc.xyw(1, 1, 3));
		builder.addLabel("Rocking curve (%)", cc.xy(1, 3));
		builder.add(rockingCurveTextField, cc.xy(3, 3));

		return builder.getPanel();
	}

	private JPanel getTableScrollPane() {
		// Scan list table
		final String[] columnNames = { "Scan", "Position", "Sample name",
				"Scan name", "Number of scans", "Gain auto" };

		myTableModel = new MyTableModel(columnNames, 0);
		table = new JTable(myTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(600, 200));
		tableScrollPane = new JScrollPane(table);

		registerBinding("scanPositionColumn", myTableModel,
				(ParameterToWidgetBinder) new ParameterToTableColumnBinder(1));

		// Create panel
		final FormLayout layout = new FormLayout("p", // cols
				"pref, 3dlu, p"); // rows

		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.
		// layout.setColumnGroups(new int[][]{{1, 3}});

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();

		// Obtain a reusable constraints object to place components in the grid.
		final CellConstraints cc = new CellConstraints();

		builder.addSeparator("Scan sequences", cc.xyw(1, 1, 1));
		builder.add(tableScrollPane, cc.xyw(1, 3, 1));

		return builder.getPanel();
	}

	private JPanel getTemperaturePanel() {

		final String[] columnNames = { "Step", "Temperature" };

		myTableModelTemperature = new MyTableModel(columnNames, 0);
		temperatureTable = new JTable(myTableModelTemperature);
		temperatureTable.setPreferredScrollableViewportSize(new Dimension(200,
				200));

		temperatureTableScrollPane = new JScrollPane(temperatureTable);

		// Create buttons
		addTemperatureButton = new JButton("Add temperature");
		final AddRowCommand addRowCommand = new AddRowCommand(
				myTableModelTemperature);
		addTemperatureButton.addActionListener(addRowCommand);

		deleteTemperatureButton = new JButton("Delete temperature");
		final DeleteRowCommand deleteRowCommand = new DeleteRowCommand(
				temperatureTable, myTableModelTemperature);
		deleteTemperatureButton.addActionListener(deleteRowCommand);

		// Create panel
		final FormLayout layout = new FormLayout("p, 3dlu, p", // cols
				"pref, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 30dlu"); // rows

		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.
		// layout.setColumnGroups(new int[][]{{1, 3}});

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();

		// Obtain a reusable constraints object to place components in the grid.
		final CellConstraints cc = new CellConstraints();

		builder.addSeparator("Temperature settings", cc.xyw(1, 1, 3));
		builder.add(temperatureTableScrollPane, cc.xywh(1, 3, 1, 7));
		builder.add(addTemperatureButton, cc.xyw(3, 3, 1));
		builder.add(deleteTemperatureButton, cc.xyw(3, 5, 1));

		return builder.getPanel();
	}

	public static class MyTableModel extends DefaultTableModel {
		public MyTableModel(final String[] string, final int rowCount) {
			super(string, rowCount);
		}

		@Override
		public Class getColumnClass(final int c) {
			return getValueAt(0, c).getClass();
		}

		@Override
		public boolean isCellEditable(final int row, final int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col < 1) {
				return false;
			} else {
				return true;
			}
		}
	}

	private class AddRowCommand implements ActionListener {
		private final MyTableModel myTableModel;

		private final Object[] defaultEmptyObject = { "", "", "", "", "",
				new Boolean(false) };

		public AddRowCommand(final MyTableModel myTableModel) {
			this.myTableModel = myTableModel;
		}

		public void actionPerformed(final ActionEvent e) {
			myTableModel.addRow(defaultEmptyObject);
			myTableModel.setValueAt(new Integer(myTableModel.getRowCount()),
					myTableModel.getRowCount() - 1, 0);
		}
	}

	private class DeleteRowCommand implements ActionListener {
		private final MyTableModel myTableModel;

		private final JTable table;

		public DeleteRowCommand(final JTable table,
				final MyTableModel myTableModel) {
			this.myTableModel = myTableModel;
			this.table = table;
		}

		public void actionPerformed(final ActionEvent e) {
			try {
				myTableModel.removeRow(table.getSelectedRow());

				for (int i = 0; i < myTableModel.getRowCount(); i++) {
					myTableModel.setValueAt(new Integer(i + 1), i, 0);
				}

			} catch (final ArrayIndexOutOfBoundsException exception) {
				JOptionPane.showMessageDialog(tableScrollPane,
						"No line selected!!", "Information",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	@Override
	public void setSaved() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void clearModelGraphs() {
		// TODO Auto-generated method stub

	}

}

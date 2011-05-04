/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.VerticalLayout;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.actor.gui.IPasserelleComponent;
import com.isencia.passerelle.actor.gui.IPasserelleEditorPaneFactory;
import com.isencia.passerelle.actor.gui.IPasserelleQuery;
import com.isencia.passerelle.actor.gui.PasserelleEmptyQuery;
import com.isencia.passerelle.actor.gui.PasserelleQuery;
import com.isencia.passerelle.actor.gui.IPasserelleEditorPaneFactory.ParameterEditorAuthorizer;
import com.isencia.passerelle.actor.gui.PasserelleQuery.QueryLabelProvider;
import com.isencia.passerelle.actor.gui.binding.ParameterToWidgetBinder;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.ModelUtils;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.definition.HMIDefinition;
import com.isencia.passerelle.hmi.definition.Model;
import com.isencia.passerelle.hmi.definition.ModelBundle;
import com.isencia.passerelle.hmi.definition.HMIDefinition.LayoutPreferences;
import com.isencia.passerelle.hmi.graph.EditionActions;
import com.isencia.passerelle.hmi.graph.LookInsideViewFactory;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.hmi.trace.TraceDialog;
import com.isencia.passerelle.hmi.trace.TraceVisualizer;

/**
 * @todo Class Comment
 * 
 * @author erwin dl
 */
public class GenericHMI extends HMIBase implements ParameterEditorAuthorizer,
		QueryLabelProvider {
	private final static Log logger = LogFactory.getLog(GenericHMI.class);

	private static final String HMI_FILTERS_DEF_FILE = System
			.getProperty("FilterDefPath")
			+ System.getProperty("file.separator") + "filter_def.xml";

	private static final String HMI_MODELS_DEF_FILE = "hmi_def.xml";

	private IPasserelleEditorPaneFactory editorPaneFactory;

	private JMenuBar menuBar;

	private JScrollPane parameterScrollPane;
	private JScrollPane modelGraphScrollPane;
	private JPanel paremeterFormPanel;
	private TabWindow modelGraphTabWindow;

	private JPanel tracePanel;

	private JPanel configPanel;

	private JLabel modelNameLabel;

	private JFrame parentFrame;

	private HMIDefinition hmiDef;

	private int nrColumns = 1;
	private final Map<String, View> graphTabsMap = new HashMap<String, View>();

	private LookInsideViewFactory viewFactory;

	/**
	 * @param mode
	 * @throws IOException
	 */
	public GenericHMI(final boolean showModelGraph) throws IOException {
		super(GENERIC, HMI_MODELS_DEF_FILE, showModelGraph);
		hmiDef = HMIDefinition.parseHMIDefFile(HMI_FILTERS_DEF_FILE);

		initialize();
	}

	/**
	 * @param mode
	 * @throws IOException
	 */
	public GenericHMI(final ModelBundle bundle, final boolean showModelGraph)
			throws IOException {
		super(GENERIC, bundle, showModelGraph);
		hmiDef = HMIDefinition.parseHMIDefFile(HMI_FILTERS_DEF_FILE);
		initialize();
	}

	public void initialize() {
		parameterScrollPane = getParameterScrollPane();
		paremeterFormPanel = getParameterFormPanel();
		modelGraphTabWindow = getModelGraphPanel();
		modelGraphScrollPane = getModelGraphScrollPane();
		configPanel = getConfigPanel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.hmi.HMIBase#showModelForm(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void showModelForm(final String modelKey) {
		clearModelForms();

		if (this.getParentFrame() != null) {
			final URL modelURL = getModelURL();
			if (modelURL != null) {
				this.getParentFrame().setTitle("Passerelle - " + modelURL);
				modelNameLabel = new JLabel(modelURL.toString(), JLabel.CENTER);

				parameterScrollPane.getParent().add(modelNameLabel,
						BorderLayout.NORTH);
				parameterScrollPane.getParent().validate();
			}
		}

		if (getCurrentModel() != null) {

			final LayoutPreferences layoutPrefs = hmiDef
					.getLayoutPrefs(getCurrentModel().getName());

			if (layoutPrefs != null) {
				nrColumns = layoutPrefs.getNrColumns();
			}
			// sizeAndPlaceFrame();

			configPanel.setLayout(new GridLayout(1, nrColumns));
			final JPanel[] boxes = new JPanel[nrColumns];
			for (int i = 0; i < nrColumns; ++i) {
				boxes[i] = new JPanel(new VerticalLayout());
				configPanel.add(boxes[i]);
			}
			final List entities = getCurrentModel().entityList();
			if (entities != null) {
				final Vector<JPanel> actorPanels = new Vector<JPanel>();
				// render model
				if (layoutPrefs == null || layoutPrefs.getActorNames() == null
						|| layoutPrefs.getActorNames().size() == 0) {

					// render model for model parameters
					renderModelComponent(false, getCurrentModel(), boxes[0]);

					// render model for director
					if (getCurrentModel().getDirector() != null) {
						final JPanel b = new JPanel(new VerticalLayout());
						final boolean added = renderModelComponent(false,
								getCurrentModel().getDirector(), b);
						if (added) {
							actorPanels.add(b);
						}
					}

					// build the vector of panels with only visible actors
					for (int i = 0; i < entities.size(); ++i) {
						final NamedObj e = (NamedObj) entities.get(i);
						final JPanel b = new JPanel(new VerticalLayout());
						// JPanel b = boxes[(i+1) / nrEntriesPerColumn];
						// Box b = boxes[(i+1) / nrEntriesPerColumn];
						final boolean added = renderModelComponent(true, e, b);
						if (added) {
							actorPanels.add(b);
						}
					}
				} else {// an actor order has been defined
					final List<String> actorNames = layoutPrefs.getActorNames();
					boolean modelParamsRendered = false;
					for (final String actorName : actorNames) {
						final Entity e = getCurrentModel().getEntity(actorName);
						final Attribute param = getCurrentModel().getAttribute(
								actorName);
						if (getCurrentModel().getDirector().getName().equals(
								actorName)) {
							// render model for director
							if (getCurrentModel().getDirector() != null) {
								final JPanel b = new JPanel(
										new VerticalLayout());
								final boolean added = renderModelComponent(
										false, getCurrentModel().getDirector(),
										b);
								if (added) {
									actorPanels.add(b);
								}
							}
						} else if (e != null) {
							// render model actors
							final JPanel b = new JPanel(new VerticalLayout());
							final boolean added = renderModelComponent(true, e,
									b);
							if (added) {
								actorPanels.add(b);
							}
						} else if (param != null) {
							// render model for model parameters
							if (!modelParamsRendered) {
								// since all model' params are rendered at the
								// same time
								// and it is possible to have several params in
								// the layoutPrefs, make sure that is called
								// only once
								final JPanel b = new JPanel(
										new VerticalLayout());
								final boolean added = renderModelComponent(
										false, getCurrentModel(), b);
								if (added) {
									actorPanels.add(b);
								}
								modelParamsRendered = true;
							}
						} else {
							// it's an invalid layoutPrefs definition, e.g.
							// defined for another model
							// with same name or...
							layoutPrefs.getActorNames().clear();
							showModelForm(modelKey);
							break;
						}
					}
				}
				// display actors in fonction of layout prefs.
				final int nrEntriesPerColumn = actorPanels.size() / nrColumns;
				final int more = actorPanels.size() % nrColumns;
				final Iterator it = actorPanels.iterator();
				for (int i = 0; i < nrColumns; i++) {
					final JPanel column = boxes[i];
					if (i < more) {
						if (it.hasNext()) {
							final JPanel actor = (JPanel) it.next();
							column.add(actor);
						}
					}
					for (int j = 0; j < nrEntriesPerColumn; j++) {
						if (it.hasNext()) {
							final JPanel actor = (JPanel) it.next();
							column.add(actor);
						}
					}
				}
			}
			paremeterFormPanel.add("Center", configPanel);

			parameterScrollPane.validate();
		}
	}

	@Override
	public void loadModel(final URL _modelFile, final String modelKey)
			throws Exception {
		try {
			super.loadModel(_modelFile, modelKey);
		} catch (final Exception e) {
			clearModelForms();
			modelNameLabel = new JLabel(_modelFile
					+ " not found or is not a moml file", JLabel.CENTER);
			modelNameLabel.setForeground(Color.RED);
			parameterScrollPane.getParent().add(modelNameLabel,
					BorderLayout.NORTH);
			throw e;
		}
	}

	@Override
	public void changeExecuted(final ChangeRequest change) {
		if (change.getDescription() != null) {
			final String[] importantChanges = new String[] { "entity",
					"property", "deleteEntity", "deleteProperty", "class" };

			for (final String changeType : importantChanges) {
				if (change.getDescription().contains(changeType)) {
					graphTabsMap.get(getModelURL().toString())
							.getViewProperties().setTitle(
									getCurrentModel().getName() + " - ***");

					break;
				}
			}
		}
		super.changeExecuted(change);
	}

	@Override
	public void setSaved() {
		if (this.getModelURL() != null) {
			if (graphTabsMap.get(this.getModelURL().toString()) != null) {
				graphTabsMap.get(this.getModelURL().toString())
						.getViewProperties().setTitle(
								getCurrentModel().getName());
			}
		}
	};

	@Override
	protected void showModelGraph(final String modelKey) {
		// clearModelGraphs();
		try {
			if (graphPanelEffigy == null) {
				graphPanelEffigy = new PtolemyEffigy(getPtolemyConfiguration(),
						modelKey);
			}
			graphPanel = new ModelGraphPanel(getCurrentModel(),
					graphPanelEffigy);
			// TODO BEWARE : animation now happens in the actor
			// thread and blocks for a second!!
			graphPanel.setAnimationDelay(1000);

			final View mainGraphTab = new View(
					this.getCurrentModel().getName(),
					new ImageIcon(Toolkit.getDefaultToolkit().getImage(
							getClass().getResource("/com/isencia/passerelle/hmi/resources/ModelDataIcon.gif"))),
					graphPanel);
			graphTabsMap.put(this.getModelURL().toString(), mainGraphTab);
			mainGraphTab.getWindowProperties().setCloseEnabled(false);
			mainGraphTab.setName(this.getModelURL().toString());
			final TabWindow win = getModelGraphPanel();
			viewFactory = new LookInsideViewFactory(win, getPtolemyConfiguration(), graphPanelEffigy);
			graphPanel.registerViewFactory(viewFactory);

			win.addTab(mainGraphTab);
			getModelGraphScrollPane().validate();

			new EditionActions().addActions(graphPanel);
		} catch (final Throwable t) {
			logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
		}
	}

	@Override
	protected void clearModelGraphs() {
		if(viewFactory!=null)
			viewFactory.closeAllViews();
		for(View view : graphTabsMap.values()) {
			view.close();
		}
		graphTabsMap.clear();
	}

	/**
	 * @param nObj
	 * @param panel
	 * @return true if a form was effectively rendered, i.e. when at least 1
	 *         parameter was available
	 * @throws IllegalActionException
	 */
	private boolean renderModelComponent(final boolean deep,
			final NamedObj nObj, final JPanel panel) {
		if (logger.isDebugEnabled()) {
			logger
					.debug("renderModelComponent() - Entity " + nObj.getFullName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (nObj instanceof CompositeActor && deep) {
			return renderCompositeModelComponent((CompositeActor) nObj, panel);
		} else {
			renderModelComponentAnnotations(nObj, panel);
			final IPasserelleEditorPaneFactory epf = getEditorPaneFactoryForComponent(nObj);
			Component component = null;
			// XXX: temp need to use the new isencia api
			final IPasserelleQuery passerelleQuery = epf
					.createEditorPaneWithAuthorizer(nObj, this, this);
			if (!passerelleQuery.hasAutoSync()) {
				try {
					final Set<ParameterToWidgetBinder> queryBindings = passerelleQuery
							.getParameterBindings();
					for (final ParameterToWidgetBinder parameterToWidgetBinder : queryBindings) {
						hmiFields.put(parameterToWidgetBinder
								.getBoundParameter().getFullName(),
								parameterToWidgetBinder);
					}
				} catch (final Exception exception) {
					throw new RuntimeException(
							"Error creating bindings for passerelleQuery",
							exception);
				}
			}

			final IPasserelleComponent passerelleComponent = passerelleQuery
					.getPasserelleComponent();
			if (!(passerelleComponent instanceof Component)) {
				return false;
			}

			component = (Component) passerelleComponent;
			// System.out.println("renderModelComponent "+passerelleComponent);
			// Component c =
			// EditorPaneFactory.createEditorPaneWithAuthorizer(nObj, this,
			// this);
			if (component != null
					&& !(component instanceof PasserelleEmptyQuery)) {
				final String name = ModelUtils.getFullNameButWithoutModelName(
						getCurrentModel(), nObj);
				component.setName(name);
				final JPanel globalPanel = new JPanel(new BorderLayout());

				// Panel for title
				final JPanel titlePanel = createTitlePanel(name);

				// Add a nice background to panels
				titlePanel.setBackground(panel.getBackground());
				((JComponent) component).setBackground(panel.getBackground());
				// Border
				final Border loweredbevel = BorderFactory
						.createLoweredBevelBorder();
				final TitledBorder border = BorderFactory
						.createTitledBorder(loweredbevel/* ,name */);
				globalPanel.setBorder(border);

				globalPanel.add(titlePanel, BorderLayout.NORTH);
				globalPanel.add(component, BorderLayout.CENTER);
				panel.add(globalPanel);

				// StateMachine stuff
				StateMachine.getInstance().registerActionForState(
						StateMachine.MODEL_OPEN, name, component);
				StateMachine.getInstance().compile();
				return true;
			}
			return false;
		}
	}

	private JPanel createTitlePanel(final String name) {
		final JPanel result = new JPanel(new BorderLayout());
		final ImageIcon icon = new ImageIcon(
				Toolkit
						.getDefaultToolkit()
						.getImage(
								getClass()
										.getResource(
												"/com/isencia/passerelle/hmi/resources/param.gif")));
		final JLabel startLabel = new JLabel(icon);
		result.add(startLabel, BorderLayout.LINE_START);

		final JLabel nameLabel = new JLabel(name);
		final Font f = nameLabel.getFont();
		nameLabel.setFont(new Font(f.getName(), f.getStyle(), f.getSize() + 2));
		nameLabel.setForeground(new Color(49, 106, 196));
		result.add(nameLabel);

		return result;
	}

	private IPasserelleEditorPaneFactory getEditorPaneFactoryForComponent(
			final NamedObj e) {
		IPasserelleEditorPaneFactory epf = null;
		try {
			epf = (IPasserelleEditorPaneFactory) e.getAttribute(
					"_editorPaneFactory", IPasserelleEditorPaneFactory.class);
		} catch (final IllegalActionException e1) {
		}

		epf = epf != null ? epf : editorPaneFactory;
		return epf;
	}

	/**
	 * @param e
	 * @param b
	 * @return true if a form was effectively rendered, i.e. when at least 1
	 *         parameter was available
	 */
	@SuppressWarnings("unchecked")
	private boolean renderCompositeModelComponent(final CompositeActor e,
			final JPanel b) {
		if (logger.isDebugEnabled()) {
			logger
					.debug("renderCompositeModelComponent() - Entity " + e.getFullName()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final IPasserelleEditorPaneFactory epf = getEditorPaneFactoryForComponent(e);
		final IPasserelleQuery pQuery = epf.createEditorPaneWithAuthorizer(e,
				this, this);
		final JComponent c = (JComponent) pQuery.getPasserelleComponent();

		if (pQuery != null) {
			boolean nonEmptyForm = !(pQuery instanceof PasserelleEmptyQuery);
			// remove the default message in an empty query
			if (!nonEmptyForm) {
				((PasserelleEmptyQuery) pQuery).setText("");
			} else if (!pQuery.hasAutoSync()) {
				try {
					final Set<ParameterToWidgetBinder> queryBindings = pQuery
							.getParameterBindings();
					for (final ParameterToWidgetBinder parameterToWidgetBinder : queryBindings) {
						hmiFields.put(parameterToWidgetBinder
								.getBoundParameter().getFullName(),
								parameterToWidgetBinder);
					}
				} catch (final Exception exception) {
					throw new RuntimeException(
							"Error creating bindings for passerelleQuery",
							exception);
				}
			}

			final String name = ModelUtils.getFullNameButWithoutModelName(
					getCurrentModel(), e);
			final List subActors = e.entityList();
			// TODO: && changed by || because was not displaying parameters for
			// composite
			// still a problem of display with multiactor
			if (pQuery instanceof PasserelleQuery || !subActors.isEmpty()) {
				final JPanel compositeBox = createCompositePanel(b, c, name);
				renderModelComponentAnnotations(e, compositeBox);
				for (final Iterator<NamedObj> subActorItr = subActors
						.iterator(); subActorItr.hasNext();) {
					final NamedObj subActor = subActorItr.next();
					nonEmptyForm |= renderModelComponent(true, subActor,
							compositeBox);
				}
				if (nonEmptyForm) {
					b.add(compositeBox);
					StateMachine.getInstance().registerActionForState(
							StateMachine.MODEL_OPEN, name, c);
					StateMachine.getInstance().compile();
				}
				return nonEmptyForm;
			} else if (!(pQuery instanceof PasserelleEmptyQuery)) {
				// System.out.println("else name" + name);
				c.setName(name);
				c.setBorder(BorderFactory.createTitledBorder(name));
				b.add(c);
				StateMachine.getInstance().registerActionForState(
						StateMachine.MODEL_OPEN, name, c);
				StateMachine.getInstance().compile();
				return true;
			}
		}
		// System.out.println("renderComposite - out");
		return false;
	}

	private JPanel createCompositePanel(final JPanel b, final JComponent c,
			final String name) {
		final JPanel compositeBox = new JPanel(new VerticalLayout(5));
		int r = b.getBackground().getRed() - 20;
		if (r < 1) {
			r = 0;
		}
		if (r > 254) {
			r = 255;
		}
		int g = b.getBackground().getGreen() - 20;
		if (g < 1) {
			g = 0;
		}
		if (g > 254) {
			g = 255;
		}
		int bl = b.getBackground().getBlue() - 20;
		if (bl < 1) {
			bl = 0;
		}
		if (bl > 254) {
			bl = 255;
		}

		compositeBox.setBackground(new Color(r, g, bl));
		final JPanel title = new JPanel(new BorderLayout());
		title.setBackground(new Color(r, g, bl));
		/*
		 * ImageIcon icon = new ImageIcon( Toolkit .getDefaultToolkit()
		 * .getImage( (getClass()
		 * .getResource("/com/isencia/passerelle/hmi/resources/composite.gif"
		 * )))); JLabel lab = new JLabel(icon);
		 * 
		 * title.add(lab, BorderLayout.LINE_START);
		 */
		final JLabel lab2 = new JLabel(name);
		final Font f = lab2.getFont();
		lab2.setFont(new Font(f.getName(), f.getStyle(), f.getSize() + 6));
		lab2.setForeground(new Color(49, 106, 196));
		title.add(lab2);
		compositeBox.add(title);
		final Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		final TitledBorder border = BorderFactory
				.createTitledBorder(loweredbevel);

		compositeBox.setBorder(border);
		compositeBox.setName(name);
		c.setBackground(new Color(r, g, bl));
		compositeBox.add(c);
		return compositeBox;
	}

	@SuppressWarnings("unchecked")
	private void renderModelComponentAnnotations(final NamedObj e,
			final JPanel b) {
		final List<TextAttribute> annotations = e
				.attributeList(TextAttribute.class);
		if (!annotations.isEmpty()) {
			final Box annotationsBox = new Box(BoxLayout.Y_AXIS);
			// annotationsBox.setBorder(BorderFactory.createTitledBorder("Info"));
			for (final TextAttribute textAttribute : annotations) {

				if (isAnnotionAuthorizedForEditor(textAttribute)) {
					final Box subBox = new Box(BoxLayout.Y_AXIS);

					// subBox.setBorder(BorderFactory.createTitledBorder(textAttribute.getName()));
					final String[] annotationLines = textAttribute.text
							.getExpression().split("\n");

					for (final String annotationLine : annotationLines) {
						final JLabel lab = new JLabel(annotationLine);
						textAttribute.fontFamily.getExpression();
						int bold = 0;
						if (textAttribute.bold.getExpression()
								.compareTo("true") == 0) {
							bold = Font.BOLD;
						}
						int italic = 0;
						if (textAttribute.italic.getExpression().compareTo(
								"true") == 0) {
							italic = Font.ITALIC;
						}
						final int textSize = Integer
								.valueOf(textAttribute.textSize.getExpression());
						final Font font = new Font(textAttribute.fontFamily
								.getExpression(), bold | italic, textSize);
						lab.setFont(font);
						final String colorString = textAttribute.textColor
								.getExpression();
						final String sub = colorString.substring(1, colorString
								.lastIndexOf("}"));
						final String[] rgba = sub.split(",");
						final float r = Float.valueOf(rgba[0]);
						final float g = Float.valueOf(rgba[1]);
						final float bl = Float.valueOf(rgba[2]);
						final float a = Float.valueOf(rgba[3]);
						final Color color = new Color(r, g, bl, a);
						lab.setForeground(color);
						subBox.add(lab);
					}
					annotationsBox.add(subBox);
				}
			}
			b.add(annotationsBox);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.hmi.HMIBase#clearModelForms()
	 */
	@Override
	protected void clearModelForms() {
		configPanel.removeAll();
		configPanel.repaint();

		if (this.getParentFrame() != null) {
			this.getParentFrame().setTitle("Passerelle");
		}
		if (modelNameLabel != null) {
			parameterScrollPane.getParent().remove(modelNameLabel);
		}

	}

	private class ParameterFilterOpener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			if (getCurrentModel() != null) {
				final ParameterFilterDialog dialog = new ParameterFilterDialog(
						getParentFrame(), getCurrentModel(), hmiDef);
				dialog.setVisible(true);
				final HMIDefinition filterSettingsFromDialog = dialog
						.getModelParameterFilterConfig();
				if (filterSettingsFromDialog != null) {
					hmiDef = filterSettingsFromDialog;
					saveAndApplySettings();
				}
			} else {
				JOptionPane.showMessageDialog(getParentFrame(),
						"Please select/open a model first", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private class ActorOrderOpener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			if (getCurrentModel() != null) {
				final ActorOrderDialog dialog = new ActorOrderDialog(
						getParentFrame(), getCurrentModel(), hmiDef);
				dialog.setVisible(true);
				final HMIDefinition filterSettingsFromDialog = dialog
						.getModelParameterFilterConfig();
				if (filterSettingsFromDialog != null) {
					hmiDef = filterSettingsFromDialog;
					saveAndApplySettings();
				}
			} else {
				JOptionPane.showMessageDialog(getParentFrame(),
						"Please select/open a model first", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private class ColumnCountDialogOpener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final Integer chosenNrColumns = (Integer) JOptionPane
					.showInputDialog(getParentFrame(),
							"Please enter nr of desired columns", "Layout",
							JOptionPane.QUESTION_MESSAGE, null, new Object[] {
									new Integer(1), new Integer(2),
									new Integer(3) }, new Integer(nrColumns));
			if (chosenNrColumns != null) {
				nrColumns = chosenNrColumns.intValue();
			}
			LayoutPreferences layoutPrefs = hmiDef
					.getLayoutPrefs(getCurrentModel().getFullName());
			if (layoutPrefs == null) {
				layoutPrefs = new LayoutPreferences(nrColumns);
				hmiDef.addModelLayout(getCurrentModel().getFullName(),
						layoutPrefs);
			} else {
				layoutPrefs.setNrColumns(nrColumns);
			}
			saveAndApplySettings();
		}
	}

	private void saveAndApplySettings() {
		final String def = ModelBundle.generateDef(hmiDef);
		Writer defWriter = null;
		try {
			defWriter = new FileWriter(HMI_FILTERS_DEF_FILE);
			defWriter.write(def);
		} catch (final IOException e1) {
			e1.printStackTrace();
			PopupUtil.showError(hookComponent, "impossible to save filter", e1
					.getMessage());
		} finally {
			if (defWriter != null) {
				try {
					defWriter.close();
				} catch (final Exception e1) {
				}
			}
		}
		// refresh current form
		showModelForm(null);
	}

	public boolean isAnnotionAuthorizedForEditor(final TextAttribute p) {
		// CHeck if parameter is present in the filter configuration
		if (hmiDef == null || getCurrentModel() == null
				|| hmiDef.getModel(getCurrentModel().getFullName()) == null) {
			// we've got no usefull filter info
			return true;
		}
		final Model filterDefForCurrentModel = hmiDef
				.getModel(getCurrentModel().getFullName());
		final String alias = filterDefForCurrentModel.getFieldMapping()
				.getValueForKey(
						ModelUtils.getFullNameButWithoutModelName(
								getCurrentModel(), p));
		return alias != null;
	}

	public boolean isAuthorizedForEditor(final Settable p) {
		// CHeck if parameter is present in the filter configuration
		if (hmiDef == null || getCurrentModel() == null
				|| hmiDef.getModel(getCurrentModel().getName()) == null) {
			// we've got no usefull filter info
			return true;
		}
		final Model filterDefForCurrentModel = hmiDef
				.getModel(getCurrentModel().getName());
		final String alias = filterDefForCurrentModel.getFieldMapping()
				.getValueForKey(
						ModelUtils.getFullNameButWithoutModelName(
								getCurrentModel(), p));
		return alias != null;
	}

	public String getLabelFor(final Settable settable) {
		// CHeck if parameter is present in the filter configuration
		if (hmiDef == null || getCurrentModel() == null
				|| hmiDef.getModel(getCurrentModel().getName()) == null) {
			// we've got no usefull filter info
			return settable.getName();
		}
		final Model filterDefForCurrentModel = hmiDef
				.getModel(getCurrentModel().getName());
		final String alias = filterDefForCurrentModel.getFieldMapping()
				.getValueForKey(
						ModelUtils.getFullNameButWithoutModelName(
								getCurrentModel(), settable));
		return alias != null ? alias : settable.getFullName();
	}

	public HMIDefinition getHmiDef() {
		return hmiDef;
	}

	public void addPrefsMenu(final JMenuBar menuBar) {
		final JMenu prefsMenu = new JMenu(HMIMessages
				.getString(HMIMessages.MENU_PREFS));
		prefsMenu.setMnemonic(HMIMessages.getString(
				HMIMessages.MENU_PREFS + HMIMessages.KEY).charAt(0));
		final JMenuItem layoutMenuItem = new JMenuItem(HMIMessages
				.getString(HMIMessages.MENU_LAYOUT), HMIMessages.getString(
				HMIMessages.MENU_LAYOUT + HMIMessages.KEY).charAt(0));
		layoutMenuItem.addActionListener(new ColumnCountDialogOpener());
		prefsMenu.add(layoutMenuItem);
		final JMenuItem actorOrderMenuItem = new JMenuItem(HMIMessages
				.getString(HMIMessages.MENU_ACTOR_ORDER), HMIMessages
				.getString(HMIMessages.MENU_ACTOR_ORDER + HMIMessages.KEY)
				.charAt(0));
		actorOrderMenuItem.addActionListener(new ActorOrderOpener());
		prefsMenu.add(actorOrderMenuItem);
		final JMenuItem paramFilterMenuItem = new JMenuItem(HMIMessages
				.getString(HMIMessages.MENU_PARAM_VISIBILITY), HMIMessages
				.getString(HMIMessages.MENU_PARAM_VISIBILITY + HMIMessages.KEY)
				.charAt(0));
		paramFilterMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_P, InputEvent.CTRL_MASK));
		paramFilterMenuItem.addActionListener(new ParameterFilterOpener());
		prefsMenu.add(paramFilterMenuItem);
		menuBar.add(prefsMenu);

		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_PREFS, prefsMenu);
	}

	public JMenuBar getMenuBar() {
		if (menuBar == null) {
			final Set<String> hideItemsSet = new HashSet<String>();
			// hideItemsSet.add(HMIMessages.MENU_TEMPLATES);
			hideItemsSet.add(HMIMessages.MENU_TRACING);
			menuBar = createDefaultMenu(null, hideItemsSet);

			addPrefsMenu(menuBar);
			StateMachine.getInstance().compile();
			StateMachine.getInstance().transitionTo(StateMachine.READY);
		}
		return menuBar;
	}

	public JScrollPane getParameterScrollPane() {
		if (parameterScrollPane == null) {
			parameterScrollPane = new JScrollPane(getParameterFormPanel());
		}
		return parameterScrollPane;
	}

	public JPanel getParameterFormPanel() {
		if (paremeterFormPanel == null) {
			paremeterFormPanel = new JPanel(new BorderLayout());
		}
		return paremeterFormPanel;
	}

	public JScrollPane getModelGraphScrollPane() {
		if (modelGraphScrollPane == null) {
			modelGraphScrollPane = new JScrollPane(getModelGraphPanel());
		}
		return modelGraphScrollPane;
	}

	public JPanel getConfigPanel() {
		if (configPanel == null) {
			configPanel = new JPanel(/* new GridLayout(1, nrColumns) */);
		}
		return configPanel;
	}

	public TabWindow getModelGraphPanel() {
		if (modelGraphTabWindow == null) {
			modelGraphTabWindow = new TabWindow();
			modelGraphTabWindow.addListener(new DockingWindowListener() {

				
				public void viewFocusChanged(final View previouslyFocusedView,
						final View focusedView) {
					// TODO Auto-generated method stub

				}

				
				public void windowAdded(final DockingWindow addedToWindow,
						final DockingWindow addedWindow) {
					// TODO Auto-generated method stub

				}

				
				public void windowClosed(final DockingWindow window) {
					// refresh the parameters panel upon the selected model
					// graph
					updateParamsPanel(GenericHMI.this.modelGraphTabWindow
							.getSelectedWindow());
				}

				private void updateParamsPanel(final DockingWindow window) {
					if (window != null) {
						final String modelURL = window.getName();
						if (modelURL != null
								&& !modelURL.equals(GenericHMI.this
										.getModelURL())) {
							try {
								GenericHMI.this.refreshParamsForm(new URL(
										modelURL), null);
							} catch (final MalformedURLException e) {
							} catch (final Exception e) {
							}
						}
					}
				}

				
				public void windowClosing(final DockingWindow window)
						throws OperationAbortedException {
					// TODO Auto-generated method stub

				}

				
				public void windowDocked(final DockingWindow window) {
					// TODO Auto-generated method stub

				}

				
				public void windowDocking(final DockingWindow window)
						throws OperationAbortedException {
					// TODO Auto-generated method stub

				}

				
				public void windowHidden(final DockingWindow window) {
					// TODO Auto-generated method stub

				}

				
				public void windowMaximized(final DockingWindow window) {
					// TODO Auto-generated method stub

				}

				
				public void windowMaximizing(final DockingWindow window)
						throws OperationAbortedException {
					// TODO Auto-generated method stub

				}

				
				public void windowMinimized(final DockingWindow window) {
					// TODO Auto-generated method stub

				}

				
				public void windowMinimizing(final DockingWindow window)
						throws OperationAbortedException {
					// TODO Auto-generated method stub

				}

				
				public void windowRemoved(
						final DockingWindow removedFromWindow,
						final DockingWindow removedWindow) {
					// TODO Auto-generated method stub

				}

				
				public void windowRestored(final DockingWindow window) {
					// TODO Auto-generated method stub

				}

				
				public void windowRestoring(final DockingWindow window)
						throws OperationAbortedException {
					// TODO Auto-generated method stub

				}

				
				public void windowShown(final DockingWindow window) {
					// refresh the parameters panel upon the selected model
					// graph
					updateParamsPanel(window);
				}

				
				public void windowUndocked(final DockingWindow window) {
					// TODO Auto-generated method stub

				}

				
				public void windowUndocking(final DockingWindow window)
						throws OperationAbortedException {
					// TODO Auto-generated method stub

				}

			});
			// modelGraphPanel.getTabWindowProperties().getTabbedPanelProperties().setTabAreaOrientation(Direction.DOWN);
			// modelGraphPanel.getTabWindowProperties().getCloseButtonProperties()
			// .setVisible(true);
			// modelGraphPanel.getWindowProperties().setCloseEnabled(true);
		}
		return modelGraphTabWindow;
	}

	public JPanel getTracePanel() {
		if (tracePanel == null) {
			final TraceVisualizer traceComponent = this.getTraceComponent();
			final TraceDialog traceDialog = (TraceDialog) traceComponent;

			tracePanel = new JPanel(new BorderLayout());
			tracePanel.add("Center", traceDialog.getContentPane());
		}
		return tracePanel;
	}

	public JFrame getParentFrame() {

		// Container parentComponent = UIScrollPane.getParent();
		//
		// if (parentComponent != null) {
		// while (!(parentComponent instanceof JFrame)) {
		// parentComponent = parentComponent.getParent();
		// }
		//
		// parentFrame = (JFrame) parentComponent;
		// }
		parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class,
				parameterScrollPane);
		return parentFrame;
	}

	@Override
	protected Component initUI(final String title) {
		return parameterScrollPane;
	}

	public IPasserelleEditorPaneFactory getEditorPaneFactory() {
		return editorPaneFactory;
	}

	public void setEditorPaneFactory(
			final IPasserelleEditorPaneFactory editorPaneFactory) {
		this.editorPaneFactory = editorPaneFactory;
	}
}

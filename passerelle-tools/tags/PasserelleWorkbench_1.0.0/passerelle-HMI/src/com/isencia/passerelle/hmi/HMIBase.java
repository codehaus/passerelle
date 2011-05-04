/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.constants.IPropertyNames;
import com.isencia.passerelle.actor.gui.binding.ParameterToWidgetBinder;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.ext.ExecutionTracer;
import com.isencia.passerelle.ext.FiringEventListener;
import com.isencia.passerelle.ext.impl.DefaultActorErrorControlStrategy;
import com.isencia.passerelle.hmi.action.AskTheUserErrorControlStrategy;
import com.isencia.passerelle.hmi.action.ModelCreator;
import com.isencia.passerelle.hmi.action.ModelDebugStepper;
import com.isencia.passerelle.hmi.action.ModelDebugger;
import com.isencia.passerelle.hmi.action.ModelExecutor;
import com.isencia.passerelle.hmi.action.ModelResumer;
import com.isencia.passerelle.hmi.action.ModelStopper;
import com.isencia.passerelle.hmi.action.ModelSuspender;
import com.isencia.passerelle.hmi.action.SaveAction;
import com.isencia.passerelle.hmi.action.SaveAsAction;
import com.isencia.passerelle.hmi.binding.ParameterChangeListener;
import com.isencia.passerelle.hmi.definition.Model;
import com.isencia.passerelle.hmi.definition.ModelBundle;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.hmi.trace.HMIExecutionTracer;
import com.isencia.passerelle.hmi.trace.TraceDialog;
import com.isencia.passerelle.hmi.trace.TraceVisualizer;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.util.swing.components.FinderAccessory;

import diva.gui.ExtensionFileFilter;

public abstract class HMIBase implements ChangeListener {
	private final static Log logger = LogFactory.getLog(HMIBase.class);

	public static final String EXECUTION_CONTROL_ATTR_NAME = "_executionControl";

	public static final String USER_TRACER_ATTR_NAME = "_userTracer";

	public static class Mode {
		private Mode() {

		}
	}

	// accept all models and try to automatically build a UI
	public final static Mode GENERIC = new Mode();

	// validate models if they fit within the list
	// of supported model types
	// and show a custom UI for them
	public final static Mode SPECIFIC = new Mode();

	// this is the hmi config info parsed from some cfg file
	// current implementation uses XMLStreamer files
	private ModelBundle hmiModelsDef;

	// this is constructed when a model is selected
	private CompositeActor currentModel;

	private Model currentModelDef;

	protected Map<String, ParameterToWidgetBinder> hmiFields = new HashMap<String, ParameterToWidgetBinder>();

	// this represents the path to be used to open/save the model
	private URL modelURL;

	// this contains the instance of the Passerelle/Ptolemy execution manager
	// while a model is running
	private Manager manager;

	protected Component hookComponent;

	private TraceVisualizer traceComponent;

	private ExecutionTracer executionTracer;

	private final Mode mode;

	// some actions that are shared between menu items and toolbar buttons
	private ModelExecutor modelExecutor;

	private ModelDebugger modelDebugger;

	private ModelSuspender modelSuspender;

	private ModelResumer modelResumer;

	private ModelStopper modelStopper;

	private ModelDebugStepper modelDebugStepper;

	private SaveAction saveAction;
	private SaveAsAction saveAsAction;

	public SaveAction getSaveAction() {
		return saveAction;
	}

	public SaveAsAction getSaveAsAction() {
		return saveAsAction;
	}

	private Action modelCreatorAction;

	private boolean animateModelExecution = false;

	private boolean interactiveErrorControl = false;

	// private EditorGraphController animationController;

	private AnimationEventListener animationEventListener;

	protected ModelGraphPanel graphPanel;

	private Configuration config;
	private final boolean showModelGraph;
	protected PtolemyEffigy graphPanelEffigy;

	public static final String MODELS_URL_STRING = System.getProperty("generic.hmi.modelURL");

	public HMIBase(final Mode mode, final ModelBundle hmiModelsDef,
			final boolean showModelGraph) throws IOException {
		this.mode = mode;
		this.hmiModelsDef = hmiModelsDef;
		this.showModelGraph = showModelGraph;
	}

	public HMIBase(final Mode mode, final String cfgDefPath,
			final boolean showModelGraph) throws IOException {
		this.mode = mode;
		hmiModelsDef = ModelBundle.parseModelBundleDefFile(cfgDefPath);
		this.showModelGraph = showModelGraph;
	}

	protected Configuration getPtolemyConfiguration() {
		return config;
	}

	public void init() {
		// register exit hook to try to enforce a nice shutdown
		// even with Ctrl-C etc
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				if (StateMachine.MODEL_EXECUTING.equals(StateMachine
						.getInstance().getCurrentState())) {
					if (getCurrentModel() != null
							&& getCurrentModel().getManager() != null) {
						try {
							getCurrentModel().getManager().stop();
						} catch (final Throwable t) {
							t.printStackTrace();
							logger.error("Error stopping model "
									+ getCurrentModel().getFullName()
									+ " during application shutdown", t);
						}
					}
				}
			}
		}));
		String configurationFile = null;
		try {
			configurationFile = System.getProperty(IPropertyNames.APP_HOME,
					IPropertyNames.APP_HOME_DEFAULT)
					+ java.io.File.separatorChar
					+ System.getProperty(IPropertyNames.APP_CFG,
							IPropertyNames.APP_CFG_DEFAULT)
					+ java.io.File.separatorChar + "passerelle-actor.xml";
			System.out.println("readconfiguration file " + configurationFile);
			logger.debug("readconfiguration file " + configurationFile);
			config = readConfiguration(configurationFile);

		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("readconfiguration failed " + e.toString());
		}

		traceComponent = createTraceComponent();
		hookComponent = initUI(HMIMessages.getString(HMIMessages.TITLE));
		if (executionTracer == null) {
			executionTracer = new HMIExecutionTracer(traceComponent);
		}
		ExecutionTracerService.registerTracer(executionTracer);
	}

	/**
	 * Method that can be overridden if execution trace messages need to be
	 * displayed in a custom component.
	 * 
	 * Default implementation uses a TraceDialog, that wraps a TracePanel. The
	 * component should implement the TraceVisualizer interface.
	 * 
	 * @return the chosen TraceVisualizer UI implementation
	 */
	protected TraceVisualizer createTraceComponent() {
		final TraceDialog traceDialog = new TraceDialog((JFrame) hookComponent);
		traceDialog.setVisible(false);

		return traceDialog;
	}

	public CompositeActor getCurrentModel() {
		return currentModel;
	}

	public Model getCurrentModelDef() {
		return currentModelDef;
	}

	public URL getModelURL() {
		return modelURL;
	}

	public void setModelURL(final URL modelURL) {
		this.modelURL = modelURL;
	}

	/**
	 * Must be implemented to show the general frame of the HMI, without actual
	 * forms/fields for specific models in it yet.
	 * 
	 * @param title
	 * @return some component we can use for locating pop-ups etc
	 */
	protected abstract Component initUI(String title);

	/**
	 * Must fill the form/field contents of the HMI corresponding to what's
	 * needed to configure models corresponding to the given key.
	 * 
	 * @param modelKey
	 */
	protected abstract void showModelForm(String modelKey);

	protected abstract void clearModelForms();

	private final Map<URL, CompositeActor> loadedModels = new HashMap<URL, CompositeActor>();

	/**
	 * @param _modelURL
	 * @throws Exception
	 */
	public void loadModel(final URL _modelURL, String modelKey)
			throws Exception {
		final CompositeActor currentModelTmp = ModelUtils.loadModel(_modelURL);
		loadedModels.put(_modelURL, currentModelTmp);
		modelKey = validateModel(currentModelTmp, modelKey);
		modelURL = _modelURL;
		currentModelDef = hmiModelsDef.getModel(modelKey);
		setCurrentModel(currentModelTmp, modelKey, showModelGraph);
	}

	protected void refreshParamsForm(final URL _modelURL, String modelKey)
			throws Exception {
		if (loadedModels.containsKey(_modelURL)) {
			final CompositeActor currentModelTmp = loadedModels.get(_modelURL);
			modelKey = validateModel(currentModelTmp, modelKey);
			modelURL = _modelURL;
			currentModelDef = hmiModelsDef.getModel(modelKey);
			setCurrentModel(currentModelTmp, modelKey, false);
		}

	}

	protected void setCurrentModel(final CompositeActor model,
			final String modelKey, final boolean loadGraphPanel) {
		if (currentModel != null) {
			currentModel.removeChangeListener(this);
		}
		currentModel = model;
		hmiFields.clear();
		showModelForm(modelKey);
		if (loadGraphPanel) {
			showModelGraph(modelKey);
		}

		currentModel.addChangeListener(this);
	}

	/**
	 * Checks if the given model is compatible with this HMI, based on the HMI
	 * configuration definition.
	 * 
	 * @param currentModelTmp
	 * @return
	 */
	protected String validateModel(final CompositeActor model,
			final String modelKey) {
		if (SPECIFIC.equals(mode)) {
			String result = null;
			// now let's check if we support it
			// the model name is used as model type key
			final String _modelkey = model.getName();
			if (modelKey != null && !modelKey.equals(_modelkey)) {
				throw new IllegalArgumentException("Invalid model key in "
						+ model.getName() + ", expecting " + modelKey);
			} else {
				// check if the _modelkey from the model is support by this HMI
				if (hmiModelsDef.getModel(_modelkey) != null) {
					// finally, it's a supported model type
					result = _modelkey;
				}
			}
			if (result == null) {
				throw new IllegalArgumentException(
						"Invalid model for this HMI " + model.getName());
			}

			return result;
		} else {
			return modelKey;
		}
	}

	protected JMenuBar createDefaultMenu() {
		return createDefaultMenu(null, null);
	}

	private boolean showThing(final String thingName,
			final Set<String> thingsToShow, final Set<String> thingsToHide) {
		return (thingsToShow == null || thingsToShow.contains(thingName))
				&& (thingsToHide == null || !thingsToHide.contains(thingName));
	}

	/**
	 * The code here will be executing before application exit Put your code
	 * here. DBA : to be validating
	 */
	public void exitApplication() {
	}

	/**
	 * Constructs a default menu.
	 * <ul>
	 * <li>If the menuItems set is null, all default items are created.
	 * <li>If the set is not null, only the menu items whose names are in there
	 * are shown
	 * </ul>
	 * For an overview of the names, check HMIMessages.MENU_...
	 * 
	 * @param menuItemsToShow
	 * @param menuItemsToHide
	 * @return
	 */
	public JMenuBar createDefaultMenu(final Set<String> menuItemsToShow,
			final Set<String> menuItemsToHide) {
		final JMenuBar menuBar = new JMenuBar();

		final JMenu fileMenu = new JMenu(HMIMessages
				.getString(HMIMessages.MENU_FILE));
		fileMenu.setMnemonic(HMIMessages.getString(
				HMIMessages.MENU_FILE + HMIMessages.KEY).charAt(0));

		if (showThing(HMIMessages.MENU_TEMPLATES, menuItemsToShow,
				menuItemsToHide)) {
			final JMenu templatesSubMenu = new JMenu(HMIMessages
					.getString(HMIMessages.MENU_TEMPLATES));
			final Iterator itr = hmiModelsDef.getModels().keySet().iterator();

			final List<String> orderedList = new ArrayList<String>();
			while (itr.hasNext()) {
				final String modelKey = (String) itr.next();
				final String labelKey = modelKey; // HMIMessages.getString(modelKey);
				orderedList.add(labelKey);
			}
			Collections.sort(orderedList);

			for (int i = 0; i < orderedList.size(); i++) {
				try {
					// JMenuItem templateMenuItem = new
					// JMenuItem(HMIMessages.getString(HMIMessages.MENU_TEMPLATES)
					// + " " + HMIMessages.getString(modelKey));
					final JMenuItem templateMenuItem = new JMenuItem(
							HMIMessages.getString(orderedList.get(i)));
					templateMenuItem.addActionListener(new TemplateModelOpener(
							orderedList.get(i)));
					templatesSubMenu.add(templateMenuItem);
				} catch (final Exception e1) {
					e1.printStackTrace();
					logger.error("", e1);
				}
			}
			fileMenu.add(templatesSubMenu);

			StateMachine.getInstance().registerActionForState(
					StateMachine.READY, HMIMessages.MENU_TEMPLATES,
					templatesSubMenu);
			StateMachine.getInstance().registerActionForState(
					StateMachine.MODEL_OPEN, HMIMessages.MENU_TEMPLATES,
					templatesSubMenu);

		}

		if (showModelGraph) {
			if (showThing(HMIMessages.MENU_NEW, menuItemsToShow,
					menuItemsToHide)) {
				if (modelCreatorAction == null) {
					modelCreatorAction = new ModelCreator(this);
				}
				fileMenu.add(modelCreatorAction);
				// final JMenuItem fileNewMenuItem = new JMenuItem(HMIMessages
				// .getString(HMIMessages.MENU_NEW), HMIMessages.getString(
				// HMIMessages.MENU_NEW + HMIMessages.KEY).charAt(0));
				// fileNewMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				// KeyEvent.VK_N, InputEvent.CTRL_MASK));
				// fileNewMenuItem.addActionListener(new ModelCreator());
				// fileMenu.add(fileNewMenuItem);

				// StateMachine.getInstance().registerActionForState(
				// StateMachine.READY, HMIMessages.MENU_NEW, fileNewMenuItem);
				// StateMachine.getInstance().registerActionForState(
				// StateMachine.MODEL_OPEN, HMIMessages.MENU_NEW,
				// fileNewMenuItem);
			}
		}

		if (showThing(HMIMessages.MENU_OPEN, menuItemsToShow, menuItemsToHide)) {
			final JMenuItem fileOpenMenuItem = new JMenuItem(HMIMessages
					.getString(HMIMessages.MENU_OPEN), HMIMessages.getString(
					HMIMessages.MENU_OPEN + HMIMessages.KEY).charAt(0));
			fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_O, InputEvent.CTRL_MASK));
			fileOpenMenuItem.addActionListener(new ModelOpener());
			fileMenu.add(fileOpenMenuItem);

			StateMachine.getInstance()
					.registerActionForState(StateMachine.READY,
							HMIMessages.MENU_OPEN, fileOpenMenuItem);
			StateMachine.getInstance().registerActionForState(
					StateMachine.MODEL_OPEN, HMIMessages.MENU_OPEN,
					fileOpenMenuItem);
		}

		if (showThing(HMIMessages.MENU_CLOSE, menuItemsToShow, menuItemsToHide)) {
			final JMenuItem fileCloseMenuItem = new JMenuItem(HMIMessages
					.getString(HMIMessages.MENU_CLOSE), HMIMessages.getString(
					HMIMessages.MENU_CLOSE + HMIMessages.KEY).charAt(0));
			fileCloseMenuItem.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_C, InputEvent.CTRL_MASK));
			fileCloseMenuItem.addActionListener(new ModelCloser());
			fileMenu.add(fileCloseMenuItem);
			StateMachine.getInstance().registerActionForState(
					StateMachine.MODEL_OPEN, HMIMessages.MENU_CLOSE,
					fileCloseMenuItem);
		}
		if (showThing(HMIMessages.MENU_SAVE, menuItemsToShow, menuItemsToHide)) {
			fileMenu.add(new JSeparator());
			if (saveAction == null) {
				saveAction = new SaveAction(this);
			}
			fileMenu.add(saveAction);
			// StateMachine.getInstance().registerActionForState(
			// StateMachine.MODEL_OPEN, HMIMessages.MENU_SAVE, save);
		}
		if (showThing(HMIMessages.MENU_SAVEAS, menuItemsToShow, menuItemsToHide)) {
			if (saveAsAction == null) {
				saveAsAction = new SaveAsAction(this);
			}
			fileMenu.add(saveAsAction);
			// StateMachine.getInstance().registerActionForState(
			// StateMachine.MODEL_OPEN, HMIMessages.MENU_SAVEAS, save);
		}
		if (showThing(HMIMessages.MENU_EXIT, menuItemsToShow, menuItemsToHide)) {
			final JMenuItem exitMenuItem = new JMenuItem(HMIMessages
					.getString(HMIMessages.MENU_EXIT), HMIMessages.getString(
					HMIMessages.MENU_EXIT + HMIMessages.KEY).charAt(0));
			exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
					InputEvent.CTRL_MASK));
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (logger.isTraceEnabled()) {
						logger.trace("Exit action - entry"); //$NON-NLS-1$
					}

					logger.info(HMIMessages.getString(HMIMessages.INFO_EXIT));
					// DBA to be validating...
					// Application which use HMIBase needs to call exit
					// treatment before exiting
					exitApplication();

					if (logger.isTraceEnabled()) {
						logger.trace("Exit action - exit"); //$NON-NLS-1$
					}

					System.exit(0);
				}
			});
			fileMenu.add(new JSeparator());
			fileMenu.add(exitMenuItem);

			StateMachine.getInstance().registerActionForState(
					StateMachine.READY, HMIMessages.MENU_EXIT, exitMenuItem);
			StateMachine.getInstance().registerActionForState(
					StateMachine.MODEL_OPEN, HMIMessages.MENU_EXIT,
					exitMenuItem);
		}

		final JMenu runMenu = new JMenu(HMIMessages
				.getString(HMIMessages.MENU_RUN));
		runMenu.setMnemonic(HMIMessages.getString(
				HMIMessages.MENU_RUN + HMIMessages.KEY).charAt(0));

		if (showThing(HMIMessages.MENU_EXECUTE, menuItemsToShow,
				menuItemsToHide)) {
			if (modelExecutor == null) {
				modelExecutor = new ModelExecutor(this);
			}
			final JMenuItem runExecuteMenuItem = new JMenuItem(modelExecutor);
			runMenu.add(runExecuteMenuItem);
			StateMachine.getInstance().registerActionForState(
					StateMachine.MODEL_OPEN, HMIMessages.MENU_EXECUTE,
					modelExecutor);
		}
		if (showThing(HMIMessages.MENU_STOP, menuItemsToShow, menuItemsToHide)) {
			if (modelStopper == null) {
				modelStopper = new ModelStopper(this);
			}
			final JMenuItem stopExecuteMenuItem = new JMenuItem(modelStopper);
			runMenu.add(stopExecuteMenuItem);
			StateMachine.getInstance().registerActionForState(
					StateMachine.MODEL_EXECUTING, HMIMessages.MENU_STOP,
					modelStopper);
		}
		if (showThing(HMIMessages.MENU_INTERACTIVE_ERRORHANDLING,
				menuItemsToShow, menuItemsToHide)) {
			final JMenuItem interactiveErrorCtrlMenuItem = new JCheckBoxMenuItem(
					HMIMessages
							.getString(HMIMessages.MENU_INTERACTIVE_ERRORHANDLING));
			interactiveErrorCtrlMenuItem
					.addActionListener(new ActionListener() {
						public void actionPerformed(final ActionEvent e) {
							interactiveErrorControl = ((JCheckBoxMenuItem) e
									.getSource()).getState();
						}

					});
			runMenu.add(new JSeparator());
			runMenu.add(interactiveErrorCtrlMenuItem);
		}

		final JMenu graphMenu = new JMenu(HMIMessages
				.getString(HMIMessages.MENU_GRAPH));
		graphMenu.setMnemonic(HMIMessages.getString(
				HMIMessages.MENU_GRAPH + HMIMessages.KEY).charAt(0));

		// if (showThing(HMIMessages.MENU_SHOW, menuItemsToShow,
		// menuItemsToHide)) {
		// JMenuItem graphViewMenuItem = new
		// JCheckBoxMenuItem(HMIMessages.getString(HMIMessages.MENU_SHOW));
		// graphMenu.add(graphViewMenuItem);
		//
		// graphViewMenuItem.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// if (logger.isTraceEnabled()) {
		//						logger.trace("Graph Show action - entry"); //$NON-NLS-1$
		// }
		// boolean showGraph = ((JCheckBoxMenuItem) e.getSource()).getState();
		// if(showGraph) {
		// applyFieldValuesToParameters();
		// clearModelForms();
		// showModelGraph(HMIBase.this.currentModel.getName());
		// } else {
		// clearModelGraphs();
		// showModelForm(HMIBase.this.currentModel.getName());
		// }
		// if (logger.isTraceEnabled()) {
		//						logger.trace("Graph Show action - exit"); //$NON-NLS-1$
		// }
		// }
		//
		//
		// });
		// }
		if (showThing(HMIMessages.MENU_ANIMATE, menuItemsToShow,
				menuItemsToHide)) {
			final JMenuItem animateGraphViewMenuItem = new JCheckBoxMenuItem(
					HMIMessages.getString(HMIMessages.MENU_ANIMATE));
			animateGraphViewMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					animateModelExecution = ((JCheckBoxMenuItem) e.getSource())
							.getState();
				}

			});
			graphMenu.add(new JSeparator());
			graphMenu.add(animateGraphViewMenuItem);
		}

		if (fileMenu.getMenuComponentCount() > 0) {
			menuBar.add(fileMenu);
		}
		if (runMenu.getMenuComponentCount() > 0) {
			menuBar.add(runMenu);
			StateMachine.getInstance().registerActionForState(
					StateMachine.MODEL_OPEN, HMIMessages.MENU_RUN, runMenu);
			StateMachine.getInstance()
					.registerActionForState(StateMachine.MODEL_EXECUTING,
							HMIMessages.MENU_RUN, runMenu);
		}
		if (graphMenu.getMenuComponentCount() > 0) {
			menuBar.add(graphMenu);
			StateMachine.getInstance().registerActionForState(
					StateMachine.MODEL_OPEN, HMIMessages.MENU_GRAPH, graphMenu);
			StateMachine.getInstance().registerActionForState(
					StateMachine.MODEL_EXECUTING, HMIMessages.MENU_GRAPH,
					graphMenu);
		}

		final JMenu monitoringMenu = new JMenu(HMIMessages
				.getString(HMIMessages.MENU_MONITORING));
		monitoringMenu.setMnemonic(HMIMessages.getString(
				HMIMessages.MENU_MONITORING + HMIMessages.KEY).charAt(0));
		if (showThing(HMIMessages.MENU_TRACING, menuItemsToShow,
				menuItemsToHide)) {
			final JMenuItem traceMenuItem = new JMenuItem(HMIMessages
					.getString(HMIMessages.MENU_TRACING), HMIMessages
					.getString(HMIMessages.MENU_TRACING + HMIMessages.KEY)
					.charAt(0));
			traceMenuItem.addActionListener(new TraceDialogOpener());
			monitoringMenu.add(traceMenuItem);
		}

		if (monitoringMenu.getMenuComponentCount() > 0) {
			menuBar.add(monitoringMenu);
		}

		StateMachine.getInstance().compile();
		StateMachine.getInstance().transitionTo(StateMachine.READY);
		return menuBar;
	}

	protected void showModelGraph(final String modelKey) {
		try {
			final JFrame gf = new JFrame(HMIMessages
					.getString(HMIMessages.GRAPH_TITLE));
			gf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			if (graphPanelEffigy == null) {
				graphPanelEffigy = new PtolemyEffigy(getPtolemyConfiguration(),
						modelKey);
			}
			graphPanel = new ModelGraphPanel(currentModel, graphPanelEffigy);

			// TODO BEWARE : animation now happens in the actor
			// thread and blocks for a second!!
			// graphPanel.setAnimationDelay(1000);
			gf.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(final WindowEvent e) {
					clearModelGraphs();
				}
			});
			gf.getContentPane().add(graphPanel);
			gf.setLocation(400, 300);
			gf.setSize(700, 400);
			gf.setVisible(true);

		} catch (final Throwable t) {
			logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
		}
	}

	protected abstract void clearModelGraphs();

	public JToolBar createDefaultToolbar() {
		final JToolBar toolBar = new JToolBar();

		if (modelExecutor == null) {
			modelExecutor = new ModelExecutor(this);
		}
		if (modelStopper == null) {
			modelStopper = new ModelStopper(this);
		}
		if (modelDebugger == null) {
			modelDebugger = new ModelDebugger(this);
		}
		if (modelDebugStepper == null) {
			modelDebugStepper = new ModelDebugStepper(this);
		}
		if (modelSuspender == null) {
			modelSuspender = new ModelSuspender(this);
		}
		if (modelResumer == null) {
			modelResumer = new ModelResumer(this);
		}
		if (saveAction == null) {
			saveAction = new SaveAction(this);
		}
		// DBA : add tooltip for each button
		createToolbarButton(modelExecutor, toolBar, HMIMessages
				.getString(HMIMessages.MENU_EXECUTE));
		createToolbarButton(modelSuspender, toolBar, HMIMessages
				.getString(HMIMessages.MENU_SUSPEND));
		createToolbarButton(modelResumer, toolBar, HMIMessages
				.getString(HMIMessages.MENU_RESUME));
		createToolbarButton(modelStopper, toolBar, HMIMessages
				.getString(HMIMessages.MENU_STOP));
		toolBar.addSeparator();
		createToolbarButton(modelDebugger, toolBar, HMIMessages
				.getString(HMIMessages.MENU_DEBUG));
		createToolbarButton(modelDebugStepper, toolBar, HMIMessages
				.getString(HMIMessages.MENU_DEBUG_STEP));
		toolBar.addSeparator();
		toolBar.addSeparator();
		createToolbarButton(saveAction, toolBar, HMIMessages
				.getString(HMIMessages.MENU_SAVE));

		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_EXECUTE,
				modelExecutor);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_DEBUG, modelDebugger);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_DEBUGGING, HMIMessages.MENU_DEBUG_STEP,
				modelDebugStepper);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_DEBUGGING, HMIMessages.MENU_STOP,
				modelStopper);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_EXECUTING, HMIMessages.MENU_STOP,
				modelStopper);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_EXECUTING_SUSPENDED, HMIMessages.MENU_STOP,
				modelStopper);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_EXECUTING, HMIMessages.MENU_SUSPEND,
				modelSuspender);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_EXECUTING_SUSPENDED,
				HMIMessages.MENU_RESUME, modelResumer);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_DEBUGGING, HMIMessages.MENU_RESUME,
				modelResumer);

		StateMachine.getInstance().compile();
		StateMachine.getInstance().transitionTo(StateMachine.READY);

		return toolBar;
	}

	/**
	 * DBA : add tooltip for each button
	 * 
	 * @return
	 */
	private JButton createToolbarButton(final Action action,
			final JToolBar toolbar, final String tooltip) {
		final JButton b = new JButton(action);
		b.setBorderPainted(false);
		b.setToolTipText(tooltip);
		b.setText(null);
		toolbar.add(b);
		return b;
	}

	/**
	 * 
	 * @param mappingKey
	 * @param uiWidget
	 * @param binder
	 * @return
	 */
	protected Object registerBinding(final String mappingKey,
			final Object uiWidget, final ParameterToWidgetBinder binder) {
		final String paramName = getCurrentModelDef().getFieldMapping()
				.getValueForKey(mappingKey);
		try {
			final Parameter p = ModelUtils.getParameter(getCurrentModel(),
					paramName);
			p.addValueListener(new ParameterChangeListener(binder));
			binder.setBoundComponent(uiWidget);
			binder.setBoundParameter(p);
			hmiFields.put(mappingKey, binder);
			// now make sure the fields contain the param values from the model
			binder.fillWidgetFromParameter();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return uiWidget;
	}

	public abstract void setSaved();

	public void saveModelAs(final CompositeActor model, final URL destinationURL)
			throws IOException, IllegalActionException,
			NameDuplicationException {
		File destinationFile = null;

		try {
			destinationFile = getLocalFileFromURL(destinationURL);
		} catch (final Exception e) {
			// ignore, means that the URL is unknown or a remote model URL
			// this is handled further below
		}
		applyFieldValuesToParameters();
		if (destinationFile != null
				&& (!destinationFile.exists() || destinationFile.canWrite())) {
			BufferedWriter outputWriter = null;
			try {
				outputWriter = new BufferedWriter(new FileWriter(
						destinationFile));
				// set name of the model with file name
				String newModelName = destinationFile.getName();
				if (newModelName.contains(".moml")) {
					newModelName = newModelName.replace(".moml", "");
				}
				model.setName(newModelName);
				model.exportMoML(outputWriter);
			} finally {
				if (outputWriter != null) {
					outputWriter.flush();
					outputWriter.close();
					this.setSaved();
				}
			}
		} else {
			throw new IOException("File not writable " + destinationFile);
		}

	}

	public File getLocalFileFromURL(final URL destinationURL)
			throws IOException {
		File destinationFile = null;
		try {
			destinationFile = new File(destinationURL.toURI());
		} catch (final URISyntaxException e) {
			throw new IOException("Destination URL not a valid local file "
					+ destinationURL);
		}
		return destinationFile;
	}

	/**
	 *
	 */
	protected void applyFieldValuesToParameters() {
		final Iterator fieldsItr = hmiFields.values().iterator();
		while (fieldsItr.hasNext()) {
			final ParameterToWidgetBinder pwb = (ParameterToWidgetBinder) fieldsItr
					.next();
			pwb.fillParameterFromWidget();
		}
	}

	public Director getDirector() {
		return (Director) currentModel.getDirector();
	}

	public void launchModel(final ModelExecutor executor) {
		applyFieldValuesToParameters();
		launchModel(getCurrentModel(), executor, false, true);
	}

	public void launchModel(final CompositeActor model,
			final ModelExecutor executor, final boolean blocking,
			final boolean updateState) {
		try {
			manager = new Manager(model.workspace(), model.getName());
			model.setManager(manager);

			final Director director = (Director) model.getDirector();
			if (interactiveErrorControl) {
				director
						.setErrorControlStrategy(new AskTheUserErrorControlStrategy(
								this, executor));
			} else {
				director
						.setErrorControlStrategy(new DefaultActorErrorControlStrategy());
			}

			/*
			 * java.lang.reflect.Method m =
			 * director.getClass().getDeclaredMethod("setInteractiveExecution",
			 * boolean.class); if(m != null) m.invoke(director, true);
			 */
			/*
			 * if (interactiveErrorControl) {
			 * getDirector().setErrorControlStrategy(new
			 * AskTheUserErrorControlStrategy(this, executor)); }
			 */
			/* specific for Soleil 
			if (getDirector() instanceof fr.soleil.passerelle.domain.BasicDirector) {
				((fr.soleil.passerelle.domain.BasicDirector) getDirector())
						.setInteractiveExecution(interactiveErrorControl);
			}
			*/
			// we enforce a suspend/resume control
			// TODO investigate if we should be able to support multiple
			// execution controllers,
			// e.g. when one has been configured already for the director...
			final Attribute tmp = director
					.getAttribute(HMIBase.EXECUTION_CONTROL_ATTR_NAME);
			if (tmp != null) {
				// remove the previous execution controller
				tmp.setContainer(null);
			}
			if (executor != null) {
				executor.createExecutionControlStrategy(director,
						HMIBase.EXECUTION_CONTROL_ATTR_NAME);
			}

			setModelConfigurationBeforeExecution(model);
			animationEventListener = setModelAnimation(model,
					animateModelExecution, animationEventListener);

			if (blocking) {
				manager.execute();
			} else {
				manager.startRun();
			}

			if (updateState && executor != null) {
				StateMachine.getInstance().transitionTo(
						executor.getSuccessState());
			}
		} catch (final Throwable t) {
			t.printStackTrace();
			logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
			PopupUtil.showError(hookComponent, "error.execution.error", t
					.getMessage());
			StateMachine.getInstance().transitionTo(executor.getErrorState());
		}
	}

	protected Configuration readConfiguration(final String urlSpec)
			throws Exception {
		logger.debug("read Passerelle Configuration");

		final URL inurl = specToURL(urlSpec);
		final MoMLParser parser = new MoMLParser();
		final Configuration toplevel = (Configuration) parser.parse(inurl,
				inurl.openStream());
		// If the toplevel model is a configuration containing a directory,
		// then create an effigy for the configuration itself, and put it
		// in the directory.
		final ComponentEntity directoryEntity = toplevel.getEntity("directory");
		if (directoryEntity instanceof ModelDirectory) {
			final PtolemyEffigy effigy = new PtolemyEffigy(
					(ModelDirectory) directoryEntity, toplevel.getName());
			effigy.setModel(toplevel);
			effigy.identifier.setExpression(inurl.toExternalForm());
		}
		return toplevel;
	}

	/**
	 * Given the name of a file or a URL, convert it to a URL. This first
	 * attempts to do that directly by invoking a URL constructor. If that
	 * fails, then it tries to interpret the spec as a file name on the local
	 * file system. If that fails, then it tries to interpret the spec as a
	 * resource accessible to the classloader, which uses the classpath to find
	 * the resource. If that fails, then it throws an exception. The
	 * specification can give a file name relative to current working directory,
	 * or the directory in which this application is started up.
	 * 
	 * @param spec
	 *            The specification.
	 * @exception IOException
	 *                If it cannot convert the specification to a URL.
	 */
	public static URL specToURL(final String spec) throws IOException {
		try {
			// First argument is null because we are only
			// processing absolute URLs this way. Relative
			// URLs are opened as ordinary files.
			return new URL(null, spec);
		} catch (final MalformedURLException ex) {
			try {
				final File file = new File(spec);
				if (!file.exists()) {
					throw new MalformedURLException();
				}
				return file.getCanonicalFile().toURL();
			} catch (final MalformedURLException ex2) {
				try {
					// Try one last thing, using the classpath.
					// Need a class context, and this is a static method, so...
					// NOTE: There doesn't seem to be any way to convert
					// this a canonical name, so if a model is opened this
					// way, and then later opened as a file, the model
					// directory will think it has two different files.
					final Class refClass = Class
							.forName("ptolemy.kernel.util.NamedObj");
					final URL inurl = refClass.getClassLoader().getResource(
							spec);
					if (inurl == null) {
						throw new Exception();
					} else {
						return inurl;
					}
				} catch (final Exception exception) {
					throw new IOException("File not found: " + spec);
				}
			}
		}
	}

	/**
	 * Override this method in HMI subclasses if you want to add extra
	 * configurations to the model. <br>
	 * By default, this sets a ModelExecutionListener that will show a pop-up
	 * when a model has finished its execution (possible with error info).
	 * 
	 * @param model
	 */
	protected void setModelConfigurationBeforeExecution(
			final CompositeActor model) {
		final Director director = (Director) model.getDirector();
		director.removeAllErrorCollectors();
		final ModelExecutionListener executionListener = createExecutionListener();
		director.addErrorCollector(executionListener);
		model.getManager().addExecutionListener(executionListener);
	}

	/**
	 * Override this method if you need another kind of execution listener in a
	 * specific HMI application
	 * 
	 * @return
	 */
	protected ModelExecutionListener createExecutionListener() {
		return new ModelExecutionListener();
	}

	/**
	 * Override this method in HMI subclasses if you want to specify animation
	 * behaviour for the model. <br>
	 * By default, this uses an AnimationEventListener that will animate the
	 * graphical view of the model being executed.
	 * 
	 * @param model
	 * @param animateModelExecution
	 *            if true, some kind of animation should be provided
	 * @param currentListener
	 *            the current animation listener that was set, e.g. for a
	 *            previous model execution
	 * @return the listener that was effectively set if animation is needed, or
	 *         null if no animation is set
	 */
	protected AnimationEventListener setModelAnimation(
			final CompositeActor model, final boolean animateModelExecution,
			AnimationEventListener currentListener) {
		final Director director = (Director) model.getDirector();
		if (animateModelExecution) {
			if (currentListener == null) {
				currentListener = createAnimationListener();
			}
			director.registerFiringEventListener(currentListener);
		} else {
			director.removeFiringEventListener(animationEventListener);
			currentListener = null;
		}
		return currentListener;
	}

	/**
	 * Override this method if you need another kind of animation listener in a
	 * specific HMI application
	 * 
	 * @return
	 */
	protected AnimationEventListener createAnimationListener() {
		return new AnimationEventListener();
	}

	public void stopModel() {
		try {
			if (manager != null) {
				manager.stop();
				// desactivate toolbar while stopping manager.stop() since is
				// not a
				// blocking method
				// see ModelExecutionListener.executionFinished where the model
				// is really stop
				StateMachine.getInstance().transitionTo(StateMachine.READY);
			}
		} catch (final Throwable t) {
			t.printStackTrace();
			logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
			PopupUtil.showError(hookComponent, HMIMessages.ERROR_GENERIC, t
					.getMessage());
		}
	}

	/**
	 * DBA : suspend execution Need to be validating
	 */
	public void suspendModel() {
		try {
			if (manager != null) {
				manager.pause();
			}
			StateMachine.getInstance().transitionTo(
					StateMachine.MODEL_EXECUTING_SUSPENDED);
		} catch (final Throwable t) {
			t.printStackTrace();
			logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
			PopupUtil.showError(hookComponent, HMIMessages.ERROR_GENERIC, t
					.getMessage());
		}
	}

	public void setHmiModelsDef(final ModelBundle hmiModelsDef) {
		this.hmiModelsDef = hmiModelsDef;
	}

	/**
	 * DBA : suspend execution Need to be validating
	 */
	public void resumeModel() {
		try {
			if (manager != null) {
				manager.resume();
			}
			StateMachine.getInstance().transitionTo(
					StateMachine.MODEL_EXECUTING);
		} catch (final Throwable t) {
			t.printStackTrace();
			logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
			PopupUtil.showError(hookComponent, HMIMessages.ERROR_GENERIC, t
					.getMessage());
		}
	}

	public void setTraceComponent(final TraceVisualizer traceComponent) {
		this.traceComponent = traceComponent;
	}

	/**
	 * Opens a new form for a given model key, i.e. a form containing values
	 * from the "template" model file.
	 * 
	 * @author erwin dl
	 */
	protected class TemplateModelOpener implements ActionListener {

		String modelKey;

		public TemplateModelOpener(final String key) {
			modelKey = key;
		}

		public void actionPerformed(final ActionEvent e) {
			try {
				currentModelDef = hmiModelsDef.getModel(modelKey);
				loadModel(currentModelDef.getMomlPath(), null);

				StateMachine.getInstance()
						.transitionTo(StateMachine.MODEL_OPEN);

			} catch (final Exception e1) {
				e1.printStackTrace();
				logger.error("", e1);
				PopupUtil.showError(hookComponent, "error.file.open");
			}
		}

	}

	protected class ModelCloser implements ActionListener {

		public ModelCloser() {
		}

		public void actionPerformed(final ActionEvent e) {
			if (logger.isTraceEnabled()) {
				logger.trace("File Close action - entry"); //$NON-NLS-1$
			}

			clearModelForms();
			clearModelGraphs();
			try {
				MoMLParser.purgeModelRecord(modelURL);
			} catch (final Exception ex) {
				// ignore, just trying to be nice and clean up memory
			}

			StateMachine.getInstance().transitionTo(StateMachine.READY);

			if (logger.isTraceEnabled()) {
				logger.trace("File Close action - exit"); //$NON-NLS-1$
			}
		}
	}

	protected class ModelOpener implements ActionListener {

		public ModelOpener() {
			super();
		}

		public void actionPerformed(final ActionEvent e) {
			if (logger.isTraceEnabled()) {
				logger.trace("File Open action - entry"); //$NON-NLS-1$
			}
			try {
				JFileChooser fileChooser;

				fileChooser = new JFileChooser(new URL(
						HMIBase.MODELS_URL_STRING).getFile());

				fileChooser.setAccessory(new FinderAccessory(fileChooser));
				fileChooser.addChoosableFileFilter(new ExtensionFileFilter(
						new String[] { "xml", "moml" },
						"Passerelle model files"));
				final int returnVal = fileChooser.showOpenDialog((Component) e
						.getSource());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					modelURL = fileChooser.getSelectedFile().toURL();
					loadModel(modelURL, null);

					StateMachine.getInstance().transitionTo(
							StateMachine.MODEL_OPEN);
				}
			} catch (final Exception e1) {
				logger.error("", e1);
				PopupUtil.showError(hookComponent, "error.file.open");
			}
			if (logger.isTraceEnabled()) {
				logger
						.trace("File Open action - exit - current file : " + modelURL); //$NON-NLS-1$
			}
		}
	}

	protected class ModelExecutionListener implements ExecutionListener,
			ErrorCollector {

		private boolean popUpError = true;

		public void acceptError(final PasserelleException e) {
			executionError(null, e);
		}

		public void executionError(final Manager manager,
				final Throwable throwable) {
			logger.error(HMIMessages.getString("error.execution.error"),
					throwable);
			if (popUpError) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						PopupUtil
								.showError(hookComponent,
										"error.execution.error", throwable
												.getMessage());
						// StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
					}
				});
				popUpError = false;
			}
		}

		public void executionFinished(final Manager manager) {
			StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
			// SwingUtilities.invokeLater(new Runnable() {
			// public void run() {
			// PopupUtil
			// .showInfo(hookComponent, "info.execution.finished");
			// }
			// });
		}

		public void managerStateChanged(final Manager manager) {
			// System.out.println(manager.getState());
			if (manager.getState().equals(Manager.INITIALIZING)) {
				popUpError = true;
			}
		}
	}

	protected class TraceDialogOpener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			traceComponent.show();
		}
	}

	protected class AnimationEventListener implements FiringEventListener {
		public void onEvent(final FiringEvent e) {
			if (graphPanel != null) {
				graphPanel.event(e);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("onEvent() : " + ((Entity) e.getActor()).getName()
						+ ":" + e.getType());
			}
		}
	}

	public ModelBundle getHmiModelsDef() {
		return hmiModelsDef;
	}

	public TraceVisualizer getTraceComponent() {
		return traceComponent;
	}

	/**
	 * If it's a structural change, we need to adapt the cfg forms. Any
	 * graphical views should be model change listeners already, and will
	 * probably be the origin of the change anyway...
	 */
	public void changeExecuted(final ChangeRequest change) {
		if (change.getDescription() != null) {
			final String[] importantChanges = new String[] { "entity",
					"property", "deleteEntity", "deleteProperty", "class" };
			boolean shouldRefreshForms = false;
			for (final String changeType : importantChanges) {
				if (change.getDescription().contains(changeType)) {
					shouldRefreshForms = true;
					break;
				}
			}
			if (shouldRefreshForms) {
				showModelForm(null);
			}
		}
	}

	public void changeFailed(final ChangeRequest change,
			final Exception exception) {
		// TODO Auto-generated method stub

	}

}

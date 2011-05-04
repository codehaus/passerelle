package com.isencia.passerelle.hmi.generic;

import java.awt.BorderLayout;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.ClassicDockingTheme;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.PropertiesUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;

import org.apache.log4j.Logger;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.gui.PasserelleEditorPaneFactory;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.definition.FieldMapping;
import com.isencia.passerelle.hmi.definition.Model;
import com.isencia.passerelle.hmi.definition.ModelBundle;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.hmi.util.GenericHMIUserPref;
import com.isencia.passerelle.model.FlowHandle;

/**
 * Build the panels for Passerelle GUI.
 * 
 * @author ABEILLE
 * 
 */
public class PasserelleGUIBuilder {

	private GenericHMI genericHMI;

	private JPanel beanPanel;

	private JFrame mainFrame;

	private View parametersView;
	private View logView;
	private TabWindow mainWindow;

	private RootWindow rootWindow;

	private boolean parametersVisible = true;

	private boolean logVisible = true;

	private boolean toolbarVisible = true;

	private boolean standalone;
	private final boolean showModelGraph;
	// private final FlowManager flowManager = new FlowManager();

	// a bundle that lists all models of a directory
	private static final ModelBundle BUNDLE = new ModelBundle();

	public PasserelleGUIBuilder(final boolean showModelGraph,
			final boolean standalone) {
		this.showModelGraph = showModelGraph;
		this.standalone = standalone;
		try {
			this.buildGenericHMI(showModelGraph);
			this.displayFrame();
		} catch (final Throwable t) {
			System.err.println("ERROR: Impossible to create MainGenericHMI");
			t.printStackTrace();
			PopupUtil.showError(new TextArea(), "Please contact ICA");
		}
	}

	public PasserelleGUIBuilder(final URL modelToLoad,
			final boolean showModelGraph) {
		this.showModelGraph = showModelGraph;
		this.loadModel(modelToLoad);
	}

	public PasserelleGUIBuilder(final String modelToLoad,
			final boolean showModelGraph, final boolean standalone)
			throws MalformedURLException {
		this.showModelGraph = showModelGraph;
		this.standalone = standalone;
		URL url = null;
		if (modelToLoad.startsWith("file") || modelToLoad.startsWith("http")) {
			url = new URL(modelToLoad);
		} else {
			url = new URL("file:" + modelToLoad);
		}
		loadModel(url);
	}

	private void loadModel(final URL modelToLoad) {
		try {
			this.buildGenericHMI(showModelGraph);
			genericHMI.loadModel(modelToLoad, null);
			this.displayFrame();
			// state transition must be done only after the frame has been
			// diplayed
			StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
		} catch (final Throwable t) {
			System.err.println("ERROR: Sequence file not found:" + modelToLoad);
			t.printStackTrace();
			PopupUtil.showError(new TextArea(), "Not found: " + modelToLoad);
		} finally {
			this.displayFrame();
		}
	}

	private void buildGenericHMI(final boolean showModelGraph)
			throws IOException, IllegalActionException,
			NameDuplicationException {
		// create a bundle that make appears the list of all models of
		// a directory in the GenericHMI menus
		if (HMIBase.MODELS_URL_STRING != null) {
			final URL modelURL = new URL(HMIBase.MODELS_URL_STRING);
			final FieldMapping m1FieldBundle = new FieldMapping();
			// Collection<FlowHandle> flowsFromResourceLocation = null;
			// Collection<FlowHandle> flowsFromResourceLocation =
			// FlowManager.getFlowsFromResourceLocation(modelURL);
			// FIXME waiting for bug fix in getFlowsFromResourceLocation
			// it's a local directory
			final Collection<FlowHandle> flowsFromResourceLocation = new ArrayList<FlowHandle>();
			try {
				final File dir = new File(modelURL.toURI());
				if (dir != null && dir.exists()) {
					// find all files that end with ".moml"
					final FilenameFilter filter = new FilenameFilter() {
						public boolean accept(final File file, final String name) {
							return name.endsWith(".moml");
						}
					};
					final File[] fileList = dir.listFiles(filter);
					for (final File file : fileList) {
						final FlowHandle flowHandle = new FlowHandle(null, file
								.getName(), file.toURI().toURL());
						flowsFromResourceLocation.add(flowHandle);
					}
				}
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (flowsFromResourceLocation != null
					&& !flowsFromResourceLocation.isEmpty()) {
				for (final FlowHandle flowHandle : flowsFromResourceLocation) {
					final Model m1 = new Model(flowHandle
							.getAuthorativeResourceLocation(), m1FieldBundle);
					BUNDLE.addModel(flowHandle.getName(), m1);
				}
				genericHMI = new GenericHMI(BUNDLE, showModelGraph);
			}
		}
		if (genericHMI == null) {
			genericHMI = new GenericHMI(showModelGraph);
		}

		genericHMI.setEditorPaneFactory(new PasserelleEditorPaneFactory());
		genericHMI.init();
	}

	public View getParametersView() {
		if (parametersView == null) {
			parametersView = new View(
					"Parameters",
					new ImageIcon(
							Toolkit
									.getDefaultToolkit()
									.getImage(
											getClass()
													.getResource(
															"/com/isencia/passerelle/hmi/resources/settings.gif"))),
					genericHMI.getParameterScrollPane());
		}
		return parametersView;
	}

	public View getLogView() {
		if (logView == null) {
			logView = new View(
					"Log",
					new ImageIcon(
							Toolkit
									.getDefaultToolkit()
									.getImage(
											getClass()
													.getResource(
															"/com/isencia/passerelle/hmi/resources/console.gif"))),
					genericHMI.getTracePanel());
		}
		return logView;
	}

	public RootWindow getMainRootWindow() {
		if (rootWindow == null) {
			final ViewMap viewMap = new ViewMap();

			final TabWindow modelGraphPanel = genericHMI.getModelGraphPanel();
			final View graphView;
			int i = 0;
			if (showModelGraph) {
				graphView = new View(
						"Graph",
						new ImageIcon(Toolkit.getDefaultToolkit().getImage(
									getClass().getResource("/com/isencia/passerelle/hmi/resources/ModelDataIcon.gif"))),
						modelGraphPanel);
				viewMap.addView(i++, graphView);
			} else {
				graphView = null;
			}
			viewMap.addView(i++, getParametersView());
			viewMap.addView(i++, getLogView());

			rootWindow = DockingUtil.createRootWindow(viewMap, true);
			prepareRootWindow();
			try {
				final Runnable run = new Runnable() {
					public void run() {
						try {
							if (showModelGraph) {
								modelGraphPanel.getWindowProperties().addSuperObject(rootWindow.getWindowProperties());
								mainWindow = new TabWindow(new DockingWindow[] {
										graphView, getParametersView() });
								mainWindow.setSelectedTab(0);
								rootWindow.setWindow(new SplitWindow(false,
										0.6f, mainWindow, getLogView()));
							} else {
								rootWindow
										.setWindow(new SplitWindow(false, 0.5f,
												getParametersView(),
												getLogView()));
							}

							// loadWindowLayoutPreferences(rootWindow, null);
						} catch (final Exception e) {
							Logger.getLogger(getClass()).error(
									"Error while creating docking components",
									e);
						}
					}
				};
				if (Thread.currentThread().getName().startsWith("AWT-Event")) {
					// GBS runs everything in the AWT EventDispatchThread
					// Javadoc invokeAndWait: It should'nt be called from the
					// EventDispatchThread.
					run.run();
				} else {
					// Default Java apps are launched from Main Thread
					// We have to do this since Infonode is not thread safe
					SwingUtilities.invokeAndWait(run);
				}

			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rootWindow;
	}

	private void prepareRootWindow() {
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		rootWindow.getRootWindowProperties().getDockingWindowProperties()
				.setUndockEnabled(false);
		rootWindow.getRootWindowProperties().getDockingWindowProperties()
				.setCloseEnabled(false);
		final DockingWindowsTheme theme = new ClassicDockingTheme();
		// Apply theme
		rootWindow.getRootWindowProperties().addSuperObject(
				theme.getRootWindowProperties());

		final RootWindowProperties titleBarStyleProperties = PropertiesUtil
				.createTitleBarStyleRootWindowProperties();
		// Enable title bar style
		rootWindow.getRootWindowProperties().addSuperObject(
				titleBarStyleProperties);
	}

	public GenericHMI getGenericHMI() throws IOException,
			IllegalActionException, NameDuplicationException,
			PasserelleException {
		if (genericHMI == null) {
			// create a bundle that make appears the list of all models of
			// a directory in the GenericHMI menus
			if (HMIBase.MODELS_URL_STRING != null) {
				final URL modelURL = new URL(HMIBase.MODELS_URL_STRING);
				final FieldMapping m1FieldBundle = new FieldMapping();
				// Collection<FlowHandle> flowsFromResourceLocation = null;
				// Collection<FlowHandle> flowsFromResourceLocation =
				// FlowManager.getFlowsFromResourceLocation(modelURL);
				// FIXME waiting for bug fix in getFlowsFromResourceLocation
				// it's a local directory
				final Collection<FlowHandle> flowsFromResourceLocation = new ArrayList<FlowHandle>();
				try {
					final File dir = new File(modelURL.toURI());
					if (dir != null && dir.exists()) {
						// find all files that end with ".moml"
						final FilenameFilter filter = new FilenameFilter() {
							public boolean accept(final File file,
									final String name) {
								return name.endsWith(".moml");
							}
						};
						final File[] fileList = dir.listFiles(filter);
						for (final File file : fileList) {
							final FlowHandle flowHandle = new FlowHandle(null,
									file.getName(), file.toURI().toURL());
							flowsFromResourceLocation.add(flowHandle);
						}
					}
				} catch (final Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (flowsFromResourceLocation != null
						&& !flowsFromResourceLocation.isEmpty()) {
					for (final FlowHandle flowHandle : flowsFromResourceLocation) {
						final Model m1 = new Model(flowHandle
								.getAuthorativeResourceLocation(),
								m1FieldBundle);
						BUNDLE.addModel(flowHandle.getName(), m1);
					}
					genericHMI = new GenericHMI(BUNDLE, true);
				} else {
					genericHMI = new GenericHMI(true);
				}
			} else {
				genericHMI = new GenericHMI(true);
			}

			genericHMI.setEditorPaneFactory(new PasserelleEditorPaneFactory());
			genericHMI.init();

		}
		return genericHMI;
	}

	public JPanel getBeanPanel() {
		// if (beanPanel == null) {
		beanPanel = new JPanel(new BorderLayout());
		if (toolbarVisible) {
			beanPanel.add(BorderLayout.BEFORE_FIRST_LINE, genericHMI
					.createDefaultToolbar());
		}
		if (parametersVisible && logVisible) {
			beanPanel.add(BorderLayout.CENTER, getMainRootWindow());
		} else if (parametersVisible) {
			beanPanel.add(BorderLayout.CENTER, genericHMI
					.getParameterScrollPane());
		} else if (logVisible) {
			beanPanel.add(BorderLayout.CENTER, genericHMI.getTracePanel());
		}
		// }
		return beanPanel;
	}

	public JFrame getMainFrame() {
		if (mainFrame == null) {
			mainFrame = new JFrame("Passerelle");
			mainFrame.setContentPane(getBeanPanel());
			mainFrame
					.setIconImage(Toolkit
							.getDefaultToolkit()
							.getImage(
									PasserelleIDEMain.class
											.getResource("/com/isencia/passerelle/hmi/resources/runidew.gif")));
			mainFrame.setJMenuBar(genericHMI.getMenuBar());
			if (standalone) {
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			} else {
				mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
		}
		return mainFrame;
	}

	private void displayFrame() {
		final JFrame frame = getMainFrame();
		sizeAndPlaceFrame(frame);
		frame.addWindowListener(new WindowAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent
			 * )
			 */
			@Override
			public void windowClosed(final WindowEvent e) {
				saveWindowLayoutPreferencesAndExit(rootWindow);
				super.windowClosed(e);
			}
		});
		// frame.validate();
		frame.setVisible(true);
	}

	private void sizeAndPlaceFrame(final JFrame frame) {
		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - 30) / 2);
		frame.setLocation(
				(Toolkit.getDefaultToolkit().getScreenSize().width - frame
						.getSize().width) / 2, (Toolkit.getDefaultToolkit()
						.getScreenSize().height - frame.getSize().height) / 2);
	}

	private void saveWindowLayoutPreferencesAndExit(final RootWindow rootWindow) {
		// FIXME: java.io.IOException: Serialization of unknown view!
		// try {
		// ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// final ObjectOutputStream out = new ObjectOutputStream(bos);
		// rootWindow.write(out, false);
		// out.close();
		// final byte[] layout = bos.toByteArray();
		// GenericHMIUserPref.putByteArrayPref(GenericHMIUserPref.LAYOUT,
		// layout);
		// bos.close();
		// bos = null;
		// System.exit(0);
		// } catch (final Exception e) {
		// e.printStackTrace();
		// JOptionPane.showMessageDialog(rootWindow,
		// "ScreenManager.saveWindowLayoutPreferences() :"
		// + " Unexpected Error (see traces)",
		// "Generic HMI - Error", JOptionPane.ERROR_MESSAGE);
		// }
	}

	public void loadWindowLayoutPreferences(final RootWindow rootWindow,
			final byte[] defaultLayout) {
		final byte[] prefs = GenericHMIUserPref.getByteArrayPref(
				GenericHMIUserPref.LAYOUT, defaultLayout);
		try {
			if (prefs != null) {
				rootWindow.read(new ObjectInputStream(new ByteArrayInputStream(
						prefs)));
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isLogVisible() {
		return logVisible;
	}

	public void setLogVisible(final boolean logVisible) {
		this.logVisible = logVisible;
	}

	public boolean isParametersVisible() {
		return parametersVisible;
	}

	public void setParametersVisible(final boolean parametersVisible) {
		this.parametersVisible = parametersVisible;
	}

	public boolean isToolbarVisible() {
		return toolbarVisible;
	}

	public void setToolbarVisible(final boolean toolbarVisible) {
		this.toolbarVisible = toolbarVisible;
	}
}

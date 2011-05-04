/*
 * (c) Copyright 2001-2005, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.graph;

import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;

import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;
import com.isencia.passerelle.actor.gui.graph.EditorGraphController.ViewFactory;
import com.isencia.passerelle.hmi.HMIMessages;

/**
 * A helper class that reacts to call-backs from Passerelle actor look-inside
 * actions, to open a new view in the IDE. For Netbeans, the views are
 * implemented as ModelGraphTopComponent.
 * 
 * @author erwin dl
 */
public class LookInsideViewFactory implements ViewFactory {

	private final static Log logger = LogFactory
			.getLog(LookInsideViewFactory.class);

	private final TabWindow parentTabPanel;
	private final PtolemyEffigy graphPanelEffigy;

	private final Map<String, ModelGraphPanel> loadedModel = new HashMap<String, ModelGraphPanel>();

	private final Map<String, Integer> openTabs = new HashMap<String, Integer>();

	// private final ViewMap tabViewMap = new ViewMap();

	/**
	 *
	 */
	public LookInsideViewFactory(final TabWindow parentTabPanel,
			final Configuration parentConfiguration,
			final PtolemyEffigy graphPanelEffigy) {
		this.parentTabPanel = parentTabPanel;
		this.graphPanelEffigy = graphPanelEffigy;
		this.parentTabPanel.addListener(new DockingWindowListener() {

			public void viewFocusChanged(final View previouslyFocusedView,
					final View focusedView) {
				// TODO Auto-generated method stub

			}

			public void windowAdded(final DockingWindow addedToWindow,
					final DockingWindow addedWindow) {
				// TODO Auto-generated method stub

			}

			public void windowClosed(final DockingWindow closedWindow) {
				if (openTabs.containsKey(closedWindow.getName())) {
					openTabs.remove(closedWindow.getName());
					System.out.println("Closed window"+closedWindow.getName());
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

			
			public void windowRemoved(final DockingWindow removedFromWindow,
					final DockingWindow removedWindow) {
				if (openTabs.containsKey(removedWindow.getName())) {
					openTabs.remove(removedWindow.getName());
					System.out.println("Removed window"+removedWindow.getName());
				}

			}

			
			public void windowRestored(final DockingWindow window) {
				// TODO Auto-generated method stub

			}

			
			public void windowRestoring(final DockingWindow window)
					throws OperationAbortedException {
				// TODO Auto-generated method stub

			}

			
			public void windowShown(final DockingWindow window) {
			}

			
			public void windowUndocked(final DockingWindow window) {
				// TODO Auto-generated method stub

			}

			
			public void windowUndocking(final DockingWindow window)
					throws OperationAbortedException {
				// TODO Auto-generated method stub

			}

		});
	}

	public void openView(final PtolemyEffigy effigy) {
		if (logger.isTraceEnabled()) {
			logger.trace("entry - effigy :" + effigy);
		}

		// String name = null;
		String fullName = null;
		if (effigy.getModel() != null) {
			// name = effigy.getModel().getName();
			fullName = effigy.getModel().getFullName();
		} else {
			// name = effigy.getName();
			fullName = effigy.getFullName();
		}
		try {
			// if tab for this ModelGraphPanel is already open just focus on it

			if (openTabs.containsKey(fullName)) {
				parentTabPanel.setSelectedTab(openTabs.get(fullName));
			} else {
				ModelGraphPanel panel;

				if (loadedModel.containsKey(fullName)) {
					panel = loadedModel.get(fullName);
				} else {
					panel = new ModelGraphPanel(effigy.getModel(), graphPanelEffigy);
					loadedModel.put(fullName, panel);
					panel.registerViewFactory(this);
				}

				final View tab = new View(
						fullName,
						new ImageIcon(Toolkit.getDefaultToolkit().getImage(
										getClass().getResource("/com/isencia/passerelle/hmi/resources/ModelDataIcon.gif"))),
						panel);

				new EditionActions().addActions(panel);

				tab.getWindowProperties().setCloseEnabled(true);
				tab.setName(fullName);

				parentTabPanel.addTab(tab);
				openTabs.put(fullName, parentTabPanel.getChildWindowCount() - 1);
				parentTabPanel.setSelectedTab(parentTabPanel.getChildWindowCount() - 1);
				parentTabPanel.validate();
			}

		} catch (final Throwable t) {
			logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("exit");
		}
	}
	
	public void closeAllViews() {
		loadedModel.clear();
		int tabCount = parentTabPanel.getChildWindowCount();
		for(int i=0;i<tabCount;++i) {
			parentTabPanel.getChildWindow(i).close();
		}
		openTabs.clear();
	}

}

/* Copyright 2010 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
/* 

 Copyright (c) 1998-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package com.isencia.passerelle.actor.gui.graph;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.actor.gui.SaveInLibraryAction;

import ptolemy.actor.Actor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.actor.ActorInstanceController;
import ptolemy.vergil.actor.ClassDefinitionController;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.actor.IOPortController;
import ptolemy.vergil.actor.LinkController;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.PopupMouseFilter;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.kernel.RelationController;
import ptolemy.vergil.toolbox.MenuItemFactory;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.Site;
import diva.canvas.connector.AutonomousSite;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.CompositeInteractor;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.SelectionDragger;
import diva.graph.GraphPane;
import diva.gui.toolbox.JContextMenu;
import diva.gui.toolbox.MenuCreator;

/**
 * EditorGraphController
 * 
 * TODO: class comment
 * 
 * @author erwin dl
 */
public class EditorGraphController extends ActorEditorGraphController implements ViewOpener{
	
	private final static Logger logger = LoggerFactory.getLogger(EditorGraphController.class);

	// we're keeping track of this for undo/redo support
	// TODO we've got to break the referral cycle between
	// the panel and the controller, as the objects will
	// not be released for garbage collection
	private ModelGraphPanel parentPanel;

	private ViewFactory parentViewFactory;
	
    // The selection interactor for drag-selecting nodes
    private SelectionDragger _selectionDragger;


	/**
	 * 
	 */
	public EditorGraphController(ModelGraphPanel parentPanel) {
		super();
		this.parentPanel = parentPanel;
	}
	
	public ModelGraphPanel getParentPanel() {
		return parentPanel;
	}
	
	/**
	 * This override merges some things we want to keep from
	 * the implementations of a nr of base classes,
	 * and has a nr of things removed which we don't want.
	 * (like all the units stuff, classes stuff etc)
	 */
    protected void initializeInteraction() {
        GraphPane pane = getGraphPane();
        
        // Create and set up the selection dragger
        _selectionDragger = new SelectionDragger(pane);
        _selectionDragger.addSelectionModel(getSelectionModel());
        // If the selectionDragger is consuming, then popup menus don't 
        // disappear properly.
        _selectionDragger.setConsuming(false);


        _menuFactory = new SchematicContextMenuFactory(this);
        _menuCreator = new MenuCreator(_menuFactory);
        _menuCreator.setMouseFilter(new PopupMouseFilter());
            
        _saveInLibraryAction = new SaveInLibraryAction();
        _saveInLibraryAction.setConfiguration(getConfiguration());
        _editPreferencesAction = new EditPreferencesAction(this);
        _editPreferencesAction.setConfiguration(getConfiguration());
        
        _menuFactory.addMenuItemFactory(new CompositeActorMenuItemFactory());

        // Note that the menuCreator cannot be an interactor, because
        // it accepts all events.
        pane.getBackgroundEventLayer().addInteractor(_menuCreator);
        pane.getBackgroundEventLayer().setConsuming(false);
        
        // Create listeners that creates new relations.
        _relationCreator = new RelationCreator();
        _relationCreator.setMouseFilter(_shortcutFilter);

        pane.getBackgroundEventLayer().addInteractor(_relationCreator);
        // Note that shift-click is already bound to the dragSelection
        // interactor when adding things to a selection.

        // Create the interactor that drags new edges.
        _linkCreator = new LinkCreator();
        _linkCreator.setMouseFilter(_shortcutFilter);
        
        // NOTE: Do not use _initializeInteraction() because we are
        // still in the constructor, and that method is overloaded in
        // derived classes.
        ((CompositeInteractor)_portController.getNodeInteractor())
            .addInteractor(_linkCreator);
        ((CompositeInteractor)_entityPortController.getNodeInteractor())
            .addInteractor(_linkCreator);
        ((CompositeInteractor)_relationController.getNodeInteractor())
            .addInteractor(_linkCreator);

        LinkCreator linkCreator2 = new LinkCreator();
        linkCreator2.setMouseFilter(
                new MouseFilter(InputEvent.BUTTON1_MASK, 0));
        ((CompositeInteractor)_entityPortController.getNodeInteractor())
            .addInteractor(linkCreator2);

    }


	/**
	 * @return
	 */
	public AttributeController getAttributeController() {
		return (AttributeController) _attributeController;
	}

	/**
	 * @return
	 */
	public IOPortController getEntityPortController() {
		return (IOPortController) _entityPortController;
	}

	/**
	 * @return
	 */
	public ExternalIOPortController getPortController() {
		return (ExternalIOPortController) _portController;
	}

	/**
	 * Create the controllers for nodes in this graph. In this class,
	 * controllers with FULL access are created. This is called by the
	 * constructor, so derived classes that override this must be careful not to
	 * reference local variables defined in the derived classes, because the
	 * derived classes will not have been fully constructed by the time this is
	 * called.
	 */
	protected void _createControllers() {
		_attributeController = new AttributeController(this, AttributeController.FULL);
		_classDefinitionController = new ClassDefinitionController(this);
		_entityController = new ActorInstanceController(this);
		_entityPortController = new IOPortController(this, AttributeController.FULL);
		_portController = new ExternalIOPortController(this, AttributeController.FULL);
		_relationController = new RelationController(this);
		_linkController = new LinkController(this);
	}

	/**
	 * Specify the factory that is able to open a (new) view for a Ptolemy
	 * effigy, in the specific IDE implementation.
	 * 
	 * @param viewFactory
	 */
	public void registerViewFactory(ViewFactory viewFactory) {
		this.parentViewFactory = viewFactory;
	}
	
    public void event(DebugEvent event) {
        if (event instanceof FiringEvent) {
            Actor actor = ((FiringEvent)event).getActor();
            if (actor instanceof NamedObj) {
                NamedObj objToHighlight = (NamedObj)actor;

                // If the object is not contained by the associated
                // composite, then find an object above it in the hierarchy
                // that is.
                AbstractBasicGraphModel graphModel =
                    (AbstractBasicGraphModel)getGraphModel();
                NamedObj toplevel = graphModel.getPtolemyModel();
                while (objToHighlight != null
                        && objToHighlight.getContainer() != toplevel) {
                    objToHighlight = (NamedObj)objToHighlight.getContainer();
                }
                if (objToHighlight == null) {
                    return;
                }
                Object location = objToHighlight.getAttribute("_location");
                if (location != null) {
                    final Figure figure = getFigure(location);
                    if (figure != null) {
                        if (_animationRenderer == null) {
                            _animationRenderer = new AnimationRenderer();
                        }
                        FiringEvent.FiringEventType type
                            = ((FiringEvent)event).getType();
                        if (type == FiringEvent.BEFORE_FIRE) {
					        _animationRenderer.renderSelected(figure);
					        _animated = figure;
					        long animationDelay = getAnimationDelay();
					        if (animationDelay > 0) {
					            try {
					                Thread.sleep(animationDelay);
					            } catch (InterruptedException ex) {}
					        }
                        } else {
                            if (_animated != null) {
                                _animationRenderer.renderDeselected(_animated);
                            }
                        }
                    }
                }
            }
        }
    }


	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.vergil.actor.ActorEditorGraphController#addToMenuAndToolbar(javax.swing.JMenu,
	 *      javax.swing.JToolBar)
	 */
	public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
		if (menu == null) {
			// need to do it ourselves, as the Ptolemy docs mention that you can
			// pass
			// menu=null, but it's not correctly implemented and we will get
			// NPE in that case by calling super.add...
			toolbar.addSeparator();
			diva.gui.GUIUtilities.addToolBarButton(toolbar, _newInputPortAction);
			diva.gui.GUIUtilities.addToolBarButton(toolbar, _newOutputPortAction);
			// EDL : multiport and composites don't go well together
//			diva.gui.GUIUtilities.addToolBarButton(toolbar, _newInputMultiportAction);
//			diva.gui.GUIUtilities.addToolBarButton(toolbar, _newOutputMultiportAction);
			toolbar.addSeparator();
	        diva.gui.GUIUtilities.addToolBarButton(toolbar, _newRelationAction);
		} else {
			super.addToMenuAndToolbar(menu, toolbar);
		}
	}

	/**
	 * Facade method to hide specific Panel implementation for edit actions on
	 * graph nodes.
	 * 
	 * @param editMsg
	 */
	public void postUndoableEdit(String editMsg) {
		if (parentPanel != null)
			parentPanel.postUndoableEdit(editMsg);
	}

	/**
	 * Facade method to hide specific implementation of IDE-integration
	 * components. For the moment, this is used only for the LookInsideAction on
	 * composite actors.
	 * 
	 * @param model
	 *            the object that should be shown in a new view
	 * @throws Exception
	 *             when there was a problem opening the view
	 */
	public void openView(NamedObj model, Configuration configuration) throws Exception {
		if (parentViewFactory != null) {
			parentViewFactory.openView(_getEffigy(model, configuration));
		} else {
			throw new Exception("No view factory");
		}
	}

	/**
	 * Copied and adapted from ptolemy.actor.gui.Configuration._openModel,
	 * to obtain a correctly initialized and registered effigy 
	 * for the look-inside action.
	 * 
	 * @param entity
	 * @param cfg
	 * @return
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	private PtolemyEffigy _getEffigy(NamedObj entity, Configuration cfg) throws IllegalActionException, NameDuplicationException {
		// Search the model directory for an effigy that already
		// refers to this entity.
		PtolemyEffigy effigy = cfg.getEffigy(entity);
		if (effigy != null) {
			// Found one. 
			return effigy;
		} else {
			// There is no pre-existing effigy. Create one.
			effigy = new PtolemyEffigy(cfg.workspace());
			effigy.setModel(entity);

			// Look to see whether the model has a URIAttribute.
			List attributes = entity.attributeList(URIAttribute.class);
			if (attributes.size() > 0) {
				// The entity has a URI, which was probably
				// inserted by MoMLParser.

				URI uri = ((URIAttribute) attributes.get(0)).getURI();

				// Set the URI and identifier of the effigy.
				effigy.uri.setURI(uri);

				// NOTE: The uri might be null, which results in
				// a null pointer exception below. In particular,
				// the class Effigy always has a URI attribute, but
				// the value might not get set.
				if (uri == null) {
					effigy.identifier.setExpression(_effigyIdentifier(effigy, entity));
				} else {
					effigy.identifier.setExpression(uri.toString());
				}

				// Put the effigy into the directory
				ModelDirectory directory = cfg.getDirectory();
				effigy.setName(directory.uniqueName(entity.getName()));
				effigy.setContainer(directory);
				// Create a default tableau.
				return effigy;
			} else {
				// If we get here, then we are looking inside a model
				// that is defined within the same file as the parent,
				// probably. Create a new PtolemyEffigy
				// and open a tableau for it.

				// Put the effigy inside the effigy of the parent,
				// rather than directly into the directory.
				NamedObj parent = (NamedObj) entity.getContainer();
				PtolemyEffigy parentEffigy = null;
				// Find the first container above in the hierarchy that
				// has an effigy.
				while (parent != null && parentEffigy == null) {
					parentEffigy = cfg.getEffigy(parent);
					parent = (NamedObj) parent.getContainer();
				}
				boolean isContainerSet = false;
				if (parentEffigy != null) {
					// OK, we can put it into this other effigy.
					effigy.setName(parentEffigy.uniqueName(entity.getName()));
					effigy.setContainer(parentEffigy);

					// Set the uri of the effigy to that of
					// the parent.
					effigy.uri.setURI(parentEffigy.uri.getURI());

					// Indicate success.
					isContainerSet = true;
				}
				// If the above code did not find an effigy to put
				// the new effigy within, then put it into the
				// directory directly or the specified container.
				if (!isContainerSet) {
					CompositeEntity directory = cfg.getDirectory();
					effigy.setName(directory.uniqueName(entity.getName()));
					effigy.setContainer(directory);
				}
				effigy.identifier.setExpression(_effigyIdentifier(effigy, entity));

				return effigy;
			}
		}
	}
	
    /** Return an identifier for the specified effigy based on its
     *  container (if any) and its name.
     *  @return An identifier for the effigy.
     */
    private String _effigyIdentifier(Effigy effigy, NamedObj entity) {
        // Set the identifier of the effigy to be that
        // of the parent with the model name appended.
        Effigy parentEffigy=null;
		try {
			parentEffigy = (Effigy)effigy.getContainer();
		} catch (ClassCastException e) {
			// means it's another kind of container, e.g. a Directory
		}
        if (parentEffigy == null) {
            return effigy.getFullName();
        }
        // Note that we add a # the first time, and
        // then add . after that.  So
        // file:/c:/foo.xml#bar.bif is ok, but
        // file:/c:/foo.xml#bar#bif is not
        // If the title does not contain a legitimate
        // way to reference the submodel, then the user
        // is likely to look at the title and use the wrong
        // value if they xml edit files by hand. (cxh-4/02)
        String entityName = parentEffigy.identifier.getExpression();
        String separator = "#";
        if (entityName.indexOf("#") > 0) {
            separator = ".";
        }
        return (entityName + separator + entity.getName());
    }

	/** Action for creating a new input port. */
	private Action _newInputPortAction = new NewPortAction(ExternalIOPortController._GENERIC_INPUT, "New input port", KeyEvent.VK_I);

	/** Action for creating a new output port. */
	private Action _newOutputPortAction = new NewPortAction(ExternalIOPortController._GENERIC_OUTPUT, "New output port",
			KeyEvent.VK_O);

//	/** Action for creating a new input multiport. */
//	private Action _newInputMultiportAction = new NewPortAction(ExternalIOPortController._GENERIC_INPUT_MULTIPORT,
//			"New input multiport", KeyEvent.VK_N);
//
//	/** Action for creating a new output multiport. */
//	private Action _newOutputMultiportAction = new NewPortAction(ExternalIOPortController._GENERIC_OUTPUT_MULTIPORT,
//			"New output multiport", KeyEvent.VK_U);

    protected SaveInLibraryAction _saveInLibraryAction;
    protected EditPreferencesAction _editPreferencesAction;

    /** Action for creating a new relation. */
    private Action _newRelationAction = new NewRelationAction();

    /** The interactors that interactively creates edges. */
    private LinkCreator _linkCreator;  // For control-click
    //   private LinkCreator _linkCreator2;  // For shift-click

    /** The filter for shortcut operations.  This is used for creation
     *  of relations and creation of links from relations. Under PC,
     *  this is a control-1 click.  Under Mac OS X, the control key is
     *  used for context menus and this corresponds to the command-1
     *  click.  For details, see the Apple java archive
     *  http://lists.apple.com/archives/java-dev User: archives,
     *  passwd: archives
     */
    private MouseFilter _shortcutFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

    /** The interactor for creating new relations. */
    private RelationCreator _relationCreator; 

	/**
	 * 
	 * ViewFactory
	 * 
	 * TODO: class comment
	 * 
	 * @author erwin dl
	 */
	public interface ViewFactory {
		void openView(PtolemyEffigy effigy);
	}
	
    ///////////////////////////////////////////////////////////////////
    //// LinkCreator

    /** This class is an interactor that interactively drags edges from
     *  one terminal to another, creating a link to connect them.
     */
    protected class LinkCreator extends AbstractInteractor {

        /** Create a new edge when the mouse is pressed. */
        public void mousePressed(LayerEvent event) {
            Figure source = event.getFigureSource();
            NamedObj sourceObject = (NamedObj) source.getUserObject();

            // Create the new edge.
            Link link = new Link();
            // Set the tail, going through the model so the link is added
            // to the list of links.
            ActorGraphModel model = (ActorGraphModel)getGraphModel();
            model.getLinkModel().setTail(link, sourceObject);

            try {
                // add it to the foreground layer.
                FigureLayer layer =
                    getGraphPane().getForegroundLayer();
                Site headSite, tailSite;

                // Temporary sites.  One of these will get blown away later.
                headSite = new AutonomousSite(layer,
                        event.getLayerX(),
                        event.getLayerY());
                tailSite = new AutonomousSite(layer,
                        event.getLayerX(),
                        event.getLayerY());
                // Render the edge.
                Connector c = getEdgeController(link)
                    .render(link, layer, tailSite, headSite);
                // get the actual attach site.
                tailSite =
                    getEdgeController(link).getConnectorTarget()
                    .getTailSite(c, source,
                            event.getLayerX(),
                            event.getLayerY());
                if (tailSite == null) {
                    throw new RuntimeException("Invalid connector target: " +
                            "no valid site found for tail of new connector.");
                }

                // And reattach the connector.
                c.setTailSite(tailSite);

                // Add it to the selection so it gets a manipulator, and
                // make events go to the grab-handle under the mouse
                getSelectionModel().addSelection(c);
                ConnectorManipulator cm =
                    (ConnectorManipulator) c.getParent();
                GrabHandle gh = cm.getHeadHandle();
                layer.grabPointer(event, gh);
            } catch (Exception ex) {
                MessageHandler.error("Drag connection failed:", ex);
            }
        }
    }

    private class CompositeActorMenuItemFactory implements MenuItemFactory {

        public JMenuItem create(final JContextMenu menu, NamedObj object) {
            JMenuItem retv = null;
            
            if (object != null && object instanceof CompositeEntity) {
                retv = menu.add(_saveInLibraryAction,"Save In Library");
                retv = menu.add(_editPreferencesAction,"Show/hide parameters");
            }
            return retv;
        }
        
    }
    

}

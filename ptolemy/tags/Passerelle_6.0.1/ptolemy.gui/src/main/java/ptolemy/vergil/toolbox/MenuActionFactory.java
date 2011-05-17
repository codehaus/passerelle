/* A menu item factory that creates actions for firing actions

 Copyright (c) 2000-2006 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.kernel.util.NamedObj;
import diva.gui.toolbox.JContextMenu;

//////////////////////////////////////////////////////////////////////////
//// MenuActionFactory

/**
 A factory that adds a given action or set of actions
 to a context menu. If an array of actions is given to
 the constructor, then the actions will be put in a submenu
 with the specified label.

 @author Steve Neuendorffer
 @version $Id: MenuActionFactory.java,v 1.28 2006/09/21 15:40:12 cxh Exp $
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class MenuActionFactory implements MenuItemFactory {
    /** Construct a factory that adds a given action to a given context menu.
     *  @param action The action to be associated with the context menu.
     */
    public MenuActionFactory(Action action) {
        _action = action;
    }

    /** Construct a factory that adds a given group of actions
     *  to a given context menu in a submenu with the specified label.
     *  @param actions The actions to be in the submenu.
     *  @param label The label for the submenu.
     */
    public MenuActionFactory(Action[] actions, String label) {
        _actions = actions;
        _label = label;
    }

    /** Add an action to the pre-existing group of actions.
     *  If this was constructed with the single argument, then this
     *  converts the menu action into a submenu with the specified label.
     *  @param action The action to add.
     *  @param label The label to give to the menu group.
     */
    public void addAction(Action action, String label) {
        if (_action != null) {
            // Previously, there was only one action.
            // Convert to a subaction.
            _actions = new Action[2];
            _actions[0] = _action;
            _actions[1] = action;
            _action = null;
        } else {
            // Create a new actions array.
            Action[] newActions = new Action[_actions.length + 1];
            System.arraycopy(_actions, 0, newActions, 0, _actions.length);
            newActions[_actions.length] = action;
            _actions = newActions;
        }
        _label = label;
    }

    /** Add a set of action to the pre-existing group of actions.
     *  If this was constructed with the single argument, then this
     *  converts the menu action into a submenu with the specified label.
     *  @param actions The actions to add.
     *  @param label The label to give to the menu group if it
     *   previously not a menu group.
     */
    public void addActions(Action[] actions, String label) {
        int start = 0;
        if (_action != null) {
            // Previously, there was only one action.
            // Convert to a subaction.
            _actions = new Action[actions.length + 1];
            _actions[0] = _action;
            start = 1;
            _action = null;
        } else {
            Action[] newActions = new Action[_actions.length + actions.length];
            System.arraycopy(_actions, 0, newActions, 0, _actions.length);
            start = _actions.length;
            _actions = newActions;
        }
        System.arraycopy(actions, 0, _actions, start, actions.length);
        _label = label;
    }

    /** Add an item to the given context menu that will configure the
     *  parameters on the given target.
     */
    public JMenuItem create(JContextMenu menu, NamedObj object) {
        if (_action != null) {
            // Single action as a simple menu entry.
            return menu.add(_action, (String) _action.getValue(Action.NAME));
        } else {
            // Requested a submenu with a group of actions.
            final JMenu submenu = new JMenu(_label);
            menu.add(submenu, _label);
            for (int i = 0; i < _actions.length; i++) {
                submenu.add(_actions[i]);
            }
            return submenu;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The action that will be added to the context menu. */
    private Action _action;

    /** The group of actions that will be added in a submenu. */
    private Action[] _actions;

    /** The submenu label, if one was given. */
    private String _label;
}

/* An instance of FunctionDependencyOfModalModel describes the function
 dependency information between the outputs and intputs of a modal model.

 Copyright (c) 2004-2006 The Regents of the University of California.
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
package ptolemy.domains.fsm.modal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependencyOfModalModel

/** An instance of FunctionDependencyOfModalModel describes the function
 dependency information between the outputs and inputs of a modal model.
 Depending on the value of conservativeAnalysis parameter of the modal
 model that containing this attribute, either optimimistic (dynamic)
 calculation or conservation approximation of the function dependencies is
 performed.

 @see ptolemy.actor.util.FunctionDependencyOfCompositeActor
 @author Haiyang Zheng
 @version $Id: FunctionDependencyOfModalModel.java,v 1.36 2007/12/07 06:27:32 cxh Exp $
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class FunctionDependencyOfModalModel extends
        FunctionDependencyOfCompositeActor {
    /** Construct a FunctionDependency in the given actor.
     *  The name of this attribute is always "_functionDependency".
     *  @param compositeActor The associated actor.
     *  @exception NameDuplicationException If the container already contains
     *  an entity with the specified name.
     *  @exception IllegalActionException If the name has a period in it, or
     *  the attribute is not compatible with the specified container.
     */
    public FunctionDependencyOfModalModel(CompositeActor compositeActor)
            throws IllegalActionException, NameDuplicationException {
        super(compositeActor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a dependency loop in the dependency graph of this
     *  FunctionDependency object, return the nodes in the
     *  dependency loop. If there are multiple loops, the nodes in all the
     *  loops will be returned. If there is no loop, return an empty array.
     *  The elements of the returned array are instances of IOPort.
     *  Note that the returned result is actually an object array instead
     *  of an array of IOPorts.
     *  @param flag True if we are to perform a conservative approximation
     *  in {@link #_getEntities()}.
     */
    public void setConservativeAnalysis(boolean flag) {
        _conservativeAnalysis = flag;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get a list of refinements of the current state for function
     *  dependency calculation.
     *  @return A list of refinements associated with the current state.
     */
    protected List _getEntities() {
        LinkedList entities = new LinkedList();

        try {
            FSMActor controller = ((ModalModel) getContainer()).getController();

            if (_conservativeAnalysis) {
                // Conservative approximation
                Iterator states = controller.entityList(State.class).iterator();
                while (states.hasNext()) {
                    State state = (State) states.next();
                    Actor[] actors = state.getRefinement();
                    // If the modal model has no refinements, the modal model is
                    // basically an FSM actor. We use the function dependency of
                    // the controller instead.
                    if ((actors != null) && (actors.length > 0)) {
                        for (int i = 0; i < actors.length; ++i) {
                            if (!entities.contains(actors[i])) {
                                entities.add(actors[i]);
                            }
                        }
                    }
                }
            } else {
                // Optimistic approximation
                Actor[] actors = controller.currentState().getRefinement();
                // If the modal model has no refinements, the modal model is
                // basically an FSM actor. We use the function dependency of
                // the controller instead.
                if ((actors != null) && (actors.length > 0)) {
                    for (int i = 0; i < actors.length; ++i) {
                        entities.add(actors[i]);
                    }
                } else {
                    entities.add(controller);
                }
            }
        } catch (IllegalActionException e) {
            MessageHandler.error("Invalid refinements.", e);
        }
        return entities;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    boolean _conservativeAnalysis = true;
}

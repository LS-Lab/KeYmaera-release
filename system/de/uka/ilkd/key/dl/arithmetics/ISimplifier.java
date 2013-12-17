/***************************************************************************
 *   Copyright (C) 2007 by Jan-David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.ArrayList;

import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;

/**
 * An {@link ISimplifier} is a {@link IMathSolver} that is capable of formula simplification.
 * 
 * Implementations can be accessed using the {@link MathSolverManager}.
 * 
 * @author jdq
 * @since Aug 17, 2007
 * 
 */
public interface ISimplifier extends IMathSolver {
    /**
     * Simplifies the given term if its a mathematical expression
     * 
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     * @throws UnableToConvertInputException
     * 
     */
    public abstract Term simplify(Term form, NamespaceSet nss) throws RemoteException,
            SolverException;

    /**
     * Simplifies the given term if its a mathematical expression
     * 
     */
    public abstract Term simplify(Term form, Set<Term> assumptions, NamespaceSet nss)
            throws RemoteException, SolverException;

    /**
     * Computes parity decomposition of the formula and outputs an equivalent formula
     * in which all atomic terms are square-free.
     * 
     */
    public abstract Term parityNF(Term form, NamespaceSet nss)
            throws RemoteException, SolverException;
    
    /**
     * @author s0805753@sms.ed.ac.uk
     * 
     * Computes the boundary of a closed semi-algebraic set
     * 
     * N.B. presently can only handle formulas of the form p { <=, =, >= } 0
     * where p is square-free, otherwise the method returns the set itself and
     * the overall effect is equivalent to standard differential induction.
     * 
     */
    public abstract Term getBoundary(Term form, NamespaceSet nss)
            throws RemoteException, SolverException;

    
    /**
     * @author s0805753@sms.ed.ac.uk
     * 
     * Computes a conjunctive description of a quantifier-free formula in which
     * all predicate symbols are '<=', if such a description is possible.
     * 
     * N.B. equations '==' are <b>not</b> converted to '<='.
     * 
     */
    public abstract Term toLessEqualConjunct(Term form, NamespaceSet nss)
            throws RemoteException, SolverException;
      
    /**
     * @author s0805753@sms.ed.ac.uk
     * 
     * Checks if the formula is a conjunction of atoms where 
     * all predicate symbols are '<='.
     * 
     * N.B. equations '==' are <b>not</b> converted to '<='.
     * 
     */
    public abstract boolean isLessEqualConjunct(Term form, NamespaceSet nss)
            throws RemoteException, SolverException;
    
    
    /**
     * @author s0805753@sms.ed.ac.uk
     *      
     * Computes the condition ensuring the gradient vector is non-zero
     * 
     * N.B. presently can only handle formulas of the form p { <=, =, >= } 0
     * where p is square-free, otherwise the method returns the set itself and
     * the overall effect is equivalent to standard differential induction.
     * 
     */
    public abstract Term nonZeroGrad(Term form, ArrayList<String> vars, NamespaceSet nss)
            throws RemoteException, SolverException;
    
    
    /**
     * @author s0805753@sms.ed.ac.uk
     *      
     * Computes the verification conditions for non-smooth barrier certificates.
     * 
     * N.B. presently can only handle atoms of the form p { <=, >= } 0.
     * 
     */
    
    public abstract Term getVCs(Term form, Term chi, ArrayList<Term> vectorField, ArrayList<String> stateVars, NamespaceSet nss)
            throws RemoteException, SolverException;
    
    /**
     * Fully simplifies the given term if its a mathematical expression
     * 
     */
    public abstract Term fullSimplify(Term form, NamespaceSet nss) throws RemoteException,
            SolverException;

}

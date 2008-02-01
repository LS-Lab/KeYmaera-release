/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
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
import java.util.List;

import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;

/**
 * This class encapsulates {@link IMathSolver} that are capable of applying
 * quantifier elimination.
 * 
 * The implementations can be accessed using the {@link MathSolverManager}.
 * 
 * @author jdq
 * @since Aug 17, 2007
 * 
 */
public interface IQuantifierEliminator extends IMathSolver {

    /**
     * This class represents a quantified variable together with the type of
     * quantification.
     * 
     * @author jdq
     * @since Aug 31, 2007
     * 
     */
    public class PairOfTermAndQuantifierType {
        /**
         * @param t
         * @param forall
         */
        public PairOfTermAndQuantifierType(Term term, QuantifierType type) {
            this.term = term;
            this.type = type;
        }

        public Term term;

        public QuantifierType type;
    }

    public static enum QuantifierType {
        FORALL, EXISTS;
    }

    /**
     * Reduces the given term if its a mathematical expression
     * 
     * @throws UnableToConvertInputException
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     * 
     */
    public abstract Term reduce(Term form, NamespaceSet nss)
            throws RemoteException, SolverException;

    public abstract Term reduce(Term form, NamespaceSet nss, long timeout)
            throws RemoteException, SolverException;

    // /**
    // * Reduces the given term if its a mathematical expression
    // *
    // */
    // public abstract Term reduce(Term form, List<String> names)
    // throws RemoteException;

    // /**
    // * Reduces the given term if its a mathematical expression
    // *
    // */
    // public abstract Term reduce(Term form, Term quantifedSymbol,
    // QuantifierType type) throws RemoteException;

    /**
     * Reduces the given term if its a mathematical expression
     * 
     */
    public abstract Term reduce(Term form, List<String> names,
            List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
            throws RemoteException, SolverException;

    public abstract Term reduce(Term form, List<String> names,
            List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
            long timeout) throws RemoteException, SolverException;

    /**
     * Reduces the given term if its a mathematical expression
     * 
     * @param query
     * @param orderedList
     * @param singleton
     * @return
     */
    public abstract Term reduce(Term query,
            List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
            throws RemoteException, SolverException;

    public abstract Term reduce(Term query,
            List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
            long timeout) throws RemoteException, SolverException;
}

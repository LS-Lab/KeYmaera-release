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
package de.uka.ilkd.key.dl.gui;

import java.util.HashMap;
import java.util.Map;

import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.impl.AndImpl;
import de.uka.ilkd.key.dl.model.impl.AssignImpl;
import de.uka.ilkd.key.dl.model.impl.BiimpliesImpl;
import de.uka.ilkd.key.dl.model.impl.ChoiceImpl;
import de.uka.ilkd.key.dl.model.impl.ChopImpl;
import de.uka.ilkd.key.dl.model.impl.DivImpl;
import de.uka.ilkd.key.dl.model.impl.EqualsImpl;
import de.uka.ilkd.key.dl.model.impl.ExpImpl;
import de.uka.ilkd.key.dl.model.impl.FreeFunctionImpl;
import de.uka.ilkd.key.dl.model.impl.FreePredicateImpl;
import de.uka.ilkd.key.dl.model.impl.GreaterEqualsImpl;
import de.uka.ilkd.key.dl.model.impl.GreaterImpl;
import de.uka.ilkd.key.dl.model.impl.ImpliesImpl;
import de.uka.ilkd.key.dl.model.impl.LessEqualsImpl;
import de.uka.ilkd.key.dl.model.impl.LessImpl;
import de.uka.ilkd.key.dl.model.impl.MinusImpl;
import de.uka.ilkd.key.dl.model.impl.MinusSignImpl;
import de.uka.ilkd.key.dl.model.impl.MultImpl;
import de.uka.ilkd.key.dl.model.impl.NotImpl;
import de.uka.ilkd.key.dl.model.impl.OrImpl;
import de.uka.ilkd.key.dl.model.impl.ParallelImpl;
import de.uka.ilkd.key.dl.model.impl.PlusImpl;
import de.uka.ilkd.key.dl.model.impl.QuestImpl;
import de.uka.ilkd.key.dl.model.impl.RandomAssignImpl;
import de.uka.ilkd.key.dl.model.impl.StarImpl;
import de.uka.ilkd.key.dl.model.impl.UnequalsImpl;
import de.uka.ilkd.key.java.ProgramElement;

/**
 * PresentationInfo is used to store informations about DLProgramElements. These
 * informations are used by the PrettyPrinter to decide how to print the element
 * (which symbol to use, infix/prefix/postfix notation, partheses or not...)
 * 
 * @author jdq
 * 
 */
public class PresentationInfo {

    public static enum Fix {
        PREFIX, INFIX, POSTFIX
    }

    public static enum Associativity {
        LEFT, RIGHT, NONE
    }

    public static enum LineBreak {
        BEFORE, AFTER, WITHIN, NONE
    }

    public static class PresentationInfoEntry implements
            Comparable<PresentationInfoEntry> {

        private int priority;

        private String symbol;

        private Associativity leftAssociative;

        private Fix fix;

        private LineBreak lineBreak;

        private boolean bracesOnEqualPriority;

        /**
         * @param priority
         *                priority of the operator. Priority has to be high for
         *                strong opperators
         * @param symbol
         * @param infix
         * @param bracesOnEqualPriority
         */
        private PresentationInfoEntry(int priority, String symbol,
                Associativity leftAssociative, Fix fix, LineBreak lineBreak,
                boolean bracesOnEqualPriority) {
            super();
            this.priority = priority;
            this.symbol = symbol;
            this.leftAssociative = leftAssociative;
            this.fix = fix;
            this.lineBreak = lineBreak;
            this.bracesOnEqualPriority = bracesOnEqualPriority;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(PresentationInfoEntry o) {
            return priority - o.priority;
        }

        /**
         * @return the bracesOnEqualPriority
         */
        public boolean isBracesOnEqualPriority() {
            return bracesOnEqualPriority;
        }

        /**
         * @return the fix
         */
        public Fix getFix() {
            return fix;
        }

        /**
         * @return the leftAssociative
         */
        public Associativity getLeftAssociative() {
            return leftAssociative;
        }

        /**
         * @return the priority
         */
        public int getPriority() {
            return priority;
        }

        /**
         * @return the symbol
         */
        public String getSymbol() {
            return symbol;
        }

        /**
         * @return the lineBreak
         */
        public LineBreak getLineBreak() {
            return lineBreak;
        }

    }

    public static final PresentationInfo INSTANCE = new PresentationInfo();

    private Map<Class<? extends ProgramElement>, PresentationInfoEntry> presentationMap;

    private PresentationInfo() {
        presentationMap = new HashMap<Class<? extends ProgramElement>, PresentationInfoEntry>();
        generateDefaultFeatures();
    }

    /**
     * 
     */
    private void generateDefaultFeatures() {

        addEntry(PlusImpl.class, 100, "+", Associativity.LEFT, Fix.INFIX,
                LineBreak.NONE, false);
        addEntry(MinusImpl.class, 100, "-", Associativity.LEFT, Fix.INFIX,
                LineBreak.NONE, false);
        addEntry(MultImpl.class, 200, "*", Associativity.LEFT, Fix.INFIX,
                LineBreak.NONE, false);
        addEntry(DivImpl.class, 210, "/", Associativity.LEFT, Fix.INFIX,
                LineBreak.NONE, false);
        addEntry(ExpImpl.class, 300, "^", Associativity.RIGHT, Fix.INFIX,
                LineBreak.NONE, true);
        addEntry(MinusSignImpl.class, 1000, "-", Associativity.LEFT,
                Fix.PREFIX, LineBreak.NONE, false);

        addEntry(FreeFunctionImpl.class, 2000, null, Associativity.RIGHT,
                Fix.PREFIX, LineBreak.NONE, true);

        addEntry(LessImpl.class, 50, "<", Associativity.RIGHT, Fix.INFIX,
                LineBreak.NONE, true);
        addEntry(LessEqualsImpl.class, 50, "<=", Associativity.RIGHT,
                Fix.INFIX, LineBreak.NONE, true);
        addEntry(EqualsImpl.class, 50, "=", Associativity.RIGHT, Fix.INFIX,
                LineBreak.NONE, true);
        addEntry(UnequalsImpl.class, 50, "!=", Associativity.RIGHT, Fix.INFIX,
                LineBreak.NONE, true);
        addEntry(GreaterEqualsImpl.class, 50, ">=", Associativity.RIGHT,
                Fix.INFIX, LineBreak.NONE, true);
        addEntry(GreaterImpl.class, 50, ">", Associativity.RIGHT, Fix.INFIX,
                LineBreak.NONE, true);

        addEntry(FreePredicateImpl.class, 2000, null, Associativity.RIGHT,
                Fix.PREFIX, LineBreak.NONE, true);

        addEntry(OrImpl.class, 10, "|", Associativity.LEFT, Fix.INFIX,
                LineBreak.NONE, false);
        addEntry(AndImpl.class, 15, "&", Associativity.LEFT, Fix.INFIX,
                LineBreak.NONE, false);
        addEntry(ImpliesImpl.class, 20, "->", Associativity.LEFT, Fix.INFIX,
                LineBreak.NONE, false);
        addEntry(BiimpliesImpl.class, 25, "<->", Associativity.LEFT, Fix.INFIX,
                LineBreak.NONE, false);
        addEntry(NotImpl.class, 30, "!", Associativity.LEFT, Fix.PREFIX,
                LineBreak.NONE, false);

        addEntry(QuestImpl.class, 400, "?", Associativity.RIGHT, Fix.PREFIX,
                LineBreak.NONE, false);
        addEntry(AssignImpl.class, 400, ":=", Associativity.NONE, Fix.INFIX,
                LineBreak.NONE, false);
        addEntry(RandomAssignImpl.class, 400, ":=*", Associativity.NONE,
                Fix.POSTFIX, LineBreak.NONE, false);

        addEntry(ParallelImpl.class, 200, "||", Associativity.LEFT, Fix.INFIX,
                LineBreak.AFTER, false);
        addEntry(ChopImpl.class, 300, ";", Associativity.LEFT, Fix.INFIX,
                LineBreak.AFTER, false);
        addEntry(ChoiceImpl.class, 350, "++", Associativity.LEFT, Fix.INFIX,
                LineBreak.WITHIN, false);
        addEntry(StarImpl.class, 300, "*", Associativity.RIGHT, Fix.POSTFIX,
                LineBreak.NONE, true);
    }

    private void addEntry(Class<? extends ProgramElement> class_, int priority,
            String symbol, Associativity associativity, Fix fix,
            LineBreak lineBreak, boolean bracesOnEqualPriority) {
        presentationMap.put(class_, new PresentationInfoEntry(priority, symbol,
                associativity, fix, lineBreak, bracesOnEqualPriority));
    }

    public PresentationInfoEntry getEntry(ProgramElement pe) {

        PresentationInfoEntry presentationInfoEntry = presentationMap.get(pe
                .getClass());
        if (presentationInfoEntry == null
                && (pe instanceof FunctionTerm || pe instanceof PredicateTerm)) {
            presentationInfoEntry = getEntry(((DLNonTerminalProgramElement) pe)
                    .getChildAt(0));
        }
        return presentationInfoEntry;
    }
}

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
package de.uka.ilkd.key.dl.transitionmodel;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import de.uka.ilkd.key.dl.model.ProgramVariable;

public class DependencyState {

    private LinkedHashMap<ProgramVariable, LinkedHashSet<ProgramVariable>> dependencyMap;

    private LinkedHashMap<ProgramVariable, Boolean> writeBeforeReadList;

    public DependencyState() {
        dependencyMap = new LinkedHashMap<ProgramVariable, LinkedHashSet<ProgramVariable>>();
        writeBeforeReadList = new LinkedHashMap<ProgramVariable, Boolean>();
    }

    public DependencyState(DependencyState copy) {
        this();
        this.dependencyMap.putAll(copy.dependencyMap);
        this.writeBeforeReadList.putAll(copy.writeBeforeReadList);
    }

    public void mergeWith(Collection<DependencyState> states) {
        for (DependencyState s : states) {
            addDependencies(s.dependencyMap);
            for (ProgramVariable p : s.writeBeforeReadList.keySet()) {
                if ((s.writeBeforeReadList.get(p) != null && s.writeBeforeReadList
                        .get(p) == false)
                        || (writeBeforeReadList.get(p) != null && writeBeforeReadList
                                .get(p) == false)) {
                    writeBeforeReadList.put(p, false);
                } else if ((s.writeBeforeReadList.get(p) != null && s.writeBeforeReadList
                        .get(p) == true)
                        || (writeBeforeReadList.get(p) != null && writeBeforeReadList
                                .get(p) == true)) {
                    writeBeforeReadList.put(p, true);
                } else {
                    writeBeforeReadList.remove(p);
                }
            }
        }
    }

    /**
     * Add all variable dependencies given by the Map parameter
     * 
     * @param dependencies
     */
    public void addDependencies(
            Map<ProgramVariable, LinkedHashSet<ProgramVariable>> dependencies) {
        for (ProgramVariable var : dependencies.keySet()) {
            if (dependencyMap.containsKey(var)) {
                dependencyMap.get(var).addAll(dependencies.get(var));
            } else {
                dependencyMap.put(var, dependencies.get(var));
            }
        }
    }

    /**
     * Get the variable dependency relation.
     * 
     * @return a map where get(x) is the set of all variables on which the value of x depends during the execution of the hybrid program.
     */
    public LinkedHashMap<ProgramVariable, LinkedHashSet<ProgramVariable>> getDependencies() {
        return dependencyMap;
    }

    /**
     * @return the writeBeforeReadList
     */
    public LinkedHashMap<ProgramVariable, Boolean> getWriteBeforeReadList() {
        return writeBeforeReadList;
    }
}
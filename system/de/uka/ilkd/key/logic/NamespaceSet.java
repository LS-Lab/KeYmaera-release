// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.logic;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class NamespaceSet {

    private Namespace varNS=new Namespace();
    private Namespace progVarNS=new Namespace();
    private Namespace funcNS=new Namespace();
    private Namespace ruleSetNS=new Namespace();
    private Namespace sortNS=new Namespace();
    private Namespace choiceNS=new Namespace();
    
    private LinkedHashMap<String, Integer> currentCounter = new LinkedHashMap<String, Integer>();

    public NamespaceSet() {
    }

    public NamespaceSet(Namespace varNS, Namespace funcNS, 
                        Namespace sortNS, Namespace ruleSetNS,
			Namespace choiceNS, Namespace programVarNS) {
	this.varNS=varNS;
	this.progVarNS = programVarNS;
	this.funcNS=funcNS;
	this.sortNS=sortNS;
	this.ruleSetNS=ruleSetNS;
	this.choiceNS=choiceNS;
    }

    public Namespace variables() {
	return varNS;
    }

    public void setVariables(Namespace varNS) {
	this.varNS=varNS;
    }

    public Namespace programVariables() {
	return progVarNS;
    }

    public void setProgramVariables(Namespace progVarNS) {
	this.progVarNS=progVarNS;
    }

    public Namespace functions() {
	return funcNS;
    }

    public void setFunctions(Namespace funcNS) {
	this.funcNS=funcNS;
    }


    public Namespace ruleSets() {
	return ruleSetNS;
    }

    public void setRuleSets(Namespace ruleSetNS) {
	this.ruleSetNS=ruleSetNS;
    }


    public Namespace sorts() {
	return sortNS;
    }

    public void setSorts(Namespace sortNS) {
	this.sortNS=sortNS;
    }

    public Namespace choices() {
	return choiceNS;
    }

    public void setChoices(Namespace choiceNS) {
	this.choiceNS=choiceNS;
    }

    public void add(NamespaceSet ns) {
	variables().add(ns.variables());
	programVariables().add(ns.programVariables());
	sorts().add(ns.sorts());
	ruleSets().add(ns.ruleSets());
	functions().add(ns.functions());
	choices().add(ns.choices());
    }

    public NamespaceSet copy() {
	NamespaceSet c = new NamespaceSet();
	c.setSorts(sorts().copy());
	c.setRuleSets(ruleSets().copy());
	c.setFunctions(functions().copy());
	c.setVariables(variables().copy());
	c.setProgramVariables(programVariables().copy());
	c.setChoices(choices().copy());
	c.currentCounter.putAll(currentCounter);
	return c;
    }
    
    public boolean equalContent(NamespaceSet nss) {
        return sorts().equalContent(nss.sorts())
            && ruleSets().equalContent(nss.ruleSets())
            && functions().equalContent(nss.functions())
            && variables().equalContent(nss.variables())
            && programVariables().equalContent(nss.programVariables())
            && choices().equalContent(nss.choices());
    }
    
    /**
     * starts the protocol of all contained namespaces
     */
    public void startProtocol() {
	variables().startProtocol();
	programVariables().startProtocol();
	sorts().startProtocol();
	ruleSets().startProtocol();
	functions().startProtocol();
	choices().startProtocol();        
    }
       
    /**
     * returns all namespaces in an array     
     */
    private Namespace[] asArray() {
        return new Namespace[]{
                variables(), programVariables(), 
                sorts(), ruleSets(), functions(),
                choices()
        };
    }
    
    /**
     * returns all namespaces with symbols that may occur
     * in a real sequent (this means all namespaces without
     * variables, choices and ruleSets)      
     */
    private Namespace[] logicAsArray() {
        return new Namespace[]{
           programVariables(), sorts(), functions()
        };
    }
    
    /**
     * adds the protocolled names of the given NamespaceSet to this one    
     */
    public void addProtocolled(NamespaceSet nss) {
      final Namespace[] myNames = asArray();
      final Namespace[] otherNames = nss.asArray();
      for (int i = 0; i<myNames.length; i++) {
          final Iterator<Named> it = otherNames[i].getProtocolled();
          while (it.hasNext()) {
              myNames[i].add(it.next());
          }
      }       
    }

    /**
     * looks up if the given name is found in one of the namespaces
     * and returns the named object or null if no object with the same name 
     * has been found
     */
    public Named lookup(Name name) {
	final Namespace[] spaces = asArray();
	return lookup(name, spaces);
    }

    /**
     * looks up for the symbol in the namespaces sort, functions and
     * programVariables
     * @param name the Name to look up
     * @return the element of the given name or null
     */
    public Named lookupLogicSymbol(Name name) {        
        return lookup(name, logicAsArray());
    }
    
    /**
     * @param name
     * @param spaces
     * @return the element with the given name if found in the 
     * given namespaces, otherwise <tt>null</tt>
     */
    private Named lookup(Name name, final Namespace[] spaces) {
        for (Namespace space : spaces) {
            final Named n = space.lookup(name);
            if (n != null) return n;
        } 
        return null;
    }
    
    /**
     * Get a unique name within the _current_ namespaces
     * 
     * WARNING: Note that if you need multiple names, you need to add the object for
     * which you generated the name first. Otherwise, you might get the same
     * name twice!
     * 
     * @param prefix
     * @return
     */
    public String getUniqueName(String prefix) {
        return getUniqueName(prefix, false);
    }
    
    /**
     * Get a unique name within the _current_ namespaces
     * 
     * WARNING: Note that if you need multiple names, you need to add the object for
     * which you generated the name first. Otherwise, you might get the same
     * name twice!
     * 
     * @param prefix
     * @return
     */
    public String getUniqueName(String prefix, boolean increaseIndex) {
        if (prefix == null || prefix.equals("")) {
            prefix = "_var";
        }
        String pref = prefix;
        String result = prefix;
        Named n = lookup(new Name(result));
        int i = 0;
        if(currentCounter.containsKey(prefix)) {
            i = currentCounter.get(prefix);
        }
        if (increaseIndex && pref.contains("_")) {
            try {
                i = Integer.parseInt(pref.substring(pref.lastIndexOf('_') + 1));
                i++;
                pref = pref.substring(0, pref.lastIndexOf('_') + 1);
            } catch (NumberFormatException e) {
                pref = pref + "_";
            }
        } else {
            pref = pref + "_";
        }
        while (n != null) {
            result = pref + i++;
            n = lookup(new Name(result));
        }
        currentCounter.put(prefix, i);
        return result;
    }

    public String toString() {
	return "Sorts: "+sorts()+"\n"+
	    "Functions: "+functions()+"\n"+
	    "Variables: "+variables()+"\n"+
	    "ProgramVariables: "+programVariables()+"\n"+
	    "Heuristics: "+ruleSets()+"\n"+
	    "Taclet Options: "+choices()+"\n";
    }

  

}

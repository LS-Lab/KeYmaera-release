// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//
package de.uka.ilkd.key.smt.taclettranslation;

import javax.jws.soap.SOAPBinding.Use;

/**
 * TreeItem represents the user data in a tree model.
 * 
 */
public class TreeItem {
    public enum SelectionMode {all,nothing,user};
    private String text;

    private SelectionMode mode = SelectionMode.nothing;
    private int selectedChildCount = 0;
    private int childCount = 0;
    private int genericCount =0;
    private UsedTaclets.Category category = UsedTaclets.Category.NO_CATEGORY;
    

    TreeItem(String text, int genericCount){
	this.text = text;
	this.genericCount = genericCount;
    }

    TreeItem(String text, UsedTaclets.Category cat){
	this.text = text;
	this.category = cat;
    }
    
    TreeItem(String text, boolean checked){
	this.text = text;

    
    }
    
    
    
    public UsedTaclets.Category getCategory() {
        return category;
    }

    public int getGenericCount(){
	return genericCount;
    }
    
    public int getSelectedChildCount() {
        return selectedChildCount;
    }

    public void setSelectedChildCount(int selectedChildCount) {
        this.selectedChildCount = selectedChildCount;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public SelectionMode getMode() {
        return mode;
    }

    public void setMode(SelectionMode mode) {
        this.mode = mode;
    }


    public String toComplexString(){
	return mode.name()+";"+category.name()+";"+text;
    }

    public String toString(){
	return text;
    }
    
    public int hashCode(){
	return text.hashCode();
    }
    

    
    
    
    
    
}
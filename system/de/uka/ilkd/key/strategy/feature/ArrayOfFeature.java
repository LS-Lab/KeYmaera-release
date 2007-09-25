

package de.uka.ilkd.key.strategy.feature;
/** wraps an array of Features to achieve immutability */

import java.util.List;

public class ArrayOfFeature  implements java.io.Serializable {

    private final Feature[] arr; private int hashCode;

    /** creates an empty new FeatureArray
     */
    public ArrayOfFeature() {
	this.arr = new Feature[0];
    }

    /** creates a new FeatureArray
     * @param arr the ProgrammElement array to wrap
     */
    public ArrayOfFeature(Feature[] arr) {
	this.arr = new Feature[arr.length]; System.arraycopy(arr,0,this.arr,0,arr.length);
    }


    /** creates a new FeatureArray with one element
     * @param el the Feature that is put into the array
     */
    public ArrayOfFeature(Feature el) {
	this.arr = new Feature[]{el};
    }

    /** creates a new FeatureArray
     * @param list a LinkedList (order is preserved)
     */
    public ArrayOfFeature(List list) {
	this.arr = (Feature[])list.toArray(new Feature[list.size()]);
    }


    /** gets the element at the specified position
     * @param pos an int describing the position
     * @return the element at pos
     */
    public final Feature getFeature(int pos) {
	return arr[pos];
    }

    /** 
     * returns the last element of the array
     * @return the element at position size() - 1
     */
    public final Feature lastFeature() {
	return getFeature(size() - 1);
    }


    /** @return size of the array */
    public int size() {
	return arr.length;
    }

    public void arraycopy(int srcIdx, Object dest, int destIndex, int length) { System.arraycopy(this.arr, srcIdx, dest, destIndex, length); }

    public int hashCode() { if (hashCode == 0) { for (int i = 0; i<arr.length; i++) { hashCode += 17*arr[i].hashCode();} hashCode = ((hashCode == 0) ? -1 : hashCode);} return hashCode; }
    
    public boolean equals(Object o) { if (!(o instanceof ArrayOfFeature)) return false; ArrayOfFeature cmp = (ArrayOfFeature)o; if (cmp.size() != size()) return false; for (int i = 0; i<arr.length; i++) { if (!arr[i].equals(cmp.arr[i])) { return false;} } return true;  }
 
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("[");
        for (int i = 0; i<size(); i++) { 
		sb.append(""+getFeature(i));
		if (i<size()-1) sb.append(",");
	}
	sb.append("]");
	return sb.toString();
    }
    
}

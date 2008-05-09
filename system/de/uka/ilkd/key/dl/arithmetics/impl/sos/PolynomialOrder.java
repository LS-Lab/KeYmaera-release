/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.sos;

import java.util.Set;

import orbital.math.Polynomial;

/**
 * @author jdq
 * 
 */
public interface PolynomialOrder {

	public Polynomial getNext();

	public void setMaxDegree(int degree);

	public boolean hasNext();

	public void setF(Set<Polynomial> f);

	public void setG(Set<Polynomial> g);

	public void setH(Set<Polynomial> h);
}

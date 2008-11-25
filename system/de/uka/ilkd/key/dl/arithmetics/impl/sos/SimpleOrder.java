/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.sos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import orbital.algorithm.Combinatorical;
import orbital.math.Polynomial;
import orbital.math.Values;

/**
 * @author jdq
 * 
 */
public class SimpleOrder implements PolynomialOrder {

	public static class PolynomialComparator implements Comparator<Polynomial> {

		public static final PolynomialComparator INSTANCE = new PolynomialComparator();

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		/*@Override*/
		public int compare(Polynomial o1, Polynomial o2) {
			return o1.degree().intValue() - o2.degree().intValue();
		}

	}

	private ArrayList<Polynomial> f;
	private ArrayList<Polynomial> g;
	private ArrayList<Polynomial> h;

	private int maxdegree;

	private int currentDegree = 0;

	private Polynomial next = null;

	private int currentGTupelSize = 0;

	private Combinatorical currentGCombinations;
	private Collection<Polynomial> currentF = new HashSet<Polynomial>();
	private Collection<Polynomial> currentG = new HashSet<Polynomial>();
	private Collection<Polynomial> currentH = new HashSet<Polynomial>();
	private Combinatorical currentFCombinations;
	private int currentFTupelSize = 0;
	private Combinatorical currentHCombinations;
	private int currentHTupelSize = 0;

	public static Polynomial createCombined(Collection<? extends Polynomial> f,
			Collection<? extends Polynomial> g,
			Collection<? extends Polynomial> h) {
		assert !(f.isEmpty() && g.isEmpty() && h.isEmpty());
		Polynomial result = null;
		for (Polynomial p : f) {
			if (result == null) {
				result = p;
			} else {
				result = result.add(p);
			}
		}
		Polynomial pg = null;
		if (result != null) {
			pg = (Polynomial) result.one();
		} else if (!g.isEmpty()) {
			pg = (Polynomial) g.iterator().next().one();
		} else if (!h.isEmpty()) {
			pg = (Polynomial) h.iterator().next().one();
		}
		assert (pg != null);
		for (Polynomial p : g) {
			pg.multiply(p);
		}
		assert (result != null);
		result.add((Polynomial) pg.power(Values.getDefault().valueOf(2)));
		for (Polynomial p : h) {
			result = result.add(p);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#getNext()
	 */
	/*@Override*/
	public Polynomial getNext() {
		// warning this method is _not_ threadsafe (like most java collections)
		Polynomial n = next;
		next = null;
		return n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#hasNext()
	 */
	/*@Override*/
	public boolean hasNext() {
		if (next == null) {
			next = computeNext();
			System.out.println("Next is now: " + next);
		}
		return next != null && next.degree().intValue() <= maxdegree;
	}

	/**
	 * Compute the next polynomial of the form f + g^2 + h where the set of
	 * possible polynomials in f are combined as cone, those in g as monoid and
	 * those in h as ideal.
	 * 
	 * @return
	 */
	private Polynomial computeNext() {
		System.out.println("Computing next");
		boolean nextF = true;
		boolean nextG = true;
		boolean nextH = true;

		if (nextG) {
			computeG();
		}
		if (nextF) {
			computeNextF();
		}
		if (nextH) {
			computeNextH();
		}
		if(currentF.isEmpty() && currentG.isEmpty() && currentH.isEmpty()) {
			return null;
		}
		return createCombined(currentF, currentG, currentH);
	}

	/**
	 * 
	 */
	private boolean computeNextH() {
		// TODO jdq: use that h is an ideal
		if (h.size() == 0
				|| (currentHTupelSize == h.size() && (currentHCombinations == null || !currentHCombinations
						.hasNext()))) {
			currentH.clear();
			return true;
		}
		if (currentHCombinations == null || !currentHCombinations.hasNext()) {
			currentHCombinations = Combinatorical.getCombinations(
					++currentHTupelSize, h.size(), true);
		}
		int[] next2 = currentHCombinations.next();
		currentH.clear();
		int maxd = 0;
		for (int i : next2) {
			Polynomial polynomial = h.get(i);
			if (polynomial.degree().intValue() > maxd) {
				maxd = polynomial.degree().intValue();
			}
			currentH.add(polynomial);
		}
		return maxd == currentDegree;
	}

	/**
	 * 
	 */
	private boolean computeNextF() {
		// TODO jdq: use that f is a cone
		if (f.size() == 0
				|| (currentFTupelSize == f.size() && (currentFCombinations == null || !currentFCombinations
						.hasNext()))) {
			currentF.clear();
			return true;
		}
		if (currentFCombinations == null || !currentFCombinations.hasNext()) {
			currentFCombinations = Combinatorical.getCombinations(
					++currentFTupelSize, f.size(), true);
		}
		int[] next2 = currentFCombinations.next();
		currentF.clear();
		int maxd = 0;
		for (int i : next2) {
			Polynomial polynomial = f.get(i);
			if (polynomial.degree().intValue() > maxd) {
				maxd = polynomial.degree().intValue();
			}
			currentF.add(polynomial);
		}
		return maxd == currentDegree;
	}

	/**
	 * 
	 */
	private void computeG() {
		if (g.size() == 0
				|| (currentGTupelSize == g.size() && (currentGCombinations == null || !currentGCombinations
						.hasNext()))) {
			currentG.clear();
			return;
		}
		if (currentGCombinations == null || !currentGCombinations.hasNext()) {
			currentGCombinations = Combinatorical.getCombinations(
					++currentGTupelSize, g.size(), true);
		}
		int[] next2 = currentGCombinations.next();
		currentG.clear();
		int maxd = 0;
		for (int i : next2) {
			Polynomial polynomial = g.get(i);
			if (polynomial.degree().intValue() > maxd) {
				maxd = polynomial.degree().intValue();
			}
			currentG.add(polynomial);
		}
		// multiply this by two as it will be g^2 in the result polynomial
		currentDegree = maxdegree * 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#setF(java.util.Set)
	 */
	/*@Override*/
	public void setF(Set<Polynomial> f) {
		this.f = new ArrayList<Polynomial>(f);
		Collections.sort(this.f, PolynomialComparator.INSTANCE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#setG(java.util.Set)
	 */
	/*@Override*/
	public void setG(Set<Polynomial> g) {
		this.g = new ArrayList<Polynomial>(g);
		Collections.sort(this.g, PolynomialComparator.INSTANCE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#setH(java.util.Set)
	 */
	/*@Override*/
	public void setH(Set<Polynomial> h) {
		this.h = new ArrayList<Polynomial>(h);
		Collections.sort(this.h, PolynomialComparator.INSTANCE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#setMaxDegree(int)
	 */
	/*@Override*/
	public void setMaxDegree(int degree) {
		this.maxdegree = degree;
	}

}

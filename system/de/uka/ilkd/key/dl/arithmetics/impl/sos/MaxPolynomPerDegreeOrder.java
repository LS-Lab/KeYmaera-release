/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.sos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import orbital.algorithm.Combinatorical;
import orbital.math.AlgebraicAlgorithms;
import orbital.math.Polynomial;
import orbital.math.Real;
import orbital.math.Values;
import orbital.math.Vector;
import de.uka.ilkd.key.dl.arithmetics.impl.sos.SimpleOrder.PolynomialComparator;

/**
 * @author jdq
 * 
 */
public class MaxPolynomPerDegreeOrder implements PolynomialOrder {

	public static class TotalPolynomialOrderComparator implements
			Comparator<Polynomial> {

		public static final TotalPolynomialOrderComparator INSTANCE = new TotalPolynomialOrderComparator();

		/**
		 * The resulting order garanties [x1,x2,x3,...,xn] -> x1 < x2 < x3 < ...
		 * > xn
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		/* @Override */
		public int compare(Polynomial o1, Polynomial o2) {
			for (int i = 0; i < o1.rank(); i++) {
				int cur = o1.degrees()[i] - o2.degrees()[i];
				if (cur != 0) {
					return cur;
				}
			}
			Iterator indices1 = o1.indices();
			ListIterator coeff1 = o1.iterator();
			Iterator indices2 = o2.indices();
			ListIterator coeff2 = o2.iterator();
			Comparator<Vector> vectorComparator = new Comparator<Vector>() {

				@Override
				public int compare(Vector arg0, Vector arg1) {
					for (int i = 0; i < arg0.dimension(); i++) {
						int cur = ((orbital.math.Integer) arg0.get(i))
								.intValue()
								- ((orbital.math.Integer) arg1.get(i))
										.intValue();
						if (cur != 0) {
							return cur;
						}
					}
					return 0;
				}

			};
			TreeMap<Vector, Real> in1 = new TreeMap<Vector, Real>(
					vectorComparator);
			TreeMap<Vector, Real> in2 = new TreeMap<Vector, Real>(
					vectorComparator);
			List<Vector> all = new ArrayList<Vector>();
			while (indices1.hasNext()) {
				Vector next2 = (Vector) indices1.next();
				Real next3 = (Real) coeff1.next();
				if (next3.doubleValue() != 0) {
					in1.put(next2, next3);
					all.add(next2);
				}
			}
			while (indices2.hasNext()) {
				Vector next2 = (Vector) indices2.next();
				Real next3 = (Real) coeff2.next();
				if (next3.doubleValue() != 0) {
					in2.put(next2, next3);
					if (!all.contains(next2)) {
						all.add(next2);
					}
				}
			}
			Collections.sort(all, vectorComparator);
			for (Vector v : all) {
				if (in1.containsKey(v) && !in2.containsKey(v)) {
					return 1; // o1 > o2
				} else if (!in1.containsKey(v) && in2.containsKey(v)) {
					return -1; // o1 < o2
				}
			}

			return 0;

		}
	}

	public static class Monoid implements Iterable<Polynomial> {

		/**
		 * @author jdq TODO Documentation since Feb 9, 2009
		 */
		private static final class MonoidIterator implements
				Iterator<Polynomial> {

			private List<Polynomial> generator;

			private Polynomial one;

			private PriorityQueue<Polynomial> s;

			private List<Polynomial> p;

			private PriorityQueue<Polynomial> results;

			private Set<Polynomial> usedResults;

			private Comparator polynomComparator;

			/**
			 * 
			 */
			public MonoidIterator(List<Polynomial> generator, Polynomial one) {
				this.generator = new ArrayList<Polynomial>(generator);
				polynomComparator = AlgebraicAlgorithms
						.INDUCED(AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);
				Collections.sort(this.generator, polynomComparator);
				s = new PriorityQueue<Polynomial>(100, polynomComparator);
				p = new ArrayList<Polynomial>();
				results = new PriorityQueue<Polynomial>(100, polynomComparator);
				usedResults = new TreeSet<Polynomial>(polynomComparator);
				s.addAll(generator);
				s.add(one);
				p.add(one);
				this.one = one;
			}

			@Override
			public boolean hasNext() {
				return !generator.isEmpty();
			}

			@Override
			public Polynomial next() {
				while (results.isEmpty()) {
					Combinatorical combinations = Combinatorical
							.getCombinations(2, p.size(), true);
					while (combinations.hasNext()) {
						int[] curComb = combinations.next();
						s.add(p.get(curComb[0]).multiply(p.get(curComb[1])));
					}
					p.add(s.poll());
					Collections.sort(p, polynomComparator);
					s.removeAll(p);
					results.addAll(s);
					results.removeAll(usedResults);
				}
				Polynomial res = results.poll();
				usedResults.add(res);
				return res;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}

		private List<Polynomial> generator;
		private Polynomial one;

		/**
		 * 
		 */
		public Monoid(List<Polynomial> g, Polynomial one) {
			this.generator = g;
			this.one = one;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<Polynomial> iterator() {
			return new MonoidIterator(generator, one);
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
	private Polynomial currentF = null;
	private Polynomial currentG;
	private Polynomial currentH = null;
	private Polynomial one;
	private Iterator<Polynomial> gIterator;

	/**
	 * 
	 */
	public MaxPolynomPerDegreeOrder(Polynomial one) {
		this.one = one;
		currentG = one;
	}

	public static Polynomial createCombined(Polynomial f, Polynomial g,
			Polynomial h) {
		assert !(f == null && g == null && h == null);
		Polynomial result = f;
		Polynomial pg = null;
		if (result != null) {
			pg = (Polynomial) result.one();
		} else if (g != null) {
			pg = g;
		} else if (h != null) {
			pg = (Polynomial) h.one();
		}
		assert (pg != null);
		assert (result != null);
		result.add((Polynomial) pg.power(Values.getDefault().valueOf(2)));
		result = result.add(h);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#getNext()
	 */
	/* @Override */
	public Polynomial getNext() {
		// warning this method is _not_ threadsafe (like most java collections)
		Polynomial n = next;
		next = null;
		currentDegree++;
		return n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#hasNext()
	 */
	/* @Override */
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

		int degree = computeG(currentDegree);
		if (currentF == null || degree > currentF.degree().intValue()) {
			computeNextF(degree);
		}
		if (currentH == null || degree > currentH.degree().intValue()) {
			computeNextH(currentF.degreeValue());
		}
		if (currentG == null && currentF == null && currentH == null) {
			return null;
		}
		return createCombined(currentF, currentG, currentH);
	}

	/**
	 * @param degree
	 * 
	 */
	private boolean computeNextH(int degree) {
		// int maxd = 0;
		// for (int i : next2) {
		// Polynomial polynomial = h.get(i);
		// if (polynomial.degree().intValue() > maxd) {
		// maxd = polynomial.degree().intValue();
		// }
		// currentH.add(polynomial);
		// }
		// return maxd == degree;
		return false;
	}

	/**
	 * @param degree
	 * 
	 */
	private void computeNextF(int degree) {
		if (currentF == null) {
			currentF = one;
		}
		int d = currentF.degreeValue();
		while (currentF.degreeValue() <= degree) {
			d++;
			Iterator<Polynomial> it = f.iterator();
			Polynomial next = it.next();
			if (next.degreeValue() < d) {

			}

		}
	}

	/**
	 * @param degree
	 * 
	 */
	private int computeG(int degree) {
		// multiply this by two as it will be g^2 in the result polynomial
		if (gIterator == null) {
			Monoid m = new Monoid(g, one);
			gIterator = m.iterator();
		}
		currentG = gIterator.next();
		currentDegree = currentG.degreeValue() * 2;
		return currentDegree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#setF(java.util
	 * .Set)
	 */
	/* @Override */
	public void setF(Set<Polynomial> f) {
		this.f = new ArrayList<Polynomial>(f);
		Collections.sort(this.f, PolynomialComparator.INSTANCE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#setG(java.util
	 * .Set)
	 */
	/* @Override */
	public void setG(Set<Polynomial> g) {
		this.g = new ArrayList<Polynomial>(g);
		Collections.sort(this.g, PolynomialComparator.INSTANCE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#setH(java.util
	 * .Set)
	 */
	/* @Override */
	public void setH(Set<Polynomial> h) {
		this.h = new ArrayList<Polynomial>(h);
		Collections.sort(this.h, PolynomialComparator.INSTANCE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder#setMaxDegree(int)
	 */
	/* @Override */
	public void setMaxDegree(int degree) {
		this.maxdegree = degree;
	}

}

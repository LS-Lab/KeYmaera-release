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

import orbital.math.Integer;
import orbital.math.AlgebraicAlgorithms;
import orbital.math.Polynomial;
import orbital.math.Real;
import orbital.math.Values;
import orbital.math.Vector;
import orbital.util.KeyValuePair;
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
			final Iterator<KeyValuePair> monomials = o1.monomials();
			final Iterator<KeyValuePair> monomials2 = o2.monomials();
			final Comparator<Vector> monomComparator = new Comparator<Vector>() {

				/*@Override*/
				public int compare(Vector arg0, Vector arg1) {
					int left = 0;
					int right = 0;
					for (int i = 0; i < arg0.dimension(); i++) {
						left += ((orbital.math.Integer) arg0.get(i)).intValue();
						right += ((orbital.math.Integer) arg1.get(i))
								.intValue();
					}
					return left - right;
				}

			};
			final TreeMap<Vector, Real> in1 = new TreeMap<Vector, Real>(
					monomComparator);
			final TreeMap<Vector, Real> in2 = new TreeMap<Vector, Real>(
					monomComparator);
			final List<Vector> all = new ArrayList<Vector>();
			while (monomials.hasNext()) {
				KeyValuePair nextMono = monomials.next();
				Object nextVector = nextMono.getKey();
				
				Vector next2 = null;
				if(nextVector instanceof Vector ) {
					next2 = (Vector) nextVector;
				} else {
					next2 = Values.getDefault().valueOf(new Integer[] { (Integer) nextVector });
				}
				Real next3 = (Real) nextMono.getValue();
				if (!next3.equals(next3.zero())) {
					in1.put(next2, next3);
					all.add(next2);
				}
			}
			while (monomials2.hasNext()) {
				KeyValuePair nextMono = monomials2.next();
				Object nextVector = nextMono.getKey();
				
				Vector next2 = null;
				if(nextVector instanceof Vector ) {
					next2 = (Vector) nextVector;
				} else {
					next2 = Values.getDefault().valueOf(new Integer[] { (Integer) nextVector });
				}
				Real next3 = (Real) nextMono.getValue();
				if (!next3.equals(next3.zero())) {
					in2.put(next2, next3);
					if (!all.contains(next2)) {
						all.add(next2);
					}
				}
			}
			Collections.sort(all, monomComparator);
			Collections.reverse(all);
			for (Vector v : all) {
				if (in1.containsKey(v) && !in2.containsKey(v)) {
					return 1; // o1 > o2
				} else if (!in1.containsKey(v) && in2.containsKey(v)) {
					return -1; // o1 < o2
				}
			}
			// now we know that all vectors occur in both polynomials
			for (Vector v : all) {
				int result = in1.get(v).compareTo(in2.get(v));
				if (result != 0) {
					return result;
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
				System.out.println("generator is " + generator);
//				 polynomComparator = AlgebraicAlgorithms
//				 .INDUCED(AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);
				polynomComparator = new Comparator<Polynomial>() {

					public int compare(Polynomial o1, Polynomial o2) {
						return -TotalPolynomialOrderComparator.INSTANCE.compare(o1, o2);
					}
					
				};
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

			/*@Override*/
			public boolean hasNext() {
				return !generator.isEmpty();
			}

			/*@Override*/
			public Polynomial next() {
				do {
					// System.out.println("P is " + p);// XXX
					for (Polynomial pPoly : p) {
						for (Polynomial pPoly2 : p) {
							Polynomial multiply = pPoly.multiply(pPoly2);
							if (!s.contains(multiply)) {
								s.add(multiply);
							}
							// System.out.println("Adding to s: " + multiply);
						}
					}
					Polynomial poll = s.poll();
					while (p.contains(poll)) {
						poll = s.poll();
					}
					p.add(poll);
					// System.out.println("Added to p: " + poll);
					Collections.sort(p, polynomComparator);
					results.addAll(s);
					// System.out.println("Result: " + results);
					for (Polynomial o : usedResults) {
						if (results.contains(o)) {
							results.remove(o);
						}
					}
					// System.out.println("Used results: " + usedResults);
					// System.out.println("results is now " + results);// XXX
					s.removeAll(p);
					// System.out.println("s is now " + s);// XXX
				} while(results.isEmpty());
				Polynomial res = results.poll();
				usedResults.add(res);
				System.out.println(results);//XXX
				return res;
			}

			/*@Override*/
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
		/*@Override*/
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

	private Polynomial currentF = null;
	private Polynomial currentG = null;
	private Polynomial currentH = null;
	private Polynomial one;
	private Iterator<Polynomial> gIterator;
	private ArrayList<Polynomial> fMonoidGenerator;

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
		result = result.add((Polynomial) pg.power(Values.getDefault()
				.valueOf(2)));
		if (h != null) {
			result = result.add(h);
		}
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
		System.out.println("fMonoidGenerator " + fMonoidGenerator);// XXX
		assert fMonoidGenerator.size() != 0 || f.size() == 0;
		if (f.size() == 0) {
			return;
		}
		Monoid m = new Monoid(fMonoidGenerator, one);
		Iterator<Polynomial> iterator = m.iterator();
		Polynomial next2 = iterator.next();
		d++;
		while (computeDegree(next2) < d) {
			next2 = iterator.next();
			System.out.println("Testing " + next2);// XXX
		}
		while (computeDegree(currentF) <= degree) {
			do {
				currentF = currentF.add(next2);
				System.out.println("Adding " + next2);// XXX
				next2 = iterator.next();
			} while (computeDegree(next2) == d);
			System.out.println("currentF is now " + currentF);
			d++;
		}
		System.out.println("New f is " + currentF);// XXX
	}

	/**
	 * @param next2
	 * @return TODO documentation since Feb 10, 2009
	 */
	private static int computeDegree(Polynomial next2) {
		int result = 0;
		for (int i = 0; i < next2.rank(); i++) {
			result += next2.degrees()[i];
		}
		return result;
	}

	/**
	 * @param degree
	 * 
	 */
	private int computeG(int degree) {
		// multiply this by two as it will be g^2 in the result polynomial
		if (g.isEmpty()) {
			currentG = one;
			return 0;
		}
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
		fMonoidGenerator = new ArrayList<Polynomial>(f);
		// add squares of all variables occurring in the cone
		if (!f.isEmpty()) {
			int rank = f.iterator().next().rank();
			for (int i = 0; i < rank; i++) {
				int[] var = new int[rank];
				var[i] = 2;
				Polynomial monomial = Values.getDefault().MONOMIAL(var);
				if (!fMonoidGenerator.contains(monomial)) {
					fMonoidGenerator.add(monomial);
				}
			}
		}
		Collections.sort(this.fMonoidGenerator, PolynomialComparator.INSTANCE);
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

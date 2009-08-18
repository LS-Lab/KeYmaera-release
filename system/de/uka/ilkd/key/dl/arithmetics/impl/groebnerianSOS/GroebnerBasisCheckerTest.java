/***************************************************************************
 *   Copyright (C) 2008 by Jan David Quesel                                *
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
package de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import orbital.math.Polynomial;
import orbital.math.ValueFactory;
import orbital.math.Values;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jdq TODO Documentation since 27.04.2009
 */
public class GroebnerBasisCheckerTest {

	private ValueFactory vf;
	private GroebnerBasisChecker gbChecker;

	/**
	 * @throws java.lang.Exception
	 *             TODO documentation since 27.04.2009
	 */
	@Before
	public void setUp() throws Exception {
		vf = Values.getDefault();
		gbChecker = new GroebnerBasisChecker(null);	
	}

	/**
	 * Test method for
	 * {@link de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.GroebnerBasisChecker#createOptimiseGroebnerBasis(java.util.Set, boolean)}
	 * .
	 */
	@Test
	public void testCreateOptimiseGroebnerBasis() {
		Polynomial xsquare = vf.MONOMIAL(new int[] { 2, 0, 0 });
		Polynomial ysquare = vf.MONOMIAL(new int[] { 0, 2, 0 });
		Polynomial zsquare = vf.MONOMIAL(new int[] { 0, 0, 2 });
		Polynomial combine = xsquare.multiply(xsquare).add(ysquare).subtract(
				zsquare);
		Polynomial combine2 = ysquare.multiply(ysquare);
		Polynomial combine3 = xsquare.subtract(ysquare.multiply(zsquare))
				.subtract(zsquare);

		HashSet<Polynomial> polys = new HashSet<Polynomial>();
		polys.add(combine);
		polys.add(combine2);
		polys.add(combine3);
		Set<Polynomial> createOptimiseGroebnerBasis = gbChecker
				.createOptimiseGroebnerBasis(polys, false);
		System.out.println(createOptimiseGroebnerBasis);
		assertTrue(createOptimiseGroebnerBasis.size() == 1
				&& createOptimiseGroebnerBasis.iterator().next().equals(
						vf.MONOMIAL(new int[] { 4 }).subtract(
								vf.MONOMIAL(new int[] { 2 }))));
	}
	
	/**
	 * Test method for
	 * {@link de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.GroebnerBasisChecker#createOptimiseGroebnerBasis(java.util.Set, boolean)}
	 * .
	 */
	@Test
	public void testCreateOptimiseGroebnerBasis2() {
		Polynomial xsquare = vf.MONOMIAL(new int[] { 2, 0, 0 });
		HashSet<Polynomial> polys = new HashSet<Polynomial>();
		polys.add(xsquare);
		Set<Polynomial> createOptimiseGroebnerBasis = gbChecker
				.createOptimiseGroebnerBasis(polys, false);
		System.out.println(createOptimiseGroebnerBasis);
		assertTrue(createOptimiseGroebnerBasis.size() == 0);
	}

	/**
	 * Test method for
	 * {@link de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.GroebnerBasisChecker#createOptimiseGroebnerBasis(java.util.Set, boolean)}
	 * .
	 */
	@Test
	public void testCreateOptimiseGroebnerBasis3() {
		Polynomial xsquare = vf.MONOMIAL(new int[] { 2, 0, 0 });
		Polynomial xcube = vf.MONOMIAL(new int[] { 3, 0, 0 });
		Polynomial ysquare = vf.MONOMIAL(new int[] { 0, 2, 0 });
		Polynomial zsquare = vf.MONOMIAL(new int[] { 0, 0, 2 });
		Polynomial combine = xsquare.subtract(ysquare);
		Polynomial combine2 = zsquare.add(xsquare).subtract(xcube);
		HashSet<Polynomial> polys = new HashSet<Polynomial>();
		polys.add(combine);
		polys.add(combine2);
		Set<Polynomial> createOptimiseGroebnerBasis = gbChecker
		.createOptimiseGroebnerBasis(polys, false);
		System.out.println(createOptimiseGroebnerBasis);
		assertEquals(createOptimiseGroebnerBasis, polys);
	}

	/**
	 * Test method for
	 * {@link de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.GroebnerBasisChecker#createOptimiseGroebnerBasis(java.util.Set, boolean)}
	 * .
	 */
	@Test
	public void testCreateOptimiseGroebnerBasis4() {
		Polynomial x = vf.MONOMIAL(new int[] { 1, 0, 0 });
		Polynomial xcube = vf.MONOMIAL(new int[] { 3, 0, 0 });
		Polynomial ysquare = vf.MONOMIAL(new int[] { 0, 2, 0 });
		Polynomial zsquare = vf.MONOMIAL(new int[] { 0, 0, 2 });
		Polynomial combine = x.subtract(ysquare.add(zsquare));
		Polynomial combine2 = zsquare.add(x).subtract(xcube);
		HashSet<Polynomial> polys = new HashSet<Polynomial>();
		polys.add(combine);
		polys.add(combine2);
		Set<Polynomial> createOptimiseGroebnerBasis = gbChecker
		.createOptimiseGroebnerBasis(polys, false);
		System.out.println(createOptimiseGroebnerBasis);
	}

}

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

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import orbital.math.Polynomial;
import orbital.math.ValueFactory;
import orbital.math.Values;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

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
		gbChecker = new GroebnerBasisChecker(new Node() {

			@Override
			public Node appendChild(Node arg0) throws DOMException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Node cloneNode(boolean arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public short compareDocumentPosition(Node arg0) throws DOMException {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public NamedNodeMap getAttributes() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getBaseURI() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public NodeList getChildNodes() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getFeature(String arg0, String arg1) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Node getFirstChild() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Node getLastChild() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getLocalName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getNamespaceURI() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Node getNextSibling() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getNodeName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public short getNodeType() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getNodeValue() throws DOMException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Document getOwnerDocument() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Node getParentNode() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getPrefix() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Node getPreviousSibling() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getTextContent() throws DOMException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getUserData(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean hasAttributes() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean hasChildNodes() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Node insertBefore(Node arg0, Node arg1) throws DOMException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isDefaultNamespace(String arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isEqualNode(Node arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isSameNode(Node arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isSupported(String arg0, String arg1) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String lookupNamespaceURI(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String lookupPrefix(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void normalize() {
				// TODO Auto-generated method stub

			}

			@Override
			public Node removeChild(Node arg0) throws DOMException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Node replaceChild(Node arg0, Node arg1) throws DOMException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setNodeValue(String arg0) throws DOMException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setPrefix(String arg0) throws DOMException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setTextContent(String arg0) throws DOMException {
				// TODO Auto-generated method stub

			}

			@Override
			public Object setUserData(String arg0, Object arg1,
					UserDataHandler arg2) {
				// TODO Auto-generated method stub
				return null;
			}

		});
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

}

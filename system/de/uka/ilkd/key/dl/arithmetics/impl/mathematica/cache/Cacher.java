/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache;

import com.wolfram.jlink.Expr;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper.ExprAndMessages;

/**
 * Cacher implements a cache-behavior. It uses the ExprRenamer-Class to rename
 * the given expression to a uniform name and stores their String-representation
 * into the cache. The cache is not publicly available.
 * 
 * @author Timo Michelsen
 */
public class Cacher implements ICacher {

	private Cache cache = new Cache();
	
	private int maxSize = 10000;
	
	/**
	 * Checks, if a given expression is already cached.
	 */
	public boolean contains(Expr expr) {

		// Rename given expression
		RenameTable tbl = ExprRenamer.getRenaming(expr);
		Expr renamedExpr = ExprRenamer.rename(expr, tbl);
		String exprName = renamedExpr.toString();
		System.out.println("Contains " + exprName + " " + this.cache.containsKey(exprName));//XXX
		return this.cache.containsKey(exprName);
	}

	/**
	 * Return the related ExprAndMessages-Instance of a given expression, if it
	 * exists in the cache. If not, null will be returned.
	 */
	public ExprAndMessages get(Expr expr) {

		// Rename given expression
		RenameTable tbl = ExprRenamer.getRenaming(expr);
		Expr renamedExpr = ExprRenamer.rename(expr, tbl);
		String exprName = renamedExpr.toString();

		// Check, if renamed expression is already cached
		if (this.cache.containsKey(exprName)) {

			ExprAndMessages exprAndMessages = this.cache.get(exprName);

			// Reverse table
			tbl.reverse();
			
			System.out.println("Found " + exprAndMessages.expression);//XXX
			
			// Rename the variables back.
			// All Variables have now the names for the current context.
			exprAndMessages.expression = ExprRenamer.rename(
					exprAndMessages.expression, tbl);
			System.out.println("Returning " + exprAndMessages.expression);//XXX
			return exprAndMessages;

		} else {
			// related ExprAndMessages-instance not found
			return null;
		}
	}

	/**
	 * Puts a given expression, related to a given ExprAndMessages-Instance,
	 * into the cache. If the expression already exists in the cache, nothing
	 * happens.
	 */
	public void put(Expr expr, ExprAndMessages exprAndMessages) {
		// TODO: implement a better strategy to handle full caches
		if(cache.size() > maxSize) {
			cache.clear();
		}
		ExprAndMessages copy = new ExprAndMessages(exprAndMessages.expression, exprAndMessages.messages);
		System.out.println("Put " + expr + " to " + copy.expression); //XXX
		// Rename given expression
		RenameTable tbl = ExprRenamer.getRenaming(expr);
		Expr renamedExpr = ExprRenamer.rename(expr, tbl);
		String exprName = renamedExpr.toString();

		// Rename expression in exprAndMessages
		tbl = ExprRenamer.getRenaming(copy.expression);
		copy.expression = ExprRenamer.rename(
				copy.expression, tbl);
		System.out.println("Putting as " + exprName + " to " + copy.expression); //XXX
		// Only put it in the cache, when it doesn't already exists
		if (!this.cache.containsKey(exprName)) {
			this.cache.put(exprName, copy);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.ICacher#put(de.
	 * uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.ICacher)
	 */
	/*@Override*/
	public void put(ICacher cache) {
		if (cache instanceof Cacher) {
			this.cache.putAll(((Cacher) cache).cache);
		} else {
			throw new UnsupportedOperationException(
					"Dont know how to add the cache of concreate type "
							+ cache.getClass());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.ICacher#setMaxCacheSize
	 * (int)
	 */
	/*@Override*/
	public void setMaxCacheSize(int size) {
		this.maxSize = size;
	}
}

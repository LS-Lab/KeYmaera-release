/**
 * Provides utilities for generating Iterator classes.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 */

package de.uka.ilkd.key.dl.image_compute;

import java.util.*;

class IteratorUtil
{

	/**
	 * Iterator that contains nothing.
	 */
	static class EmptyIterator implements Iterator
	{
		public EmptyIterator()
		{
			// empty
		}

		public boolean hasNext()
		{
			return false;
		}

		public Object next()
		{
			return null;
		}

		public void remove()
		{
			throw new UnsupportedOperationException(this.getClass().getName()
					+ " doesn't support reomve");
		}
	}

	/**
	 * Iterator to a single object.
	 */
	static class SingleIterator implements Iterator
	{
		private Object obj = null;
		private boolean hasNext = true;

		public SingleIterator(Object obj)
		{
			this.obj = obj;
		}

		public boolean hasNext()
		{
			return hasNext;
		}

		public Object next()
		{
			if (hasNext) {
				hasNext = false;
				return obj;
			}
			return null;
		}

		public void remove()
		{
			throw new UnsupportedOperationException(this.getClass().getName()
					+ " doesn't support remove");
		}
	}

}

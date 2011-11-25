/**
 * Machine precision value factory.
 *
 * Singleton shared by all classes in current package.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 */

package de.uka.ilkd.key.dl.image_compute;

import java.util.*;

import orbital.math.Values;
import orbital.math.ValueFactory;

public class MachValueFactory
{
	private static final ValueFactory vf;
	static {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("orbital.math.Scalar.precision", "machine");
		vf = Values.getInstance(properties);
	}

	private MachValueFactory() { }

	public static ValueFactory getInstance()
	{
		return vf;
	}
}

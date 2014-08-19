package de.uka.ilkd.key.dl.arithmetics;

/**
 * Dispose resources.
 * @author smitsch
 */
public interface IDisposable {
	/**
	 * Disposes all resources
	 * @throws Exception If an error occurs while disposing resources.
	 */
	void dispose() throws Exception;
}

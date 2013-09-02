package edu.cmu.cs.ls.lyusimul
/**
 * Class for masking names:
 *
 * - Replace underscores by something that is not interpreted by Mathematica
 * - Add a context to each name such that Mathematica does not get confused by variables/functions named,
 *   for instance, E, I, or D.
 */
object NameMasker {

  val USCORE_ESCAPE = "\\$u";

  val SCOPE = "KeYmaera`"

  def mask(e: String): String = if (!isMasked(e)) mask(e, SCOPE) else e
  def mask(e: String, s: String): String = if (!isMasked(e)) s + e.replaceAll("_", USCORE_ESCAPE) else e

  def unmask(e: String): String = e.substring(e.indexOf('`')+1).replaceAll(USCORE_ESCAPE, "_")

  def isMasked(e: String): Boolean = e.contains('`')
}
/**
 * 
 */
package de.uka.ilkd.key.util.pp;

import java.io.IOException;
import java.io.Writer;

/**
 * @author mokom
 *
 */
public class LayouterHtml extends Layouter{

	public LayouterHtml(Writer writer, int lineWidth, int indentation) {
		super(writer, lineWidth, indentation);
		// TODO Auto-generated constructor stub
	}

	public LayouterHtml(Writer writer, int lineWidth) { 
		super(writer, lineWidth);
		// TODO Auto-generated constructor stub
	}

	public LayouterHtml(Writer writer) {
		super(writer);
		// TODO Auto-generated constructor stub
	}

	public LayouterHtml(Backend back, int indentation) {
		super(back, indentation);
		// TODO Auto-generated constructor stub
	}
	  // PRIMITIVE STREAM OPERATIONS ------------------------------------

    /** Output text material.  The string <code>s</code> should not
     * contain newline characters.  If you have a string with newline
     * characters, and want to retain its formatting, consider using
     * the {@link #pre(String s)} method.  The Layouter will not
     * insert any line breaks in such a string.
     *
     * @param s the String to print.
     * @return this
     */
    public Layouter print(String s) 
    	throws IOException
    {
	if (delimStack.isEmpty()) {
	    out.print(s);
	    totalSize+=back.measure(s);
	    totalOutput+=back.measure(s);
	} else {
	    enqueue(new StringToken(s));
	    totalSize+=back.measure(s);

	    while(totalSize-totalOutput > out.space() &&
		  !delimStack.isEmpty()) {
		popBottom().setInfiniteSize();
		advanceLeft();
	    }
	}
	return this;
    }

  
}

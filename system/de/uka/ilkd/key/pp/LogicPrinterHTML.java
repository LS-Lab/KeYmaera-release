package de.uka.ilkd.key.pp;

import java.io.IOException;
import java.util.Stack;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.util.pp.Backend;
import de.uka.ilkd.key.util.pp.Layouter;


public class LogicPrinterHTML extends LogicPrinter {

    public LogicPrinterHTML(ProgramPrinter prgPrinter,
	    NotationInfo notationInfo, Services services, boolean purePrint) {
	super(prgPrinter, notationInfo, services, purePrint);
	backend = new PosTableStringBackendHTML(lineWidth);
	layouter = new Layouter(backend, 2);
	// TODO Auto-generated constructor stub
    }

    public LogicPrinterHTML(ProgramPrinter prgPrinter,
	    NotationInfo notationInfo, Services services) {
	super(prgPrinter, notationInfo, services);
	// TODO Auto-generated constructor stub
    }

    public LogicPrinterHTML(ProgramPrinter prgPrinter,
	    NotationInfo notationInfo, Backend backend, Services services,
	    boolean purePrint) {

	super(prgPrinter, notationInfo, backend, services, purePrint);
	backend = new PosTableStringBackendHTML(lineWidth);
	layouter = new Layouter(backend, 2);
	// TODO Auto-generated constructor stub
    }

    public LogicPrinterHTML(ProgramPrinter prgPrinter,
	    NotationInfo notationInfo, Backend backend, Services services) {
	super(prgPrinter, notationInfo, backend, services);
	backend = new PosTableStringBackendHTML(lineWidth);
	layouter = new Layouter(backend, 2);
	// TODO Auto-generated constructor stub
    }

    /**
     * Resets the Backend, the Layouter and (if applicable) the ProgramPrinter
     * of this Object.
     */
    public void reset() {
	backend = new PosTableStringBackendHTML(lineWidth);
	layouter = new Layouter(backend, 2);
	if (prgPrinter != null) {
	    prgPrinter.reset();
	}
    }

    /**
     * Returns the pretty-printed sequent. This should only be called after a
     * <tt>printSequent</tt> invocation returns.
     * 
     * @return the pretty-printed sequent.
     */
    public String toString() {
	try {
	    layouter.flush();
	} catch (IOException e) {
	    throw new RuntimeException("IO Exception in pretty printer:\n" + e);
	}
	if(backend instanceof PosTableStringBackendHTML) {
		return ((PosTableStringBackendHTML) backend).getString() + "\n";
	} else if(backend instanceof PosTableStringBackend) {
		return ((PosTableStringBackend) backend).getString() + "\n";
	} else {
		throw new IllegalStateException("Unknown backend " + backend.getClass());
    }
	}

    /**
     * returns the PositionTable representing position information on the
     * sequent of this Html LogicPrinter. Subclasses may overwrite this method with a
     * null returning body if position information is not computed there.
     */
    public InitialPositionTable getPositionTable() {
	if (pure) {
	    return null;
	}
	if(backend instanceof PosTableStringBackendHTML) {
		return ((PosTableStringBackendHTML) backend).getPositionTable();
	} else if(backend instanceof PosTableStringBackend) {
		return ((PosTableStringBackend) backend).getPositionTable();
	} else {
		throw new IllegalStateException("Unknown backend " + backend.getClass());
    }
    }

    protected Layouter mark(Object o) {
	if (pure) {
	    return null;
	} else {
	    return layouter.mark(o);
	}
    }

    /**
     * A {@link de.uka.ilkd.key.util.pp.Backend} which puts its result in a
     * StringBuffer and builds a PositionTable. Position table construction is
     * done using the {@link de.uka.ilkd.key.util.pp.Layouter#mark(Object)}
     * facility of the layouter with the various static <code>MARK_</code>
     * objects declared {@link LogicPrinter}.
     */
    protected class PosTableStringBackendHTML extends StringBackendHTML {

	/** The top PositionTable */
	private InitialPositionTable initPosTbl = new InitialPositionTable();

	/** The resulting position table or an intermediate result */
	private PositionTable posTbl = initPosTbl;

	/** The position in result where the current subterm starts */
	private int pos = 0;

	/**
	 * The stack of StackEntry representing the nodes above the current
	 * subterm
	 */
	private Stack<StackEntry> stack = new Stack<StackEntry>();

	/**
	 * If this is set, a ModalityPositionTable will be built next.
	 */
	private boolean need_modPosTable = false;

	/**
	 * These two remember the range corresponding to the first executable
	 * statement in a JavaBlock
	 */
	private int firstStmtStart;
	private Range firstStmtRange;

	/** Remembers the start of an update to create a range */
	private int updateStart;

	PosTableStringBackendHTML(int lineWidth) {
	    super(lineWidth);
	}

	PosTableStringBackendHTML(StringBuffer sb, int lineWidth) {
	    super(sb, lineWidth);
	    // TODO Auto-generated constructor stub
	}

	/**
	 * Returns the constructed position table.
	 * 
	 * @return the constructed position table
	 */
	public InitialPositionTable getPositionTable() {
	    return initPosTbl;
	}

	private void setupModalityPositionTable(StatementBlock block) {
	    int count = block.getStatementCount();
	    int position = 0;
	    for (int i = 0; i < count; i++) {
		posTbl.setStart(position);
		position += block.getStatementAt(i).toString().length();
		posTbl.setEnd(position - 1, null);
	    }
	}

	/**
	 * Receive a mark and act appropriately.
	 */
	public void mark(Object o) {

	    // IMPLEMENTATION NOTE
	    //
	    // This if-cascade is really ugly. In paricular the part
	    // which says <code>instanceof Integer</code>, which stand
	    // for a startTerm with given arity.
	    //
	    // The alternative would be to 1.: spread these
	    // mini-functionalties across several inner classes in a
	    // visitor-like style, effectively preventing anybody from
	    // finding out what happens, and 2.: allocate separate
	    // objects for each startTerm call to wrap the arity.
	    //
	    // I (MG) prefer it this way.
	    if (o == MARK_START_SUB) {
		posTbl.setStart(count() - pos);
		stack.push(new StackEntry(posTbl, pos));
		pos = count();
	    } else if (o == MARK_END_SUB) {
		StackEntry se = stack.peek();
		stack.pop();
		pos = se.pos();
		se.posTbl().setEnd(count() - pos + 1, posTbl);
		posTbl = se.posTbl();
	    } else if (o == MARK_MODPOSTBL) {
		need_modPosTable = true;
	    } else if (o instanceof Integer) {
		// This is sent by startTerm
		int rows = ((Integer) o).intValue();
		if (need_modPosTable) {
		    posTbl = new ModalityPositionTable(rows);
		} else {
		    posTbl = new PositionTable(rows);
		}
		need_modPosTable = false;
	    } else if (o == MARK_START_FIRST_STMT) {
		firstStmtStart = count() - pos;
	    } else if (o == MARK_END_FIRST_STMT) {
		firstStmtRange = new Range(firstStmtStart, count() - pos + 1);
		((ModalityPositionTable) posTbl)
		        .setFirstStatementRange(firstStmtRange);
	    } else if (o == MARK_START_UPDATE) {
		updateStart = count();
	    } else if (o == MARK_END_UPDATE) {
		initPosTbl.addUpdateRange(new Range(updateStart, count() + 1));
	    }
	}
    }
}

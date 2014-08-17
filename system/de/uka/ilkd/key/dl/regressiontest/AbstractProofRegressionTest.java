package de.uka.ilkd.key.dl.regressiontest;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.uka.ilkd.key.dl.DLInitializer;
import de.uka.ilkd.key.dl.arithmetics.ICounterExampleGenerator;
import de.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator;
import de.uka.ilkd.key.dl.arithmetics.IMathSolver;
import de.uka.ilkd.key.dl.arithmetics.IODESolver;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.ISOSChecker;
import de.uka.ilkd.key.dl.arithmetics.ISimplifier;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.gui.IMain;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.ProverTaskListener;
import de.uka.ilkd.key.gui.TaskFinishedInfo;
import de.uka.ilkd.key.proof.ProblemLoader;

/**
 * Base class for proof regression tests.
 * Subclasses must provide the static data() method to provide
 * the test files.
 * 
 * @author smitsch
 */
@RunWith(Parameterized.class)
public abstract class AbstractProofRegressionTest {
	
//	@SuppressWarnings("serial")
//	private static class MainSingletonDestroyer extends Main {
//		public MainSingletonDestroyer(String title) { super(title); }
//		public static void cleanUp() { instance = null; }
//	}
	
	private static class Pair<F,S> {
		private F first;
		private S second;
		
		public Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}
		
		public F getFirst() { return first; }
		public S getSecond() { return second; }
	}
	
	private static class SingletonDestroyer {
		private Class<?> clazz;
		private Pair<String,? extends Object>[] fields;
		
		@SafeVarargs
		public SingletonDestroyer(Class<?> clazz, Pair<String, ? extends Object>... fields) {
			this.clazz = clazz;
			this.fields = fields;
		}
		
		public void cleanUp() throws Exception {
			for (Pair<String,? extends Object> field : fields) {
				Field f = clazz.getDeclaredField(field.getFirst());
				f.setAccessible(true);
				f.set(null, field.getSecond());
			}
		}
	}
	
	/**
	 * The KeYmaera main object.
	 */
	private IMain main;
	
	/**
	 * True, if the proof was successful; false otherwise.
	 */
	private boolean proved;
	
	/**
	 * True, if the proof is still running; false otherwise.
	 */
	private boolean running;
	
	/**
	 * Prover notifies test assertion when done.
	 */
	private Object semaphor;
	
	/**
	 * The name of the file under test.
	 */
	private String fileName;
	
	/**
	 * Initializes a new proof regression test object with
	 * the specified proof file under test.
	 * @param fileName The file under test.
	 */
	protected AbstractProofRegressionTest(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Sets up the test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		// TODO too many singletons, run new JVMs instead
		
		proved = false;
		running = true;
		semaphor = new Object();
		Main.configureLogger();
        Main.evaluateOptions(new String[] {fileName, "dL", "auto"});
        main = Main.getInstance(false);
	}

	/**
	 * Cleans up after testing.
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		proved = false;
		running = false;
		semaphor = null;
		main = null;
		
		// tear down those annoying singletons
		new SingletonDestroyer(Main.class, new Pair<String,Object>("instance", null)).cleanUp();
		new SingletonDestroyer(DLInitializer.class, new Pair<String,Boolean>("initialized", Boolean.FALSE)).cleanUp();
		new SingletonDestroyer(MathSolverManager.class, 
				new Pair<String,LinkedHashMap<String,IQuantifierEliminator>>("QUANTIFIER_ELMINIATORS", new LinkedHashMap<String, IQuantifierEliminator>()),
				new Pair<String,LinkedHashMap<String,ICounterExampleGenerator>>("COUNTEREXAMPLE_GENERATORS", new LinkedHashMap<String, ICounterExampleGenerator>()),
				new Pair<String,LinkedHashMap<String,IODESolver>>("ODESOLVERS", new LinkedHashMap<String, IODESolver>()),
				new Pair<String,LinkedHashMap<String,ISimplifier>>("SIMPLIFIERS", new LinkedHashMap<String, ISimplifier>()),
				new Pair<String,LinkedHashMap<String,IGroebnerBasisCalculator>>("GROEBNER_BASIS_CALCULATORS", new LinkedHashMap<String, IGroebnerBasisCalculator>()),
				new Pair<String,LinkedHashMap<String,ISOSChecker>>("SOS_CHECKERS", new LinkedHashMap<String, ISOSChecker>()),
				new Pair<String,LinkedHashMap<String,IMathSolver>>("UNCONFIGURED", new LinkedHashMap<String, IMathSolver>())
				).cleanUp();
	}

	/**
	 * The test method. Called once per entry in the static
	 * data() parameter provider.
	 */
	@Test
	public void test() {
		File file = new File(fileName);
		
		final ProblemLoader pl = 
			    new ProblemLoader(file, main, main.mediator().getProfile(), false);
		pl.addTaskListener(new ProverTaskListener() {
			@Override
			public void taskStarted(String message, int size) {
				running = true;
			}

			@Override
			public void taskProgress(int position) { /* nothing to do here */ }

			@Override
			public void taskFinished(TaskFinishedInfo info) {
				running = false;
				proved = info != null 
						&& info.getProof() != null 
						&& info.getProof().closed();
				synchronized(semaphor) { semaphor.notifyAll(); }
			}
			
		});
		pl.run();
		
		while (running) {
			try {
				synchronized(semaphor) { semaphor.wait(); }
			} catch (InterruptedException e) {}
		}
		
		assertTrue(String.format("File %s should prove", file.getName()), proved);
	}
}

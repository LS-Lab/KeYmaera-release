package de.uka.ilkd.key.dl.regressiontest.issues.issue0001;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.ProverTaskListener;
import de.uka.ilkd.key.gui.TaskFinishedInfo;
import de.uka.ilkd.key.proof.ProblemLoader;

/**
 * Regression test for issue #0001.
 * Issue: QE with universal closure and saved reducevariables does not reload correctly
 * 
 * @author smitsch
 */
public class Issue0001Test {
	
	private boolean proved;
	private boolean running;
	private Object mutex;
	private Main main;
	
	@BeforeClass
	public void setUpClass() throws Exception {
		Main.configureLogger();
        Main.evaluateOptions(new String[] {"dL", "auto"});        
        main = Main.getInstance(false);
	}

	@Before
	public void setUp() throws Exception {
		proved = false;
		running = true;
	}

	@After
	public void tearDown() throws Exception {
		proved = false;
		running = false;
	}

	@Test
	public void test() {
		File file = new File("proofExamples/dev/issues_keymaera/issue_0001/nodelay_204.key.proof");
		
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
				proved = info.getProof().closed();
				mutex.notifyAll();
			}
			
		});
		pl.run();
		
		while (running) {
			try {
				mutex.wait();
			} catch (InterruptedException e) {}
		}
		
		assertTrue(String.format("File %s should prove", file.getName()), proved);
	}

}

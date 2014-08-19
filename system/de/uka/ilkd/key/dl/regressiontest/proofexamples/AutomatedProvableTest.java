package de.uka.ilkd.key.dl.regressiontest.proofexamples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uka.ilkd.key.dl.regressiontest.AbstractProofRegressionTest;

/**
 * Regression test for LICS tutorials.
 * 
 * @author smitsch
 */
@RunWith(Parameterized.class)
public class AutomatedProvableTest extends AbstractProofRegressionTest {
	
	/**
	 * Provides the test parameters (one file at a time).
	 * @return The files under test.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> data = new ArrayList<Object[]>();
		try (BufferedReader r = new BufferedReader(new FileReader("proofExamples/index/automaticDL.txt"))) {
			String testFileLine = r.readLine();
			// format in automaticDL.txt: ./fileName timeout
			String[] testFile = testFileLine.split(" ");
			// format for JUnit tests: proofExamples/fileName expectedResult
			data.add(new Object[] {testFile[0].replaceFirst("./", "proofExamples/"), 0});
		} catch (IOException e) {
			Assert.fail(String.format("Error accessing test data: %s", e.getMessage()));
		}
		
		return data;
		
//		return Arrays.asList(new Object[][] { 
//				{ "proofExamples/hybrid/tutorial/lics1-continuous-forward.key", 0 },
//				{ "proofExamples/hybrid/tutorial/lics2-hybrid-forward.key", 0 },
//				{ "proofExamples/hybrid/tutorial/lics3a-event-forward.key", 0 },
//				{ "proofExamples/hybrid/tutorial/lics3b-event-safe.key", 0 },
//				{ "proofExamples/hybrid/tutorial/lics3c-event-safe-stuck.key", 0 },
//				{ "proofExamples/hybrid/tutorial/lics4a-time-safe.key", 0 },
//				{ "proofExamples/hybrid/tutorial/lics4b-time-live.key", 0 },
//				{ "proofExamples/hybrid/tutorial/lics4c-time-safe-relative.key", 0 },
//				{ "proofExamples/hybrid/tutorial/lics5-controllability-equivalence.key", 0 },
//				{ "proofExamples/hybrid/tutorial/lics6-MPC-acceleration-equivalence.key", 0 },
//				{ "proofExamples/hybrid/tutorial/lics7-MPC.key", 0 },
//				{ "proofExamples/hybrid/tutorial/tutorial1-continuous.key", 0 },
//				{ "proofExamples/hybrid/tutorial/tutorial2-hybrid.key", 0 },
//				{ "proofExamples/hybrid/tutorial/tutorial3-event.key", 0 },
//				{ "proofExamples/hybrid/tutorial/tutorial3b-event.key", 0 },
//				{ "proofExamples/hybrid/tutorial/tutorial4-time.key", 0 },
//				{ "proofExamples/hybrid/tutorial/tutorial6-nondet_assignment.key", 0 },
//				{ "proofExamples/hybrid/tutorial/tutorial7-nondet_braking.key", 0 },
//				{ "proofExamples/hybrid/bouncing-ball/bouncing-ball-if.key", 0 },
//				{ "proofExamples/hybrid/ETCS/safety/ETCS-essentials.key", 0 },
//				{ "proofExamples/hybrid/ATC/roundabout/TRM-essentials.key", 0 },
//				{ "proofExamples/hybrid/ATC/roundabout/TRM-essentials2.key", 0 },
//				{ "proofExamples/hybrid/ETCS/paper/safety-lemma.key", 0 },
//				{ "proofExamples/hybrid/ETCS/paper/controllability-lemma.key", 0 },
//				{ "proofExamples/hybrid/ETCS/paper/reactivity-lemma.key", 0 },
//				{ "proofExamples/hybrid/ETCS/paper/controllability-lemma-disturbed.key", 0 },
//				{ "proofExamples/hybrid/switchstab-param.key", 0 },
//				{ "proofExamples/hybrid/dynamical/nonlinear1.key", 0 },
//				{ "proofExamples/hybrid/dynamical/Riccati.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl10.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl12.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl14.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl15.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl16.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl20.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl21.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl33.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl36.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl3.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl40.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl42.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl43.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl4.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl52.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl54.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl55.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl56.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl57.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl5.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl60.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl61.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl8.key", 0 },
//				{ "proofExamples/z3/nonlinear1/nl9.key", 0 },
//				{ "proofExamples/hybrid/aligator/conditional/lcm_gcd_dijkstra.key", 0 },
//				{ "proofExamples/hybrid/aligator/conditional/simple_example_1.key", 0 },
//				{ "proofExamples/hybrid/aligator/conditional/simple_example_2.key", 0 },
//				{ "proofExamples/hybrid/aligator/conditional/simple_example_3.key", 0 },
//				{ "proofExamples/hybrid/aligator/conditional/square_root_floor_dijkstra.key", 0 },
//				{ "proofExamples/hybrid/aligator/conditional/square_root_zuse.key", 0 },
//				{ "proofExamples/hybrid/aligator/conditional/wensey_division_wegbreit.key", 0 },
//				{ "proofExamples/hybrid/aligator/consecutive_cubes_cohen.key", 0 },
//				{ "proofExamples/hybrid/aligator/division_dijkstra.key", 0 },
//				{ "proofExamples/hybrid/aligator/fibonacci_knuth.key", 0 },
//				{ "proofExamples/hybrid/aligator/fibonacci_stansley.key", 0 },
//				{ "proofExamples/hybrid/aligator/HC_polyominoes_stansley.key", 0 },
//				{ "proofExamples/hybrid/aligator/integer_cubic_root_knuth.key", 0 },
//				{ "proofExamples/hybrid/aligator/integer_square_root_kirchner.key", 0 },
//				{ "proofExamples/hybrid/aligator/integer_square_root_knuth.key", 0 },
//				{ "proofExamples/hybrid/aligator/simple_example_1.key", 0 },
//				
//				
//				./hybrid/aligator/simple_example_2.key 600
//				./hybrid/aligator/simple_example_3.key 600
//				./hybrid/aligator/simple_example_4.key 600
//				./hybrid/aligator/sum_of_powers_5_petter.key 600
//				./hybrid/aligator/tribonacci_stansley.key 600
//				./hybrid/semi_definite_polynomials/quaternary2.key 600
//				./hybrid/semi_definite_polynomials/quaternary4.key 600
//				./hybrid/semi_definite_polynomials/ternary1.key 600
//				./hybrid/semi_definite_polynomials/ternary2.key 600
//				./hybrid/semi_definite_polynomials/ternary4.key 600
//				./hybrid/semi_definite_polynomials/ternary5.key 600
//				./hybrid/dynamical/nonlinear2.key 600
//				./hybrid/dynamical/nonlinear4.key 600
//				./hybrid/dynamical/nonlinear5.key 600
//				./hybrid/weispfennig/angle2.key 600
//				./hybrid/weispfennig/steiner-lehmus-theorem.key 600
//				./hybrid/weispfennig/pedos_inequality.key 600
//				./hybrid/accel-simple.key 600
//				./hybrid/magnetic_field.key 600
//				./hybrid/ETCS/controllability/ETCS-d-braking.key 600
//				./hybrid/water_tank/water_tank.key 600
//				./hybrid/ETCS/safety/ETCS-essentials.key 600
//				./hybrid/moving-point.key 600
//				./hybrid/ETCS/decomposed/controllability_lemma/to_left.key 600
//				./hybrid/ETCS/decomposed/controllability_lemma/to_right.key 600
//				./hybrid/ETCS/decomposed/essentials/accelerating.key 600
//				./hybrid/ETCS/decomposed/essentials/breaking.key 600
//				./hybrid/ETCS/decomposed/essentials/invariant-initially-valid.key 600
//				./hybrid/ETCS/decomposed/essentials/use-case.key 600
//				./hybrid/ETCS/decomposed/safety-lemma/initially-valid.key 600
//				./hybrid/ETCS/decomposed/safety-lemma/rbc_goal1.key 600
//				./hybrid/ETCS/decomposed/safety-lemma/rbc_goal2.key 600
//				./hybrid/ETCS/decomposed/safety-lemma/train_goal1.key 600
//				./hybrid/ETCS/decomposed/safety-lemma/train_goal2.key 600
//				./hybrid/ETCS/decomposed/safety-lemma/train_goal3.key 600
//				./hybrid/ETCS/decomposed/safety-lemma/train_goal4.key 600
//				./hybrid/ETCS/decomposed/safety-lemma/train_goal5.key 600
//				./hybrid/ETCS/decomposed/safety-lemma/train_goal6.key 600
//				./hybrid/ETCS/decomposed/safety-lemma/use-case.key 600
//				./hybrid/bouncing-ball/bouncing-ball.key 600
//				./hybrid/bouncing-ball/bouncing-ball-inv.key 600
//				./hybrid/bouncing-ball/bouncing-ball-simple.key 600
//				./hybrid/bouncing-ball/bouncing-ball-if.key 600
//				./hybrid/ETCS/safety/binary_driver-2007-10-09.key 600
//				./hybrid/ETCS/paper/rbc-controllability-lemma.key 600
//				./hybrid/ETCS/paper/rbc-controllability-characterisation.key 600
//				./hybrid/ATC/roundabout/TRM-essentials.key 600
//				./hybrid/ATC/roundabout/TRM-essentials2.key 600
//				./hybrid/weispfennig/angle.key 600
//				./hybrid/ETCS/paper/safety-lemma.key 600
//				./hybrid/ETCS/paper/controllability-lemma.key 600
//				./hybrid/ETCS/paper/reactivity-lemma.key 600
//				./hybrid/ETCS/paper/controllability-lemma-disturbed.key 600
//				./hybrid/ATC/roundabout/TRM-essentials-3.key 600
//				./hybrid/ATC/roundabout/bounded-angular-linear-speed.key 600
//				./hybrid/ATC/roundabout/FTRM-entry-tang-simplified.key 600
//				./hybrid/ATC/roundabout/FTRM-entry-tang-feasible.key 600
//				./hybrid/ATC/roundabout/FTRM-entry-tang-circular.key 600
//				./hybrid/ATC/roundabout/limited-progress.key 600
//				./hybrid/ATC/roundabout/FTRM-agree-mutual2-all.key 600
//				./hybrid/ATC/roundabout/FTRM-agree-mutual2-simplified.key 600
//				./hybrid/ATC/roundabout/FTRM-agree-mutual2-far-all.key 600
//				./hybrid/ATC/roundabout/exit-simultaneous-indep-simplified.key 600
//				./hybrid/ATC/roundabout/exit-simultaneous-different-directions.key 600
//				./hybrid/ATC/roundabout/linear-progress.key 600 
//				./hybrid/switchstab-param.key 600
//				./hybrid/dynamical/nonlinear1.key 600
//				./hybrid/dynamical/Riccati.key 600
//				./hybrid/dynamical/nonlinear-diffcut.key 600
//				./hybrid/complicated_arithmetic/train.key 600
//				./hybrid/complicated_arithmetic/complicated-trivial.key 600
//				./hybrid/complicated_arithmetic/ETCS-essentials-surprise2.key 600
//				./realTacletPOs/decompose_fract2.key 30
//				./realTacletPOs/decompose_fract.key 30
//				./realTacletPOs/decompose_mult.key 30
//				./realTacletPOs/div_axiom.key 30
//				./realTacletPOs/elimGcdEq.key 30
//				./realTacletPOs/elimGcdGreater.key 30
//				./realTacletPOs/elimGcdLeq.key 30
//				./realTacletPOs/inEqSimp_contradInEq20.key 30
//				./realTacletPOs/inEqSimp_contradInEq2.key 30
//				./realTacletPOs/inEqSimp_exactShadow01.key 30
//				./realTacletPOs/inEqSimp_exactShadow0.key 30
//				./realTacletPOs/inEqSimp_subsumption20.key 30
//				./realTacletPOs/inEqSimp_subsumption2.key 30
//				./realTacletPOs/multiply_inEq0.key 30
//				./realTacletPOs/multiply_inEq2.key 30

//		});
	}
	
	/**
	 * Initializes a new test with the specified file under test.
	 * @param fileName The file under test.
	 */
	public AutomatedProvableTest(String fileName, int expected) {
		super(fileName, expected);
	}

}

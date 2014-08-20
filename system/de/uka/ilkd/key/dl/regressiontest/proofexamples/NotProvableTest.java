package de.uka.ilkd.key.dl.regressiontest.proofexamples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uka.ilkd.key.dl.regressiontest.AbstractProofRegressionTest;

/**
 * Regression test for invalid formulas.
 * 
 * @author smitsch
 */
@RunWith(Parameterized.class)
public class NotProvableTest extends AbstractProofRegressionTest {
	
	/**
	 * Provides the test parameters (one file at a time).
	 * @return The files under test.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> data = new ArrayList<Object[]>();
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader("proofExamples/index/notProvableDL.txt"));
			String testFileLine = r.readLine();
			while (testFileLine != null) {
				// format in automaticDL.txt: ./fileName timeout expectedResult
				String[] testFile = testFileLine.split(" ");
				// format for JUnit tests: proofExamples/fileName expectedResult
				data.add(new Object[] {testFile[0].replaceFirst("./", "proofExamples/"), Integer.parseInt(testFile[2])});
				testFileLine = r.readLine();
			}
		} catch (IOException e) {
			Assert.fail(String.format("Error accessing test data: %s", e.getMessage()));
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					Assert.fail(String.format("Error closing test data: %s", e.getMessage()));
				}
			}
		}
		
		return data;
	}
	
	/**
	 * Initializes a new test with the specified file under test.
	 * @param fileName The file under test.
	 */
	public NotProvableTest(String fileName, int expected) {
		super(fileName, expected);
	}

}

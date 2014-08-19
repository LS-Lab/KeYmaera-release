package de.uka.ilkd.key.dl.regressiontest;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.uka.ilkd.key.dl.gui.KeYmaera;

/**
 * Base class for proof regression tests.
 * Subclasses must implement the static data() method to provide
 * the test files.
 * 
 * @author smitsch
 */
@RunWith(Parameterized.class)
public abstract class AbstractProofRegressionTest {
	private static final String JARS_DIR = System.getProperty("user.dir") + "/../key-ext-jars/";
	
	private static final String TMP_DIR = "/tmp/keymaeratests/";
	
	private static final String CLASS_PATH =
			JARS_DIR + "keymaera.jar" + ":" 
		  + JARS_DIR + "antlr-3.4-complete.jar" + ":"
		  + JARS_DIR + "log4j.jar" + ":"
		  + JARS_DIR + "JLink/JLink.jar" + ":"
		  + JARS_DIR + "orbital-core.jar" + ":"
		  + JARS_DIR + "orbital-ext.jar" + ":"
		  + JARS_DIR + "scala-library.jar" + ":"
		  + JARS_DIR + "scala-swing.jar" + ":"
		  + JARS_DIR + "recoderKey.jar" + ":"
		  + JARS_DIR + "commons-compress-1.4.jar" + ":"
		  + JARS_DIR + "jmathplot.jar";
	
	private static final String JLINK_NATIVE_LIB_DIR = "/Applications/Mathematica.app/SystemFiles/Links/JLink/SystemFiles/Libraries/MacOSX-x86-64";
	
	/**
	 * The name of the file under test.
	 */
	private File file;
	
	/**
	 * The expected test result.
	 */
	private int expected;
	
	/**
	 * The KeYmaera process.
	 */
	private Process p;
	
	/**
	 * Initializes a new proof regression test object with
	 * the specified proof file under test.
	 * @param fileName The file under test.
	 */
	protected AbstractProofRegressionTest(String fileName, int expected) {
		this.file = new File(fileName);
		this.expected = expected;
	}
	
	/**
	 * Sets up the test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		if (!Files.exists(FileSystems.getDefault().getPath(TMP_DIR))) {
			Files.createDirectory(FileSystems.getDefault().getPath(TMP_DIR));
		}
		Files.copy(file.toPath(), 
				FileSystems.getDefault().getPath(TMP_DIR, file.getName()), 
				StandardCopyOption.REPLACE_EXISTING);
		
		ProcessBuilder pb = new ProcessBuilder("java",
				"-ea:de.uka.ilkd.key...", "-Xms64m", "-Xmx1024m",
				String.format("-Dcom.wolfram.jlink.libdir=%s", JLINK_NATIVE_LIB_DIR),
				KeYmaera.class.getName(), TMP_DIR + file.getName(), "dL", "auto");
		pb.directory(new File(TMP_DIR));
		pb.environment().put("CLASSPATH", CLASS_PATH);
		pb.environment().put("JAVA_HOME", System.getProperty("java.home"));
		pb.environment().put("JLINK_LIB_DIR", JLINK_NATIVE_LIB_DIR);
		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);
		p = pb.start();
	}

	/**
	 * Cleans up after testing.
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		p.destroy();
		p = null;
		Files.deleteIfExists(FileSystems.getDefault().getPath(TMP_DIR, file.getName()));
		Files.deleteIfExists(FileSystems.getDefault().getPath(TMP_DIR, "*.auto.*.proof"));
	}

	/**
	 * The test method. Called once per entry in the static
	 * data() parameter provider.
	 * @throws Exception If the KeYmaera child process throws an exception 
	 */
	@Test
	public void test() throws Exception {
		int result = p.waitFor();
		Assert.assertEquals(String.format("File %s", file.getName()), expected, result);
	}
}

package nbcu.compass.amorttemplate.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.TestNG;

public class MainClass {

	public static void main(String args[]) {
		List<String> file = new ArrayList<String>();
		File directory = new File(".");
		String strBasepath;
		try {
			strBasepath = directory.getCanonicalPath();
			file.add(strBasepath+File.separator+"testng.xml");
		    TestNG testNG = new TestNG();
		    testNG.setTestSuites(file);
		    testNG.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

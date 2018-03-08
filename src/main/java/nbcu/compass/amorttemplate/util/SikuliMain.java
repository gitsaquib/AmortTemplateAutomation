package nbcu.compass.amorttemplate.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.Keys;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.testng.annotations.Test;

public class SikuliMain {
	
	@Test
	public void testM() {
		Screen screen = new Screen();
		File directory = new File(".");
		String strBasepath;
		try {
			Thread.sleep(10000);
			strBasepath = directory.getCanonicalPath();
			String iconPath = strBasepath + File.separator + "images" + File.separator;
			Pattern username = new Pattern(iconPath + "username.png");
			Pattern password = new Pattern(iconPath + "password.png");
			Pattern signin = new Pattern(iconPath + "signin.png");
			Pattern network = new Pattern(iconPath + "network.png");
			Pattern distributor = new Pattern(iconPath + "distributor.png");
			Pattern distributorpackage = new Pattern(iconPath + "distributorpackage.png");
			Pattern dealtype = new Pattern(iconPath + "dealtype.png");
			Pattern negotiatedby = new Pattern(iconPath + "negotiatedby.png");
			/*
			screen.wait(username, 10);	
			screen.type(username, "206534643");
			screen.type(password, "toddler@10");
			screen.click(signin);
			*/
			screen.doubleClick(network);
			for(int i=0;i<11; i++) {
				screen.type(Key.BACKSPACE);	
			}
			screen.type("SYFY");
			Thread.sleep(10000);
			SimpleDateFormat df = new SimpleDateFormat("YYYYMMDDHHmmss");
			String packageName = "TestPackage_" + df.format(new Date());
			screen.type(distributorpackage, packageName);
			screen.type(distributor, "10X10 Entertainment");
			screen.type(Key.TAB);
			Thread.sleep(2000);
			screen.type(dealtype, "Cash");
			screen.type(negotiatedby, "LEGAL");
			
		} catch (IOException | FindFailed | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void startExeApp(String appPath) {
		try {
			Process process = new ProcessBuilder(appPath).start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch(Exception e) {
			return;
		}
	}
}
/*
// Creating Object of 'Screen' class
//Screen is a base class provided by Sikuli. It allows us to access all the methods provided by Sikuli.
Screen screen = new Screen();
// Creating Object of Pattern class and specify the path of specified images
// I have captured images of Facebook Email id field, Password field and Login button and placed in my local directory
// Facebook user id image 
Pattern username = new Pattern("C:\\Users\\admin\\Desktop\\Sikuli Images For Selenium\\FacebookEmail.png");
// Facebook password image
Pattern password = new Pattern("C:\\Users\\admin\\Desktop\\Sikuli Images For Selenium\\FacebookPassword.png");
// Facebook login button image
Pattern login = new Pattern("C:\\Users\\admin\\Desktop\\Sikuli Images For Selenium\\FacebookLogin.png");
// Initialization of driver object to launch firefox browser 
System.setProperty("webdriver.gecko.driver", System.getProperty("user.dir")+"\\src\\drivers\\geckodriver.exe");
WebDriver driver = new FirefoxDriver();
// To maximize the browser
driver.manage().window().maximize();
// Open Facebook
driver.get("https://en-gb.facebook.com/");
screen.wait(username, 10);	 
// Calling 'type' method to enter username in the email field using 'screen' object
screen.type(username, "softwaretestingmaterial@gmail.com");
// Calling the same 'type' method and passing text in the password field
screen.type(password, "softwaretestingmaterial");
// This will click on login button
screen.click(login);
*/

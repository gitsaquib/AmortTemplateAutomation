package nbcu.compass.amorttemplate.util;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;

public class AutomationAgent {
	
	@SuppressWarnings("rawtypes")
	private static WindowsDriver appSession = null;
	
	private Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
	
	@SuppressWarnings("rawtypes")
	public void launchApplicationFromBrowser(String url, String appWebUrl) {
		WebDriver ieDriver = null;
		File directory = new File(".");
		String strBasepath;
		try {
			strBasepath = directory.getCanonicalPath();
			System.setProperty("webdriver.ie.driver", strBasepath + File.separator + "TestData"+ File.separator +"IEDriverServer.exe");
			DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
			ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			ieDriver = new InternetExplorerDriver(ieCapabilities);
			ieDriver.manage().window().maximize();
			ieDriver.navigate().to(appWebUrl);
			Thread.sleep(10000);
			DesiredCapabilities appCapabilities = new DesiredCapabilities();
			appCapabilities.setCapability("app", "Root");
			WindowsDriver<WindowsElement> driver = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities);
			WebElement cortana = driver.findElementByName("TrustManagerDialog");
			String handleStr = cortana.getAttribute("NativeWindowHandle");
			int handleInt = Integer.parseInt(handleStr);
			String handleHex = Integer.toHexString(handleInt);
			DesiredCapabilities appCapabilities2 = new DesiredCapabilities();
			appCapabilities2.setCapability("appTopLevelWindow", handleHex);
			WindowsDriver driver2 = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities2);
			driver2.getKeyboard().sendKeys(Keys.chord(Keys.ALT, "r"));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			ieDriver.quit();
		}
	}
	
	public void closeApplication() {
		appSession.closeApp();
		clickYesOnPopup("Are you sure you want to exit the application?");
	}
	
	public void clickAt(int x, int y) {
		Actions vActions = new Actions(appSession);
		vActions.moveToElement(appSession.findElementByName("System"), 0,0);
		Action vClickAction = vActions.build();
		vClickAction.perform();
        vActions.moveByOffset(x, y);
		vActions.doubleClick();
		vClickAction = vActions.build();
		vClickAction.perform();
    }

	public Map<Integer, String> generateAmort(double totalLicenseFee) throws InterruptedException {
		appSession.findElementByName("Amortize").click();
		appSession.findElementByName("Generate Amort").click();
		clickYesOnPopup("All old amort data will be deleted. Do you want to continue?");
		clickYesOnPopup("Effective Date should be earlier or equal to the Amort Window Start Date");
		readAmortAmtRows(totalLicenseFee, 0);
		return amorts;
	}

	@SuppressWarnings("unchecked")
	private void readAmortAmtRows(double totalLicenseFee, int previousLastMonth) {
		try {
			int scrollCount = 0;
			List<WebElement> dataRows = appSession.findElements(By.className("DataRow"));
			int lastMonth = 0;
			for(WebElement dataRow:dataRows) {
				if(dataRow.getLocation().getY() < 0) {
					continue;
				}
				dataRow.click();
				WebElement amortAmt = dataRow.findElement(By.name("Amort Amt"));
				WebElement month = dataRow.findElement(By.name("Months in Contract"));
				String amtStr = amortAmt.getText();
				amorts.put(Integer.parseInt(month.getText()), amtStr);
				amtStr = amtStr.replace("$", "");
				amtStr = amtStr.replace(",", "");
				double amtDouble = Double.parseDouble(amtStr);
				totalLicenseFee = totalLicenseFee - amtDouble;
				lastMonth = Integer.parseInt(month.getText());
				scrollCount++;
			}
			if(lastMonth==previousLastMonth) {
				return;
			} else {
				if(totalLicenseFee > 0) {
					for(int i=0; i<scrollCount-1; i++) {
						appSession.getKeyboard().sendKeys(Keys.ARROW_DOWN);
					}
					readAmortAmtRows(totalLicenseFee, lastMonth);
				} else {
					System.out.println("TotalLicenseFee left:"+totalLicenseFee);
				}
			}
		} catch (Exception ex) {
			System.out.println("Unable to read rows in Amortize tab");
			ex.printStackTrace();
			return;
		}
	}
	
	private void clickYesOnPopup(String message) {
		try {
			WebElement element = appSession.findElementByName(message);
			if(null != element) {
				appSession.findElementByName("Yes").click();
			}
		} catch (Exception e) {
			;
		}
	}
	
	@SuppressWarnings("unchecked")
	public double setAllocationData(String licenseType, String licenseAmount, String amortTemplate) throws InterruptedException {
		double totalLicenseFee = 0.0;
		appSession.findElementByName("Allocation").click();
		appSession.findElementByAccessibilityId("Row_0").click();
		Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
		Actions vActions = new Actions(appSession);
		moveByOffsetAndClick(vActions, -125, 0);
		moveByOffsetAndClick(vActions, -7, 28);
		moveByOffsetAndClick(vActions, 45, 0);
		Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
		appSession.getKeyboard().sendKeys("");
		appSession.getKeyboard().sendKeys(licenseType);
		moveByOffsetAndClick(vActions, 60, 0);
		appSession.getKeyboard().sendKeys(licenseAmount);
		appSession.findElementByAccessibilityId("SaveButton").click();
		Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
		clickElement("License Fee Type");
		moveByOffsetAndClick(vActions, -35, 0);
		moveByOffsetAndClick(vActions, -30, 0);
		moveByOffsetAndClick(vActions, 0, 30);
		clickElement("License Fee Type");
		clickElement("Amortization Template");
		appSession.getKeyboard().sendKeys(amortTemplate);
		appSession.findElementByAccessibilityId("SaveButton").click();
		Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
		List<WebElement> elements = appSession.findElementsByName("Total License Fee");
		for(WebElement element:elements) {
			WebElement textBlock = element.findElement(By.className("TextBlock"));
			String text = textBlock.getText();
			if(!text.endsWith("Total License Fee")) {
				text = text.replace("$","");
				text = text.replace(",", "");
				totalLicenseFee = Double.parseDouble(text);
			}
		}
		return totalLicenseFee;
	}

	private void moveByOffsetAndClick(Actions vActions, int x, int y) {
		vActions.moveByOffset(x, y);
		vActions.click();
		Action vClickAction = vActions.build();
		vClickAction.perform();
	}
	
	@SuppressWarnings("unchecked")
	private void clickElement(String ele) {
		List<WebElement> elements;
		elements = appSession.findElementsByName(ele);
		for(WebElement element:elements) {
			try {
				WebElement textBox = element.findElement(By.className("Cell"));
				if(null != textBox) 
				{
					element.click();
					textBox.click();
					return;
				}
			} catch (Exception e) {
				;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setPlayWindowAttribute(String key, String value) throws InterruptedException {
		List<WebElement> elements = appSession.findElementsByName(key);
		for(WebElement element:elements) {
			if(element.getText().equals("") || element.getText().equals("0")) {
				element.click();
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				appSession.getKeyboard().sendKeys(value);
			}
		}
	}

	public void openTitleAndWindow(String financeType, List<Window> windows) throws InterruptedException {
		appSession.findElementByAccessibilityId("Row_0").click();
		Actions vActions = new Actions(appSession);
		vActions.moveByOffset(-170, 0);
		vActions.doubleClick();
		Action vClickAction = vActions.build();
		vClickAction.perform();
		Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
		appSession.findElementByName("Window").click();
		for(Window window:windows) {
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			appSession.findElementByName("AddWindow").click();
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			setPlayWindowAttribute("Start Date", window.getStartDate());
			setPlayWindowAttribute("End Date", window.getEndDate());
			setPlayWindowAttribute("Runs in Play Day", window.getRunInPlayDay());
			setPlayWindowAttribute("Runs/PD Allowed", window.getRunsPDAllowed());
		}
		clearAndSetValueInDropdown("FinanceTypeCombobox", financeType);
		setValueInDropdown("MasterSeriesCombobox", "TEST 123");
		appSession.findElementByName("TEST 123").click();
		appSession.findElementByAccessibilityId("SaveButton").click();
		Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
	}
	
	public void launchAppUsingNativeWindowHandle(String appPath, String url, String appName) {
		Process process;
		DesiredCapabilities appCapabilities = new DesiredCapabilities();
		appCapabilities.setCapability("app", "Root");
		try {
			process = new ProcessBuilder(appPath).start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
			WindowsDriver<WindowsElement> driver = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities);
			WebElement cortana = driver.findElementByName(appName);
			String handleStr = cortana.getAttribute("NativeWindowHandle");
			int handleInt = Integer.parseInt(handleStr);
			String handleHex = Integer.toHexString(handleInt);
			DesiredCapabilities appCapabilities2 = new DesiredCapabilities();
			appCapabilities2.setCapability("appTopLevelWindow", handleHex);
			appSession = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities2);
			appSession.manage().window().maximize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loginCompass(String username, String password) throws InterruptedException {
		appSession.findElementByAccessibilityId("username").clear();
		appSession.findElementByAccessibilityId("username").sendKeys(username);
		appSession.findElementByAccessibilityId("password").clear();
		appSession.findElementByAccessibilityId("password").sendKeys(password);
		appSession.findElementByName("Sign In").click();
		Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
		Log.pass("User ["+username+"] successfully logged in Compass application");
	}

	public void createContract(String distributor, String dealType, String negotiatedBy, String titleName, String titleType) throws InterruptedException {
		appSession.findElementByName("Create New Contract").click();
		Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
		SimpleDateFormat df = new SimpleDateFormat("YYYYMMDDHHmmss");
		String packageName = "TestPackage_" + df.format(new Date());
		appSession.findElementByAccessibilityId("DistributorPackageTextBox").sendKeys(packageName);
		setValueInDropdown("DistributorComboBox", distributor);
		setValueInDropdown("DealTypeCombobox", dealType);
		setValueInDropdown("NegotiatedByCombobox", negotiatedBy);
		addTitle(titleName, titleType);
		appSession.findElementByAccessibilityId("SaveButton").click();
		Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
		Log.pass("Successfully created contract with DistributorPackage: "+ packageName);
	}

	@SuppressWarnings("unchecked")
	private void addTitle(String titleName, String titleType) {
		try {
			List<WebElement> elements = appSession.findElements(By.className("HeaderFooterCell"));
			boolean titleNameSet = false, titleTypeSet = false;
			for(WebElement element:elements) {
				if(titleNameSet && titleTypeSet) {
					return;
				}
				element.click();
				try {
					WebElement textBox = element.findElement(By.className("TextBox"));
					if(null != textBox) 
					{
						textBox.click();
						textBox.sendKeys(titleName);
						titleNameSet= true;
					}
				} catch (Exception e) {
					WebElement comboBox = element.findElement(By.className("RadComboBox"));
					if(null != comboBox) 
					{
						comboBox.click();
						Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
						WebElement webElement = appSession.findElementByName(titleType);
						if(null != webElement) {
							webElement.click();
							appSession.getKeyboard().sendKeys(titleType);
							titleTypeSet= true;
						}
					}
				}
			}
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
		} catch(InterruptedException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void clearAndSetValueInDropdown(String key, String value) {
		WebElement parent =  appSession.findElementByAccessibilityId(key);
        parent.click();
        WebElement textBox = parent.findElement(By.className("TextBox"));
        String existingValue = textBox.getText();
        textBox.click();
        for(int i=0; i<existingValue.length(); i++) {
        	appSession.getKeyboard().sendKeys(Keys.BACK_SPACE);
        }
        appSession.getKeyboard().sendKeys(Keys.CLEAR);
        appSession.getKeyboard().sendKeys(value);
	}
	
	private void setValueInDropdown(String key, String value) {
		WebElement parent =  appSession.findElementByAccessibilityId(key);
        parent.click();
        WebElement textBox = parent.findElement(By.className("TextBox"));
    	textBox.click();
    	textBox.sendKeys(value);
	}
	
	public String setTableStyleForExtentReport() {
		return
				"<style type=\"text/css\">\r\n" + 
                ".tg  {border-collapse:collapse;border-spacing:0;border-color:#999;}\r\n" + 
                ".tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#999;color:#444;background-color:#F7FDFA;}\r\n" + 
                ".tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#999;color:#fff;background-color:#26ADE4;}\r\n" + 
                ".tg .tg-3fs7{font-size:11px;font-family:Tahoma, Geneva, sans-serif !important;;vertical-align:top}\r\n" + 
                ".tg .tg-yw4l{vertical-align:top}\r\n" + 
                "</style>";
	}
	
	public String openTable() {
		return "<table class=\"tg\">";
	}
	
	public String addTableHeader() {
		return "<th><td class=\"tg-3fs7\">Month</td><td class=\"tg-yw4l\">Application</td><td class=\"tg-yw4l\">Amort Calulation</td></th>";
	}
	
	public String setTableBodyForExtentReport(String month, String valueFromApp, String valueFromCalculation) {
		return "<tr><td class=\"tg-yw4l\">"+month+"</td><td class=\"tg-yw4l\">"+valueFromApp+"</td><td class=\"tg-yw4l\">+"+valueFromCalculation+"+</td></tr>";
	}
	
	public String closeTable() {
		return "</table>";
	}
}

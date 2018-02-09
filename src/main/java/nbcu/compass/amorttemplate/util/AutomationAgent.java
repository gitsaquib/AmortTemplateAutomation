package nbcu.compass.amorttemplate.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
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
	
	public void writeResultInTxtFile(String network, String status) {
		Log.message("writeResultInTxtFile: writing results in txt file: "+ status);
		try {
			File directory = new File(".");
			File txtFile = new File(directory.getCanonicalPath() + File.separator + "TestData"+ File.separator + "AmortTemplate"+network+"-Results.txt");
			FileWriter fw = new FileWriter(txtFile, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(status);
			pw.close();
		} catch (IOException e) {
			Log.fail(e.getMessage(), appSession);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static WindowsDriver getAppSession() {
		return appSession;
	}

	private Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
	
	@SuppressWarnings("rawtypes")
	public void launchApplicationFromBrowser(String url, String appWebUrl) {
		Log.message("launchApplicationFromBrowser: launching app from web browser");
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
			ieDriver.quit();
		} catch (IOException | InterruptedException e) {
			if(null != ieDriver) {
				ieDriver.quit();
			}
			Log.fail(e.getMessage(), appSession);
		}
	}
	
	public void closeApplication() {
		Log.message("closeApplication: Closing app session");
		try {
			if(null != appSession) {
				appSession.closeApp();
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				if(!clickYesOrNoOnPopup("Do you want to save the changes?", "No")) {
					Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
					clickYesOrNoOnPopup("Are you sure you want to exit the application?", "Yes");
				}
			}
		} catch(Exception e) {
			Log.fail(e.getMessage(), appSession);
		}
	}
	
	public void clickAt(int x, int y) {
		Log.message("clickAt: clicking on x: "+x+", y: "+y);
		Actions vActions = new Actions(appSession);
		vActions.moveToElement(appSession.findElementByName("System"), 0,0);
		Action vClickAction = vActions.build();
		vClickAction.perform();
        vActions.moveByOffset(x, y);
		vActions.doubleClick();
		vClickAction = vActions.build();
		vClickAction.perform();
    }

	public Map<Integer, String> generateAmort(double totalLicenseFee) {
		Log.message("generateAmort: generating amort for "+totalLicenseFee);
		try {
			appSession.findElementByName("Amortize").click();
			appSession.findElementByName("Generate Amort").click();
			clickYesOrNoOnPopup("All old amort data will be deleted. Do you want to continue?", "Yes");
			clickYesOrNoOnPopup("Effective Date should be earlier or equal to the Amort Window Start Date", "Yes");
			readAmortAmtRows(totalLicenseFee, 0);
			return amorts;
		} catch(Exception e) {
			Log.fail(e.getMessage(), appSession);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private void readAmortAmtRows(double totalLicenseFee, int previousLastMonth) {
		Log.message("readAmortAmtRows: read amort amounts");
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
		} catch (Exception e) {
			Log.fail(e.getMessage(), appSession);
			return;
		}
	}
	
	private boolean clickYesOrNoOnPopup(String message, String yesOrNo) {
		Log.message("clickYesOrNoOnPopup: click on yes or no button");
		try {
			WebElement element = appSession.findElementByName(message);
			if(null != element) {
				appSession.findElementByName(yesOrNo).click();
				return true;
			}
		} catch (Exception e) {
			;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public Double setAllocationData(String licenseType, String licenseAmount, String amortTemplate) {
		Log.message("setAllocationData: licenseType"+licenseType+", licenseAmount: "+licenseAmount+", amortTemplate: "+amortTemplate);
		try {
			double totalLicenseFee = 0.0;
			appSession.findElementByName("Allocation").click();
			appSession.findElementByAccessibilityId("Row_0").click();
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Actions vActions = new Actions(appSession);
			moveByOffsetAndClick(vActions, -125, 0);
			moveByOffsetAndClick(vActions, -7, 28);
			appSession.getKeyboard().sendKeys(licenseType);
			moveByOffsetAndClick(vActions, 100, 0);
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
		} catch(Exception e) {
			Log.fail(e.getMessage(), appSession);
			return null;
		}
	}

	private void moveByOffsetAndClick(Actions vActions, int x, int y) {
		Log.message("moveByOffsetAndClick: x: "+x+", y: "+y);
		vActions.moveByOffset(x, y);
		vActions.click();
		Action vClickAction = vActions.build();
		vClickAction.perform();
	}
	
	@SuppressWarnings("unchecked")
	private void clickElement(String ele) {
		Log.message("clickElement: "+ele);	
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
	private void setPlayWindowAttribute(String key, String value) {
		Log.message("setPlayWindowAttribute: key: "+key+", value: "+value);
		try {
			List<WebElement> elements = appSession.findElementsByName(key);
			for(WebElement element:elements) {
				if(element.getText().equals("") || element.getText().equals("0")) {
					element.click();
					Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
					appSession.getKeyboard().sendKeys(value);
				}
			}
		} catch(Exception e) {
			Log.fail(e.getMessage(), appSession);
		}
	}

	public void openTitleAndWindow(String financeType, List<Window> windows) {
		Log.message("openTitleAndWindow: financeType: "+financeType+", windows: "+windows);
		try {
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
			if(isElementFoundByAccessibilityId("MasterSeriesCombobox")) {	
				setValueInDropdown("MasterSeriesCombobox", "TEST 123");
				appSession.findElementByName("TEST 123").click();
			}
			appSession.findElementByAccessibilityId("SaveButton").click();
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
		} catch(Exception e) {
			Log.fail(e.getMessage(), appSession);
		}
	}
	
	public void launchAppUsingNativeWindowHandle(String appPath, String url, String appName, boolean retry) {
		Log.message("launchAppUsingNativeWindowHandle: Launching app using window handle");
		try {
			DesiredCapabilities appCapabilities = new DesiredCapabilities();
			appCapabilities.setCapability("app", "Root");
			WindowsDriver<WindowsElement> driver = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities);
			WebElement element = getElement(driver, By.name(appName));
			if(element == null) {
				if(!retry) {
					startExeApp(appPath);
					launchAppUsingNativeWindowHandle(appPath, url, appName, true);
				}
			} else {
				String handleStr = element.getAttribute("NativeWindowHandle");
				int handleInt = Integer.parseInt(handleStr);
				String handleHex = Integer.toHexString(handleInt);
				DesiredCapabilities appCapabilities2 = new DesiredCapabilities();
				appCapabilities2.setCapability("appTopLevelWindow", handleHex);
				appSession = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities2);
				appSession.manage().window().maximize();
			}
		} catch (Exception e) {
			Log.fail(e.getMessage(), appSession);
		}
	}
	
	private WebElement getElement(WindowsDriver<WindowsElement> driver, By by) {
		Log.message("getElement: By: "+by);
		try {
			WebElement element = driver.findElement(by);
			return element;
		} catch(Exception e) {
			return null;
		}
	}

	private void startExeApp(String appPath) {
		Log.message("startExeApp: appPath: "+appPath);
		try {
			Process process = new ProcessBuilder(appPath).start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
		} catch(Exception e) {
			return;
		}
	}
	
	public void killApp() {
		Log.message("killApp: taskkill /F /IM NBCU.Compass.exe");
		try {
			Runtime rt = Runtime.getRuntime();
			rt.exec("taskkill /F /IM NBCU.Compass.exe");
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
		} catch(Exception e) {
			return;
		}
	}
	
	public void readPopupMessage() {
		Log.message("readPopupMessage: read message from popup");
		try {
			double coordinates[] = SikuliImageRecognitionUtil.findImage(appSession, "no-amort-rule.png", 169, 18);
			if(null != coordinates) {
				System.out.println(coordinates[0]+" - "+coordinates[1]);
			}
		} catch (Exception e) {
			Log.fail(e.getMessage(), appSession);
		}
	}

	public void loginCompass(String username, String password, String displayName) {
		Log.message("loginCompass: Logging in application for user: "+username);
		try {
			if(isElementFoundByAccessibilityId("username")) {
				appSession.findElementByAccessibilityId("username").clear();
				appSession.findElementByAccessibilityId("username").sendKeys(username);
				appSession.findElementByAccessibilityId("password").clear();
				appSession.findElementByAccessibilityId("password").sendKeys(password);
				appSession.findElementByName("Sign In").click();
				Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
				Log.pass("User ["+username+"] successfully logged in Compass application");
			} else {
				if(isElementFoundByName(displayName)) {
					Log.message("User ["+username+"] already logged in Compass application");
					if(isElementFoundByName("CloseButton")) {
						appSession.findElementByName("CloseButton").click();
						clickYesOrNoOnPopup("Do you want to save the changes?", "No");
						Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
						appSession.findElementByName("Contract Management").click();
					}
				}
				
			}
		} catch(Exception e) {
			Log.fail(e.getMessage(), appSession);
		}
	}

	public void createContract(String distributor, String dealType, String negotiatedBy, String titleName, String titleType) {
		Log.message("createContract: distributor: "+distributor+", dealType: "+dealType+", negotiatedBy: "+negotiatedBy);
		try {
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
		} catch(Exception e) {
			Log.fail(e.getMessage(), appSession);
		}
	}

	@SuppressWarnings("unchecked")
	private void addTitle(String titleName, String titleType) {
		Log.message("addTitle: titleName: "+titleName+", titleType: "+titleType);
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
					if(null != textBox) {
						textBox.click();
						textBox.sendKeys(titleName);
						titleNameSet= true;
					}
				} catch (Exception e) {
					WebElement comboBox = element.findElement(By.className("RadComboBox"));
					if(null != comboBox) {
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
		} catch(Exception e) {
			Log.fail(e.getMessage(), appSession);
		}
	}
	
	private void clearAndSetValueInDropdown(String key, String value) {
		Log.message("clearAndSetValueInDropdown: key:"+key+", value: "+value);
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
		Log.message("setValueInDropdown: key:"+key+", value: "+value);
		WebElement parent =  appSession.findElementByAccessibilityId(key);
        parent.click();
        WebElement textBox = parent.findElement(By.className("TextBox"));
    	textBox.click();
    	textBox.sendKeys(value);
	}
	
	private boolean isElementFoundByAccessibilityId(String key) {
		Log.message("isElementFound: "+key);
		try {
			WebElement element =  appSession.findElementByAccessibilityId(key);
			return true;
		} catch(Exception e) {
			return false;	
		}
	}
	
	private boolean isElementFoundByName(String key) {
		Log.message("isElementFound: "+key);
		try {
			WebElement element =  appSession.findElementByName(key);
			return true;
		} catch(Exception e) {
			return false;	
		}
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
		return "<tr><td class=\"tg-yw4l\">Month</td><td class=\"tg-yw4l\">Application</td><td class=\"tg-yw4l\">Amort Calulation</td></tr>";
	}
	
	public String setTableBodyForExtentReport(String month, String valueFromApp, String valueFromCalculation) {
		return "<tr><td class=\"tg-yw4l\">"+month+"</td><td class=\"tg-yw4l\">"+valueFromApp+"</td><td class=\"tg-yw4l\">"+valueFromCalculation+"</td></tr>";
	}
	
	public String closeTable() {
		return "</table>";
	}
}

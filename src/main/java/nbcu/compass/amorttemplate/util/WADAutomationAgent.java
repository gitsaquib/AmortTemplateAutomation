package nbcu.compass.amorttemplate.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;
import nbcu.compass.amorttemplate.factory.AutomationAgent;

public class WADAutomationAgent extends AutomationAgent {
	
	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();
	
	private static String iconPath = "";
	static {
		try {
			iconPath = new File(".").getCanonicalPath() + File.separator + "images" + File.separator;
			Settings.OcrTextRead = true;
			Settings.OcrTextSearch = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private Screen screen = null;
	
	@SuppressWarnings("rawtypes")
	private static WindowsDriver appSession = null;
	
	@SuppressWarnings("rawtypes")
	public static WindowsDriver getAppSession() {
		return appSession;
	}

	@SuppressWarnings("rawtypes")
	public void launchApplicationFromBrowser(String url, String appWebUrl, String statusMessage) {
		Log.message("Start launchApplicationFromBrowser: launching app from web browser");
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
			Log.message("End launchApplicationFromBrowser: launching app from web browser");
		} catch (IOException | InterruptedException e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			if(null != ieDriver) {
				ieDriver.quit();
			}
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}
	
	public void closeApplication(String statusMessage) {
		Log.message("Start closeApplication: Closing app session");
		try {
			if(null != appSession) {
				closePopupIfAny();
				appSession.closeApp();
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				if(!clickYesOrNoOnPopup("Do you want to save the changes?", "No")) {
					clickYesOrNoOnPopup("Are you sure you want to exit the application?", "Yes");
					Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
				} else {
					Thread.sleep(AmortTemplateConstants.ONEMINUTEWAITTIME);
				}
			}
			Log.message("End closeApplication: Closing app session");
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}
	
	private void closePopupIfAny() {
		while(isElementFoundByName("COMPASS")) {
			appSession.findElementByName("Yes").click();
		}
	}
	
	public void doubelClickAt(int x, int y) {
		Log.message("Start clickAt: clicking on x: "+x+", y: "+y);
		Actions vActions = new Actions(appSession);
		vActions.moveToElement(appSession.findElementByName("System"), 0,0);
		Action vClickAction = vActions.build();
		vClickAction.perform();
        vActions.moveByOffset(x, y);
		vActions.doubleClick();
		vClickAction = vActions.build();
		vClickAction.perform();
		Log.message("End clickAt: clicking on x: "+x+", y: "+y);
    }

	@SuppressWarnings("unchecked")
	public void scheduleTitle(String url, String appName, String titleId, String startDate, String endDate, int runCnt, String statusMessage) {
		try {
			Log.message("Start scheduleTitle: scheduling title: "+titleId);
			/*
			appSession.findElementByName("Scheduling").click();
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			appSession.findElementByName("Create New Schedule").click();
			WebElement parent =  appSession.findElementByAccessibilityId("ScheduleName");
	        parent.click();
	        parent.clear();
	        parent.sendKeys(titleId);
	        setScheduleDates("ScheduleStartDate", startDate);
	        setScheduleDates("ScheduleEndDate", endDate);
	        clickSaveButton();
	        Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
	        */
	        appSession.findElementByName(titleId).click();
	        appSession.findElementByName("Open Schedule").click();
	        Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
	        DesiredCapabilities appCapabilities = new DesiredCapabilities();
			appCapabilities.setCapability("app", "Root");
			WindowsDriver<WindowsElement> driver = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities);
			WebElement element = getElement(driver, By.name("MONTHLY SCHEDULE"));
			String handleStr = element.getAttribute("NativeWindowHandle");
			int handleInt = Integer.parseInt(handleStr);
			String handleHex = Integer.toHexString(handleInt);
			DesiredCapabilities appCapabilities2 = new DesiredCapabilities();
			appCapabilities2.setCapability("appTopLevelWindow", handleHex);
			appSession = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities2);
			int cnt = 1;
			List<WebElement> dayElements = appSession.findElementsByClassName("GroupHeader");
			for(WebElement dayElement:dayElements) {
				
				try {
					Actions act = new Actions(appSession);
					act.moveToElement(dayElement).perform();
					act.contextClick().perform();
					WebElement newBtn = appSession.findElementByName("New");
					if(newBtn.isEnabled()) {
						newBtn.click();
						WebElement appointmentItem = appSession.findElementByName("AppointmentItem");
						act = new Actions(appSession);
						act.moveToElement(appointmentItem).perform();
						act.contextClick().perform();
						appSession.findElementByName("Apply Episodes").click();
						Thread.sleep(AmortTemplateConstants.ONEMINUTEWAITTIME);
						appCapabilities = new DesiredCapabilities();
						appCapabilities.setCapability("app", "Root");
						driver = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities);
						element = getElement(driver, By.name("Apply Episodes"));
						handleStr = element.getAttribute("NativeWindowHandle");
						handleInt = Integer.parseInt(handleStr);
						handleHex = Integer.toHexString(handleInt);
						appCapabilities2 = new DesiredCapabilities();
						appCapabilities2.setCapability("appTopLevelWindow", handleHex);
						appSession = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities2);
						
						if(cnt == runCnt) {
							break;
						}
						cnt++;
					}
					Log.message("End scheduleTitle: scheduling title: "+titleId);
				} catch(Exception e) {
					System.out.println("Error");
				}
			}
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}
	
	public Map<Integer, String> generateAmort(double totalLicenseFee, String statusMessage) {
		Log.message("Start generateAmort: generating amort for "+totalLicenseFee);
		try {
			appSession.findElementByName("Amortize").click();
			appSession.findElementByName("Generate Amort").click();
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			clickYesOrNoOnPopup("All old amort data will be deleted. Do you want to continue?", "Yes");
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			clickYesOrNoOnPopup("Effective Date should be earlier or equal to the Amort Window Start Date", "Yes");
			Map<Integer, String> amorts = readAmortAmtRows(totalLicenseFee, 0, statusMessage);
			if(null == amorts || amorts.size() <= 0) {
				writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
				Log.fail("Amort not generated", appSession);
				killApp();
				return null;	
			}
			Log.message("End generateAmort: generating amort for "+totalLicenseFee);
			return amorts;
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, String> readAmortAmtRows(double totalLicenseFee, int previousLastMonth, String statusMessage) {
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		Log.message("Start readAmortAmtRows: read amort amounts");
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
				return amorts;
			} else {
				if(totalLicenseFee > 0) {
					for(int i=0; i<scrollCount-1; i++) {
						appSession.getKeyboard().sendKeys(Keys.ARROW_DOWN);
					}
					readAmortAmtRows(totalLicenseFee, lastMonth, statusMessage);
				} else {
					System.out.println("TotalLicenseFee left:"+totalLicenseFee);
				}
			}
			Log.message("readAmortAmtRows: read amort amounts");
		} catch (Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
			return amorts;
		}
		return amorts;
	}
	
	private boolean clickYesOrNoOnPopup(String message, String yesOrNo) {
		Log.message("Start clickYesOrNoOnPopup: "+message+", "+yesOrNo);
		try {
			WebElement element = appSession.findElementByName(message);
			if(null != element) {
				Log.message("Popup found with message: "+message);
				appSession.findElementByName(yesOrNo).click();
				Log.message("End clickYesOrNoOnPopup: "+message+", "+yesOrNo);
				return true;
			}
		} catch (Exception e) {
			;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public Double setAllocationData(String licenseType, String licenseAmount, String amortTemplate, String statusMessage) {
		Log.message("Start setAllocationData: licenseType"+licenseType+", licenseAmount: "+licenseAmount+", amortTemplate: "+amortTemplate);
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
			clickSaveButton(statusMessage);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			clickElement("License Fee Type");
			moveByOffsetAndClick(vActions, -35, 0);
			moveByOffsetAndClick(vActions, -30, 0);
			moveByOffsetAndClick(vActions, 0, 30);
			clickElement("License Fee Type");
			clickElement("Amortization Template");
			appSession.getKeyboard().sendKeys(amortTemplate);
			clickSaveButton(statusMessage);
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
			Log.message("End setAllocationData: licenseType"+licenseType+", licenseAmount: "+licenseAmount+", amortTemplate: "+amortTemplate);
			return totalLicenseFee;
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
			return null;
		}
	}

	private void clickSaveButton(String statusMessage) {
		try {
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			WebElement saveButtonEle = appSession.findElementByAccessibilityId("SaveButton");
			int retry = 0;
			while(retry<3) {
				if(saveButtonEle.isEnabled()) {
					saveButtonEle.click();	
					Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
					return;
				}
				retry++;
			}
		} catch (InterruptedException e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}

	private void moveByOffsetAndClick(Actions vActions, int x, int y) {
		Log.message("Start moveByOffsetAndClick: x: "+x+", y: "+y);
		vActions.moveByOffset(x, y);
		vActions.click();
		Action vClickAction = vActions.build();
		vClickAction.perform();
		Log.message("End moveByOffsetAndClick: x: "+x+", y: "+y);
	}
	
	@SuppressWarnings("unchecked")
	private void clickElement(String ele) {
		Log.message("Start clickElement: "+ele);	
		List<WebElement> elements;
		elements = appSession.findElementsByName(ele);
		for(WebElement element:elements) {
			try {
				WebElement textBox = element.findElement(By.className("Cell"));
				if(null != textBox) {
					element.click();
					textBox.click();
					Log.message("End clickElement: "+ele);
					return;
				}
			} catch (Exception e) {
				;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setPlayWindowAttribute(String key, String value, String statusMessage) {
		Log.message("Start setPlayWindowAttribute: key: "+key+", value: "+value);
		try {
			List<WebElement> elements = appSession.findElementsByName(key);
			for(WebElement element:elements) {
				if(element.getText().equals("") || element.getText().equals("0")) {
					element.click();
					Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
					appSession.getKeyboard().sendKeys(value);
				}
			}
			Log.message("End setPlayWindowAttribute: key: "+key+", value: "+value);
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void setScheduleDates(String key, String value, String statusMessage) {
		Log.message("Start setScheduleDates: key: "+key+", value: "+value);
		try {
			List<WebElement> elements = appSession.findElementsByAccessibilityId(key);
			for(WebElement element:elements) {
				element.click();
				element.clear();
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				appSession.getKeyboard().sendKeys(value);
			}
			Log.message("End setScheduleDates: key: "+key+", value: "+value);
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}


	@SuppressWarnings("unchecked")
	public void openTitleAndWindow(String financeType, List<Window> windows, String statusMessage) {
		Log.message("Start openTitleAndWindow: financeType: "+financeType+", windows: "+windows);
		try {
			screen = new Screen();
			Pattern expand = new Pattern(iconPath + "expand.png");
			screen.doubleClick(expand);
			Pattern selectAnAction = new Pattern(iconPath +"selectanaction.png");
			Match found = waitForElementToAppearByPattern(selectAnAction, 0);
			if(null == found) {
				writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
				Log.fail("Unable to open title in one minute", screen);
				killApp();
			}
			Pattern selectedWindowTab = new Pattern(iconPath + "selecedwindowtab.png");
			if(!isElementFoundByImage(selectedWindowTab)) {
				Pattern windowTab = new Pattern(iconPath + "windowtab.png");
				screen.click(windowTab);
			}
			for(Window window:windows) {
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				appSession.findElementByName("AddWindow").click();
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				setPlayWindowAttribute("Start Date", window.getStartDate(), statusMessage);
				setPlayWindowAttribute("End Date", window.getEndDate(), statusMessage);
				setPlayWindowAttribute("Runs in Play Day", window.getRunInPlayDay(), statusMessage);
				setPlayWindowAttribute("Runs/PD Allowed", window.getRunsPDAllowed(), statusMessage);
			}
			clearAndSetValueInDropdown("FinanceTypeCombobox", financeType);
			if(isElementFoundByAccessibilityId("MasterSeriesCombobox")) {	
				setValueInDropdown("MasterSeriesCombobox", "TEST 123");
				appSession.findElementByName("TEST 123").click();
			}
			clickSaveButton(statusMessage);
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
			Log.message("End openTitleAndWindow: financeType: "+financeType+", windows: "+windows);
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}
	
	public void launchAppUsingNativeWindowHandle(String appPath, String url, String appName, String statusMessage) {
		Log.message("Start launchAppUsingNativeWindowHandle: Launching app using window handle");
		try {
			killApp();
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			startExeApp(appPath);
			DesiredCapabilities appCapabilities = new DesiredCapabilities();
			appCapabilities.setCapability("app", "Root");
			WindowsDriver<WindowsElement> driver = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities);
			WebElement element = getElement(driver, By.name(appName));
			String handleStr = element.getAttribute("NativeWindowHandle");
			int handleInt = Integer.parseInt(handleStr);
			String handleHex = Integer.toHexString(handleInt);
			DesiredCapabilities appCapabilities2 = new DesiredCapabilities();
			appCapabilities2.setCapability("appTopLevelWindow", handleHex);
			appSession = new WindowsDriver<WindowsElement>(new URL(url), appCapabilities2);
			appSession.manage().window().maximize();
			Log.message("End launchAppUsingNativeWindowHandle: Launching app using window handle");
		} catch (Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}
	
	private WebElement getElement(WindowsDriver<WindowsElement> driver, By by) {
		Log.message("Start getElement: By: "+by);
		try {
			WebElement element = driver.findElement(by);
			Log.message("End getElement: By: "+by);
			return element;
		} catch(Exception e) {
			return null;
		}
	}

	private void startExeApp(String appPath) {
		Log.message("Start startExeApp: appPath: "+appPath);
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
			Log.message("End startExeApp: appPath: "+appPath);
		} catch(Exception e) {
			return;
		}
	}
	
	public void readPopupMessage(String statusMessage) {
		Log.message("Start readPopupMessage: read message from popup");
		try {
			double coordinates[] = SikuliImageRecognitionUtil.findImage(appSession, "no-amort-rule.png", 169, 18);
			if(null != coordinates) {
				System.out.println(coordinates[0]+" - "+coordinates[1]);
			}
			Log.message("End readPopupMessage: read message from popup");
		} catch (Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}

	public void loginCompass(String username, String password, String displayName, String statusMessage) {
		Log.message("Start loginCompass: Logging in application for user: "+username);
		try {
			if(isElementFoundByAccessibilityId("username")) {
				Log.message("User ["+username+"] is not already logged in Compass application");
				appSession.findElementByAccessibilityId("username").clear();
				appSession.findElementByAccessibilityId("username").sendKeys(username);
				appSession.findElementByAccessibilityId("password").clear();
				appSession.findElementByAccessibilityId("password").sendKeys(password);
				appSession.findElementByName("Sign In").click();
				Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
				Log.pass("User ["+username+"] successfully logged in Compass application");
			} else {
				closePopupIfAny();
				if(isElementFoundByName(displayName)) {
					Log.message("User ["+username+"] already logged in Compass application");
					while(isElementFoundByName("CloseButton")) {
						appSession.findElementByName("CloseButton").click();
						clickYesOrNoOnPopup("Do you want to save the changes?", "No");
						Thread.sleep(AmortTemplateConstants.ONEMINUTEWAITTIME);
					}
					appSession.findElementByName("Contract Management").click();
				}
				
			}
			Log.message("End loginCompass: Logging in application for user: "+username);
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}

	public void createContract(String network, String distributor, String dealType, String negotiatedBy, String titleName, String titleType, String statusMessage) {
		Log.message("Start createContract: distributor: "+distributor+", dealType: "+dealType+", negotiatedBy: "+negotiatedBy);
		try {
			appSession.findElementByName("Create New Contract").click();
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			SimpleDateFormat df = new SimpleDateFormat("YYYYMMDDHHmmss");
			String packageName = "TestPackage_" + df.format(new Date());
			appSession.findElementByAccessibilityId("DistributorPackageTextBox").sendKeys(packageName);
			clearAndSetValueInDropdown("OriginatingNetworkComboBox", network);
			setValueInDropdown("DistributorComboBox", distributor);
			setValueInDropdown("DealTypeCombobox", dealType);
			setValueInDropdown("NegotiatedByCombobox", negotiatedBy);
			addTitle(titleName, titleType, statusMessage);
			clickSaveButton(statusMessage);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Log.pass("Successfully created contract with DistributorPackage: "+ packageName);
			Log.message("End createContract: distributor: "+distributor+", dealType: "+dealType+", negotiatedBy: "+negotiatedBy);
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}

	@SuppressWarnings("unchecked")
	private void addTitle(String titleName, String titleType, String statusMessage) {
		Log.message("Start addTitle: titleName: "+titleName+", titleType: "+titleType);
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
						appSession.getKeyboard().sendKeys(titleType);
						WebElement webElement = appSession.findElementByName(titleType);
						if(null != webElement) {
							webElement.click();
							titleTypeSet= true;
						}
					}
				}
			}
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Log.message("End addTitle: titleName: "+titleName+", titleType: "+titleType);
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void setEpisodeValue(String key, String value, String statusMessage) {
		List<WebElement> elements = appSession.findElementsByAccessibilityId(key);
		for(WebElement element:elements) {
			try { 
				element.click();
				WebElement childElement = element.findElement(By.className("Cell"));
				childElement.click();
				appSession.getKeyboard().sendKeys(value);
				Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
				break;
			} catch(Exception e) {
				;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchTitleWithEpisodes(String network, String showId, String titleName, String titleType, String statusMessage) {
		try {
			Actions vActions = new Actions(appSession);
			appSession.findElementByName("Titles").click();
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			clearAndSetValueInDropdown2("NetworkComboBox", network);
			appSession.findElementByAccessibilityId("ShowIdTextBox").clear();
			appSession.findElementByAccessibilityId("ShowIdTextBox").sendKeys(showId);
			appSession.findElementByAccessibilityId("SearchButton").click();
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			List<WebElement> types = appSession.findElementsByName("Type");
			for (WebElement type:types) {
				try {
					WebElement typeCell = type.findElement(By.className("Cell"));
					typeCell.click();
					vActions.moveByOffset(0, 0);
					vActions.doubleClick();
					Action vClickAction = vActions.build();
					vClickAction.perform();
				} catch(Exception e) {
					
				}
			}
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			appSession.findElementByAccessibilityId("ActionDropDownButton").click();
			appSession.findElementByName("Open Contract").click();
			List<WebElement> titles = appSession.findElementsByName("Type");
			for (WebElement title:titles) {
				try {
					WebElement typeCell = title.findElement(By.className("Cell"));
					typeCell.click();
					appSession.getKeyboard().sendKeys(titleType);
					WebElement webElement = appSession.findElementByName(titleType);
					if(null != webElement) {
						webElement.click();
					}
				} catch(Exception e) {
					
				}
			}
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			clickSaveButton(statusMessage);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			List<WebElement> titleNames = appSession.findElementsByAccessibilityId("Cell_TitleName");
			for(WebElement titleN:titleNames) {
				try {
					WebElement title = titleN.findElement(By.className("Cell"));
					title.click();
					break;
				} catch (Exception e) {
					;
				}
			}
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			vActions.moveByOffset(-50, 0);
			vActions.doubleClick();
			Action vClickAction = vActions.build();
			vClickAction.perform();
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), appSession);
			killApp();
		}
	}
	
	private void clearAndSetValueInDropdown2(String key, String value) {
		Log.message("Start clearAndSetValueInDropdown: key:"+key+", value: "+value);
		WebElement parent =  appSession.findElementByAccessibilityId(key);
        parent.click();
    	Actions vActions = new Actions(appSession);
    	vActions.moveByOffset(40, 0);
    	vActions.click();
		Action vClickAction = vActions.build();
		vClickAction.perform();
		appSession.findElementByName(value).click();
        Log.message("End clearAndSetValueInDropdown: key:"+key+", value: "+value);
	}
	
	private void clearAndSetValueInDropdown(String key, String value) {
		Log.message("Start clearAndSetValueInDropdown: key:"+key+", value: "+value);
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
        Log.message("End clearAndSetValueInDropdown: key:"+key+", value: "+value);
	}
	
	private void setValueInDropdown(String key, String value) {
		Log.message("Start setValueInDropdown: key:"+key+", value: "+value);
		WebElement parent =  appSession.findElementByAccessibilityId(key);
        parent.click();
        WebElement textBox = parent.findElement(By.className("TextBox"));
    	textBox.click();
    	textBox.sendKeys(value);
    	Log.message("End setValueInDropdown: key:"+key+", value: "+value);
	}
	
	private boolean isElementFoundByAccessibilityId(String key) {
		Log.message("Start isElementFoundByAccessibilityId: "+key);
		try {
			appSession.findElementByAccessibilityId(key);
			Log.message("End isElementFoundByAccessibilityId: "+key);
			return true;
		} catch(Exception e) {
			return false;	
		}
	}
	
	private boolean isElementFoundByName(String key) {
		Log.message("Start isElementFoundByName: "+key);
		try {
			appSession.findElementByName(key);
			Log.message("End isElementFoundByName: "+key);
			return true;
		} catch(Exception e) {
			return false;	
		}
	}
	
	@Override
	public void addEpisode(String statusMessage) {
		Screen screen = new Screen();
		File directory = new File(".");
		String strBasepath;
		try {
			strBasepath = directory.getCanonicalPath();
			String iconPath = strBasepath + File.separator + "images" + File.separator;
			Pattern episodeTab = new Pattern(iconPath + "episodetab.png");
			Pattern addEpisodeBtn = new Pattern(iconPath + "addepisodebtn.png");
			screen.mouseMove(episodeTab);
			screen.click();
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			screen.click(addEpisodeBtn);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Pattern episodeName = new Pattern(iconPath + "episodename.png");
			screen.click(episodeName);
			screen.type("TestEpisode-1");
			screen.type(Key.TAB);
			screen.type("Season1");
			clickSaveButton(statusMessage);
		} catch (IOException | FindFailed | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private Match waitForElementToAppearByPattern(Pattern pattern, int retryCnt) {
		while(retryCnt < 3) {
			try {
				Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
				screen = new Screen();
				return screen.find(pattern);
			} catch (InterruptedException | FindFailed e) {
				retryCnt++;
			}
		}
		return null;
	}
	
	private boolean isElementFoundByImage(Pattern image) {
		screen = new Screen();
		try {
			@SuppressWarnings("unused")
			Match imageFound = screen.find(image);
			return true;
		} catch (FindFailed e) {
			;
		}
		return false;
	}
}

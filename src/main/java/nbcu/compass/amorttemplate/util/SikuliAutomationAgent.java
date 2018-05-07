package nbcu.compass.amorttemplate.util;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;
import nbcu.compass.amorttemplate.factory.AutomationAgent;

public class SikuliAutomationAgent extends AutomationAgent {
	
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
	
	public void doubelClickAt(int x, int y) {
		Log.message("Start clickAt: clicking on x: "+x+", y: "+y);
		screen = new Screen();
		Rectangle rectange = new Rectangle(x, y, 0, 0);
		screen.setRect(rectange);
		screen.doubleClick();
		screen = new Screen();
		Log.message("End clickAt: clicking on x: "+x+", y: "+y);
    }

	public Map<Integer, String> generateAmort(double totalLicenseFee, String statusMessage) {
		Log.message("Start generateAmort: generating amort for "+totalLicenseFee);
		try {
			screen = new Screen();
			Pattern amortizeTab = new Pattern(iconPath + "amortizetab.png");
			screen.click(amortizeTab);
			Thread.sleep(AmortTemplateConstants.THIRTYSECONDSWAITTIME);
			Pattern generateAmort = new Pattern(iconPath + "generateamort.png");
			screen.click(generateAmort);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			clickYesOrNoOnPopup("All old amort data will be deleted. Do you want to continue?", "Yes");
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			clickYesOrNoOnPopup("Effective Date should be earlier or equal to the Amort Window Start Date", "Yes");
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			launchAppUsingWAD(configProperty.getProperty("url"), configProperty.getProperty("appName"));
			Map<Integer, String> amorts = readAmortAmtRows(totalLicenseFee, 0, statusMessage);
			if(null == amorts || amorts.size() == 0) {
				writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
				Log.fail("Amort not generated", screen);
				killApp();
				return null;	
			}
			Log.message("End generateAmort: generating amort for "+totalLicenseFee);
			return amorts;
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
			return null;
		}
	}
	
	public void launchAppUsingWAD( String url, String appName) {
		try {
			Log.message("Start launchAppUsingWAD: Launching app using window handle");
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
			Log.message("End launchAppUsingWAD: Launching app using window handle");
		} catch (MalformedURLException e) {
			e.printStackTrace();
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
		screen = new Screen();
		Log.message("Start clickYesOrNoOnPopup: "+message+", "+yesOrNo);
		Pattern elementFound = null;
		try {
			if(yesOrNo.equalsIgnoreCase("yes")) {
				elementFound = new Pattern(iconPath + "yes.png");
			} else {
				elementFound = new Pattern(iconPath + "no.png");
			}
			screen.click(elementFound);
			Thread.sleep(AmortTemplateConstants.TWOECONDSWAITTIME);
			if(isElementFoundByImage(elementFound)) {
				return clickYesOrNoOnPopup(message, yesOrNo);
			}
			return true;
		} catch (FindFailed | InterruptedException e) {
			;
		}
		return false;
	}
	
	public Double setAllocationData(String licenseType, String licenseAmount, String amortTemplate, String statusMessage) {
		screen = new Screen();
		Log.message("Start setAllocationData: licenseType"+licenseType+", licenseAmount: "+licenseAmount+", amortTemplate: "+amortTemplate);
		try {
			double totalLicenseFee = 0.0;
			Pattern allocationTab = new Pattern(iconPath + "allocationtab.png");
			screen.click(allocationTab);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			
			Pattern dropIcon = new Pattern(iconPath + "drop.png");			
			screen.click(dropIcon);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			
			Pattern asterisk = new Pattern(iconPath + "asterisk.png");
			screen.click(asterisk);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			
			screen.type(licenseType);
			screen.type(Key.TAB);
			screen.type(licenseAmount);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			
			clickSaveButton(statusMessage);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			
			screen.click(dropIcon);
			Pattern amortizedCheckBox = new Pattern(iconPath + "amortiedcheckbox.png");
			screen.click(amortizedCheckBox);
			for(int i=0; i<5; i++) {
				screen.type(Key.TAB);
			}
			screen.type(amortTemplate);
			clickSaveButton(statusMessage);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			totalLicenseFee = Double.parseDouble(licenseAmount);
			Log.message("End setAllocationData: licenseType"+licenseType+", licenseAmount: "+licenseAmount+", amortTemplate: "+amortTemplate);
			return totalLicenseFee;
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
			return null;
		}
	}

	private void clickSaveButton(String statusMessage, int...retry) {
		screen = new Screen();
		try {
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen = new Screen();
			Pattern save = new Pattern(iconPath + "save.png");
			screen.click(save);
			Pattern saveDisabled = new Pattern(iconPath + "savedisabled.png");
			Match found = waitForElementToAppearByPattern(saveDisabled, 0);
			if(null == found) {
				if(null != retry && retry.length == 1) {
					writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
					Log.fail("Unable to save data in one minute", screen);
					killApp();
				} else {
					clickSaveButton(statusMessage, 1);
				}
			}
		} catch (InterruptedException | FindFailed e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
		}
	}
	
	public void openTitleAndWindow(String financeType, List<Window> windows, String statusMessage) {
		Log.message("Start openTitleAndWindow: financeType: "+financeType+", windows: "+windows);
		screen = new Screen();
		try {
			Pattern expand = new Pattern(iconPath + "expand.png");
			screen.doubleClick(expand);
			Pattern selectAnAction = new Pattern(iconPath +"selectanaction.png");
			Match found = waitForElementToAppearByPattern(selectAnAction, 0);
			if(null == found && isElementFoundByImage(expand)) {
				screen.doubleClick(expand);
			}
			Pattern selectedWindowTab = new Pattern(iconPath + "selecedwindowtab.png");
			if(!isElementFoundByImage(selectedWindowTab)) {
				Pattern windowTab = new Pattern(iconPath + "windowtab.png");
				screen.click(windowTab);
			}
			
			for(Window window:windows) {
				Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
				Pattern addWindowBtn = new Pattern(iconPath + "addwindowbutton.png");
				found = waitForElementToAppearByPattern(addWindowBtn, 0);
				screen.click(addWindowBtn);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				Pattern addWindowIcon = new Pattern(iconPath + "addwindowicon.png");
				screen.click(addWindowIcon);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				screen.type(Key.TAB);
				screen.type(Key.TAB);
				screen.type(window.getStartDate());
				screen.type(Key.TAB);
				screen.type(window.getEndDate());
				screen.type(Key.TAB);
				screen.type(window.getDefinition());
				screen.type(Key.TAB);
				screen.type(window.getRunInPlayDay());
				screen.type(Key.TAB);
				screen.type(window.getRunsPDAllowed());
			}
			setFinanceType(financeType);
			
			Pattern masterSeries = new Pattern(iconPath + "masterseries.png");
			if(isElementFoundByImage(masterSeries)) {	
				screen.type(masterSeries, "TEST 123");
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				screen.type(Key.TAB);
				screen.type(Key.TAB);
			}
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			clickSaveButton(statusMessage);
			Log.message("End openTitleAndWindow: financeType: "+financeType+", windows: "+windows);
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
		}
	}

	private void setFinanceType(String financeType) throws FindFailed {
		Pattern financeTypeImg = new Pattern(iconPath + "financetype.png");
		Match financeTypeFound = screen.find(financeTypeImg);
		financeTypeFound.click();
		financeTypeFound.setTargetOffset(100, 0);
		screen.mouseMove();
		screen.doubleClick();
		for(int i=0;i<20; i++) {
			screen.type(Key.BACKSPACE);	
		}
		screen.type(financeType);
	}
	
	public void launchAppUsingNativeWindowHandle(String appPath, String url, String appName, String statusMessage) {
		Log.message("Start launchAppUsingNativeWindowHandle: Launching app using window handle");
		try {
			killApp();
			startExeApp(appPath);
			screen = new Screen();
			Log.message("End launchAppUsingNativeWindowHandle: Launching app using window handle");
		} catch (Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
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
			Match foundMaximize = waitForElementToAppearByText("COMPASS", 0);
			if(null != foundMaximize) {
				foundMaximize.doubleClick();
			}
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Log.message("End startExeApp: appPath: "+appPath);
		} catch(InterruptedException | IOException e) {
			return;
		}
	}
	
	private Match waitForElementToAppearByText(String text, int retryCnt) {
		while(retryCnt < 3) {
			try {
				Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
				screen = new Screen();
				return screen.findText(text);
			} catch (InterruptedException | FindFailed e) {
				retryCnt++;
			}
		}
		return null;
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
	
	public void loginCompass(String username, String password, String displayName, String statusMessage) {
		Log.message("Start loginCompass: Logging in application for user: "+username);
		screen = new Screen();
		try {
			Pattern usernameImg = new Pattern(iconPath + "username.png");
			Pattern passwordImg = new Pattern(iconPath + "password.png");
			Pattern signin = new Pattern(iconPath + "signin.png");
			screen.wait(usernameImg, 10);	
			screen.type(usernameImg, username);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen.type(passwordImg, password);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			screen.click(signin);
			Pattern createContract = new Pattern(iconPath + "createcontract.png");
			Match found = waitForElementToAppearByPattern(createContract, 0);
			if(null != found) {
				Log.message("End loginCompass: Logging in application for user: "+username+" passed");	
			} else {
				Log.message("End loginCompass: Logging in application for user: "+username+" failed");
			}
		} catch(FindFailed | InterruptedException e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
		}
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
	
	private boolean isElementFoundByText(String text) {
		screen = new Screen();
		Settings.OcrTextRead = true;
		Settings.OcrTextSearch = true;
		try {
			@SuppressWarnings("unused")
			Match imageFound = screen.findText(text);
			imageFound.highlight(2);
			return true;
		} catch (FindFailed e) {
			;
		}
		return false;
	}
	
	public boolean isEpisodicTitle() {
		screen = new Screen();
		File directory = new File(".");
		String strBasepath;
		try {
			strBasepath = directory.getCanonicalPath();
			String iconPath = strBasepath + File.separator + "images" + File.separator;
			Pattern episodeTab = new Pattern(iconPath + "episodetab.png");
			Pattern episodeSelectedTab = new Pattern(iconPath + "episodetabselected.png");
			if(!isElementFoundByImage(episodeTab)) {
				return isElementFoundByImage(episodeSelectedTab);
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public void addEpisode(String statusMessage, String episodeName) {
		Screen screen = new Screen();
		File directory = new File(".");
		String strBasepath;
		try {
			strBasepath = directory.getCanonicalPath();
			String iconPath = strBasepath + File.separator + "images" + File.separator;
			Pattern episodeTab = new Pattern(iconPath + "episodetab.png");
			Pattern episodeSelectedTab = new Pattern(iconPath + "episodetabselected.png");
			if(isElementFoundByImage(episodeTab)) {
				screen.mouseMove(episodeTab);
				screen.click();
			} else if(isElementFoundByImage(episodeSelectedTab)) {
				;
			}
			Pattern addEpisodeBtn = new Pattern(iconPath + "addepisodebtn.png");
			
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			screen.click(addEpisodeBtn);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Pattern episodeNamePattern = new Pattern(iconPath + "episodename.png");
			screen.click(episodeNamePattern);
			screen.type(episodeName);
			screen.type(Key.TAB);
			screen.type("Season1");
			clickSaveButton(statusMessage);
		} catch (IOException | FindFailed | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void createContract(String network, String distributor, String dealType, String negotiatedBy, String titleName, String titleType, String statusMessage) {
		Log.message("Start createContract: distributor: "+distributor+", dealType: "+dealType+", negotiatedBy: "+negotiatedBy);
		try {
			screen = new Screen();
			Pattern createContract = new Pattern(iconPath + "createcontract.png");
			screen.click(createContract);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			SimpleDateFormat df = new SimpleDateFormat("YYYYMMDDHHmmss");
			String packageName = "TestPackage_" + df.format(new Date());
			Pattern distributorPackage = new Pattern(iconPath + "distributorpackage.png");
			screen.type(distributorPackage, packageName);
			Thread.sleep(AmortTemplateConstants.TWOECONDSWAITTIME);
			clearAndSetValueInDropdown("network", network);
			Thread.sleep(AmortTemplateConstants.TWOECONDSWAITTIME);
			setValueInDropdown("distributor", distributor);
			Thread.sleep(AmortTemplateConstants.TWOECONDSWAITTIME);
			screen.click();
			Thread.sleep(AmortTemplateConstants.TWOECONDSWAITTIME);
			setValueInDropdown("dealtype", dealType);
			Thread.sleep(AmortTemplateConstants.TWOECONDSWAITTIME);
			setValueInDropdown("negotiatedby", negotiatedBy);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			addTitle(titleName, titleType, statusMessage);
			clickSaveButton(statusMessage);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Log.pass("Successfully created contract with DistributorPackage: "+ packageName);
			Log.message("End createContract: distributor: "+distributor+", dealType: "+dealType+", negotiatedBy: "+negotiatedBy);
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
		}
	}

	private void addTitle(String titleName, String titleType, String statusMessage) {
		Log.message("Start addTitle: titleName: "+titleName+", titleType: "+titleType);
		screen = new Screen();
		try {
			Pattern asterisk = new Pattern(iconPath + "asterisk.png");
			screen.click(asterisk);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			if(isElementFoundByImage(asterisk)) {
				screen.click(asterisk);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			}
			screen.type(titleName);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			
			Pattern spanishTitleHeader = new Pattern(iconPath + "spanishtitleheader.png");
			Match found = waitForElementToAppearByPattern(spanishTitleHeader, 0);
			int tabCount = 0;
			if(null != found) {
				tabCount = 2;
			} else {
				tabCount = 1;
			}
			for(int i=0; i<tabCount; i++) {
				screen.type(Key.TAB);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			}
			screen.type(titleType);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Log.message("End addTitle: titleName: "+titleName+", titleType: "+titleType);
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
		}
	}
	
	private void clearAndSetValueInDropdown(String key, String value) {
		Log.message("Start clearAndSetValueInDropdown: key:"+key+", value: "+value);
		screen = new Screen();
		Pattern element = new Pattern(iconPath + key+".png");
		try {
			screen.doubleClick(element);
			for(int i=0;i<20; i++) {
				screen.type(Key.BACKSPACE);	
			}
			screen.type(value);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Log.message("End clearAndSetValueInDropdown: key:"+key+", value: "+value);
		} catch (FindFailed | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void setValueInDropdown(String key, String value) {
		Log.message("Start setValueInDropdown: key:"+key+", value: "+value);
		screen = new Screen();
		Pattern element = new Pattern(iconPath + key+".png");
		try {
			screen.type(element, value);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
	    	Log.message("End setValueInDropdown: key:"+key+", value: "+value);
		} catch (FindFailed | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void createSchedule(String network) {
		try {
			screen = new Screen();
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			if(!isElementFoundByText(network+"TestSchedule")) {
				Pattern createSchedule = new Pattern(iconPath+"createschedule.png");
				screen.click(createSchedule);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			} else {
				System.out.println(network+"TestSchedule found");
			}
		} catch(FindFailed | InterruptedException e) {
			;
		}
	}
	
	public String scheduleTitle(String scheduleName, String network, String titleType, String episodeOrTitleName, String statusMessage, int run) {
		screen = new Screen();
		try {
			Pattern schedulingTab = new Pattern(iconPath+"schedulingtab.png");
			screen.click(schedulingTab);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Match networkFound = screen.findText(network);
			networkFound.click();
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			
			Pattern approvedSchedule = new Pattern(iconPath + "approvedschedule.png");
			if(isElementFoundByImage(approvedSchedule)) {
				screen.click(approvedSchedule);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				screen.rightClick(approvedSchedule);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				Pattern unapprove = new Pattern(iconPath + "unapprove.png");
				screen.click(unapprove);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			}
			
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Match openSchedule = screen.findText("Open Schedule");
			openSchedule.click();
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern okonpopup = new Pattern(iconPath + "okonpopup.png");
			if(isElementFoundByImage(okonpopup)) {
				screen.click(okonpopup);
			}
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
			
			for(int i=0; i<run; i++) {
				Pattern nextWeek = new Pattern(iconPath+"nextweek.png");
				screen.click(nextWeek);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			}
			
			Pattern emptyslot = new Pattern(iconPath+"emptyslot.png");
			screen.rightClick(emptyslot);
			
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern newIcon = new Pattern(iconPath+"newicon.png");
			screen.click(newIcon);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			screen.click(emptyslot);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);

			
			Pattern whiteslot = new Pattern(iconPath+"whiteslot.png");
			screen.rightClick(whiteslot);
			
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern applyEpisodes = new Pattern(iconPath+"applyepisodes.png");
			screen.click(applyEpisodes);
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
			Pattern programType = new Pattern(iconPath + "programtype.png");
			screen.type(programType, titleType);
			Pattern episodeTitle = new Pattern(iconPath + "episodetitle.png");
			screen.type(episodeTitle, episodeOrTitleName);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern searchBtn = new Pattern(iconPath+"search.png");
			screen.click(searchBtn);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Pattern selectSearchedItem = new Pattern(iconPath+"selectsearcheditem.png");
			screen.click(selectSearchedItem);
			Pattern checkbox = new Pattern(iconPath+"checkbox.png");
			screen.click(checkbox);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen.type(Key.TAB);
			screen.type(Key.TAB);
			screen.type(Key.TAB);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen.type(Key.SPACE);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen.type(Key.SPACE);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern save = new Pattern(iconPath + "saveschedule.png");
			screen.click(save);
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
			Pattern closeSchedule = new Pattern(iconPath + "closeschedule.png");
			screen.click(closeSchedule);
			Thread.sleep(AmortTemplateConstants.THIRTYSECONDSWAITTIME);
			Pattern monthly = new Pattern(iconPath + "monthly2.png");
			screen.rightClick(monthly);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern approve = new Pattern(iconPath + "approve.png");
			screen.click(approve);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Pattern approvePopup = new Pattern(iconPath + "approvepopup.png");
			screen.click(approvePopup);
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
			return scheduleName;
		} catch (InterruptedException | FindFailed e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
		}
		return null;
	}
	
	public void deleteSchedules(String statusMessage) {
		screen = new Screen();
		try {
			Pattern approvedSchedule = new Pattern(iconPath + "approvedschedule.png");
			if(isElementFoundByImage(approvedSchedule)) {
				screen.click(approvedSchedule);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				screen.rightClick(approvedSchedule);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				Pattern unapprove = new Pattern(iconPath + "unapprove.png");
				screen.click(unapprove);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			}
			Pattern monthly = new Pattern(iconPath + "monthly.png");
			while(isElementFoundByImage(monthly)) {
				screen.click(monthly);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				screen.rightClick(monthly);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				Pattern deleteSchedule = new Pattern(iconPath + "deleteschedule.png");
				screen.click(deleteSchedule);
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
				clickYesOrNoOnPopup("Are you sure you want to delete selected schedule", "Yes");
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			}
		} catch (InterruptedException | FindFailed e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
		}
	}
	
	public void openTitle(String statusMessage) {
		Log.message("Start openTitleAndGenerateAmort: ");
		screen = new Screen();
		try {
			Pattern contractManagementNotSelected = new Pattern(iconPath + "contractmanagementnotselected.png");
			if(isElementFoundByImage(contractManagementNotSelected)) {
				Match contractManagementFound = screen.find(contractManagementNotSelected);
				contractManagementFound.click();
				Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			}
			Pattern expand = new Pattern(iconPath + "expand.png");
			screen.doubleClick(expand);
			Pattern selectAnAction = new Pattern(iconPath +"selectanaction.png");
			Match found = waitForElementToAppearByPattern(selectAnAction, 0);
			if(null == found) {
				writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
				Log.fail("Unable to open title in one minute", screen);
				killApp();
			}
		} catch (FindFailed | InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public void launchApplicationFromBrowser(String url, String appWebUrl, String statusMessage) {
		
	}

	@Override
	public void searchTitleWithEpisodes(String network, String showId, String titleName, String titleType, String statusMessage) {
		
	}
}

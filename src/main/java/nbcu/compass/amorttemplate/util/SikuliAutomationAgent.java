package nbcu.compass.amorttemplate.util;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

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

	private Map<Integer, String> readAmortAmtRows(double totalLicenseFee, int previousLastMonth, String statusMessage) {
		screen = new Screen();
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		Log.message("Start readAmortAmtRows: read amort amounts");
		try {
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Match found = screen.findText("TOTAL COUNT");
			System.out.println(found.getRect());
			Rectangle rectangle = new Rectangle(found.getX(), found.getY()-5, 100, 30);
			screen.setRect(rectangle);
			screen.highlight(3);
			String totalCntStr = screen.text();
			totalCntStr = totalCntStr.substring(totalCntStr.indexOf(":")+1).trim();
			int totalRowCnt = Integer.parseInt(totalCntStr);
			
			screen = new Screen();
			int rowCntPerScreen = 0;
			try {
				Pattern spanishTitleField = new Pattern(iconPath +"spanishtitlefield.png");
				found = screen.find(spanishTitleField);
				found.highlight(3);
				rowCntPerScreen = 3;
			} catch (Exception e) {
				rowCntPerScreen = 4;
			}

			int count = 0;
			while(count < totalRowCnt) {
				for(int i = 1; i <= rowCntPerScreen; i++) {
					if(count == totalRowCnt) {
						break;
					}
					Thread.sleep(3000);
					rectangle = new Rectangle(500, 550+(i*25), 95, 20);
					screen.setRect(rectangle);
					screen.click();
					String amortAmt = screen.text();
					count++;
					amorts.put(count, amortAmt);
				}
				for(int i = 0; i < rowCntPerScreen-1; i++) {
					screen.type(Key.DOWN);
				}
			}
			Log.message("readAmortAmtRows: read amort amounts");
		} catch (Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
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
			return true;
		} catch (FindFailed e) {
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
			
			screen.click(dropIcon);
			Pattern amortizedCheckBox = new Pattern(iconPath + "amortiedcheckbox.png");
			screen.click(amortizedCheckBox);
			for(int i=0; i<5; i++) {
				screen.type(Key.TAB);
			}
			screen.type(amortTemplate);
			clickSaveButton(statusMessage);
			
			Rectangle rectangle = new Rectangle(250, 525, 75, 30);
			screen.setRect(rectangle);
			String amount = screen.text().trim();
			amount = amount.replace("$", "").replace(",", "");
			screen = new Screen();
			totalLicenseFee = Double.parseDouble(amount);
			Log.message("End setAllocationData: licenseType"+licenseType+", licenseAmount: "+licenseAmount+", amortTemplate: "+amortTemplate);
			return totalLicenseFee;
		} catch(Exception e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
			return null;
		}
	}

	private void clickSaveButton(String statusMessage) {
		screen = new Screen();
		try {
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen = new Screen();
			Pattern save = new Pattern(iconPath + "save.png");
			screen.click(save);
			Pattern saveDisabled = new Pattern(iconPath + "savedisabled");
			Match found = waitForElementToAppearByPattern(saveDisabled, 0);
			if(null == found) {
				writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
				Log.fail("Unable to save data in one minute", screen);
				killApp();
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
			Match found = waitForElementToAppearByText("Select an Action", 0);
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
				Pattern addWindowBtn = new Pattern(iconPath + "addwindowbutton.png");
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
			screen.type(passwordImg, password);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			screen.click(signin);
			Match found = waitForElementToAppearByText("Loading Contract Management", 0);
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
			clearAndSetValueInDropdown("network", network);
			setValueInDropdown("distributor", distributor);
			screen.click();
			setValueInDropdown("dealtype", dealType);
			setValueInDropdown("negotiatedby", negotiatedBy);
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
			screen.type(titleName);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			
			Match found = waitForElementToAppearByText("Spanish Name of the Title", 0);
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

	@Override
	public void launchApplicationFromBrowser(String url, String appWebUrl, String statusMessage) {
		
	}

	@Override
	public void searchTitleWithEpisodes(String network, String showId, String titleName, String titleType, String statusMessage) {
		
	}
}

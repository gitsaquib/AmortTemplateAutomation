package nbcu.compass.amorttemplate.util;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

public class SikuliAutomationAgent {
	
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
	
	public void writeResultInTxtFile(String network, String status) {
		try {
			Log.message("Start writeResultInTxtFile: writing results in txt file: "+ status);
			File directory = new File(".");
			File txtFile = new File(directory.getCanonicalPath() + File.separator + "TestData"+ File.separator + "AmortTemplate"+network+"-Results.txt");
			FileWriter fw = new FileWriter(txtFile, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(status);
			pw.close();
			Log.message("End writeResultInTxtFile: writing results in txt file: "+ status);
		} catch (IOException e) {
			Log.fail(e.getMessage());
		}
	}
	
	private boolean closePopupIfAny() {
		screen = new Screen();
		Pattern element = new Pattern(iconPath + "yes.png");
		try {
			Match elementFound = screen.find(element);
			elementFound.click();
			return closePopupIfAny();
		} catch (FindFailed e) {
			;
		}
		return false;
	}
	
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
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			clickYesOrNoOnPopup("All old amort data will be deleted. Do you want to continue?", "Yes");
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			clickYesOrNoOnPopup("Effective Date should be earlier or equal to the Amort Window Start Date", "Yes");
			Map<Integer, String> amorts = readAmortAmtRows(totalLicenseFee, 0, statusMessage);
			if(null == amorts || amorts.size() > 0) {
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
			int count = 0;
			while(count < 9) {
				for(int i = 0; i < 4; i++) {
					if(count == 9) {
						break;
					}
					Thread.sleep(3000);
					Rectangle rectangle = new Rectangle(500, 550+(i*25), 75, 20);
					screen.setRect(rectangle);
					screen.click();
					String amortAmt = screen.text();
					amortAmt = amortAmt.substring(1);
					System.out.println("$"+amortAmt);
					
					rectangle = new Rectangle(563, 550+(i*25), 95, 20);
					screen.setRect(rectangle);
					screen.click();
					String month = screen.text();
					System.out.println(month);
					
					count++;
				}
				for(int i = 0; i < 4; i++) {
					screen.type(Key.DOWN);
					Thread.sleep(3000);
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
		Pattern element = null;
		try {
			Match messageFound = screen.findText(message);
			messageFound.highlight();
			if(yesOrNo.equalsIgnoreCase("yes")) {
				element = new Pattern(iconPath + "yes.png");
			} else {
				element = new Pattern(iconPath + "no.png");
			}
			Match elementFound = screen.find(element);
			elementFound.click();
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
			Thread.sleep(AmortTemplateConstants.THIRTYSECONDSWAITTIME);
			
			screen.click(dropIcon);
			Pattern amortizedCheckBox = new Pattern(iconPath + "amortiedcheckbox.png");
			screen.click(amortizedCheckBox);
			for(int i=0; i<5; i++) {
				screen.type(Key.TAB);
			}
			screen.type(amortTemplate);
			clickSaveButton(statusMessage);
			Thread.sleep(AmortTemplateConstants.THIRTYSECONDSWAITTIME);
			
			Rectangle rectangle = new Rectangle(250, 525, 75, 30);
			screen.setRect(rectangle);
			screen.highlight();
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
			Thread.sleep(AmortTemplateConstants.ONEMINUTEWAITTIME);
			
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
				Match masterSeriesFound = screen.findText("TEST 123");
				masterSeriesFound.click();
			}
			
			clickSaveButton(statusMessage);
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
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
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
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
			Thread.sleep(AmortTemplateConstants.ONEMINUTEWAITTIME);
			screen = new Screen();
			Match foundMaximize = screen.findText("COMPASS");
			foundMaximize.doubleClick();
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
			Log.message("End startExeApp: appPath: "+appPath);
		} catch(InterruptedException | FindFailed | IOException e) {
			return;
		}
	}
	
	public void killApp() {
		Log.message("Start killApp: taskkill /F /IM NBCU.Compass.exe");
		try {
			Runtime rt = Runtime.getRuntime();
			rt.exec("taskkill /F /IM NBCU.Compass.exe");
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
			Log.message("End killApp: taskkill /F /IM NBCU.Compass.exe");
		} catch(Exception e) {
			return;
		}
	}
	
	@SuppressWarnings("unused")
	public void loginCompass(String username, String password, String displayName, String statusMessage) {
		Log.message("Start loginCompass: Logging in application for user: "+username);
		screen = new Screen();
		try {
			Pattern usernameImg = new Pattern(iconPath + "username.png");
			Pattern passwordImg = new Pattern(iconPath + "password.png");
			Pattern signin = new Pattern(iconPath + "signin.png");
			Pattern contractManagementTab = new Pattern(iconPath + "contractManagementtab.png");
			if(isElementFoundByImage(usernameImg)) {
				Log.message("User ["+username+"] is not already logged in Compass application");
				screen.wait(usernameImg, 10);	
				screen.type(usernameImg, username);
				screen.type(passwordImg, password);
				screen.click(signin);
				Thread.sleep(AmortTemplateConstants.ONEMINUTEWAITTIME);
				int waitCount = 0;
				while(waitForLoadingContract() && waitCount < 5) {
					Thread.sleep(AmortTemplateConstants.ONEMINUTEWAITTIME);
					waitCount++;
				}
				screen.click(contractManagementTab);
				Log.pass("User ["+username+"] successfully logged in Compass application");
			} else {
				closePopupIfAny();
				if(isElementFoundByImage(contractManagementTab)) {
					Log.message("User ["+username+"] already logged in Compass application");
					Pattern closeButton = new Pattern(iconPath + "closebutton.png");
					while(isElementFoundByImage(closeButton)) {
						screen.click(closeButton);
						clickYesOrNoOnPopup("Do you want to save the changes?", "No");
						Thread.sleep(AmortTemplateConstants.ONEMINUTEWAITTIME);
					}
					try {
						Match userFound = screen.findText(displayName);
						screen.click(contractManagementTab);
					} catch(FindFailed e) {
						killApp();
						launchAppUsingNativeWindowHandle(configProperty.getProperty("appPath"), configProperty.getProperty("url"), configProperty.getProperty("appName"), statusMessage);
						loginCompass(username, password, displayName, statusMessage);
					}
					
				} else {
					killApp();
					launchAppUsingNativeWindowHandle(configProperty.getProperty("appPath"), configProperty.getProperty("url"), configProperty.getProperty("appName"), statusMessage);
					loginCompass(username, password, displayName, statusMessage);				
				}
				screen = new Screen();
			}
			Log.message("End loginCompass: Logging in application for user: "+username);
		} catch(FindFailed | InterruptedException e) {
			writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
			Log.fail(e.getMessage(), screen);
			killApp();
		}
	}
	
	@SuppressWarnings("unused")
	private boolean waitForLoadingContract() {
		try {
			Match loadingContractFound = screen.findText("Loading Contract Management");
			return true;			
		} catch (FindFailed e) {
			return false;
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
			for(int i=0; i<2; i++) {
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

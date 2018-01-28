package nbcu.compass.amorttemplate.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;

public class AutomationAgent {
	
	private static WindowsDriver appSession = null;
	
	private void clickAt(int x, int y) {
		Actions vActions = new Actions(appSession);
		vActions.moveToElement(appSession.findElementByName("System"), 0,0);
		Action vClickAction = vActions.build();
		vClickAction.perform();
        vActions.moveByOffset(x, y);
		vActions.doubleClick();
		vClickAction = vActions.build();
		vClickAction.perform();
    }

	public void generateAmort() throws InterruptedException {
		
		appSession.findElementByName("Amortize").click();
		appSession.findElementByName("Generate Amort").click();
		
		clickYesOnPopup("All old amort data will be deleted. Do you want to continue?");
		clickYesOnPopup("Effective Date should be earlier or equal to the Amort Window Start Date");

		List<WebElement> months = appSession.findElementsByName("Amort Amt");
		int count = 0;
		for(WebElement month:months) {
			if(!month.getText().equalsIgnoreCase("Amort Amt")) {
				Point point = month.getLocation();
				if(point.getY()>0) {
					month.click();
					System.out.println(month.getText());
					count++;
				}
			}
		}
		for(int i=0; i<count; i++) {
			appSession.getKeyboard().sendKeys(Keys.ARROW_DOWN);
		}
		count=0;
		months = appSession.findElementsByName("Amort Amt");
		for(WebElement month:months) {
			if(!month.getText().equalsIgnoreCase("Amort Amt")) {
				Point point = month.getLocation();
				if(point.getY()>0) {
					month.click();
					System.out.println(month.getText());
					count++;
				}
			}
		}
		for(int i=0; i<count; i++) {
			appSession.getKeyboard().sendKeys(Keys.ARROW_DOWN);
		}
		count=0;
		months = appSession.findElementsByName("Amort Amt");
		for(WebElement month:months) {
			if(!month.getText().equalsIgnoreCase("Amort Amt")) {
				Point point = month.getLocation();
				if(point.getY()>0) {
					month.click();
					System.out.println(month.getText());
					count++;
				}
			}
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
	
	public void setAllocationData() throws InterruptedException {
		appSession.findElementByName("Allocation").click();
		appSession.findElementByAccessibilityId("Row_0").click();
		Actions vActions = new Actions(appSession);
		moveByOffsetAndClick(vActions, -125, 0);
		moveByOffsetAndClick(vActions, -7, 28);
		moveByOffsetAndClick(vActions, 45, 0);
		appSession.getKeyboard().sendKeys("Breakage");
		moveByOffsetAndClick(vActions, 60, 0);
		appSession.getKeyboard().sendKeys("AmortTemplateConstants.FIVESECONDSWAITTIME");
		appSession.findElementByAccessibilityId("SaveButton").click();
		Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
		clickElement("License Fee Type");
		moveByOffsetAndClick(vActions, -35, 0);
		moveByOffsetAndClick(vActions, -30, 0);
		moveByOffsetAndClick(vActions, 0, 30);
		clickElement("License Fee Type");
		clickElement("Amortization Template");
		appSession.getKeyboard().sendKeys("Acquired Movies");
		appSession.findElementByAccessibilityId("SaveButton").click();
		Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
	}

	private void moveByOffsetAndClick(Actions vActions, int x, int y) {
		vActions.moveByOffset(x, y);
		vActions.click();
		Action vClickAction = vActions.build();
		vClickAction.perform();
	}
	
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

	public void openTitleAndWindow(String financeType, String startDate, String endDate, String runsInPlayDay, String runsPDAllowed) throws InterruptedException {
		appSession.findElementByAccessibilityId("Row_0").click();
		Actions vActions = new Actions(appSession);
		vActions.moveByOffset(-170, 0);
		vActions.doubleClick();
		Action vClickAction = vActions.build();
		vClickAction.perform();
		
		Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
		appSession.findElementByAccessibilityId("AddWindow").click();
		Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
		
		setPlayWindowAttribute("Start Date", startDate);
		setPlayWindowAttribute("End Date", endDate);
		setPlayWindowAttribute("Runs in Play Day", runsInPlayDay);
		setPlayWindowAttribute("Runs/PD Allowed", runsPDAllowed);
		appSession.findElementByAccessibilityId("SaveButton").click();
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
	}

	public void createContract(String distributor, String dealType, String negotiatedBy, String titleName, String titleType) throws InterruptedException {
		appSession.findElementByName("Create New Contract").click();
		Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
		SimpleDateFormat df = new SimpleDateFormat("YYYYMMDDHHmmss");
		appSession.findElementByAccessibilityId("DistributorPackageTextBox").sendKeys("TestPackage_" + df.format(new Date()));
		setValueInDropdown("DistributorComboBox", distributor);
		setValueInDropdown("DealTypeCombobox", dealType);
		setValueInDropdown("NegotiatedByCombobox", negotiatedBy);
		addTitle(titleName, titleType);
		appSession.findElementByAccessibilityId("SaveButton").click();
		Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
		
	}

	private void addTitle(String titleName, String titleType) throws InterruptedException {
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
					appSession.findElementByName(titleType).click();
					titleTypeSet= true;
				}
			}
		}
		Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
	}
	
	private void setValueInDropdown(String key, String value) {
		WebElement parent =  appSession.findElementByAccessibilityId(key);
        parent.click();
        WebElement textBox = parent.findElement(By.className("TextBox"));
    	textBox.click();
    	textBox.sendKeys(value);
	}
}

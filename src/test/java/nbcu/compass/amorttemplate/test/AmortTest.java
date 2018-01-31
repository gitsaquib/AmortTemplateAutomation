package nbcu.compass.amorttemplate.test;

import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import nbcu.compass.amorttemplate.util.AmortExcelReader;
import nbcu.compass.amorttemplate.util.AmortTemplateGrid;
import nbcu.compass.amorttemplate.util.AmortTemplateUtil;
import nbcu.compass.amorttemplate.util.AutomationAgent;
import nbcu.compass.amorttemplate.util.EnvironmentPropertiesReader;
import nbcu.compass.amorttemplate.util.License;
import nbcu.compass.amorttemplate.util.TestData;
import nbcu.compass.amorttemplate.util.User;

public class AmortTest {

	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();
	private AutomationAgent automationAgent = null;
	private Map<String, User> users = null;
	private Map<String, License> licenses = null;
	private Map<String, AmortTemplateGrid> amortTemplateGrids = null;
	private Map<String, TestData> testDatas = null;
	
	@BeforeSuite
	public void init() {
		AmortExcelReader excelReader = new AmortExcelReader();
		users = excelReader.readUser();
		testDatas = excelReader.readTestData();
		licenses = excelReader.readLicense();
		amortTemplateGrids = excelReader.readAmortTemplateGrid("US");
		automationAgent = new AutomationAgent();
	}
	
	@Test
	public void testAmort() throws InterruptedException {
		Map<Integer, String> amortsFromCalculation = AmortTemplateUtil.calculateAmort(amortTemplateGrids);
		User user = users.get("User1");
		AmortTemplateGrid amortTemplateGrid= amortTemplateGrids.get("Original Movies_Series_Original Movies");
		automationAgent.launchAppUsingNativeWindowHandle(configProperty.getProperty("appPath"), 
														 configProperty.getProperty("url"), 
														 configProperty.getProperty("appName"));
		TestData testData = testDatas.get("TC1"); 
		System.out.println(testData);
		if(null == user) {
			System.out.println("Unable to read user data");
		}
		/*
		automationAgent.loginCompass(user.getUsername(), user.getPassword());
		automationAgent.createContract(testData.getDistributor(), testData.getDealType(), testData.getNegotiatedBy(), testData.getTitleName(), amortTemplateGrid.getTitleTypeName());
		*/
		automationAgent.openTitleAndWindow(amortTemplateGrid.getFinanceTypeName(), testData.getWindows());
		/*
		License license = licenses.get("TC1");
		double amt = automationAgent.setAllocationData(license.getLicenseType(), license.getLicenseAmount(), amortTemplateGrid.getAmortTemplateName());
		Map<Integer, String> amortsFromApplication = automationAgent.generateAmort(amt);
		Set<Integer> keys = amortsFromApplication.keySet();
		for(Integer key:keys) {
			Assert.isTrue(amortsFromApplication.get(key).equalsIgnoreCase(amortsFromCalculation.get(key)), "Amorts are equal");
		}
		*/
		
	}
}

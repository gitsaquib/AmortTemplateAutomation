package nbcu.compass.amorttemplate;

import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;

import nbcu.compass.amorttemplate.util.AmortExcelReader;
import nbcu.compass.amorttemplate.util.AmortTemplateGrid;
import nbcu.compass.amorttemplate.util.AutomationAgent;
import nbcu.compass.amorttemplate.util.EnvironmentPropertiesReader;
import nbcu.compass.amorttemplate.util.TestData;
import nbcu.compass.amorttemplate.util.User;
import nbcu.compass.amorttemplate.util.Window;

public class AmortTest {

	private static Logger logger = LoggerFactory.getLogger(AmortTest.class);
	
	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();
	private AutomationAgent automationAgent = null;
	private Map<String, User> users = null;
	private Map<String, AmortTemplateGrid> amortTemplateGrids = null;
	private Map<String, TestData> testDatas = null;
	
	@BeforeSuite
	public void init() {
		AmortExcelReader excelReader = new AmortExcelReader();
		users = excelReader.readUser();
		testDatas = excelReader.readTestData();
		amortTemplateGrids = excelReader.readAmortTemplateGrid("US");
		automationAgent = new AutomationAgent();
	}
	
	
	@Test
	public void testAmort() throws InterruptedException {
		User user = users.get("User1");
		AmortTemplateGrid amortTemplateGrid= amortTemplateGrids.get("Original Movies_Series_Original Movies");
		automationAgent.launchAppUsingNativeWindowHandle(configProperty.getProperty("appPath"), 
														 configProperty.getProperty("url"), 
														 configProperty.getProperty("appName"));
		TestData testData = testDatas.get("TC1"); 
		logger.info("User: "+user.getUsername());
		automationAgent.loginCompass(user.getUsername(), user.getPassword());
		automationAgent.createContract(testData.getDistributor(), testData.getDealType(), testData.getNegotiatedBy(), testData.getTitleName(), amortTemplateGrid.getTitleTypeName());
		List<Window> windows = testData.getWindows();
		for(Window window:windows) {
			automationAgent.openTitleAndWindow(amortTemplateGrid.getFinanceTypeName(), window.getStartDate(), window.getEndDate(), window.getRunInPlayDay(), window.getRunsPDAllowed());
		}
		automationAgent.setAllocationData();
		automationAgent.generateAmort();
	}
}

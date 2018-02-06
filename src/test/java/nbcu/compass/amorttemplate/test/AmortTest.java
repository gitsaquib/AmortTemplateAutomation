package nbcu.compass.amorttemplate.test;

import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import nbcu.compass.amorttemplate.util.AmortDataProvider;
import nbcu.compass.amorttemplate.util.AmortExcelReader;
import nbcu.compass.amorttemplate.util.AmortTemplateGrid;
import nbcu.compass.amorttemplate.util.AmortTemplateUtil;
import nbcu.compass.amorttemplate.util.AutomationAgent;
import nbcu.compass.amorttemplate.util.EmailReport;
import nbcu.compass.amorttemplate.util.EnvironmentPropertiesReader;
import nbcu.compass.amorttemplate.util.License;
import nbcu.compass.amorttemplate.util.Log;
import nbcu.compass.amorttemplate.util.TestData;
import nbcu.compass.amorttemplate.util.User;

@Listeners(EmailReport.class)
public class AmortTest {

	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();
	private AutomationAgent automationAgent = null;
	private Map<String, User> users = null;
	private Map<String, License> licenses = null;
	private Map<Integer, AmortTemplateGrid> amortTemplateGrids = null;
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
	
	@Test(priority=1, description="Validating amort template for US networks", dataProviderClass=AmortDataProvider.class, dataProvider="amortDataProvider")
	public void testAmortTemplateUS(int uniqueId) throws InterruptedException {
		Log.message("Validating amort templaate for US networks: "+uniqueId);
		License license = licenses.get("TC1");
		TestData testData = testDatas.get("TC1"); 
		AmortTemplateGrid amortTemplateGrid= amortTemplateGrids.get(uniqueId);
		Map<Integer, String> amortsFromCalculation = AmortTemplateUtil.calculateAmort(amortTemplateGrid, license.getLicenseAmount(), testData);
		User user = users.get("User1");
		automationAgent.launchAppUsingNativeWindowHandle(configProperty.getProperty("appPath"), 
														 configProperty.getProperty("url"), 
														 configProperty.getProperty("appName"));
		if(null == user) {
			Log.message("Unable to read user data");
			return;
		}
		automationAgent.loginCompass(user.getUsername(), user.getPassword());
		automationAgent.createContract(testData.getDistributor(), testData.getDealType(), testData.getNegotiatedBy(), testData.getTitleName(), amortTemplateGrid.getTitleTypeName());
		automationAgent.openTitleAndWindow(amortTemplateGrid.getFinanceTypeName(), testData.getWindows());
		double amt = automationAgent.setAllocationData(license.getLicenseType(), license.getLicenseAmount(), amortTemplateGrid.getAmortTemplateName());
		Map<Integer, String> amortsFromApplication = automationAgent.generateAmort(amt);
		Set<Integer> keys = amortsFromApplication.keySet();
		boolean overAllPassOrFail = true;
		String reportStr = automationAgent.setTableStyleForExtentReport();
		reportStr += automationAgent.openTable();
		reportStr += automationAgent.addTableHeader();
		for(Integer key:keys) {
			reportStr += automationAgent.setTableBodyForExtentReport(key+"", amortsFromApplication.get(key), amortsFromCalculation.get(key));
			if(!amortsFromApplication.get(key).equalsIgnoreCase(amortsFromCalculation.get(key))) {
				overAllPassOrFail = false;
			}
		}
		reportStr += automationAgent.closeTable();
		if(overAllPassOrFail) {
			Log.pass(reportStr);
		} else {
			Log.pass(reportStr);
		}
		automationAgent.closeApplication();
	}
}

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
	private static AmortExcelReader excelReader = null;
	private AutomationAgent automationAgent = null;
	private Map<String, User> users = null;
	private Map<String, License> licenses = null;
	private Map<String, AmortTemplateGrid> amortTemplateGrids = null;
	private Map<String, TestData> testDatas = null;
	
	@BeforeSuite
	public void init() {
		excelReader = new AmortExcelReader();
		testDatas = excelReader.readTestData();
		licenses = excelReader.readLicense();
		users = excelReader.readUser();
		automationAgent = new AutomationAgent();
	}
	
	@SuppressWarnings("static-access")
	@Test(priority=1, description="Validating amort template for US networks", dataProviderClass=AmortDataProvider.class, dataProvider="amortDataProvider")
	public void testAmortTemplate(String uniqueKey) {
		String status = "";
		AmortTemplateGrid amortTemplateGrid = null;
		try {
			Log.message("Validating amort template for US networks: "+uniqueKey);
			Set<String> tcIds = testDatas.keySet();
			TestData testData = null;
			License license = null;
			for(String tcId:tcIds) {
				testData = testDatas.get(tcId);
				if(testData.getNetwork().equalsIgnoreCase(configProperty.getProperty("network"))) {
					license = licenses.get("TC1");
					break;
				}
			}
			
			amortTemplateGrids = excelReader.readAmortTemplateGrid(testData.getNetwork());
			amortTemplateGrid = amortTemplateGrids.get(uniqueKey);
			
			Map<Integer, String> amortsFromCalculation = AmortTemplateUtil.calculateAmort(amortTemplateGrid, license.getLicenseAmount(), testData);
			User user = users.get("User1");
			automationAgent.launchAppUsingNativeWindowHandle(configProperty.getProperty("appPath"), 
															 configProperty.getProperty("url"), 
															 configProperty.getProperty("appName"));
			automationAgent.loginCompass(user.getUsername(), user.getPassword(), user.getDisplayName());
			automationAgent.createContract(configProperty.getProperty("network"), testData.getDistributor(), testData.getDealType(), testData.getNegotiatedBy(), uniqueKey, amortTemplateGrid.getTitleTypeName());
			automationAgent.openTitleAndWindow(amortTemplateGrid.getFinanceTypeName(), testData.getWindows());
			//automationAgent.addEpisode("Episode-1", "Season1");
			Double amt = automationAgent.setAllocationData(license.getLicenseType(), license.getLicenseAmount(), amortTemplateGrid.getAmortTemplateName());
			if(null != amt) {
				Map<Integer, String> amortsFromApplication = automationAgent.generateAmort(amt);
				if(null != amortsFromApplication) {
					Set<Integer> keys = amortsFromApplication.keySet();
					boolean overAllPassOrFail = true;
					String reportStr = automationAgent.setTableStyleForExtentReport();
					reportStr += automationAgent.openTable();
					reportStr += automationAgent.addTableHeader();
					for(Integer key:keys) {
						reportStr += automationAgent.setTableBodyForExtentReport(key+"", amortsFromApplication.get(key), amortsFromCalculation.get(key));
						if(!amortsFromApplication.get(key).replace(".00", "").equalsIgnoreCase(amortsFromCalculation.get(key))) {
							overAllPassOrFail = false;
						}
					}
					reportStr += automationAgent.closeTable();
					if(overAllPassOrFail) {
						Log.pass(reportStr);
					} else {
						Log.fail(reportStr, automationAgent.getAppSession());
					}
					automationAgent.closeApplication();
					status = amortTemplateGrid.getAmortTemplateNo() 
									+ "\t" + amortTemplateGrid.getAmortTemplateName() 
									+ "\t" + amortTemplateGrid.getTitleTypeName()
									+ "\t" + amortTemplateGrid.getFinanceTypeName()
									+ "\t" + (overAllPassOrFail?"Pass":"Fail");
					automationAgent.writeResultInTxtFile(configProperty.getProperty("network"), status);
					Log.endTestCase();
				}
			}
			/*
			automationAgent.scheduleTitle(configProperty.getProperty("url"),
										  configProperty.getProperty("appName"),
										  1015807+"", "02/06/2018", "03/31/2018", 2);
			*/
		} catch(Exception e) {
			e.printStackTrace();
			status = amortTemplateGrid.getAmortTemplateNo() 
					+ "\t" + amortTemplateGrid.getAmortTemplateName() 
					+ "\t" + amortTemplateGrid.getTitleTypeName()
					+ "\t" + amortTemplateGrid.getFinanceTypeName()
					+ "\t" + "Fail";
			automationAgent.writeResultInTxtFile(configProperty.getProperty("network"), status);
			automationAgent.killApp();
			Log.fail(e.getMessage());
		}
	}
}

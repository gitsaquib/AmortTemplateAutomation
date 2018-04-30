package nbcu.compass.amorttemplate.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import nbcu.compass.amorttemplate.util.AmortDataProvider;
import nbcu.compass.amorttemplate.util.AmortExcelReader;
import nbcu.compass.amorttemplate.util.AmortTemplateGrid;
import nbcu.compass.amorttemplate.util.AmortTemplateUtil;
import nbcu.compass.amorttemplate.util.WADAutomationAgent;
import nbcu.compass.amorttemplate.util.EmailReport;
import nbcu.compass.amorttemplate.util.EnvironmentPropertiesReader;
import nbcu.compass.amorttemplate.util.License;
import nbcu.compass.amorttemplate.util.Log;
import nbcu.compass.amorttemplate.util.TestData;
import nbcu.compass.amorttemplate.util.User;

@Listeners(EmailReport.class)
public class AmortWADTest {

	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();
	private static AmortExcelReader excelReader = null;
	private WADAutomationAgent automationAgent = null;
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
		automationAgent = new WADAutomationAgent();
	}
	
	@SuppressWarnings("static-access")
	@Test(priority=1, description="Validating amort template", dataProviderClass=AmortDataProvider.class, dataProvider="amortDataProvider")
	public void testAmortTemplate(String uniqueKey) {
		String status = "";
		AmortTemplateGrid amortTemplateGrid = null;
		try {
			Log.message("Validating amort template for "+configProperty.getProperty("network")+": "+uniqueKey);
			Set<String> tcIds = testDatas.keySet();
			TestData testData = null;
			License license = null;
			for(String tcId:tcIds) {
				testData = testDatas.get(tcId);
				if(testData.getNetwork().equalsIgnoreCase(configProperty.getProperty("network"))) {
					license = licenses.get(testData.getTcNo());
					break;
				}
			}
			
			amortTemplateGrids = excelReader.readAmortTemplateGrid(testData.getNetwork());
			amortTemplateGrid = amortTemplateGrids.get(uniqueKey);
			
			String statusMessage = amortTemplateGrid.getAmortTemplateNo() 
					+ "\t" + amortTemplateGrid.getAmortTemplateName() 
					+ "\t" + amortTemplateGrid.getTitleTypeName()
					+ "\t" + amortTemplateGrid.getFinanceTypeName()
					+ "\t" +"Fail";
			
			Map<Integer, String> amortsFromCalculation = AmortTemplateUtil.calculateAmort(amortTemplateGrid, license.getLicenseAmount(), testData);
			if(null == amortsFromCalculation || amortsFromCalculation.isEmpty()) {
				status = amortTemplateGrid.getAmortTemplateNo() 
						+ "\t" + amortTemplateGrid.getAmortTemplateName() 
						+ "\t" + amortTemplateGrid.getTitleTypeName()
						+ "\t" + amortTemplateGrid.getFinanceTypeName()
						+ "\t" + "Fail";
				automationAgent.writeResultInTxtFile(configProperty.getProperty("network"), status);
				automationAgent.killApp();
				Log.fail("Calculation failed!!!");
			}
			Set<Integer> keys = amortsFromCalculation.keySet();
			for(Integer key:keys) {
				System.out.println(key+") "+amortsFromCalculation.get(key));
			}
			User user = users.get("User1");
			automationAgent.launchAppUsingNativeWindowHandle(configProperty.getProperty("appPath"), 
															 configProperty.getProperty("url"), 
															 configProperty.getProperty("appName"),
															 statusMessage);
			automationAgent.loginCompass(user.getUsername(), user.getPassword(), user.getDisplayName(), statusMessage);
			/*
			if(null != amortTemplateGrid.getAddEpisode() && "Y".equalsIgnoreCase(amortTemplateGrid.getAddEpisode())) {
				automationAgent.searchTitleWithEpisodes(configProperty.getProperty("network"), configProperty.getProperty("showId"), uniqueKey, amortTemplateGrid.getTitleTypeName(), statusMessage);
			} else {
				automationAgent.createContract(configProperty.getProperty("network"), testData.getDistributor(), testData.getDealType(), testData.getNegotiatedBy(), uniqueKey, amortTemplateGrid.getTitleTypeName(), statusMessage);
				automationAgent.openTitleAndWindow(amortTemplateGrid.getFinanceTypeName(), testData.getWindows(), statusMessage);
			}
			*/
			automationAgent.createContract(configProperty.getProperty("network"), testData.getDistributor(), testData.getDealType(), testData.getNegotiatedBy(), uniqueKey, amortTemplateGrid.getTitleTypeName(), statusMessage);
			automationAgent.openTitleAndWindow(amortTemplateGrid.getFinanceTypeName(), testData.getWindows(), statusMessage);
			if(null != amortTemplateGrid.getAddEpisode() && "Y".equalsIgnoreCase(amortTemplateGrid.getAddEpisode())) {
				SimpleDateFormat df = new SimpleDateFormat("YYYYMMDDHHmmss");
				String episodeName = "E-" + df.format(new Date());
				automationAgent.addEpisode(statusMessage, episodeName);
			}
			Double amt = automationAgent.setAllocationData(license.getLicenseType(), license.getLicenseAmount(), amortTemplateGrid.getAmortTemplateName(), statusMessage);
			if(null != amt) {
				Map<Integer, String> amortsFromApplication = automationAgent.generateAmort(amt, statusMessage);
				if(null != amortsFromApplication && amortsFromApplication.size() > 0) {
					Set<Integer> dataKeys = amortsFromApplication.keySet();
					boolean overAllPassOrFail = true;
					String reportStr = automationAgent.setTableStyleForExtentReport();
					reportStr += automationAgent.openTable();
					reportStr += automationAgent.addTableHeader();
					for(Integer key:dataKeys) {
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
					automationAgent.closeApplication(statusMessage);
					status = amortTemplateGrid.getAmortTemplateNo() 
									+ "\t" + amortTemplateGrid.getAmortTemplateName() 
									+ "\t" + amortTemplateGrid.getTitleTypeName()
									+ "\t" + amortTemplateGrid.getFinanceTypeName()
									+ "\t" + (overAllPassOrFail?"Pass":"Fail");
					automationAgent.writeResultInTxtFile(configProperty.getProperty("network"), status);
					Log.endTestCase();
				} else {
					automationAgent.writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
				}
			} else {
				automationAgent.writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
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

package nbcu.compass.amorttemplate.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.sikuli.script.Screen;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import nbcu.compass.amorttemplate.util.AmortDataProvider;
import nbcu.compass.amorttemplate.util.AmortExcelReader;
import nbcu.compass.amorttemplate.util.AmortTemplateGrid;
import nbcu.compass.amorttemplate.util.AmortTemplateUtil;
import nbcu.compass.amorttemplate.util.EmailReport;
import nbcu.compass.amorttemplate.util.EnvironmentPropertiesReader;
import nbcu.compass.amorttemplate.util.License;
import nbcu.compass.amorttemplate.util.Log;
import nbcu.compass.amorttemplate.util.SikuliAutomationAgent;
import nbcu.compass.amorttemplate.util.TestData;
import nbcu.compass.amorttemplate.util.User;
import nbcu.compass.amorttemplate.util.WADAutomationAgent;
import nbcu.compass.amorttemplate.util.Window;

@Listeners(EmailReport.class)
public class AmortSikuliTestSchedule {

	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();
	private static AmortExcelReader excelReader = null;
	private SikuliAutomationAgent sikuliAutomationAgent = null;
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
		sikuliAutomationAgent = new SikuliAutomationAgent();
	}
	
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
			
			User user = users.get("User1");
			String episodeName = "";
			SimpleDateFormat df = new SimpleDateFormat("YYYYMMDDHHmmss");
			String titleName = uniqueKey + df.format(new Date());

			sikuliAutomationAgent.launchAppUsingNativeWindowHandle(configProperty.getProperty("appPath"), 
															 configProperty.getProperty("url"), 
															 configProperty.getProperty("appName"),
															 statusMessage);
			sikuliAutomationAgent.loginCompass(user.getUsername(), 
										 user.getPassword(), 
										 user.getDisplayName(), 
										 statusMessage);
			
			sikuliAutomationAgent.createContract(configProperty.getProperty("network"), testData.getDistributor(), testData.getDealType(), testData.getNegotiatedBy(), titleName, amortTemplateGrid.getTitleTypeName(), statusMessage);
			
			for(Window window:testData.getWindows()) {
				window.setDefinition("Runs");
			}
			sikuliAutomationAgent.openTitleAndWindow(amortTemplateGrid.getFinanceTypeName(), testData.getWindows(), statusMessage);
			
			if(sikuliAutomationAgent.isEpisodicTitle()) {
				episodeName = "E-" + df.format(new Date());
				sikuliAutomationAgent.addEpisode(statusMessage, episodeName);
			}

			Double amt = sikuliAutomationAgent.setAllocationData(license.getLicenseType(), license.getLicenseAmount(), amortTemplateGrid.getAmortTemplateName(), statusMessage);
			
			String scheduleName = configProperty.getProperty("network")+"TestSchedule";
			boolean overAllPassOrFail = true;
			for(int run = 1; run <= amortTemplateGrid.getAmortSectionGrids().size(); run++) {
				testData.setRun(run);
				Map<Integer, String> amortsFromCalculation = AmortTemplateUtil.calculateAmort(amortTemplateGrid, license.getLicenseAmount(), testData);
				Set<Integer> keys = amortsFromCalculation.keySet();
				for(Integer key:keys) {
					System.out.println(key+") "+amortsFromCalculation.get(key));
				}
				if(sikuliAutomationAgent.isEpisodicTitle()) {
					scheduleName = sikuliAutomationAgent.scheduleTitle(scheduleName, configProperty.getProperty("network"), amortTemplateGrid.getTitleTypeName(), episodeName, statusMessage, run);
				} else {
					scheduleName = sikuliAutomationAgent.scheduleTitle(scheduleName, configProperty.getProperty("network"), amortTemplateGrid.getTitleTypeName(), titleName, statusMessage, run);
				}
				sikuliAutomationAgent.openTitle(statusMessage);
				if(null != amt) {
					Map<Integer, String> amortsFromApplication = sikuliAutomationAgent.generateAmort(amt, statusMessage);
					if(null != amortsFromApplication && amortsFromApplication.size() > 0) {
						Set<Integer> dataKeys = amortsFromApplication.keySet();
						
						String reportStr = sikuliAutomationAgent.setTableStyleForExtentReport();
						reportStr += sikuliAutomationAgent.openTable();
						reportStr += sikuliAutomationAgent.addTableHeader();
						for(Integer key:dataKeys) {
							reportStr += sikuliAutomationAgent.setTableBodyForExtentReport(key+"", amortsFromApplication.get(key), amortsFromCalculation.get(key));
							if(!amortsFromApplication.get(key).replace(".00", "").equalsIgnoreCase(amortsFromCalculation.get(key))) {
								overAllPassOrFail = false;
							}
						}
						reportStr += sikuliAutomationAgent.closeTable();
						if(overAllPassOrFail) {
							Log.pass(reportStr);
						} else {
							Log.fail(reportStr, new Screen());
						}
					} else {
						sikuliAutomationAgent.writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
						sikuliAutomationAgent.killApp();
						Log.fail("Unable to read amorts");
					}
				} else {
					sikuliAutomationAgent.writeResultInTxtFile(configProperty.getProperty("network"), statusMessage);
					sikuliAutomationAgent.killApp();
					Log.fail("Unable to read amorts");
				}
			}
			sikuliAutomationAgent.killApp();
			status = amortTemplateGrid.getAmortTemplateNo() 
							+ "\t" + amortTemplateGrid.getAmortTemplateName() 
							+ "\t" + amortTemplateGrid.getTitleTypeName()
							+ "\t" + amortTemplateGrid.getFinanceTypeName()
							+ "\t" + (overAllPassOrFail?"Pass":"Fail");
			sikuliAutomationAgent.writeResultInTxtFile(configProperty.getProperty("network"), status);
			Log.endTestCase();
		} catch(Exception e) {
			e.printStackTrace();
			status = amortTemplateGrid.getAmortTemplateNo() 
					+ "\t" + amortTemplateGrid.getAmortTemplateName() 
					+ "\t" + amortTemplateGrid.getTitleTypeName()
					+ "\t" + amortTemplateGrid.getFinanceTypeName()
					+ "\t" + "Fail";
			sikuliAutomationAgent.writeResultInTxtFile(configProperty.getProperty("network"), status);
			sikuliAutomationAgent.killApp();
			Log.fail(e.getMessage());
		}
	}
}

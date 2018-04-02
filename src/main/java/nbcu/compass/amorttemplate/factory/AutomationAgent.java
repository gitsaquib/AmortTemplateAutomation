package nbcu.compass.amorttemplate.factory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import nbcu.compass.amorttemplate.util.AmortTemplateConstants;
import nbcu.compass.amorttemplate.util.Log;
import nbcu.compass.amorttemplate.util.Window;

public abstract class AutomationAgent {

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
	
	public abstract void launchApplicationFromBrowser(String url, String appWebUrl, String statusMessage);
	public abstract void launchAppUsingNativeWindowHandle(String appPath, String url, String appName, String statusMessage);
	public abstract void loginCompass(String username, String password, String displayName, String statusMessage);
	public abstract void createContract(String network, String distributor, String dealType, String negotiatedBy, String titleName, String titleType, String statusMessage);
	public abstract void openTitleAndWindow(String financeType, List<Window> windows, String statusMessage);
	public abstract void addEpisode(String statusMessage);
	public abstract void searchTitleWithEpisodes(String network, String showId, String titleName, String titleType, String statusMessage);
	public abstract Double setAllocationData(String licenseType, String licenseAmount, String amortTemplate, String statusMessage);
	public abstract Map<Integer, String> generateAmort(double totalLicenseFee, String statusMessage);
}

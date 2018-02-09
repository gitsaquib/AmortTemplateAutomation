package nbcu.compass.amorttemplate.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.DataProvider;

public class AmortDataProvider {

	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();
	
	@DataProvider(parallel = false)
	public static Iterator<String> amortDataProvider() { 
		AmortExcelReader excelReader = new AmortExcelReader();
		Map<String, AmortTemplateGrid> amortTemplateGrids = excelReader.readAmortTemplateGrid(configProperty.getProperty("network"));
		Set<String> uniqueIds = amortTemplateGrids.keySet();
		return uniqueIds.iterator();
	}
}

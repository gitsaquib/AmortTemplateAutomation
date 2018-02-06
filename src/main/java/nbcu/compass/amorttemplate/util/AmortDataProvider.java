package nbcu.compass.amorttemplate.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.DataProvider;

public class AmortDataProvider {

	
	@DataProvider(parallel = true)
	public static Iterator<Integer> amortDataProvider() { 
		AmortExcelReader excelReader = new AmortExcelReader();
		Map<Integer, AmortTemplateGrid> amortTemplateGrids = excelReader.readAmortTemplateGrid("US");
		Set<Integer> uniqueIds = amortTemplateGrids.keySet();
		return uniqueIds.iterator();
	}
}

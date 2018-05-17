package nbcu.compass.amorttemplate.test;

import java.util.List;

import nbcu.compass.amorttemplate.util.AmortExcelReader;
import nbcu.compass.amorttemplate.util.Result;

public class Report {

	public static void main(String args[]) {
		AmortExcelReader reader = new AmortExcelReader();
		List<Result> results = reader.readAmortTemplateGridForMergedReport();
		reader.generateReportXlsx(results);
	}
}

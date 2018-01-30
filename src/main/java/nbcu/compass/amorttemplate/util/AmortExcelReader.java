package nbcu.compass.amorttemplate.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AmortExcelReader {
	
	private static String basePath() throws IOException {
		File directory = new File(".");
		String strBasepath = directory.getCanonicalPath();
		return strBasepath;
	}

	public Map<String, AmortTemplateGrid> readAmortTemplateGrid(String network) {
		File inputFile = null;
		XSSFWorkbook workbook = null;
		try {
			inputFile = new File(basePath() + File.separator + "TestData"+ File.separator + "AmortTemplate"+network+".xlsx");
			workbook = new XSSFWorkbook(inputFile);
		} catch (Exception e) {
		
		}
		XSSFRow sheetRow;
		XSSFSheet templateGrid = workbook.getSheet("AmortTemplateGrid");
		Map<String, Integer> headerMap = populateHeaderMap(templateGrid);
		Map<String, AmortTemplateGrid> amortTemplateGridMap = new HashMap<String, AmortTemplateGrid>();
		for(int j = 1; j < templateGrid.getLastRowNum(); j++) {
			AmortTemplateGrid amortTemplateGrid = new AmortTemplateGrid();
			sheetRow = templateGrid.getRow(j);
			String uniqueName = sheetRow.getCell(headerMap.get(HeaderEnum.AmortTemplateName.toString())).getStringCellValue()
								+ "_"
								+ sheetRow.getCell(headerMap.get(HeaderEnum.TitleTypeName.toString())).getStringCellValue()
								+ "_"
								+ sheetRow.getCell(headerMap.get(HeaderEnum.FinanceTypeName.toString())).getStringCellValue();
			amortTemplateGrid.setUniqueName(uniqueName);
			amortTemplateGrid.setFirstMonthAmortPercent(sheetRow.getCell(headerMap.get(HeaderEnum.FirstMonthAmortPercent.toString())).getNumericCellValue());
			amortTemplateGrid.setAmortTemplateName(sheetRow.getCell(headerMap.get(HeaderEnum.AmortTemplateName.toString())).getStringCellValue());
			amortTemplateGrid.setFinanceTypeName(sheetRow.getCell(headerMap.get(HeaderEnum.FinanceTypeName.toString())).getStringCellValue());
			amortTemplateGrid.setIsMultipleWindowFlag(sheetRow.getCell(headerMap.get(HeaderEnum.IsMultipleWindowFlag.toString())).getStringCellValue());
			amortTemplateGrid.setMaxMonths(sheetRow.getCell(headerMap.get(HeaderEnum.MaxMonths.toString())).getNumericCellValue());
			amortTemplateGrid.setProjectionScheduleName(sheetRow.getCell(headerMap.get(HeaderEnum.ProjectionScheduleName.toString())).getStringCellValue());
			amortTemplateGrid.setStraightLineMonths(sheetRow.getCell(headerMap.get(HeaderEnum.StraightLineMonths.toString())).getNumericCellValue());
			amortTemplateGrid.setStraightLineName(sheetRow.getCell(headerMap.get(HeaderEnum.StraightLineName.toString())).getStringCellValue());
			amortTemplateGrid.setTimePlayName(sheetRow.getCell(headerMap.get(HeaderEnum.TimePlayName.toString())).getStringCellValue());
			amortTemplateGrid.setTitleTypeName(sheetRow.getCell(headerMap.get(HeaderEnum.TitleTypeName.toString())).getStringCellValue());
			amortTemplateGridMap.put(uniqueName, amortTemplateGrid);
		}
		return amortTemplateGridMap;
	}
	
	public Map<String, User> readUser() {
		Map<String, User> usersMap = new HashMap<String, User>();
		File inputFile = null;
		XSSFWorkbook workbook = null;
		try {
			inputFile = new File(basePath() + File.separator + "TestData"+ File.separator + "TestCaseData.xlsx");
			workbook = new XSSFWorkbook(inputFile);
			XSSFSheet users = workbook.getSheet("User");
			Map<String, Integer> headerMap = populateHeaderMap(users);
			XSSFRow sheetRow;
			for(int j = 1; j <= users.getLastRowNum(); j++) {
				User user = new User();
				sheetRow = users.getRow(j);
				user.setUsername(sheetRow.getCell(headerMap.get(HeaderEnum.Username.toString())).getStringCellValue());
				user.setPassword(sheetRow.getCell(headerMap.get(HeaderEnum.Password.toString())).getStringCellValue());
				usersMap.put(sheetRow.getCell(headerMap.get(HeaderEnum.TestUser.toString())).getStringCellValue(), user);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return usersMap;
	}

	public Map<String, TestData> readTestData() {
		Map<String, TestData> testDataMap = new HashMap<String, TestData>();
		File inputFile = null;
		XSSFWorkbook workbook = null;
		try {
			inputFile = new File(basePath() + File.separator + "TestData"+ File.separator + "TestCaseData.xlsx");
			workbook = new XSSFWorkbook(inputFile);
			XSSFRow sheetRow;
			XSSFSheet testdatas = workbook.getSheet("TestData");
			Map<String, Integer> headerMap = populateHeaderMap(testdatas);
			for(int j = 1; j <= testdatas.getLastRowNum(); j++) {
				TestData testData = new TestData();
				sheetRow = testdatas.getRow(j);
				testData.setTcNo(sheetRow.getCell(headerMap.get(HeaderEnum.TcNo.toString())).getStringCellValue());
				testData.setDistributor(sheetRow.getCell(headerMap.get(HeaderEnum.Distributor.toString())).getStringCellValue());
				testData.setDealType(sheetRow.getCell(headerMap.get(HeaderEnum.DealType.toString())).getStringCellValue());
				testData.setNegotiatedBy(sheetRow.getCell(headerMap.get(HeaderEnum.NegotiatedBy.toString())).getStringCellValue());
				testData.setTitleName(sheetRow.getCell(headerMap.get(HeaderEnum.TitleName.toString())).getStringCellValue());
				testData.setWindows(getWindowData(sheetRow.getCell(headerMap.get(HeaderEnum.TcNo.toString())).getStringCellValue()));
				testDataMap.put(sheetRow.getCell(headerMap.get(HeaderEnum.TcNo.toString())).getStringCellValue(), testData);
			}
		} catch (Exception e) {
		
		}
		return testDataMap;
	}
	
	private List<Window> getWindowData(String tcNo) {
		List<Window> windowsList = new ArrayList<Window>();
		File inputFile = null;
		XSSFWorkbook workbook = null;
		try {
			inputFile = new File(basePath() + File.separator + "TestData"+ File.separator + "TestCaseData.xlsx");
			workbook = new XSSFWorkbook(inputFile);
			XSSFRow sheetRow;
			XSSFSheet windows = workbook.getSheet("Windows");
			Map<String, Integer> headerMap = populateHeaderMap(windows);
			for(int j = 1; j <= windows.getLastRowNum(); j++) {
				sheetRow = windows.getRow(j);
				if(sheetRow.getCell(headerMap.get(HeaderEnum.TcNo.toString())).getStringCellValue().equalsIgnoreCase(tcNo)) {
					Window window = new Window();
					window.setStartDate(sheetRow.getCell(headerMap.get(HeaderEnum.StartDate.toString())).getStringCellValue());
					window.setEndDate(sheetRow.getCell(headerMap.get(HeaderEnum.EndDate.toString())).getStringCellValue());
					window.setRunInPlayDay(sheetRow.getCell(headerMap.get(HeaderEnum.RunsInPlayDay.toString())).getStringCellValue());
					window.setRunsPDAllowed(sheetRow.getCell(headerMap.get(HeaderEnum.RunsPDAllowed.toString())).getStringCellValue());
					windowsList.add(window);
				}
			}
		} catch (Exception e) {
		
		}
		return windowsList;
	}
	
	private Map<String, Integer> populateHeaderMap(XSSFSheet headerRow) {
		XSSFRow sheetRow = headerRow.getRow(0);
		Map<String, Integer> headerMap = new HashMap<String, Integer>();
		for (int i = 0; i < sheetRow.getLastCellNum(); i++) {
			for(HeaderEnum header:HeaderEnum.values()) {
				if(sheetRow.getCell(i).getStringCellValue().equalsIgnoreCase(header.toString())) {
					headerMap.put(header.toString(), i);
				}
			}
		}
		return headerMap;
	}
}

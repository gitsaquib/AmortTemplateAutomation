package nbcu.compass.amorttemplate.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCell;
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
		for(int j = 1; j <= templateGrid.getLastRowNum(); j++) {
			AmortTemplateGrid amortTemplateGrid = new AmortTemplateGrid();
			sheetRow = templateGrid.getRow(j);
			String uniqueKey = sheetRow.getCell((int)headerMap.get(HeaderEnum.AmortTemplateNo.toString())).getNumericCellValue()
								+ "_" + sheetRow.getCell(headerMap.get(HeaderEnum.AmortTemplateName.toString())).getStringCellValue()
								+ "_" + sheetRow.getCell(headerMap.get(HeaderEnum.TitleTypeName.toString())).getStringCellValue()
								+ "_" + sheetRow.getCell(headerMap.get(HeaderEnum.FinanceTypeName.toString())).getStringCellValue();
			amortTemplateGrid.setAmortTemplateNo((int) sheetRow.getCell(headerMap.get(HeaderEnum.AmortTemplateNo.toString())).getNumericCellValue());
			amortTemplateGrid.setFirstMonthAmortPercent(sheetRow.getCell(headerMap.get(HeaderEnum.FirstMonthAmortPercent.toString())).getNumericCellValue());
			amortTemplateGrid.setAmortTemplateName(sheetRow.getCell(headerMap.get(HeaderEnum.AmortTemplateName.toString())).getStringCellValue());
			amortTemplateGrid.setFinanceTypeName(sheetRow.getCell(headerMap.get(HeaderEnum.FinanceTypeName.toString())).getStringCellValue());
			amortTemplateGrid.setIsMultipleWindowFlag(sheetRow.getCell(headerMap.get(HeaderEnum.IsMultipleWindowFlag.toString())).getStringCellValue());
			amortTemplateGrid.setMaxMonths(sheetRow.getCell(headerMap.get(HeaderEnum.MaxMonths.toString())).getNumericCellValue());
			amortTemplateGrid.setStraightLineMonths(sheetRow.getCell(headerMap.get(HeaderEnum.StraightLineMonths.toString())).getNumericCellValue());
			amortTemplateGrid.setStraightLineName(sheetRow.getCell(headerMap.get(HeaderEnum.StraightLineName.toString())).getStringCellValue());
			amortTemplateGrid.setTimePlayName(sheetRow.getCell(headerMap.get(HeaderEnum.TimePlayName.toString())).getStringCellValue());
			amortTemplateGrid.setTitleTypeName(sheetRow.getCell(headerMap.get(HeaderEnum.TitleTypeName.toString())).getStringCellValue());
			amortTemplateGrid.setProjSchedFlag(sheetRow.getCell(headerMap.get(HeaderEnum.ProjSchedFlag.toString())).getStringCellValue());
			amortTemplateGrid.setAddEpisode(sheetRow.getCell(headerMap.get(HeaderEnum.AddEpisode.toString())).getStringCellValue());
			amortTemplateGrid.setAmortSectionGrids(populateSectionGrid(network, 
					(int)sheetRow.getCell(headerMap.get(HeaderEnum.AmortTemplateNo.toString())).getNumericCellValue(),
					sheetRow.getCell(headerMap.get(HeaderEnum.AmortTemplateName.toString())).getStringCellValue(),
					sheetRow.getCell(headerMap.get(HeaderEnum.TitleTypeName.toString())).getStringCellValue(),
					sheetRow.getCell(headerMap.get(HeaderEnum.FinanceTypeName.toString())).getStringCellValue()));
			amortTemplateGrid.setLicenses(readLicenses());
			amortTemplateGridMap.put(uniqueKey, amortTemplateGrid);
		}
		return amortTemplateGridMap;
	}
	
	private Map<Integer, Double> populateSectionGrid(String network, int amortTemplateNo, String templateName, String titleType, String financialType) {
		File inputFile = null;
		XSSFWorkbook workbook = null;
		try {
			inputFile = new File(basePath() + File.separator + "TestData"+ File.separator + "AmortTemplate"+network+".xlsx");
			workbook = new XSSFWorkbook(inputFile);
		} catch (Exception e) {
		
		}
		XSSFRow sheetRow;
		XSSFSheet templateGrid = workbook.getSheet("AmortTemplateSectionGrid");
		Map<String, Integer> headerMap = populateHeaderMap(templateGrid);
		Map<Integer, Double> amortTemplateGridMap = new HashMap<Integer, Double>();
		for(int j = 1; j <= templateGrid.getLastRowNum(); j++) {
			sheetRow = templateGrid.getRow(j);
			if(sheetRow.getCell((int)headerMap.get(HeaderEnum.AmortTemplateNo.toString())).getNumericCellValue() == amortTemplateNo
					&& sheetRow.getCell((int)headerMap.get(HeaderEnum.AmortTemplateName.toString())).getStringCellValue().equalsIgnoreCase(templateName)
					&& sheetRow.getCell((int)headerMap.get(HeaderEnum.TitleTypeName.toString())).getStringCellValue().equalsIgnoreCase(titleType)
					&& sheetRow.getCell((int)headerMap.get(HeaderEnum.FinanceTypeName.toString())).getStringCellValue().equalsIgnoreCase(financialType)) {
				amortTemplateGridMap.put((int)sheetRow.getCell(headerMap.get(HeaderEnum.AmortSectionNo.toString())).getNumericCellValue(), sheetRow.getCell(headerMap.get(HeaderEnum.AmortSectionPercent.toString())).getNumericCellValue());
			}
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
				Double username = sheetRow.getCell(headerMap.get(HeaderEnum.Username.toString())).getNumericCellValue();
				user.setUsername(username.intValue()+"");
				user.setPassword(sheetRow.getCell(headerMap.get(HeaderEnum.Password.toString())).getStringCellValue());
				user.setDisplayName(sheetRow.getCell(headerMap.get(HeaderEnum.DisplayName.toString())).getStringCellValue());
				usersMap.put(sheetRow.getCell(headerMap.get(HeaderEnum.TestUser.toString())).getStringCellValue(), user);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return usersMap;
	}
	
	public Map<String, License> readLicense() {
		Map<String, License> licensesMap = new HashMap<String, License>();
		File inputFile = null;
		XSSFWorkbook workbook = null;
		try {
			inputFile = new File(basePath() + File.separator + "TestData"+ File.separator + "TestCaseData.xlsx");
			workbook = new XSSFWorkbook(inputFile);
			XSSFSheet licenses = workbook.getSheet("License");
			Map<String, Integer> headerMap = populateHeaderMap(licenses);
			XSSFRow sheetRow;
			for(int j = 1; j <= licenses.getLastRowNum(); j++) {
				License license = new License();
				sheetRow = licenses.getRow(j);
				license.setTcId(sheetRow.getCell(headerMap.get(HeaderEnum.TcNo.toString())).getStringCellValue());
				license.setLicenseType(sheetRow.getCell(headerMap.get(HeaderEnum.LicenseType.toString())).getStringCellValue());
				Double licenseAmount = sheetRow.getCell(headerMap.get(HeaderEnum.LicenseAmount.toString())).getNumericCellValue();
				license.setLicenseAmount(licenseAmount.doubleValue()+"");
				licensesMap.put(sheetRow.getCell(headerMap.get(HeaderEnum.TcNo.toString())).getStringCellValue(), license);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return licensesMap;
	}
	
	public List<License> readLicenses() {
		List<License> licensesList = new ArrayList<License>();
		File inputFile = null;
		XSSFWorkbook workbook = null;
		try {
			inputFile = new File(basePath() + File.separator + "TestData"+ File.separator + "TestCaseData.xlsx");
			workbook = new XSSFWorkbook(inputFile);
			XSSFSheet licenses = workbook.getSheet("License");
			Map<String, Integer> headerMap = populateHeaderMap(licenses);
			XSSFRow sheetRow;
			for(int j = 1; j <= licenses.getLastRowNum(); j++) {
				License license = new License();
				sheetRow = licenses.getRow(j);
				license.setTcId(sheetRow.getCell(headerMap.get(HeaderEnum.TcNo.toString())).getStringCellValue());
				license.setLicenseType(sheetRow.getCell(headerMap.get(HeaderEnum.LicenseType.toString())).getStringCellValue());
				Double licenseAmount = sheetRow.getCell(headerMap.get(HeaderEnum.LicenseAmount.toString())).getNumericCellValue();
				license.setLicenseAmount(licenseAmount.doubleValue()+"");
				licensesList.add(license);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return licensesList;
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
				testData.setNetwork(sheetRow.getCell(headerMap.get(HeaderEnum.Network.toString())).getStringCellValue());
				testData.setWindows(getWindowData(sheetRow.getCell(headerMap.get(HeaderEnum.TcNo.toString())).getStringCellValue()));
				testDataMap.put(sheetRow.getCell(headerMap.get(HeaderEnum.TcNo.toString())).getStringCellValue(), testData);
			}
		} catch (Exception e) {
		
		}
		return testDataMap;
	}
	
	private List<Window> getWindowData(String tcNo) {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
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
					Date startDate = sheetRow.getCell(headerMap.get(HeaderEnum.StartDate.toString())).getDateCellValue();
					window.setStartDate(df.format(startDate));
					Date endDate = sheetRow.getCell(headerMap.get(HeaderEnum.EndDate.toString())).getDateCellValue();
					window.setEndDate(df.format(endDate));
					Double runInPlayDay = sheetRow.getCell(headerMap.get(HeaderEnum.RunsInPlayDay.toString())).getNumericCellValue();
					window.setRunInPlayDay(runInPlayDay.intValue()+"");
					Double runsPDAllowed = sheetRow.getCell(headerMap.get(HeaderEnum.RunsPDAllowed.toString())).getNumericCellValue();
					window.setRunsPDAllowed(runsPDAllowed.intValue()+"");
					windowsList.add(window);
				}
			}
		} catch (Exception e) {
		
		}
		return windowsList;
	}
	
	public List<Result> readAmortTemplateGridForMergedReport() {
		XSSFWorkbook workbook = null;
		try {
			FilenameFilter filter = new FilenameFilter() {
		        public boolean accept(File directory, String fileName) {
		            return fileName.endsWith(".xlsx") && !fileName.startsWith("Merged");
		        }
		    };
		    List<Result> results = new ArrayList<Result>();
			File rootFolder = new File(basePath() + File.separator + "MasterData");
			for(File file:rootFolder.listFiles(filter)) {
				System.out.println(file.getName());
				workbook = new XSSFWorkbook(file);
				XSSFRow sheetRow;
				XSSFSheet templateGrid = workbook.getSheet("AmortTemplateGrid");
				Map<String, Integer> headerMap = populateHeaderMap(templateGrid);
				String network = getNetworkNameFromFileName(file);
				for(int j = 1; j <= templateGrid.getLastRowNum(); j++) {
					Result result = new Result();
					sheetRow = templateGrid.getRow(j);
					result.setNetwork(network);
					result.setAmortTemplateNo((int) sheetRow.getCell(headerMap.get(HeaderEnum.AmortTemplateNo.toString())).getNumericCellValue());
					result.setAmortTemplateName(sheetRow.getCell(headerMap.get(HeaderEnum.AmortTemplateName.toString())).getStringCellValue());
					result.setFinanceTypeName(sheetRow.getCell(headerMap.get(HeaderEnum.FinanceTypeName.toString())).getStringCellValue());
					result.setAddEpisode(sheetRow.getCell(headerMap.get(HeaderEnum.AddEpisode.toString())).getStringCellValue());
					result.setStatus(sheetRow.getCell(headerMap.get(HeaderEnum.Status.toString())).getStringCellValue());
					if(null != sheetRow.getCell(headerMap.get(HeaderEnum.Remarks.toString()))) {
						result.setRemarks(sheetRow.getCell(headerMap.get(HeaderEnum.Remarks.toString())).getStringCellValue());
					} else {
						result.setRemarks("");
					}
					result.setFirstMonthAmortPercent(sheetRow.getCell(headerMap.get(HeaderEnum.FirstMonthAmortPercent.toString())).getNumericCellValue());
					result.setIsMultipleWindowFlag(sheetRow.getCell(headerMap.get(HeaderEnum.IsMultipleWindowFlag.toString())).getStringCellValue());
					result.setMaxMonths(sheetRow.getCell(headerMap.get(HeaderEnum.MaxMonths.toString())).getNumericCellValue());
					result.setStraightLineMonths(sheetRow.getCell(headerMap.get(HeaderEnum.StraightLineMonths.toString())).getNumericCellValue());
					result.setStraightLineName(sheetRow.getCell(headerMap.get(HeaderEnum.StraightLineName.toString())).getStringCellValue());
					result.setTimePlayName(sheetRow.getCell(headerMap.get(HeaderEnum.TimePlayName.toString())).getStringCellValue());
					result.setTitleTypeName(sheetRow.getCell(headerMap.get(HeaderEnum.TitleTypeName.toString())).getStringCellValue());
					result.setProjSchedFlag(sheetRow.getCell(headerMap.get(HeaderEnum.ProjSchedFlag.toString())).getStringCellValue());
					results.add(result);
				}
			}
			return results;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public static void generateReportXlsx(List<Result> results) {
		try {
			File rootFolder = new File(basePath() + File.separator + "MasterData");
			SimpleDateFormat df = new SimpleDateFormat("YYYYMMDDHHmmss");
			String newFileName = "Merged. AmortTemplateAllNetworks_" + df.format(new Date())+".xlsx";
			String excelFileName = rootFolder + File.separator + newFileName;
			
			String sheetName = "Report";
			@SuppressWarnings("resource")
			XSSFWorkbook workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet(sheetName) ;
			
			int rowIndex = 0; int columnIndex = 0;
			
			XSSFRow row = sheet.createRow(rowIndex);
			
			XSSFCell cell = row.createCell(columnIndex);
			cell.setCellValue("Network");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("AmortTemplateNo");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("AmortTemplateName");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("TitleTypeName");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("FinanceTypeName");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("StraightLineName");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("StraightLineMonths");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("TimePlayName");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("MaxMonths");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("FirstMonthAmortPercent");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("IsMultipleWindowFlag");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("ProjSchedFlag");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("AddEpisode");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("ExecutionStatus");
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellValue("Remarks");
			columnIndex = 0;
			
			rowIndex++;
			
			for (Result result:results) {
				row = sheet.createRow(rowIndex);
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getNetwork());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getAmortTemplateNo());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getAmortTemplateName());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getTitleTypeName());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getFinanceTypeName());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getStraightLineName());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getStraightLineMonths());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getTimePlayName());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getMaxMonths());
				columnIndex++;
				
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getFirstMonthAmortPercent());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getIsMultipleWindowFlag());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getProjSchedFlag());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getAddEpisode());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getStatus());
				columnIndex++;
				
				cell = row.createCell(columnIndex);
				cell.setCellValue(result.getRemarks());
				columnIndex = 0;
				
				rowIndex++;
			}
			FileOutputStream fileOut = new FileOutputStream(excelFileName);
			workBook.write(fileOut);
			fileOut.flush();
			fileOut.close();
		} catch(IOException e) {
			System.out.println("Unable to generate report");
		}
	}
	
	private String getNetworkNameFromFileName(File file) {
		String name = file.getName();
		name = name.substring(name.indexOf("AmortTemplate"));
		name = name.replaceAll("AmortTemplate","").replaceAll(".xlsx", "");
		return name;
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

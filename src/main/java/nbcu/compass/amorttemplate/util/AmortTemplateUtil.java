package nbcu.compass.amorttemplate.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AmortTemplateUtil {
	
	private static DecimalFormat df = new DecimalFormat("###.##");
	private static DecimalFormat df2 = new DecimalFormat("$###,###.##");
	
	public static Map<Integer, String> calculateAmort(AmortTemplateGrid amortTemplateGrid, String licenseFeeStr, TestData testData) {
		Double licenseFee = Double.parseDouble(licenseFeeStr);
		Double sectionPercentage[] = new Double[amortTemplateGrid.getAmortSectionGrids().size()];
		Map<Integer, Double> sectionGrids = amortTemplateGrid.getAmortSectionGrids();
		Set<Integer> sectionNos = sectionGrids.keySet();
		for(Integer sectionNo:sectionNos) {
			sectionPercentage[sectionNo-1] = sectionGrids.get(sectionNo);
		}

		List<Window> windows = testData.getWindows();
		Integer amortPeriods[] = new Integer[windows.size()];
		int index = 0;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		int totalAmortMonths = 0;
		for(Window window:windows) {
			LocalDate localStartDate = LocalDate.parse(window.getStartDate(), formatter);
			LocalDate localEndDate = LocalDate.parse(window.getEndDate(), formatter);
			amortPeriods[index] = localEndDate.getMonth().getValue() - localStartDate.getMonth().getValue() + 1;
			totalAmortMonths += amortPeriods[index];
			index++;
		}
		
		if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.STRAIGHT_LINE)) {
			return amortStraightLine(amortTemplateGrid.getFirstMonthAmortPercent(), amortTemplateGrid.getMaxMonths(), licenseFee, amortTemplateGrid.getTimePlayName(), totalAmortMonths);
		} else if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.MAX_STRAIGHT_LINE)) {
			return amortMaxStraightLine(amortTemplateGrid.getMaxMonths(), licenseFee);
		} else if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.QUARTILE)) {
			return amortQuartile(amortTemplateGrid.getFirstMonthAmortPercent(), amortTemplateGrid.getStraightLineMonths(), licenseFee, amortTemplateGrid.getIsMultipleWindowFlag(), sectionPercentage,
					amortTemplateGrid.getTimePlayName(), totalAmortMonths, amortPeriods);
		} else if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.MAX_QUARTILE)) {
			return amortMaxQuartile(amortTemplateGrid.getFirstMonthAmortPercent(), amortTemplateGrid.getStraightLineMonths(), licenseFee, amortTemplateGrid.getIsMultipleWindowFlag(), sectionPercentage,
					amortTemplateGrid.getTimePlayName(), totalAmortMonths, amortPeriods, amortTemplateGrid.getMaxMonths());
		}
		return null;
	}

	private static Map<Integer, String> amortMaxQuartile(double firstMonthPercent, Integer straightLineMonths, Double licenseFee,
			String multipleWindow, Double[] sectionPercentages, String windowsBasedOrTimeBased, int totalAmortPeriod,
			Integer[] amortPeriods, Integer maxMonths) {
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		Double[] lineItemAmtSecs = new Double[sectionPercentages.length];
		int index = 0;
		for(Double sectionPercentage:sectionPercentages) {
			lineItemAmtSecs[index] = Double.valueOf(df.format((licenseFee*sectionPercentage)/100));
			index++;
		}
		index = 1; double grandSec = 0; int monthIndex = 1;
		for(Double lineItemAmtSec:lineItemAmtSecs) {
			double lineItemAmt;
			lineItemAmt = (lineItemAmtSec)/(maxMonths/sectionPercentages.length);
			lineItemAmt = Double.valueOf(df.format(lineItemAmt));
			for(int month = 1; month <= maxMonths/lineItemAmtSecs.length; month++) {
				if(index==lineItemAmtSecs.length && month == maxMonths/lineItemAmtSecs.length) {
					amorts.put(monthIndex, df2.format(licenseFee-grandSec));
				} else {
					grandSec += lineItemAmt;
					amorts.put(monthIndex, df2.format(lineItemAmt));
				}
				monthIndex++;
			}
			index++;
		}
		
		/*
		for(Double sectionPer:sectionPercentage) {
			lineItemAmtSecs[index] = Double.valueOf(df.format((licenseFee*sectionPer)/100));
			index++;
		}
		index = 1; double grandSec = 0; int monthIndex = 1;
		for(Double lineItemAmtSec:lineItemAmtSecs) {
			double lineItemAmt;
			lineItemAmt = lineItemAmtSec*lineItemAmtSecs.length/maxMonths;
			lineItemAmt = Double.valueOf(df.format(lineItemAmt));
			for(int month = 1; month <= maxMonths/lineItemAmtSecs.length; month++) {
				if(index==lineItemAmtSecs.length && month == maxMonths/lineItemAmtSecs.length) {
					amorts.put(monthIndex, df2.format(licenseFee-grandSec));
				} else {
					grandSec += lineItemAmt;
					amorts.put(monthIndex, df2.format(lineItemAmt));
				}
				monthIndex++;
			}
			index++;
		}
		*/
		return amorts;
	}
	
	private static Map<Integer, String> amortQuartile(double firstMonthPercent, Integer straightLineMonths, Double licenseFee,
			String multipleWindow, Double[] sectionPercentage, String windowsBasedOrTimeBased, int totalAmortPeriod,
			Integer[] amortPeriods) {
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.WINDOWS_BASED)) {
			if(multipleWindow.equalsIgnoreCase("Y")) {
				Double lineItemAmt[] = new Double[sectionPercentage.length];
				int index  = 1;
				for(int in = 0; in < sectionPercentage.length; in++) {
					lineItemAmt[in] = ((licenseFee*sectionPercentage[in])/100)/amortPeriods[in];
					lineItemAmt[in] = Double.valueOf(df.format(lineItemAmt[in]));
					for(int month = 1; month <= amortPeriods[in]; month++) {
						if(month == amortPeriods[in]) {
							amorts.put(index, df2.format((licenseFee*sectionPercentage[in])/100 - lineItemAmt[in]*(amortPeriods[in]-1)));
						} else {
							amorts.put(index, df2.format(lineItemAmt[in]));
						}
						index++;
					}
				}
			}
		} else if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.TIME_BASED)) {
			if(straightLineMonths > 0) {
				double lineItemAmt;
				lineItemAmt = licenseFee/totalAmortPeriod;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= totalAmortPeriod; month++) {
					if(month == totalAmortPeriod) {
						amorts.put(month, df2.format(licenseFee - lineItemAmt*(totalAmortPeriod-1)));
					} else {
						amorts.put(month, df2.format(lineItemAmt));
					}
				}
			} else {
				double lineItemAmt;
				double firstMonthAmount = Double.valueOf(df.format((licenseFee*firstMonthPercent)/100));
				amorts.put(1, df2.format(firstMonthAmount));
				totalAmortPeriod = totalAmortPeriod - 1;
				lineItemAmt = (licenseFee-firstMonthAmount)/totalAmortPeriod;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= totalAmortPeriod; month++) {
					if(month == totalAmortPeriod) {
						amorts.put(month+1, df2.format(licenseFee - firstMonthAmount - lineItemAmt*(totalAmortPeriod-1)));
					} else {
						amorts.put(month+1, df2.format(lineItemAmt));
					}
				}
			}
		} else if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.HALLMARK)) {
			if(multipleWindow.equalsIgnoreCase("Y")) {
				int grandMonths = 0;
				for(Integer amortPeriod:amortPeriods ) {
					grandMonths += amortPeriod;
				}
				double lineItemAmt = licenseFee/grandMonths;
				for(int month=1; month<=grandMonths; month++) {
					if(month == grandMonths) {
						amorts.put(month, df2.format(licenseFee-(lineItemAmt*(grandMonths-1))));
					} else {
						amorts.put(month, df2.format(lineItemAmt));
					}
				}
			} else {
				double lineItemAmt;
				lineItemAmt = licenseFee/totalAmortPeriod;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= totalAmortPeriod; month++) {
					if(month == totalAmortPeriod) {
						amorts.put(month, df2.format(licenseFee - lineItemAmt*(totalAmortPeriod-1)));
					} else {
						amorts.put(month, df2.format(lineItemAmt));
					}
				}
			}
		}
		return amorts;
	}

	private static Map<Integer, String> amortMaxStraightLine(Integer maxMonths, Double licenseFee) {
		double lineItemAmt;
		lineItemAmt = licenseFee/maxMonths;
		lineItemAmt = Double.valueOf(df.format(lineItemAmt));
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		for(int month = 1; month <= maxMonths; month++) {
			if(month == maxMonths) {
				amorts.put(month, df2.format(licenseFee - lineItemAmt*(maxMonths-1)));
			} else {
				amorts.put(month, df2.format(lineItemAmt));
			}
		}
		return amorts;
	}

	private static Map<Integer, String> amortStraightLine(double firstMonthPercent, Integer maxMonths, Double licenseFee,
			String windowsBasedOrTimeBased, int totalAmortPeriod) {
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.TIME_BASED)) {
			amorts.put(1, df2.format(licenseFee));
		} else {
			double lineItemAmt;
			int toBeUsedPeriod = 0;
			if(maxMonths != 0) {
				if(totalAmortPeriod < maxMonths) {
					toBeUsedPeriod = totalAmortPeriod;
				} else {
					toBeUsedPeriod = maxMonths;
				}
			} else {
				toBeUsedPeriod = totalAmortPeriod;
			}
			if(firstMonthPercent>0) {
				double firstMonthAmount = Double.valueOf(df.format((licenseFee*firstMonthPercent)/100));
				amorts.put(1, df2.format(firstMonthAmount));
				toBeUsedPeriod = toBeUsedPeriod - 1;
				if(toBeUsedPeriod > 0) {
					lineItemAmt = (licenseFee-firstMonthAmount)/toBeUsedPeriod;
					lineItemAmt = Double.valueOf(df.format(lineItemAmt));
					for(int month = 1; month <= toBeUsedPeriod; month++) {
						if(month == toBeUsedPeriod) {
							amorts.put(month+1, df2.format(licenseFee - firstMonthAmount - lineItemAmt*(toBeUsedPeriod-1)));
						} else {
							amorts.put(month+1, df2.format(lineItemAmt));
						}
					}
				}
			} else {
				lineItemAmt = licenseFee/toBeUsedPeriod;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= toBeUsedPeriod; month++) {
					if(month == toBeUsedPeriod) {
						amorts.put(month, df2.format(licenseFee - lineItemAmt*(toBeUsedPeriod-1)));
					} else {
						amorts.put(month, df2.format(lineItemAmt));
					}
				}
			}
		}
		return amorts;
	}
}

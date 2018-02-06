package nbcu.compass.amorttemplate.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AmortTemplateUtil {
	
	
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
		for(Window window:windows) {
			LocalDate localStartDate = LocalDate.parse(window.getStartDate(), formatter);
			LocalDate localEndDate = LocalDate.parse(window.getEndDate(), formatter);
			Period diff = Period.between(localStartDate, localEndDate);
			amortPeriods[index] = diff.getMonths();
			index++;
		}
		
		DecimalFormat df = new DecimalFormat("#.##");
		
		if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.STRAIGHT_LINE)) {
			return amortStraightLine(amortTemplateGrid.getFirstMonthAmortPercent(), amortTemplateGrid.getMaxMonths(), licenseFee, amortTemplateGrid.getTimePlayName(), amortPeriods[0], df);
		} else if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.MAX_STRAIGHT_LINE)) {
			return amortMaxStraightLine(amortTemplateGrid.getMaxMonths(), licenseFee, df);
		} else if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.QUARTILE)) {
			return amortQuartile(amortTemplateGrid.getFirstMonthAmortPercent(), amortTemplateGrid.getStraightLineMonths(), licenseFee, amortTemplateGrid.getIsMultipleWindowFlag(), sectionPercentage,
					amortTemplateGrid.getTimePlayName(), amortPeriods[0], amortPeriods, df);
		} else if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.MAX_QUARTILE)) {
			return amortMaxQuartile(amortTemplateGrid.getFirstMonthAmortPercent(), amortTemplateGrid.getStraightLineMonths(), licenseFee, amortTemplateGrid.getIsMultipleWindowFlag(), sectionPercentage,
					amortTemplateGrid.getTimePlayName(), amortPeriods[0], amortPeriods, df, amortTemplateGrid.getMaxMonths());
		}
		return null;
	}

	private static Map<Integer, String> amortMaxQuartile(double firstMonthPercent, Integer straightLineMonths, Double licenseFee,
			String multipleWindow, Double[] sectionPercentage, String windowsBasedOrTimeBased, int amortPeriod1,
			Integer[] amortPeriods, DecimalFormat df, Integer maxMonths) {
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		Double[] lineItemAmtSecs = new Double[sectionPercentage.length];
		int index = 0;
		for(Double sectionPer:sectionPercentage) {
			lineItemAmtSecs[index] = Double.valueOf(df.format((licenseFee*sectionPer)/100));
			index++;
		}
		index = 1; double grandSec = 0;
		for(Double lineItemAmtSec:lineItemAmtSecs) {
			double lineItemAmt;
			lineItemAmt = lineItemAmtSec*lineItemAmtSecs.length/maxMonths;
			lineItemAmt = Double.valueOf(df.format(lineItemAmt));
			for(int month = 1; month <= maxMonths/lineItemAmtSecs.length; month++) {
				if(index==lineItemAmtSecs.length && month == maxMonths/lineItemAmtSecs.length) {
					amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee-grandSec));
					System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee-grandSec));
				} else {
					grandSec += lineItemAmt;
					amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
				}
			}
			index++;
		}
		return amorts;
	}
	
	private static Map<Integer, String> amortQuartile(double firstMonthPercent, Integer straightLineMonths, Double licenseFee,
			String multipleWindow, Double[] sectionPercentage, String windowsBasedOrTimeBased, int amortPeriod1,
			Integer[] amortPeriods, DecimalFormat df) {
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.WINDOWS_BASED)) {
			if(multipleWindow.equalsIgnoreCase("Y")) {
				Double lineItemAmt[] = new Double[sectionPercentage.length];
				for(int in = 0; in < sectionPercentage.length; in++) {
					lineItemAmt[in] = ((licenseFee*sectionPercentage[in])/100)/amortPeriods[in];
					lineItemAmt[in] = Double.valueOf(df.format(lineItemAmt[in]));
					for(int month = 1; month <= amortPeriods[in]; month++) {
						if(month == amortPeriods[in]) {
							amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format((licenseFee*sectionPercentage[in])/100 - lineItemAmt[in]*(amortPeriods[in]-1)));
						} else {
							amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt[in]));
						}
					}
				}
			}
		} else if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.TIME_BASED)) {
			if(straightLineMonths > 0) {
				double lineItemAmt;
				lineItemAmt = licenseFee/amortPeriod1;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= amortPeriod1; month++) {
					if(month == amortPeriod1) {
						amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - lineItemAmt*(amortPeriod1-1)));
					} else {
						amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
					}
				}
			} else {
				double lineItemAmt;
				double firstMonthAmount = Double.valueOf(df.format((licenseFee*firstMonthPercent)/100));
				amorts.put(1, AmortTemplateConstants.DOLLARSIGN+firstMonthAmount);
				amortPeriod1 = amortPeriod1 - 1;
				lineItemAmt = (licenseFee-firstMonthAmount)/amortPeriod1;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= amortPeriod1; month++) {
					if(month == amortPeriod1) {
						amorts.put(month+1, AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - firstMonthAmount - lineItemAmt*(amortPeriod1-1)));
					} else {
						amorts.put(month+1, AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
					}
				}
			}
		} else if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.HALLMARK)) {
			if(straightLineMonths > 0) {
				amorts = amortMaxStraightLine(straightLineMonths, licenseFee, df);
			} else {
				double lineItemAmt;
				lineItemAmt = licenseFee/amortPeriod1;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= amortPeriod1; month++) {
					if(month == amortPeriod1) {
						amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - lineItemAmt*(amortPeriod1-1)));
					} else {
						amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
					}
				}
			}
		}
		return amorts;
	}

	private static Map<Integer, String> amortMaxStraightLine(Integer maxMonths, Double licenseFee, DecimalFormat df) {
		double lineItemAmt;
		lineItemAmt = licenseFee/maxMonths;
		lineItemAmt = Double.valueOf(df.format(lineItemAmt));
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		for(int month = 1; month <= maxMonths; month++) {
			if(month == maxMonths) {
				amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - lineItemAmt*(maxMonths-1)));
			} else {
				amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
			}
		}
		return amorts;
	}

	private static Map<Integer, String> amortStraightLine(double firstMonthPercent, Integer maxMonths, Double licenseFee,
			String windowsBasedOrTimeBased, int amortPeriod1, DecimalFormat df) {
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.TIME_BASED)) {
			amorts.put(1, AmortTemplateConstants.DOLLARSIGN+licenseFee);
		} else {
			double lineItemAmt;
			int toBeUsedPeriod = 0;
			if(maxMonths != 0) {
				if(amortPeriod1 < maxMonths) {
					toBeUsedPeriod = amortPeriod1;
				} else {
					toBeUsedPeriod = maxMonths;
				}
			} else {
				toBeUsedPeriod = amortPeriod1;
			}
			if(firstMonthPercent>0) {
				double firstMonthAmount = Double.valueOf(df.format((licenseFee*firstMonthPercent)/100));
				amorts.put(1, AmortTemplateConstants.DOLLARSIGN+firstMonthAmount);
				toBeUsedPeriod = toBeUsedPeriod - 1;
				lineItemAmt = (licenseFee-firstMonthAmount)/toBeUsedPeriod;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= toBeUsedPeriod; month++) {
					if(month == toBeUsedPeriod) {
						amorts.put(month+1, AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - firstMonthAmount - lineItemAmt*(toBeUsedPeriod-1)));
					} else {
						amorts.put(month+1, AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
					}
				}
			} else {
				lineItemAmt = licenseFee/toBeUsedPeriod;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= toBeUsedPeriod; month++) {
					if(month == toBeUsedPeriod) {
						amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - lineItemAmt*(toBeUsedPeriod-1)));
					} else {
						amorts.put(month, AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
					}
				}
			}
		}
		return amorts;
	}
}

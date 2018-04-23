package nbcu.compass.amorttemplate.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.toIntExact;

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
		int days = 0;
		for(Window window:windows) {
			LocalDate localStartDate = LocalDate.parse(window.getStartDate(), formatter);
			LocalDate localEndDate = LocalDate.parse(window.getEndDate(), formatter);
			int year = localEndDate.getYear() - localStartDate.getYear();
			amortPeriods[index] = localEndDate.getMonth().getValue() - localStartDate.getMonth().getValue() + 1 + (year*12);
			totalAmortMonths += amortPeriods[index];
			days += calculateDaysBetweenDates(localStartDate, localEndDate);
			index++;
		}
		
		int daysToBeUsed=0;
		if(amortTemplateGrid.getStraightLineMonths() == 12) {
			daysToBeUsed = 365;
		} else {
			days = 0;
		}
		
		if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.STRAIGHT_LINE)) {
			return amortStraightLine(amortTemplateGrid.getFirstMonthAmortPercent(), amortTemplateGrid.getMaxMonths(), licenseFee, amortTemplateGrid.getTimePlayName(), totalAmortMonths);
		} else if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.MAX_STRAIGHT_LINE)) {
			return amortMaxStraightLine(amortTemplateGrid.getMaxMonths(), licenseFee);
		} else if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.QUARTILE)) {
			return amortQuartile(amortTemplateGrid.getFirstMonthAmortPercent(), amortTemplateGrid.getStraightLineMonths(), licenseFee, amortTemplateGrid.getIsMultipleWindowFlag(), sectionPercentage,
					amortTemplateGrid.getTimePlayName(), totalAmortMonths, amortPeriods, amortTemplateGrid.getProjSchedFlag(), testData.getNetwork(), days, daysToBeUsed);
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
			lineItemAmt = convertToBigDecimal(lineItemAmt);
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
		return amorts;
	}
	
	private static Map<Integer, String> amortQuartile(double firstMonthPercent, Integer straightLineMonths, Double licenseFee,
			String multipleWindow, Double[] sectionPercentage, String windowsBasedOrTimeBased, int totalAmortPeriod,
			Integer[] amortPeriods, String projSchedFlag, String network, int days, int daysToBeUsed) {
		Map<Integer, String> amorts = new LinkedHashMap<Integer, String>();
		if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.WINDOWS_BASED)) {
			Double lineItemAmt[] = new Double[sectionPercentage.length];
			int index  = 1;
			double sumOfAmorts = 0.0;
			for(int in = 0; in < sectionPercentage.length; in++) {
				lineItemAmt[in] = ((licenseFee*sectionPercentage[in])/100)/amortPeriods[in];
				lineItemAmt[in] = convertToBigDecimal(lineItemAmt[in]);
				lineItemAmt[in] = Double.valueOf(df.format(lineItemAmt[in]));
				for(int month = 1; month <= amortPeriods[in]; month++) {
					if(in == (sectionPercentage.length -1) && month == amortPeriods[in]) {
						amorts.put(index, df2.format(licenseFee-sumOfAmorts));
					} else {
						sumOfAmorts += lineItemAmt[in];
						amorts.put(index, df2.format(lineItemAmt[in]));
					}
					index++;
				}
			}
		} else if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.TIME_BASED)) {
			if(straightLineMonths > 0) {
				if(totalAmortPeriod  <= straightLineMonths) {
					double lineItemAmt;
					lineItemAmt = licenseFee/totalAmortPeriod;
					lineItemAmt = convertToBigDecimal(lineItemAmt);
					lineItemAmt = Double.valueOf(df.format(lineItemAmt));
					for(int month = 1; month <= totalAmortPeriod; month++) {
						if(month == totalAmortPeriod) {
							amorts.put(month, df2.format(licenseFee - lineItemAmt*(totalAmortPeriod-1)));
						} else {
							amorts.put(month, df2.format(lineItemAmt));
						}
					}
				} else {
					Double lineItemAmt[] = new Double[sectionPercentage.length];
					int index  = 1;
					double sumOfAmorts = 0.0;
					int cnt = totalAmortPeriod/sectionPercentage.length;
					for(int in = 0; in < sectionPercentage.length; in++) {
						lineItemAmt[in] = ((licenseFee*sectionPercentage[in])/100)/cnt;
						lineItemAmt[in] = convertToBigDecimal(lineItemAmt[in]);
						lineItemAmt[in] = Double.valueOf(df.format(lineItemAmt[in]));
						for(int month = 1; month <= cnt; month++) {
							if(in == (sectionPercentage.length -1) && month == cnt) {
								amorts.put(index, df2.format(licenseFee-sumOfAmorts));
							} else {
								sumOfAmorts += lineItemAmt[in];
								amorts.put(index, df2.format(lineItemAmt[in]));
							}
							index++;
						}
					}
				}
			} else {
				double lineItemAmt;
				double firstMonthAmount = Double.valueOf(df.format(convertToBigDecimal((licenseFee*firstMonthPercent)/100)));
				amorts.put(1, df2.format(firstMonthAmount));
				totalAmortPeriod = totalAmortPeriod - 1;
				lineItemAmt = (licenseFee-firstMonthAmount)/totalAmortPeriod;
				Double roundOff = convertToBigDecimal(lineItemAmt);
				lineItemAmt = Double.valueOf(df.format(roundOff));
				for(int month = 1; month <= totalAmortPeriod; month++) {
					if(month == totalAmortPeriod) {
						amorts.put(month+1, df2.format(licenseFee - firstMonthAmount - lineItemAmt*(totalAmortPeriod-1)));
					} else {
						amorts.put(month+1, df2.format(lineItemAmt));
					}
				}
			}
		} else if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.HALLMARK)) {
			if(straightLineMonths <= totalAmortPeriod && days > daysToBeUsed) {
				int quartCnt = totalAmortPeriod / sectionPercentage.length;
				Double lineItemAmt[] = new Double[sectionPercentage.length*quartCnt];
				int index  = 1;
				double sumOfAmorts = 0.0;
				for(int in = 0; in < sectionPercentage.length; in++) {
					lineItemAmt[in] = ((licenseFee*sectionPercentage[in])/100)/quartCnt;
					lineItemAmt[in] = convertToBigDecimal(lineItemAmt[in]);
					lineItemAmt[in] = Double.valueOf(df.format(lineItemAmt[in]));
					for(int month = 1; month <= quartCnt; month++) {
						if(in == (sectionPercentage.length -1) && month == quartCnt) {
							amorts.put(index, df2.format(licenseFee-sumOfAmorts));
						} else {
							sumOfAmorts += lineItemAmt[in];
							amorts.put(index, df2.format(lineItemAmt[in]));
						}
						index++;
					}
				}
			} else {
				int grandMonths = 0;
				if(days > 0 && days == daysToBeUsed) {
					grandMonths = 12;
				} else {
					for(Integer amortPeriod:amortPeriods ) {
						grandMonths += amortPeriod;
					}
				}
				double lineItemAmt = licenseFee/grandMonths;
				lineItemAmt = convertToBigDecimal(lineItemAmt);
				for(int month=1; month<=grandMonths; month++) {
					if(month == grandMonths) {
						amorts.put(month, df2.format(licenseFee-(lineItemAmt*(grandMonths-1))));
					} else {
						amorts.put(month, df2.format(lineItemAmt));
					}
				}
			}
			/*
			if(multipleWindow.equalsIgnoreCase("Y")) {
				int grandMonths = 0;
				for(Integer amortPeriod:amortPeriods ) {
					grandMonths += amortPeriod;
				}
				double lineItemAmt = licenseFee/grandMonths;
				lineItemAmt = convertToBigDecimal(lineItemAmt);
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
				lineItemAmt = convertToBigDecimal(lineItemAmt);
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= totalAmortPeriod; month++) {
					if(month == totalAmortPeriod) {
						amorts.put(month, df2.format(licenseFee - lineItemAmt*(totalAmortPeriod-1)));
					} else {
						amorts.put(month, df2.format(lineItemAmt));
					}
				}
			}*/
		}
		return amorts;
	}

	private static Map<Integer, String> amortMaxStraightLine(Integer maxMonths, Double licenseFee) {
		double lineItemAmt;
		lineItemAmt = licenseFee/maxMonths;
		lineItemAmt = convertToBigDecimal(lineItemAmt);
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
				double firstMonthAmount = Double.valueOf(df.format(convertToBigDecimal((licenseFee*firstMonthPercent)/100)));
				double sumOfAmorts = firstMonthAmount;
				amorts.put(1, df2.format(firstMonthAmount));
				toBeUsedPeriod = toBeUsedPeriod - 1;
				if(toBeUsedPeriod > 0) {
					lineItemAmt = (licenseFee-firstMonthAmount)/toBeUsedPeriod;
					Double roundOff = convertToBigDecimal(lineItemAmt);
					lineItemAmt = Double.valueOf(df.format(roundOff));
					for(int month = 1; month <= toBeUsedPeriod; month++) {
						if(month == toBeUsedPeriod) {
							amorts.put(month+1, df2.format(licenseFee - sumOfAmorts));
						} else {
							sumOfAmorts += lineItemAmt;
							amorts.put(month+1, df2.format(lineItemAmt));
						}
					}
				}
			} else {
				lineItemAmt = licenseFee/toBeUsedPeriod;
				lineItemAmt = convertToBigDecimal(lineItemAmt);
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
	
	private static int calculateDaysBetweenDates(LocalDate localStartDate, LocalDate localEndDate) {
		Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, localStartDate.getDayOfMonth());
        cal.set(Calendar.MONTH, localStartDate.getMonthValue());
        cal.set(Calendar.YEAR, localStartDate.getYear());
        Date firstDate = cal.getTime();

        cal.set(Calendar.DAY_OF_MONTH, localEndDate.getDayOfMonth());
        cal.set(Calendar.MONTH, localEndDate.getMonthValue());
        cal.set(Calendar.YEAR, localEndDate.getYear());
        Date secondDate = cal.getTime();


        long diff = secondDate.getTime() - firstDate.getTime();

        return toIntExact(diff / 1000 / 60 / 60 / 24)+1;
	}
	
	private static Double convertToBigDecimal(Double lineItemAmt) {
		lineItemAmt= Double.valueOf(df.format(lineItemAmt));
		BigDecimal bigDecLineItem = new BigDecimal(lineItemAmt);
		BigDecimal roundOff = bigDecLineItem.setScale(2, BigDecimal.ROUND_HALF_DOWN);
		return roundOff.doubleValue();
	}
}

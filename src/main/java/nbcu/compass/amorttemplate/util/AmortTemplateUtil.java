package nbcu.compass.amorttemplate.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Set;

public class AmortTemplateUtil {
	
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		AmortExcelReader amortExcelReader = new AmortExcelReader();
		Map<String, AmortTemplateGrid> amortTemplateGridMap = amortExcelReader.readAmortTemplateGrid("US");
		Set<String> uniqueNames = amortTemplateGridMap.keySet();
		for(String uniqueName:uniqueNames) {
			System.out.println("### " + uniqueName + " ###");
			AmortTemplateGrid amortTemplateGrid = amortTemplateGridMap.get(uniqueName);
			
			Double licenseFee = 4000.00;
			int sectionPercentage[] = {80, 20};
			
			LocalDate startDate = LocalDate.of(2017, 12, 01);
			LocalDate endDate = LocalDate.of(2018, 07, 01);
			Period diff = Period.between(startDate, endDate);
			int amortPeriod1 = diff.getMonths();
			
			startDate = LocalDate.of(2018, 07, 01);
			endDate = LocalDate.of(2018, 8, 31);
			diff = Period.between(startDate, endDate);
			int amortPeriod2 = diff.getMonths();
			
			Integer amortPeriods[] = new Integer[sectionPercentage.length];
			amortPeriods[0] = amortPeriod1;
			amortPeriods[1] = amortPeriod2;
			
			DecimalFormat df = new DecimalFormat("#.##");
			
			if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.STRAIGHT_LINE)) {
				amortStraightLine(amortTemplateGrid.getFirstMonthAmortPercent(), amortTemplateGrid.getMaxMonths(), licenseFee, amortTemplateGrid.getTimePlayName(), amortPeriod1, df);
			} else if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.MAX_STRAIGHT_LINE)) {
				amortMaxStraightLine(amortTemplateGrid.getMaxMonths(), licenseFee, df);
			} else if(null != amortTemplateGrid.getStraightLineName() && amortTemplateGrid.getStraightLineName().equalsIgnoreCase(AmortTemplateConstants.QUARTILE)) {
				amortQuartile(amortTemplateGrid.getFirstMonthAmortPercent(), amortTemplateGrid.getStraightLineMonths(), licenseFee, amortTemplateGrid.getIsMultipleWindowFlag(), sectionPercentage,
						amortTemplateGrid.getTimePlayName(), amortPeriod1, amortPeriods, df);
			}
		}
	}

	private static void amortQuartile(double firstMonthPercent, Integer straightLineMonths, Double licenseFee,
			String multipleWindow, int[] sectionPercentage, String windowsBasedOrTimeBased, int amortPeriod1,
			Integer[] amortPeriods, DecimalFormat df) {
		if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.WINDOWS_BASED)) {
			if(multipleWindow.equalsIgnoreCase("Y")) {
				Double lineItemAmt[] = new Double[sectionPercentage.length];
				for(int in = 0; in < sectionPercentage.length; in++) {
					lineItemAmt[in] = ((licenseFee*sectionPercentage[in])/100)/amortPeriods[in];
					lineItemAmt[in] = Double.valueOf(df.format(lineItemAmt[in]));
					for(int month = 1; month <= amortPeriods[in]; month++) {
						if(month == amortPeriods[in]) {
							System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format((licenseFee*sectionPercentage[in])/100 - lineItemAmt[in]*(amortPeriods[in]-1)));
						} else {
							System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt[in]));
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
						System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - lineItemAmt*(amortPeriod1-1)));
					} else {
						System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
					}
				}
			} else {
				double lineItemAmt;
				double firstMonthAmount = Double.valueOf(df.format((licenseFee*firstMonthPercent)/100));
				System.out.println(AmortTemplateConstants.DOLLARSIGN+firstMonthAmount);
				amortPeriod1 = amortPeriod1 - 1;
				lineItemAmt = (licenseFee-firstMonthAmount)/amortPeriod1;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= amortPeriod1; month++) {
					if(month == amortPeriod1) {
						System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - firstMonthAmount - lineItemAmt*(amortPeriod1-1)));
					} else {
						System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
					}
				}
			}
		} else if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.HALLMARK)) {
			if(straightLineMonths > 0) {
				amortMaxStraightLine(straightLineMonths, licenseFee, df);
			} else {
				double lineItemAmt;
				lineItemAmt = licenseFee/amortPeriod1;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= amortPeriod1; month++) {
					if(month == amortPeriod1) {
						System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - lineItemAmt*(amortPeriod1-1)));
					} else {
						System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
					}
				}
			}
		}
	}

	private static void amortMaxStraightLine(Integer maxMonths, Double licenseFee, DecimalFormat df) {
		double lineItemAmt;
		lineItemAmt = licenseFee/maxMonths;
		lineItemAmt = Double.valueOf(df.format(lineItemAmt));
		for(int month = 1; month <= maxMonths; month++) {
			if(month == maxMonths) {
				System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - lineItemAmt*(maxMonths-1)));
			} else {
				System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
			}
		}
	}

	private static void amortStraightLine(double firstMonthPercent, Integer maxMonths, Double licenseFee,
			String windowsBasedOrTimeBased, int amortPeriod1, DecimalFormat df) {
		if(windowsBasedOrTimeBased.equalsIgnoreCase(AmortTemplateConstants.TIME_BASED)) {
			System.out.println(AmortTemplateConstants.DOLLARSIGN+licenseFee);
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
				System.out.println(AmortTemplateConstants.DOLLARSIGN+firstMonthAmount);
				toBeUsedPeriod = toBeUsedPeriod - 1;
				lineItemAmt = (licenseFee-firstMonthAmount)/toBeUsedPeriod;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= toBeUsedPeriod; month++) {
					if(month == toBeUsedPeriod) {
						System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - firstMonthAmount - lineItemAmt*(toBeUsedPeriod-1)));
					} else {
						System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
					}
				}
			} else {
				lineItemAmt = licenseFee/toBeUsedPeriod;
				lineItemAmt = Double.valueOf(df.format(lineItemAmt));
				for(int month = 1; month <= toBeUsedPeriod; month++) {
					if(month == toBeUsedPeriod) {
						System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(licenseFee - lineItemAmt*(toBeUsedPeriod-1)));
					} else {
						System.out.println(AmortTemplateConstants.DOLLARSIGN+df.format(lineItemAmt));
					}
				}
			}
		}
	}
}

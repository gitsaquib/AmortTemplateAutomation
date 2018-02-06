package nbcu.compass.amorttemplate.util;

import java.util.Map;

public class AmortTemplateGrid {
	
	private int amortTemplateNo;
	private String amortTemplateName;
	private String titleTypeName;
	private String financeTypeName;
	private String straightLineName;
	private double straightLineMonths;
	private String timePlayName;
	private double maxMonths;
	private double firstMonthAmortPercent;
	private String isMultipleWindowFlag;
	private Map<Integer, Double> amortSectionGrids; 
	
	public Map<Integer, Double> getAmortSectionGrids() {
		return amortSectionGrids;
	}
	public void setAmortSectionGrids(Map<Integer, Double> amortSectionGrids) {
		this.amortSectionGrids = amortSectionGrids;
	}
	public String getAmortTemplateName() {
		return amortTemplateName;
	}
	public void setAmortTemplateName(String amortTemplateName) {
		this.amortTemplateName = amortTemplateName;
	}
	public String getTitleTypeName() {
		return titleTypeName;
	}
	public void setTitleTypeName(String titleTypeName) {
		this.titleTypeName = titleTypeName;
	}
	public String getFinanceTypeName() {
		return financeTypeName;
	}
	public void setFinanceTypeName(String financeTypeName) {
		this.financeTypeName = financeTypeName;
	}
	public String getStraightLineName() {
		return straightLineName;
	}
	public void setStraightLineName(String straightLineName) {
		this.straightLineName = straightLineName;
	}
	public int getStraightLineMonths() {
		Double straightLineMonthsInt = new Double(straightLineMonths);
		return straightLineMonthsInt.intValue();
	}
	public void setStraightLineMonths(double straightLineMonths) {
		this.straightLineMonths = straightLineMonths;
	}
	public String getTimePlayName() {
		return timePlayName;
	}
	public void setTimePlayName(String timePlayName) {
		this.timePlayName = timePlayName;
	}
	public Integer getMaxMonths() {
		Double maxMonthsInt = new Double(maxMonths);
		return maxMonthsInt.intValue();
	}
	public void setMaxMonths(double maxMonths) {
		this.maxMonths = maxMonths;
	}
	public double getFirstMonthAmortPercent() {
		return firstMonthAmortPercent;
	}
	public void setFirstMonthAmortPercent(double firstMonthAmortPercent) {
		this.firstMonthAmortPercent = firstMonthAmortPercent;
	}
	public String getIsMultipleWindowFlag() {
		return isMultipleWindowFlag;
	}
	public void setIsMultipleWindowFlag(String isMultipleWindowFlag) {
		this.isMultipleWindowFlag = isMultipleWindowFlag;
	}
	public int getAmortTemplateNo() {
		return amortTemplateNo;
	}
	public void setAmortTemplateNo(int amortTemplateNo) {
		this.amortTemplateNo = amortTemplateNo;
	}
}

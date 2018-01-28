package nbcu.compass.amorttemplate.util;

public class AmortTemplateGrid {
	
	private String uniqueName;
	private String amortTemplateName;
	private String titleTypeName;
	private String financeTypeName;
	private String straightLineName;
	private double straightLineMonths;
	private String timePlayName;
	private double maxMonths;
	private double firstMonthAmortPercent;
	private String projectionScheduleName;
	private String isMultipleWindowFlag;
	
	public String getUniqueName() {
		return uniqueName;
	}
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
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
	public String getProjectionScheduleName() {
		return projectionScheduleName;
	}
	public void setProjectionScheduleName(String projectionScheduleName) {
		this.projectionScheduleName = projectionScheduleName;
	}
	public String getIsMultipleWindowFlag() {
		return isMultipleWindowFlag;
	}
	public void setIsMultipleWindowFlag(String isMultipleWindowFlag) {
		this.isMultipleWindowFlag = isMultipleWindowFlag;
	}
}

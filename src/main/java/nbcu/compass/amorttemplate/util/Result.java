package nbcu.compass.amorttemplate.util;

public class Result {
	
	private String network;
	private int amortTemplateNo;
	private String amortTemplateName;
	private String titleTypeName;
	private String financeTypeName;
	private String addEpisode;
	private String straightLineName;
	private double straightLineMonths;
	private String timePlayName;
	private double maxMonths;
	private double firstMonthAmortPercent;
	private String isMultipleWindowFlag;
	private String projSchedFlag;

	public String getStraightLineName() {
		return straightLineName;
	}
	public void setStraightLineName(String straightLineName) {
		this.straightLineName = straightLineName;
	}
	public double getStraightLineMonths() {
		return straightLineMonths;
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
	public double getMaxMonths() {
		return maxMonths;
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
	public String getProjSchedFlag() {
		return projSchedFlag;
	}
	public void setProjSchedFlag(String projSchedFlag) {
		this.projSchedFlag = projSchedFlag;
	}
	private String status;
	private String remarks;

	public int getAmortTemplateNo() {
		return amortTemplateNo;
	}
	public void setAmortTemplateNo(int amortTemplateNo) {
		this.amortTemplateNo = amortTemplateNo;
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
	public String getAddEpisode() {
		return addEpisode;
	}
	public void setAddEpisode(String addEpisode) {
		this.addEpisode = addEpisode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getNetwork() {
		return network;
	}
	public void setNetwork(String network) {
		this.network = network;
	}
}

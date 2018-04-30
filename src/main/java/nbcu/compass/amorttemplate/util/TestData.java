package nbcu.compass.amorttemplate.util;

import java.util.List;

public class TestData {

	private String tcNo;
	private String distributor;
	private String dealType;
	private String negotiatedBy;
	private String network;
	private int run;
	
	private List<Window> windows;

	public String getTcNo() {
		return tcNo;
	}
	public void setTcNo(String tcNo) {
		this.tcNo = tcNo;
	}
	public String getDistributor() {
		return distributor;
	}
	public void setDistributor(String distributor) {
		this.distributor = distributor;
	}
	public String getDealType() {
		return dealType;
	}
	public void setDealType(String dealType) {
		this.dealType = dealType;
	}
	public String getNegotiatedBy() {
		return negotiatedBy;
	}
	public void setNegotiatedBy(String negotiatedBy) {
		this.negotiatedBy = negotiatedBy;
	}
	public List<Window> getWindows() {
		return windows;
	}
	public void setWindows(List<Window> windows) {
		this.windows = windows;
	}
	public String getNetwork() {
		return network;
	}
	public void setNetwork(String network) {
		this.network = network;
	}
	public int getRun() {
		return run;
	}
	public void setRun(int run) {
		this.run = run;
	}
	
}

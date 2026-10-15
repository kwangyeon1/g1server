package org.yht.exps.express.model.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"returnData", "blTracking"})
public class HblTrackingResult{

	private InterfaceReturnData returnData;
	private List<HblReturnTracking> blTracking;
	
	@XmlElement(name = "Return")
	public InterfaceReturnData getReturnData() {
		return returnData;
	}
	public void setReturnData(InterfaceReturnData returnData) {
		this.returnData = returnData;
	}
	
	@XmlElement(name = "BlTracking")
	public List<HblReturnTracking> getBlTracking() {
		return blTracking;
	}
	public void setBlTracking(List<HblReturnTracking> blTracking) {
		this.blTracking = blTracking;
	}
}
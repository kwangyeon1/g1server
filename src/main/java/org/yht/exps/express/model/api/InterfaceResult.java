package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "root")
public class InterfaceResult{

	private InterfaceReturnData returnData;
	
	@XmlElement(name = "Return")
	public InterfaceReturnData getReturnData() {
		return returnData;
	}
	public void setReturnData(InterfaceReturnData returnData) {
		this.returnData = returnData;
	}
}
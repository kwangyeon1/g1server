package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "root")
public class HanjinConvertResult{

	private HanjinReturnConvertData hanjinReturnConvertData;
	
	@XmlElement(name = "Return")
	public HanjinReturnConvertData getHanjinReturnConvertData() {
		return hanjinReturnConvertData;
	}
	public void setReturnData(HanjinReturnConvertData hanjinReturnConvertData) {
		this.hanjinReturnConvertData = hanjinReturnConvertData;
	}
}

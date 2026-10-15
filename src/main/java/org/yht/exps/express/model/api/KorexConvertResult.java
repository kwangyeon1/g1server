package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "root")
public class KorexConvertResult{

	private KorexConvertReturnData returnKorexConvertData;
	
	@XmlElement(name = "Return")
	public KorexConvertReturnData getReturnKorexConvertData() {
		return returnKorexConvertData;
	}
	public void setReturnData(KorexConvertReturnData returnKorexConvertData) {
		this.returnKorexConvertData = returnKorexConvertData;
	}
}

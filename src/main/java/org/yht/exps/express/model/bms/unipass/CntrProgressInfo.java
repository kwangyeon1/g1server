package org.yht.exps.express.model.bms.unipass;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement(name = "cntrQryBrkdQryRtnVo")
@XmlAccessorType(XmlAccessType.FIELD)
public class CntrProgressInfo {

    @XmlElement(name = "cntrQryBrkdQryVo")
    private List<CntrQryInfo> cntrQryInfoList;

    @Data
    @XmlRootElement(name = "cntrQryBrkdQryVo")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CntrQryInfo {
        // 컨테이너번호
        @XmlElement(name = "cntrNo")
        private String cntrNo;

        // 봉인번호1
        @XmlElement(name = "cntrSelgNo1")
        private String cntrSelgNo1;

        // 봉인번호2
        @XmlElement(name = "cntrSelgNo2")
        private String cntrSelgNo2;

        // 봉인번호3
        @XmlElement(name = "cntrSelgNo3")
        private String cntrSelgNo3;

        // 컨테이너사이즈
        @XmlElement(name = "cntrStszCd")
        private String cntrStszCd;


    }
}

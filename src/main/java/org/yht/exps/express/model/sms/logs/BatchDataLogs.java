package org.yht.exps.express.model.sms.logs;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BatchDataLogs", description = "배치 처리 로그")
@Alias("batchDataLogs")
@EqualsAndHashCode(callSuper=false)
public class BatchDataLogs extends ResultList{
	int totalCount;
	String hblNo;
	String mblNo;
	String bound;
	String boundType;
	String cdType;
	String destCode;
	String destPort;
	String arDate;
	String bondedCargoYn;
	String dropYn;
	String status;
	String action;
	String odJobNo;
	String masterOdJobNo;
	String remarks;
	String ediSeqNo;
	String entCode;
	String typeCode;
	String recvStatus;
	String recvDateTime;
}

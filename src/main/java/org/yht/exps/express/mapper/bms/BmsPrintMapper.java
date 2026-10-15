package org.yht.exps.express.mapper.bms;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsPrint.BmsPrint;
import org.yht.exps.express.model.bms.bmsPrint.search.SearchBmsPrintList;
import org.yht.exps.express.model.bms.out.BmsOutSample;

@Mapper
public interface BmsPrintMapper {

	/** 반입신고 출력용 추가 데이터 리스트 */
	List<BmsPrint> selectBmsInPrintList(SearchBmsPrintList search);

	/** 반출신고 출력용 추가 데이터 리스트 */
	List<BmsPrint> selectBmsOutPrintList(SearchBmsPrintList search);
	
	/** 반출신고 출력용 추가 데이터 리스트 */
	List<BmsOutSample> selectBmsSamplePrintList(SearchBmsPrintList search);

	/** 검사계획통보 출력용 추가 데이터 리스트 */
	List<BmsPrint> selectBmsInspectPrintList(SearchBmsPrintList search);

	/** 반출통고신고 출력용 추가 데이터 리스트 */
	List<BmsPrint> selectBmsOutNoticePrintList(SearchBmsPrintList search);

	/** 반입물품이상신고 출력용 추가 데이터 리스트 */
	List<BmsPrint> selectBmsAbnormalPrintList(SearchBmsPrintList search);

	String selectBmsAbnormalPrintcontactNo(Map<String, Object> search);

}

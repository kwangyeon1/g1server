package org.yht.exps.express;

import java.util.Hashtable;

/**
 * 현재 실행중인 사업장 thread
 * @author kyle
 *
 */
public class BmsMembers {
	// 스케쥴별로 생성할 것


	static private Hashtable<String, Object> sendXmlGOVCBR632ParserUsers_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getSendXmlGOVCBR632ParserUser(String user) {
		return BmsMembers.sendXmlGOVCBR632ParserUsers_.get(user);
	}
	public void setSendXmlGOVCBR632ParserUser(String user) {
		BmsMembers.sendXmlGOVCBR632ParserUsers_.put(user, true);
	}
	public void removeSendXmlGOVCBR632ParserUser(String user) {
		BmsMembers.sendXmlGOVCBR632ParserUsers_.remove(user);
	}
	public Object getSendXmlGOVCBR632ParserUserCnt(String user) {
		return BmsMembers.sendXmlGOVCBR632ParserUsers_.get(user + "_CNT");
	}
	public void setSendXmlGOVCBR632ParserUserCnt(String user, int cnt) {
		BmsMembers.sendXmlGOVCBR632ParserUsers_.put(user + "_CNT", cnt);
	}

	/***
	 *   GOVCBR5IJ(보세구역반입예정정보) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBR5IJParser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBR5IJParserUser(String user) {
		return BmsMembers.recvXmlGOVCBR5IJParser_.get(user);
	}
	public void setRecvXmlGOVCBR5IJParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5IJParser_.put(user, true);
	}
	public void removeRecvXmlGOVCBR5IJParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5IJParser_.remove(user);
	}
	public Object getRecvXmlGOVCBR5IJParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBR5IJParser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBR5IJParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBR5IJParser_.put(user + "_CNT", cnt);
	}

	/** GOVCBR6NB(반출신고서) 송신용 Parser 정의 */
	static private Hashtable<String, Object> sendXmlGOVCBR6NBParserUsers_ = new Hashtable<String, Object>();

	public Object getSendXmlGOVCBR6NBParserUser(String user) {
		return BmsMembers.sendXmlGOVCBR6NBParserUsers_.get(user);
	}
	public void setSendXmlGOVCBR6NBParserUser(String user) {
		BmsMembers.sendXmlGOVCBR6NBParserUsers_.put(user, true);
	}
	public void removeSendXmlGOVCBR6NBParserUser(String user) {
		BmsMembers.sendXmlGOVCBR6NBParserUsers_.remove(user);
	}
	public Object getSendXmlGOVCBR6NBParserUserCnt(String user) {
		return BmsMembers.sendXmlGOVCBR6NBParserUsers_.get(user + "_CNT");
	}
	public void setSendXmlGOVCBR6NBParserUserCnt(String user, int cnt) {
		BmsMembers.sendXmlGOVCBR6NBParserUsers_.put(user + "_CNT", cnt);
	}

	/** GOVCBR5II(반출통고 목록보고서) 송신용 Parser 정의 */
	static private Hashtable<String, Object> sendXmlGOVCBR5IIParserUsers_ = new Hashtable<String, Object>();

	public Object getSendXmlGOVCBR5IIParserUser(String user) {
		return BmsMembers.sendXmlGOVCBR5IIParserUsers_.get(user);
	}
	public void setSendXmlGOVCBR5IIParserUser(String user) {
		BmsMembers.sendXmlGOVCBR5IIParserUsers_.put(user, true);
	}
	public void removeSendXmlGOVCBR5IIParserUser(String user) {
		BmsMembers.sendXmlGOVCBR5IIParserUsers_.remove(user);
	}
	public Object getSendXmlGOVCBR5IIParserUserCnt(String user) {
		return BmsMembers.sendXmlGOVCBR5IIParserUsers_.get(user + "_CNT");
	}
	public void setSendXmlGOVCBR5IIParserUserCnt(String user, int cnt) {
		BmsMembers.sendXmlGOVCBR5IIParserUsers_.put(user + "_CNT", cnt);
	}

	/** GOVCBR5LC(반입신고정정신청서) 송신용 Parser 정의 */
	static private Hashtable<String, Object> sendXmlGOVCBR5LCParserUsers_ = new Hashtable<String, Object>();

	public Object getSendXmlGOVCBR5LCParserUser(String user) {
		return BmsMembers.sendXmlGOVCBR5LCParserUsers_.get(user);
	}
	public void setSendXmlGOVCBR5LCParserUser(String user) {
		BmsMembers.sendXmlGOVCBR5LCParserUsers_.put(user, true);
	}
	public void removeSendXmlGOVCBR5LCParserUser(String user) {
		BmsMembers.sendXmlGOVCBR5LCParserUsers_.remove(user);
	}
	public Object getSendXmlGOVCBR5LCParserUserCnt(String user) {
		return BmsMembers.sendXmlGOVCBR5LCParserUsers_.get(user + "_CNT");
	}
	public void setSendXmlGOVCBR5LCParserUserCnt(String user, int cnt) {
		BmsMembers.sendXmlGOVCBR5LCParserUsers_.put(user + "_CNT", cnt);
	}

	/** GOVCBR5LD(반출신고정정신청서) 송신용 Parser 정의 */
	static private Hashtable<String, Object> sendXmlGOVCBR5LDParserUsers_ = new Hashtable<String, Object>();

	public Object getSendXmlGOVCBR5LDParserUser(String user) {
		return BmsMembers.sendXmlGOVCBR5LDParserUsers_.get(user);
	}
	public void setSendXmlGOVCBR5LDParserUser(String user) {
		BmsMembers.sendXmlGOVCBR5LDParserUsers_.put(user, true);
	}
	public void removeSendXmlGOVCBR5LDParserUser(String user) {
		BmsMembers.sendXmlGOVCBR5LDParserUsers_.remove(user);
	}
	public Object getSendXmlGOVCBR5LDParserUserCnt(String user) {
		return BmsMembers.sendXmlGOVCBR5LDParserUsers_.get(user + "_CNT");
	}
	public void setSendXmlGOVCBR5LDParserUserCnt(String user, int cnt) {
		BmsMembers.sendXmlGOVCBR5LDParserUsers_.put(user + "_CNT", cnt);
	}

	/** GOVCBRDKO(특송화물 반입물품 이상보고서) 송신용 Parser 정의 */
	static private Hashtable<String, Object> sendXmlGOVCBRDKOParserUsers_ = new Hashtable<String, Object>();

	public Object getSendXmlGOVCBRDKOParserUser(String user) {
		return BmsMembers.sendXmlGOVCBRDKOParserUsers_.get(user);
	}
	public void setSendXmlGOVCBRDKOParserUser(String user) {
		BmsMembers.sendXmlGOVCBRDKOParserUsers_.put(user, true);
	}
	public void removeSendXmlGOVCBRDKOParserUser(String user) {
		BmsMembers.sendXmlGOVCBRDKOParserUsers_.remove(user);
	}
	public Object getSendXmlGOVCBRDKOParserUserCnt(String user) {
		return BmsMembers.sendXmlGOVCBRDKOParserUsers_.get(user + "_CNT");
	}
	public void setSendXmlGOVCBRDKOParserUserCnt(String user, int cnt) {
		BmsMembers.sendXmlGOVCBRDKOParserUsers_.put(user + "_CNT", cnt);
	}

	/** GOVCBRDKP(특송화물 반입물품 이상보고 정정신청서) 송신용 Parser 정의 */
	static private Hashtable<String, Object> sendXmlGOVCBRDKPParserUsers_ = new Hashtable<String, Object>();

	public Object getSendXmlGOVCBRDKPParserUser(String user) {
		return BmsMembers.sendXmlGOVCBRDKPParserUsers_.get(user);
	}
	public void setSendXmlGOVCBRDKPParserUser(String user) {
		BmsMembers.sendXmlGOVCBRDKPParserUsers_.put(user, true);
	}
	public void removeSendXmlGOVCBRDKPParserUser(String user) {
		BmsMembers.sendXmlGOVCBRDKPParserUsers_.remove(user);
	}
	public Object getSendXmlGOVCBRDKPParserUserCnt(String user) {
		return BmsMembers.sendXmlGOVCBRDKPParserUsers_.get(user + "_CNT");
	}
	public void setSendXmlGOVCBRDKPParserUserCnt(String user, int cnt) {
		BmsMembers.sendXmlGOVCBRDKPParserUsers_.put(user + "_CNT", cnt);
	}

	/** GOVCBR5TL(특송물품 XRAY선별속성변경신청서) 송신용 Parser 정의 */
	static private Hashtable<String, Object> sendXmlGOVCBR5TLParserUsers_ = new Hashtable<String, Object>();

	public Object getSendXmlGOVCBR5TLParserUser(String user) {
		return BmsMembers.sendXmlGOVCBR5TLParserUsers_.get(user);
	}
	public void setSendXmlGOVCBR5TLParserUser(String user) {
		BmsMembers.sendXmlGOVCBR5TLParserUsers_.put(user, true);
	}
	public void removeSendXmlGOVCBR5TLParserUser(String user) {
		BmsMembers.sendXmlGOVCBR5TLParserUsers_.remove(user);
	}
	public Object getSendXmlGOVCBR5TLParserUserCnt(String user) {
		return BmsMembers.sendXmlGOVCBR5TLParserUsers_.get(user + "_CNT");
	}
	public void setSendXmlGOVCBR5TLParserUserCnt(String user, int cnt) {
		BmsMembers.sendXmlGOVCBR5TLParserUsers_.put(user + "_CNT", cnt);
	}
	
	/** GOVCBR5IG(하기결과이상보고) 송신용 Parser 정의 */
	static private Hashtable<String, Object> sendXmlGOVCBR5IGParserUsers_ = new Hashtable<String, Object>();

	public Object getSendXmlGOVCBR5IGParserUser(String user) {
		return BmsMembers.sendXmlGOVCBR5IGParserUsers_.get(user);
	}
	public void setSendXmlGOVCBR5IGParserUser(String user) {
		BmsMembers.sendXmlGOVCBR5IGParserUsers_.put(user, true);
	}
	public void removeSendXmlGOVCBR5IGParserUser(String user) {
		BmsMembers.sendXmlGOVCBR5IGParserUsers_.remove(user);
	}
	public Object getSendXmlGOVCBR5IGParserUserCnt(String user) {
		return BmsMembers.sendXmlGOVCBR5IGParserUsers_.get(user + "_CNT");
	}
	public void setSendXmlGOVCBR5IGParserUserCnt(String user, int cnt) {
		BmsMembers.sendXmlGOVCBR5IGParserUsers_.put(user + "_CNT", cnt);
	}

	/** 27.GOVCBR5HG(선반입물품반출신고독려통보서) 수신용 Parser 정의 */
	static private Hashtable<String, Object> recvXmlGOVCBR5HGParser_ = new Hashtable<String, Object>();

	public Object getRecvXmlGOVCBR5HGParserUser(String user) {
		return BmsMembers.recvXmlGOVCBR5HGParser_.get(user);
	}
	public void setRecvXmlGOVCBR5HGParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5HGParser_.put(user, true);
	}
	public void removeRecvXmlGOVCBR5HGParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5HGParser_.remove(user);
	}
	public Object getRecvXmlGOVCBR5HGParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBR5HGParser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBR5HGParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBR5HGParser_.put(user + "_CNT", cnt);
	}

	/***
	 *   22.GOVCBR5IM(보세운송신고사항변경통보서) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBR5IMParser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBR5IMParserUser(String user) {
		return BmsMembers.recvXmlGOVCBR5IMParser_.get(user);
	}
	public void setRecvXmlGOVCBR5IMParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5IMParser_.put(user, true);
	}
	public void removeRecvXmlGOVCBR5IMParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5IMParser_.remove(user);
	}
	public Object getRecvXmlGOVCBR5IMParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBR5IMParser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBR5IMParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBR5IMParser_.put(user + "_CNT", cnt);
	}
	/***
	 *   23.GOVCBR5MA(보세운송신고/승인신청검사통보서) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBR5MAParser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBR5MAParserUser(String user) {
		return BmsMembers.recvXmlGOVCBR5MAParser_.get(user);
	}
	public void setRecvXmlGOVCBR5MAParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5MAParser_.put(user, true);
	}
	public void removeRecvXmlGOVCBR5MAParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5MAParser_.remove(user);
	}
	public Object getRecvXmlGOVCBR5MAParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBR5MAParser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBR5MAParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBR5MAParser_.put(user + "_CNT", cnt);
	}

	/***
	 *   24.GOVCBR5IN(체화예정정보누락통보서)
	 */
	static private Hashtable<String, Object> recvXmlGOVCBR5INParser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBR5INParserUser(String user) {
		return BmsMembers.recvXmlGOVCBR5INParser_.get(user);
	}
	public void setRecvXmlGOVCBR5INParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5INParser_.put(user, true);
	}
	public void removeRecvXmlGOVCBR5INParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5INParser_.remove(user);
	}
	public Object getRecvXmlGOVCBR5INParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBR5INParser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBR5INParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBR5INParser_.put(user + "_CNT", cnt);
	}

	/***
	 *   25. GOVCBR099(반출승인내역서) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBR099Parser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBR099ParserUser(String user) {
		return BmsMembers.recvXmlGOVCBR099Parser_.get(user);
	}
	public void setRecvXmlGOVCBR099ParserUser(String user) {
		BmsMembers.recvXmlGOVCBR099Parser_.put(user, true);
	}
	public void removeRecvXmlGOVCBR099ParserUser(String user) {
		BmsMembers.recvXmlGOVCBR099Parser_.remove(user);
	}
	public Object getRecvXmlGOVCBR099ParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBR099Parser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBR099ParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBR099Parser_.put(user + "_CNT", cnt);
	}

	/***
	 *   26. GOVCBR5HF(보수작업및BL분할처리결과장치장통보서) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBR5HFParser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBR5HFParserUser(String user) {
		return BmsMembers.recvXmlGOVCBR5HFParser_.get(user);
	}
	public void setRecvXmlGOVCBR5HFParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5HFParser_.put(user, true);
	}
	public void removeRecvXmlGOVCBR5HFParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5HFParser_.remove(user);
	}
	public Object getRecvXmlGOVCBR5HFParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBR5HFParser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBR5HFParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBR5HFParser_.put(user + "_CNT", cnt);
	}

	/***
	 *  28.GOVCBR5FF(수입적재화물목록정정내역통보서) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBR5FFParser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBR5FFParserUser(String user) {
		return BmsMembers.recvXmlGOVCBR5FFParser_.get(user);
	}
	public void setRecvXmlGOVCBR5FFParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5FFParser_.put(user, true);
	}
	public void removeRecvXmlGOVCBR5FFParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5FFParser_.remove(user);
	}
	public Object getRecvXmlGOVCBR5FFParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBR5FFParser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBR5FFParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBR5FFParser_.put(user + "_CNT", cnt);
	}

	/***
	 *   29. GOVCBR5EM(수입통관 검사계획통보서) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBR5EMParser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBR5EMParserUser(String user) {
		return BmsMembers.recvXmlGOVCBR5EMParser_.get(user);
	}
	public void setRecvXmlGOVCBR5EMParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5EMParser_.put(user, true);
	}
	public void removeRecvXmlGOVCBR5EMParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5EMParser_.remove(user);
	}
	public Object getRecvXmlGOVCBR5EMParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBR5EMParser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBR5EMParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBR5EMParser_.put(user + "_CNT", cnt);
	}

	/***
	 *   30. GOVCBR5EN(수입통관 검사계획취소통보서) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBR5ENParser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBR5ENParserUser(String user) {
		return BmsMembers.recvXmlGOVCBR5ENParser_.get(user);
	}
	public void setRecvXmlGOVCBR5ENParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5ENParser_.put(user, true);
	}
	public void removeRecvXmlGOVCBR5ENParserUser(String user) {
		BmsMembers.recvXmlGOVCBR5ENParser_.remove(user);
	}
	public Object getRecvXmlGOVCBR5ENParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBR5ENParser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBR5ENParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBR5ENParser_.put(user + "_CNT", cnt);
	}
	/***
	 *  31. GOVCBRRAQ(화물접수통보) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBRRAQParser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBRRAQParserUser(String user) {
		return BmsMembers.recvXmlGOVCBRRAQParser_.get(user);
	}
	public void setRecvXmlGOVCBRRAQParserUser(String user) {
		BmsMembers.recvXmlGOVCBRRAQParser_.put(user, true);
	}
	public void removeRecvXmlGOVCBRRAQParserUser(String user) {
		BmsMembers.recvXmlGOVCBRRAQParser_.remove(user);
	}
	public Object getRecvXmlGOVCBRRAQParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBRRAQParser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBRRAQParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBRRAQParser_.put(user + "_CNT", cnt);
	}
	/***
	 *   32.GOVCBRRD9(화물처리결과통보) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBRRD9Parser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBRRD9ParserUser(String user) {
		return BmsMembers.recvXmlGOVCBRRD9Parser_.get(user);
	}
	public void setRecvXmlGOVCBRRD9ParserUser(String user) {
		BmsMembers.recvXmlGOVCBRRD9Parser_.put(user, true);
	}
	public void removeRecvXmlGOVCBRRD9ParserUser(String user) {
		BmsMembers.recvXmlGOVCBRRD9Parser_.remove(user);
	}
	public Object getRecvXmlGOVCBRRD9ParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBRRD9Parser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBRRD9ParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBRRD9Parser_.put(user + "_CNT", cnt);
	}
	/***
	 *   33.GOVCBRR20(사전검증 오류통보) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBRR20Parser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBRR20ParserUser(String user) {
		return BmsMembers.recvXmlGOVCBRR20Parser_.get(user);
	}
	public void setRecvXmlGOVCBRR20ParserUser(String user) {
		BmsMembers.recvXmlGOVCBRR20Parser_.put(user, true);
	}
	public void removeRecvXmlGOVCBRR20ParserUser(String user) {
		BmsMembers.recvXmlGOVCBRR20Parser_.remove(user);
	}
	public Object getRecvXmlGOVCBRR20ParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBRR20Parser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBRR20ParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBRR20Parser_.put(user + "_CNT", cnt);
	}

	/***
	 *   34. GOVCBRR48(통관목록 접수통보) 수신용 파서 체크  정의
	 */
	static private Hashtable<String, Object> recvXmlGOVCBRR48Parser_ = new Hashtable<String, Object>();
	//BMS Xml Doc
	public Object getRecvXmlGOVCBRR48ParserUser(String user) {
		return BmsMembers.recvXmlGOVCBRR48Parser_.get(user);
	}
	public void setRecvXmlGOVCBRR48ParserUser(String user) {
		BmsMembers.recvXmlGOVCBRR48Parser_.put(user, true);
	}
	public void removeRecvXmlGOVCBRR48ParserUser(String user) {
		BmsMembers.recvXmlGOVCBRR48Parser_.remove(user);
	}
	public Object getRecvXmlGOVCBRR48ParserUserCnt(String user) {
		return BmsMembers.recvXmlGOVCBRR48Parser_.get(user + "_CNT");
	}
	public void setRecvXmlGOVCBRR48ParserUserCnt(String user, int cnt) {
		BmsMembers.recvXmlGOVCBRR48Parser_.put(user + "_CNT", cnt);
	}
}

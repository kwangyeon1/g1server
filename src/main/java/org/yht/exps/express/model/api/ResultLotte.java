package org.yht.exps.express.model.api;

import java.util.List;

public class ResultLotte {
	
	private List<Rtn_list> rtn_list;
	private String message;
	private String status;
	
	public List<Rtn_list> getRtn_list() {
		return rtn_list;
	}
	public void setRtn_list(List<Rtn_list> rtn_list) {
		this.rtn_list = rtn_list;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "ResultLotte [rtn_list=" + rtn_list + ", message=" + message + ", status=" + status + "]";
	}
	
	
	
	
	
}

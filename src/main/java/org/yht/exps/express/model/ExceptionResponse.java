package org.yht.exps.express.model;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("exceptionResponse")
public class ExceptionResponse {
	private int status;
	private String message;
	private String result;

	public ExceptionResponse(int status, String message, String result) {
		super();
		this.status = status;
		this.message = message;
		this.result = result;
	}
}
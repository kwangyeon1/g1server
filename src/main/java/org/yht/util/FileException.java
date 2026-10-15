package org.yht.util;


public class FileException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6888673544642447290L;
	private String msg;
	private Exception e;



	public FileException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FileException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public FileException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public FileException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public FileException(String msg, Exception e) {
		super();
		this.msg = msg;
		this.e = e;
	}

	public String getMsg() {
		if (msg == null) {
			msg = "";
		}
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Exception getE() {
		return e;
	}

	public void setE(Exception e) {
		this.e = e;
	}
	
	

}

package org.yht.util.exception;

public class FileDownloadException extends RuntimeException {
	private static final long serialVersionUID = -2536044672369212024L;

	public FileDownloadException(String message) {
        super(message);
    }
    
    public FileDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}

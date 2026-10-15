package org.yht.util.exception;

public class FileUploadException extends RuntimeException {
	private static final long serialVersionUID = -2939598752516003181L;

	public FileUploadException(String message) {
        super(message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}

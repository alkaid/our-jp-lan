/**
 * 
 */
package com.alkaid.ojpl.common;

/**
 * @author Alkaid
 * 业务异常类
 *
 */
public class AlkaidException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6435570908929058879L;
	public AlkaidException(Throwable e) {
		super(e);
	}
	public AlkaidException(String errorMsg){
		super(errorMsg);
	}
}

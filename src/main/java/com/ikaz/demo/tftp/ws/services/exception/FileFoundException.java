package com.ikaz.demo.tftp.ws.services.exception;

/**
 * This exception maps to the TFTP Error Code:6 Description:File already exists.
 * @author icastillejos
 */
public class FileFoundException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public FileFoundException(String s){
		super(s);
	}
	
}

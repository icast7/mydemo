package com.ikaz.demo.tftp.ws.services.exception;

/**
 * This exception maps to the TFTP Error Code:1 Description:File not found.
 * @author icastillejos
 */
public class FileNotFoundException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public FileNotFoundException(String s){
		super (s);
	}
}

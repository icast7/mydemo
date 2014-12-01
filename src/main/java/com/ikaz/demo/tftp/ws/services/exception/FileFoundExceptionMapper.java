package com.ikaz.demo.tftp.ws.services.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * This class handles the response to a FileFound Exception
 * @author icastillejos
 */
public class FileFoundExceptionMapper implements ExceptionMapper<FileFoundException>
{
	@Override
	public Response toResponse(FileFoundException exception){
		return Response.status(Response.Status.METHOD_NOT_ALLOWED)
				.entity(exception.getMessage())
				.type("text/plain").build();
	}
}

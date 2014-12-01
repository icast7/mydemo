package com.ikaz.demo.tftp.ws.services.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * This class handles the response to a FileNotFound Exception
 * @author icastillejos
 */
@Provider
public class FileNotFoundExceptionMapper implements ExceptionMapper<FileNotFoundException>
{
	@Override
	public Response toResponse(FileNotFoundException exception) {
		return Response.status(Response.Status.NOT_FOUND)
				.entity(exception.getMessage())
				.type("text/plain").build();
	}
}

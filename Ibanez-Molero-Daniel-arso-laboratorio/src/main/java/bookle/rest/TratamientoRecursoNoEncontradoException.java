package bookle.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import bookle.controlador.BookleException;

public class TratamientoRecursoNoEncontradoException implements ExceptionMapper<BookleException> {
	@Override
	public Response toResponse(BookleException arg0) {
		return Response.status(Response.Status.BAD_REQUEST).entity(arg0.getMessage()).build();
	}
}

package rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class TratamientoUsuarioException implements ExceptionMapper<UsuarioException> {

	@Override
	public Response toResponse(UsuarioException exception) {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
	}
}
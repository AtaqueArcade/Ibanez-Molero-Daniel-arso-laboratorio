package bookle.rest;

import java.net.URI;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import bookle.controlador.BookleControlador;
import bookle.controlador.BookleControladorImpl;
import bookle.controlador.BookleException;
import bookle.tipos.Actividad;

@Path("actividades")
public class BookleRest {
	private BookleControlador controlador = new BookleControladorImpl();
	@Context
	private UriInfo uriInfo;

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getActividad(@PathParam("id") String id) throws BookleException {
		Actividad actividad = controlador.getActividad(id);
		return Response.status(Response.Status.OK).entity(actividad).build();

	}

	@PUT
	@Path("{id}")
	public Response updateActividad(@PathParam("id") String id, @FormParam("titulo") String titulo,
			@FormParam("descripcion") String descripcion, @FormParam("profesor") String profesor,
			@FormParam("email") String email) throws BookleException {
		controlador.updateActividad(id, titulo, descripcion, profesor, email);
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	@DELETE
	@Path("{id}")
	public Response removeActividad(@PathParam("id") String id) throws BookleException {
		controlador.removeActividad(id);
		return Response.status(Response.Status.NO_CONTENT).build();

	}

	@POST
	public Response createActividad(@FormParam("titulo") String titulo, @FormParam("descripcion") String descripcion,
			@FormParam("profesor") String profesor, @FormParam("email") String email) throws BookleException {

		String id = controlador.createActividad(titulo, descripcion, profesor, email);

		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		builder.path(id);
		URI nuevaURL = builder.build();

		return Response.created(nuevaURL).build();
	}

}
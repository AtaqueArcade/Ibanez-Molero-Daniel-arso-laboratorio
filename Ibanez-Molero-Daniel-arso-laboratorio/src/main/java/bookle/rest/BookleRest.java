package bookle.rest;

import java.net.URI;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import bookle.controlador.BookleControlador;
import bookle.controlador.BookleControladorImpl;
import bookle.controlador.BookleException;
import bookle.tipos.Actividad;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("actividades")
@Api
public class BookleRest {
	private BookleControlador controlador = new BookleControladorImpl();
	@Context
	private UriInfo uriInfo;

	@GET
	@Path("/{id}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Consulta una actividad", notes = "Retorna una actividad utilizando su identificador", response = Actividad.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_OK, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Actividad no encontrada"),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })
	public Response getActividad(@ApiParam(value = "id de la actividad", required = true) @PathParam("id") String id)
			throws BookleException {

		Actividad actividad = controlador.getActividad(id);

		return Response.status(Response.Status.OK).entity(actividad).build();
	}

	@PUT
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Actualiza una actividad", notes = "Actualiza una actividad con los parámetros proporcionados")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Actividad no encontrada"),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })
	public Response updateActividad(@ApiParam(value = "id de la actividad", required = true) @PathParam("id") String id,
			@ApiParam(value = "titulo de la actividad", required = true) @FormParam("titulo") String titulo,
			@ApiParam(value = "descripcion de la actividad", required = false) @FormParam("descripcion") String descripcion,
			@ApiParam(value = "profesor de la actividad", required = true) @FormParam("profesor") String profesor,
			@ApiParam(value = "email de la actividad", required = false) @FormParam("email") String email)
			throws BookleException {
		controlador.updateActividad(id, titulo, descripcion, profesor, email);
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	@DELETE
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Elimina una actividad", notes = "Elimina una actividad utilizando su identificador, retorna false en caso de fallo", response = Boolean.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Actividad no encontrada"),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })
	public Response removeActividad(@ApiParam(value = "id de la actividad", required = true) @PathParam("id") String id)
			throws BookleException {
		controlador.removeActividad(id);
		return Response.status(Response.Status.NO_CONTENT).build();

	}

	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Crea una actividad", notes = "Crea una actividad con los parámetros proporcionados, retorna su id", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_CREATED, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Actividad no encontrada"),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })

	public Response createActividad(
			@ApiParam(value = "titulo de la actividad", required = true) @FormParam("titulo") String titulo,
			@ApiParam(value = "descripcion de la actividad", required = false) @FormParam("descripcion") String descripcion,
			@ApiParam(value = "profesor de la actividad", required = true) @FormParam("profesor") String profesor,
			@ApiParam(value = "email de la actividad", required = false) @FormParam("email") String email)
			throws BookleException {

		String id = controlador.createActividad(titulo, descripcion, profesor, email);

		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		builder.path(id);
		URI nuevaURL = builder.build();

		return Response.created(nuevaURL).build();
	}

}
package bookle.rest;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import bookle.controlador.BookleControlador;
import bookle.controlador.BookleControladorImpl;
import bookle.controlador.BookleException;
import bookle.controlador.ListadoActividades;
import bookle.controlador.ProxyActividad;
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
	@Path("/{id}")
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

	/*
	 * @PUT
	 * 
	 * @Path("/{id}")
	 * 
	 * @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	 * 
	 * @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	 * 
	 * @ApiOperation(value = "Actualiza una actividad", notes =
	 * "Actualiza una actividad con los parámetros proporcionados")
	 * 
	 * @ApiResponses(value = { @ApiResponse(code =
	 * HttpServletResponse.SC_NO_CONTENT, message = ""),
	 * 
	 * @ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message =
	 * "Actividad no encontrada"),
	 * 
	 * @ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message =
	 * "El formato de la peticion no es correcto") }) public Response
	 * updateActividad(@PathParam("id") String id, Actividad actividad) throws
	 * BookleException { controlador.updateActividad(id, actividad.getTitulo(),
	 * actividad.getDescripcion(), actividad.getProfesor(),
	 * actividad.getEmail()); return
	 * Response.status(Response.Status.NO_CONTENT).build(); }
	 */
	@DELETE
	@Path("/{id}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Elimina una actividad", notes = "Elimina una actividad utilizando su identificador, retorna false en caso de fallo")
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
	@ApiOperation(value = "Crea una actividad", notes = "Crea una actividad con los parametros proporcionados, retorna su id", response = URI.class)
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

	@POST
	@Path("/{id}/agenda")
	@ApiOperation(value = "Crea un dia", notes = "Crea un dia en una agenda dentro de la actividad con los parámetros proporcionados", response = URI.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_CREATED, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Actividad no encontrada"),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })
	public Response addDiaActividad(@ApiParam(value = "id de la actividad", required = true) @PathParam("id") String id,
			@ApiParam(value = "fecha del dia", required = true) @FormParam("fecha") String fecha,
			@ApiParam(value = "turnos del dia", required = true) @FormParam("turno") int turno)
			throws ParseException, BookleException {
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		controlador.addDiaActividad(id, format.parse(fecha), turno);
		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		builder.path(fecha);
		URI nuevaURL = builder.build();
		return Response.created(nuevaURL).build();
	}

	@DELETE
	@Path("/{id}/agenda/{fecha}")
	@ApiOperation(value = "Elimina un dia", notes = "Elimina un dia de la agenda de la actividad en base a su fecha")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })
	public Response removeDiaActividad(
			@ApiParam(value = "id de la actividad", required = true) @PathParam("id") String id,
			@ApiParam(value = "fecha del dia", required = true) @PathParam("fecha") Date fecha) throws BookleException {
		controlador.removeDiaActividad(id, fecha);
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	@POST
	@Path("/{id}/agenda/{fecha}/turno")
	@ApiOperation(value = "Anade un turno", notes = "Crea un turno y lo anade a el dia indicado de la actividad, retorna su id", response = URI.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Actividad no encontrada"),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })
	public Response addTurnoActividad(
			@ApiParam(value = "id de la actividad", required = true) @PathParam("id") String id,
			@ApiParam(value = "fecha del dia", required = true) @PathParam("fecha") String fecha)
			throws ParseException, BookleException {
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		int turno = controlador.addTurnoActividad(id, format.parse(fecha));
		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		builder.path(Integer.toString(turno));
		URI nuevaURL = builder.build();
		return Response.created(nuevaURL).build();
	}

	@DELETE
	@Path("/{id}/agenda/{fecha}/turno/{indice}")
	@ApiOperation(value = "Elimina un turno", notes = "Elimina un turno de un dia de la agenda de la actividad indicada")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Actividad no encontrada"),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })
	public Response removeTurnoActividad(
			@ApiParam(value = "id de la actividad", required = true) @PathParam("id") String id,
			@ApiParam(value = "fecha del dia", required = true) @PathParam("fecha") Date fecha,
			@ApiParam(value = "indice del turno", required = true) @PathParam("indice") int turno)
			throws BookleException {
		controlador.removeTurnoActividad(id, fecha, turno);
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	@PUT
	@Path("/{id}/agenda/{fecha}/turno/{indice}")
	@ApiOperation(value = "Establece el horario de un turno", notes = "Establece el horario del turno indicado de la actividad")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Actividad no encontrada"),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })
	public Response setHorario(@ApiParam(value = "id de la actividad", required = true) @PathParam("id") String id,
			@ApiParam(value = "fecha del dia", required = true) @PathParam("fecha") String fecha,
			@ApiParam(value = "indice del turno", required = true) @PathParam("indice") int indice,
			@ApiParam(value = "horario del turno", required = true) @FormParam("horario") String horario)
			throws ParseException, BookleException {
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		controlador.setHorario(id, format.parse(fecha), indice, horario);
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	@POST
	@Path("/{id}/agenda/{fecha}/turno/{indice}/reserva")
	@ApiOperation(value = "Crea una reserva", notes = "Crea una reserva para un turno determinado, retorna su id")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Actividad no encontrada"),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })
	public Response createReserva(@ApiParam(value = "id de la actividad", required = true) @PathParam("id") String id,
			@ApiParam(value = "fecha del dia", required = true) @PathParam("fecha") String fecha,
			@ApiParam(value = "indice del turno", required = true) @PathParam("indice") int indice,
			@ApiParam(value = "alumno de la reserva", required = true) @FormParam("alumno") String alumno,
			@ApiParam(value = "email de la reserva", required = false) @FormParam("email") String email)
			throws BookleException, ParseException {
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		String idReserva = controlador.createReserva(id, format.parse(fecha), indice, alumno, email);
		UriBuilder builder = uriInfo.getBaseUriBuilder();
		builder.path("actividades");
		builder.path(id);
		builder.path("reservas");
		builder.path(idReserva);
		URI nuevaURL = builder.build();
		return Response.created(nuevaURL).build();
	}

	@DELETE
	@Path("/{id}/reservas/{ticket}")
	@ApiOperation(value = "Elimina una reserva", notes = "Elimina una reserva de una actividad, retorna false en caso de fallo")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Actividad no encontrada"),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion no es correcto") })
	public Response removeReserva(@ApiParam(value = "id de la actividad", required = true) @PathParam("id") String id,
			@ApiParam(value = "ticket de la reserva", required = true) @PathParam("ticket") String ticket)
			throws BookleException {
		controlador.removeReserva(id, ticket);
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Retorna un listado de actividades", response = ListadoActividades.class)
	public Response listado(
			@ApiParam(value = "profesor de la actividad", required = false) @QueryParam("profesor") String profesor,
			@ApiParam(value = "titulo de la actividad", required = false) @QueryParam("titulo") String titulo)
			throws BookleException {
		Collection<String> actividades = controlador.getIdentifidores();
		ListadoActividades listado = new ListadoActividades();
		for (String id : actividades) {
			Actividad actividad = controlador.getActividad(id);
			if ((actividad.getProfesor().equals(profesor) || profesor == null)
					&& (actividad.getTitulo().contains(titulo) || titulo == null)) {
				UriBuilder builder = uriInfo.getAbsolutePathBuilder();
				builder.path(id);
				String url = builder.build().toString();
				listado.getActividad().add(new ProxyActividad(url, actividad.getTitulo()));
			}
		}
		return Response.ok(listado).build();
	}
}
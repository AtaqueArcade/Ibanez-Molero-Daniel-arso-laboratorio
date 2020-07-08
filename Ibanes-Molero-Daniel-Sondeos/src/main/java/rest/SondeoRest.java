package rest;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import controlador.SondeoControlador;
import controlador.SondeoControladorImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api
@Path("sondeos")
public class SondeoRest {
	private SondeoControlador controlador = SondeoControladorImpl.getInstance();
	@Context
	private UriInfo uriInfo;

	@POST
	@ApiOperation(value = "Crear sondeo", notes = "Genera un sondeo", response = URI.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_CREATED, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion es incorrecto") })
	public Response crearSondeo(
			@ApiParam(value = "Correo del usuario creador", required = true) @FormParam("correo") String correo,
			@ApiParam(value = "Pregunta del sondeo", required = true) @FormParam("pregunta") String pregunta,
			@ApiParam(value = "Respuestas al sondeo") @FormParam("respuestas") List<String> respuestas,
			@ApiParam(value = "Instrucciones del sondeo", required = true) @FormParam("instrucciones") String instrucciones,
			@ApiParam(value = "Fecha y hora de apertura", required = true) @FormParam("apertura") String apertura,
			@ApiParam(value = "Fecha y hora de cierre", required = true) @FormParam("cierre") String cierre,
			@ApiParam(value = "Minimo de respuestas seleccionadas", required = true) @FormParam("minSeleccion") int minSeleccion,
			@ApiParam(value = "Máximo de respuestas seleccionadas", required = true) @FormParam("maxSeleccion") int maxSeleccion,
			@ApiParam(value = "Visibilidad del sondeo", required = true) @FormParam("visibilidad") String visibilidad)
			throws SondeoException {
		String id = controlador.createSondeo(correo, pregunta, petitionFix(respuestas), instrucciones, apertura, cierre,
				minSeleccion, maxSeleccion, visibilidad);
		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		builder.path(id);
		URI nuevaURL = builder.build();
		return Response.created(nuevaURL).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Recuperar sondeo", notes = "Devuelve un sondeo en base a su id", response = JsonObject.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El sondeo pedido no existe") })
	public Response getSondeo(@ApiParam(value = "Id del sondeo", required = true) @PathParam("id") String id) {
		JsonObject sondeo = controlador.getSondeo(id);
		return Response.status(Response.Status.OK).entity(sondeo).build();
	}

	@POST
	@Path("/{id}")
	@ApiOperation(value = "Actualizar respuestas", notes = "Actualiza las respuestas de un sondeo")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El sondeo pedido no existe") })
	public Response updateSondeo(@ApiParam(value = "Id del sondeo", required = true) @PathParam("id") String id,
			@ApiParam(value = "Respuestas al sondeo", required = true) @FormParam("respuestas") List<String> respuesta,
			@ApiParam(value = "Correo del creador del sondeo", required = true) @FormParam("correo") String correo)
			throws SondeoException {
		if (controlador.updateRespuestas(id, correo, petitionFix(respuesta)))
			return Response.status(Response.Status.NO_CONTENT).build();
		return Response.status(Response.Status.NOT_MODIFIED).build();
	}

	@POST
	@Path("/{id}/confirm")
	@ApiOperation(value = "Guardar sondeo", notes = "Confirma el sondeo en su estado actual y lo publica")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El sondeo pedido no existe") })
	public Response saveSondeo(@ApiParam(value = "Id del sondeo", required = true) @PathParam("id") String id,
			@ApiParam(value = "Correo del creador del sondeo", required = true) @FormParam("correo") String correo)
			throws SondeoException {
		controlador.confirmSondeo(id, correo);
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	@POST
	@Path("/{id}/respuestas")
	@ApiOperation(value = "Responder sondeo", notes = "Anade respuestas a un sondeo")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El sondeo pedido no existe") })
	public Response responderSondeo(@ApiParam(value = "Id del sondeo", required = true) @PathParam("id") String id,
			@ApiParam(value = "Correo del estudiante", required = true) @FormParam("correo") String correo,
			@ApiParam(value = "Respuestas al sondeo", required = true) @FormParam("respuestas") String contenido)
			throws SondeoException {
		if (controlador.addEntrada(id, correo, contenido))
			return Response.status(Response.Status.NO_CONTENT).build();
		return Response.status(Response.Status.NOT_MODIFIED).build();
	}

	@POST
	@Path("/{id}/entradas")
	@ApiOperation(value = "Recupera las entradas", notes = "Devuelve las respuestas de los alumnos al sondeo")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El sondeo pedido no existe") })
	public Response entradasSondeo(@ApiParam(value = "Id del sondeo", required = true) @PathParam("id") String id,
			@ApiParam(value = "Correo del profesor", required = true) @FormParam("correo") String correo)
			throws SondeoException {
		JsonObject sondeo = controlador.getEntradas(id, correo);
		return Response.status(Response.Status.OK).entity(sondeo).build();
	}

	@DELETE
	@Path("/{id}")
	@ApiOperation(value = "Borrar sondeo", notes = "Elimina un sondeo de la base de datos")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El sondeo pedido no existe") })
	public Response removeSondeo(@ApiParam(value = "Id del sondeo", required = true) @PathParam("id") String id,
			@ApiParam(value = "Correo del profesor", required = true) @FormParam("correo") String correo)
			throws SondeoException {
		if (controlador.removeSondeo(id, correo) == true)
			return Response.status(Response.Status.NO_CONTENT).build();
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	// Supporting methods
	// Swagger envia las listas de strings como si fuera una sola variable
	// El metodo vuelve a convertirlas en lista de ser necesario
	private List<String> petitionFix(List<String> par) {
		if (par.size() == 1)
			if (par.get(0).contains(","))
				return Stream.of(par.get(0).split(",")).map(String::trim).collect(Collectors.toList());
		return par;
	}
}
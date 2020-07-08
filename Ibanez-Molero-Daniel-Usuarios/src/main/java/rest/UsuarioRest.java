package rest;

import java.net.URI;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletResponse;
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
import controlador.UsuarioControlador;
import controlador.UsuarioControladorImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api
@Path("usuarios")
public class UsuarioRest {
	private UsuarioControlador controlador = UsuarioControladorImpl.getInstance();
	@Context
	private UriInfo uriInfo;

	@POST
	@ApiOperation(value = "Crear usuario", notes = "Genera un usuario", response = URI.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_CREATED, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El formato de la peticion es incorrecto") })
	public Response crearUsuario(
			@ApiParam(value = "Correo del usuario", required = true) @FormParam("correo") String correo,
			@ApiParam(value = "Nombre del usuario", required = true) @FormParam("nombre") String nombre,
			@ApiParam(value = "Rol del usuario", required = true) @FormParam("rol") String rol)
			throws UsuarioException {
		String id = controlador.createUsuario(correo, nombre, rol);
		if (id != null) {
			UriBuilder builder = uriInfo.getAbsolutePathBuilder();
			builder.path(correo);
			URI nuevaURL = builder.build();
			return Response.created(nuevaURL).build();
		}
		return Response.serverError().build();
	}

	@GET
	@Path("/{correo}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Recuperar usuario", notes = "Devuelve un usuario en base a su correo", response = JsonObject.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El usuario pedido no existe") })
	public Response getUsuario(
			@ApiParam(value = "Correo del usuario", required = true) @PathParam("correo") String correo) {
		JsonObject usuario = controlador.getUsuario(correo);
		return Response.status(Response.Status.OK).entity(usuario).build();
	}

	@POST
	@Path("/{correo}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Recuperar rol", notes = "Devuelve el rol de un usuario", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El usuario pedido no existe") })
	public Response getRol(
			@ApiParam(value = "Correo del usuario", required = true) @PathParam("correo") String correo) {
		String rol = controlador.getRol(correo);
		if (rol == null)
			Response.status(Response.Status.BAD_REQUEST).build();
		return Response.status(Response.Status.OK).entity(Json.createObjectBuilder().add("rol", rol).build()).build();
	}

	@GET
	@Path("/alumnos")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Recuperar alumnos", notes = "Devuelve un array que contiene todos los alumnos", response = JsonArray.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = "") })
	public Response getAlumnos() {
		return Response.status(Response.Status.OK).entity(controlador.getAllEstudiantes()).build();
	}

	@GET
	@Path("/profesores")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Recuperar profesores", notes = "Devuelve un array que contiene todos los profesores", response = JsonArray.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = "") })
	public Response getProfesores() {
		return Response.status(Response.Status.OK).entity(controlador.getAllProfesores()).build();
	}
}
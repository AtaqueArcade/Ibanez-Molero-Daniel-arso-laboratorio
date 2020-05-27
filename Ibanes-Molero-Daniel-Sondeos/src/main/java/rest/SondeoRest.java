package rest;

import java.net.URI;
import java.text.SimpleDateFormat;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import tipos.TipoSondeo;

@Path("sondeo")
@Api
public class SondeoRest {
	private SondeoControlador controlador = new SondeoControladorImpl();
	@Context
	private UriInfo uriInfo;

	@POST
	@ApiOperation(value = "Crear sondeo", notes = "Genera un sondeo", response = URI.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_CREATED, message = ""), })
	public Response crearSondeo() {
		return null;

	}

	@PUT
	@Path("/{id}")
	@ApiOperation(value = "Actualizar sondeo", notes = "Actualiza los valores de un sondeo")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El sondeo no existe") })
	public Response actualizarSondeo() {
		return null;
	}

	@DELETE
	@Path("/{id}")
	@ApiOperation(value = "Eliminar sondeos", notes = "Elimina un sondeo del repositorio")
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El sondeo no existe") })
	public Response eliminarSondeo() {
		return null;
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/{id}")
	@ApiOperation(value = "Obtener sondeo", notes = "Obtener sondeo", response = TipoSondeo.class)
	@ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_OK, message = ""),
			@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "El sondeo no existe") })
	public Response obtenerDocumentoFavoritos() {
		return null;
	}
}
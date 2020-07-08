package controlador;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import org.bson.types.ObjectId;
import repositorio.SondeoRepository;
import rest.SondeoException;
import tipos.Entrada;
import com.rabbitmq.client.ConnectionFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class SondeoControladorImpl implements SondeoControlador {
	private static SondeoControlador controlador;
	private SondeoRepository repositorio;
	private Channel channelPendientes;
	private Channel channelCompletados;
	private String exchangeName;

	public static SondeoControlador getInstance() {
		if (controlador == null) {
			try {
				controlador = new SondeoControladorImpl();
			} catch (SondeoException e) {
				e.printStackTrace();
			}
		}
		return controlador;
	}

	private SondeoControladorImpl() throws SondeoException {
		repositorio = SondeoRepository.getInstance();
		ConnectionFactory factory = new ConnectionFactory();
		try {
			factory.setUri("amqp://otbzkwgy:MUMAlBAj4iqa0y5ZX63cfDYX1hs7u00u@chinook.rmq.cloudamqp.com/otbzkwgy");
			Connection connection = factory.newConnection();
			boolean durable = true;
			boolean exclusive = false;
			boolean autoDelete = false;
			channelPendientes = connection.createChannel();
			channelCompletados = connection.createChannel();
			channelPendientes.queueDeclare("ArSoPendientes", durable, exclusive, autoDelete, null);
			channelCompletados.queueDeclare("ArSoCompletados", durable, exclusive, autoDelete, null);
			exchangeName = "";
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException
				| TimeoutException e) {
			throw new SondeoException("Error al establecer conexion con la cola de mensajes");
		}
	}

	@Override
	public String createSondeo(String correo, String pregunta, List<String> respuestas, String instrucciones,
			String sApertura, String sCierre, int minSeleccion, int maxSeleccion, String visibilidad)
			throws SondeoException {
		if (correo == null || getRol(correo) == null || !getRol(correo).equals("profesor"))
			throw new IllegalArgumentException("El correo ha de pertenecer a un profesor valido");
		if (pregunta == null || pregunta.equals(""))
			throw new IllegalArgumentException("La pregunta del sondeo no puede ser nula o vacia");
		if (instrucciones == null || instrucciones.equals(""))
			throw new IllegalArgumentException("Las instrucciones del sondeo no pueden ser nulas o vacias");
		if (sApertura == null || sApertura.equals(""))
			throw new IllegalArgumentException("La fecha de apertura no puede ser nula");
		if (sCierre == null || sCierre.equals(""))
			throw new IllegalArgumentException("La fecha de cierre no puede ser nula");
		if (minSeleccion <= 0)
			throw new IllegalArgumentException("El numero de selecciones minimas ha de ser mayor a cero");
		if (maxSeleccion < minSeleccion)
			throw new IllegalArgumentException(
					"El numero de selecciones máximas ha de ser superior o igual al de minimas");
		if (visibilidad == null || visibilidad.equals(""))
			throw new IllegalArgumentException("La visibilidad del sondeo no puede ser nula o vacia");
		LocalDateTime apertura, cierre;
		try {
			apertura = LocalDateTime.parse(sApertura);
			cierre = LocalDateTime.parse(sCierre);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Formato incorrecto de fecha y hora");
		}

		String id = null;
		id = repositorio.saveSondeo(correo, pregunta, respuestas, instrucciones, apertura, cierre, maxSeleccion,
				minSeleccion, visibilidad);
		return id;
	}

	@Override
	public boolean updateRespuestas(String id, String correo, List<String> respuestas) throws SondeoException {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		if (respuestas == null || respuestas.size() < 1)
			throw new IllegalArgumentException("Formato de respuestas incorrecto en edicion");
		JsonObject sondeo = repositorio.getSondeo(id);
		if (!correo.equals(sondeo.get("correo").toString().substring(1, sondeo.get("correo").toString().length() - 1)))
			throw new SondeoException("El correo ha de pertenecer al creador del sondeo");
		if (Boolean
				.parseBoolean(sondeo.get("final").toString().substring(1, sondeo.get("final").toString().length() - 1)))
			throw new SondeoException("El sondeo ha sido marcado como no editable");
		return repositorio.updateRespuestas(id, respuestas);
	}

	@Override
	public void confirmSondeo(String id, String correo) throws SondeoException {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		JsonObject sondeo = repositorio.getSondeo(id);
		if (!correo.equals(sondeo.get("correo").toString().substring(1, sondeo.get("correo").toString().length() - 1)))
			throw new SondeoException("El correo ha de pertenecer al creador del sondeo");
		try {
			repositorio.makeFinal(id);
			String infoTarea = "SONDEO" + ";" + id + ";" + correo + ";" + getAllAlumnos() + ";" + sondeo.get("cierre");
			channelPendientes.basicPublish(exchangeName, "ArSoPendientes", null, infoTarea.getBytes());
		} catch (IOException e) {
			throw new SondeoException("No se ha podido realizar la conexión con la cola de mensajes");
		}
	}

	@Override
	public boolean addEntrada(String id, String correo, String contenido) throws SondeoException {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		if (correo == null || getRol(correo) == null || !getRol(correo).equals("estudiante"))
			throw new IllegalArgumentException("El usuario ha de ser el correo de un estudiante valido");
		if (contenido == null || contenido.equals(""))
			throw new IllegalArgumentException("El contenido no puede ser nulo o vacio");
		Entrada e = new Entrada();
		e.setCorreo(correo);
		e.setSeleccion(contenido);

		try {
			String infoTarea = "SONDEO" + ";" + id + ";" + correo;
			channelCompletados.basicPublish(exchangeName, "ArSoCompletados", null, infoTarea.getBytes());
			return repositorio.addEntrada(id, e);
		} catch (IOException e1) {
			throw new SondeoException("No se ha podido realizar la conexión con la cola de mensajes");
		}
	}

	@Override
	public JsonObject getSondeo(String id) {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		return repositorio.getSondeo(id);
	}

	@Override
	public JsonObject getEntradas(String id, String correo) throws SondeoException {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		JsonObject sondeo = repositorio.getSondeo(id);
		if (!correo.equals(sondeo.get("correo").toString().substring(1, sondeo.get("correo").toString().length() - 1)))
			throw new SondeoException("El correo ha de pertenecer al creador del sondeo");
		return repositorio.getEntradasSondeo(id);
	}

	@Override
	public boolean removeSondeo(String id, String correo) throws SondeoException {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		JsonObject sondeo = repositorio.getSondeo(id);
		if (!correo.equals(sondeo.get("correo").toString().substring(1, sondeo.get("correo").toString().length() - 1)))
			throw new SondeoException("El correo ha de pertenecer al creador del sondeo");
		return repositorio.removeSondeo(id);
	}

	// Supporting methods
	private String getRol(String correo) throws SondeoException {
		Client client = Client.create();
		String requestURL = "http://localhost:8083/api/usuarios/" + correo;
		WebResource wr = client.resource(requestURL);
		ClientResponse response = wr.method("POST", ClientResponse.class);

		if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
			throw new SondeoException("La petición al servidor de usuarios no ha devuelto ningun dato");
		}
		InputStream inputStream = response.getEntityInputStream();
		JsonParser parser = Json.createParser(inputStream);
		while (parser.hasNext()) {
			try {
				JsonParser.Event event = parser.next();
				if (event == JsonParser.Event.KEY_NAME) {
					String key = parser.getString();
					event = parser.next();
					if (key.equals("chars")) {
						parser.close();
						inputStream.close();
						return parser.getString();
					}
				}
			} catch (Exception e) {
				throw new SondeoException("Error en la base de usuarios");
			}
		}
		throw new SondeoException("Respuesta con formato incorrecto recibida del servidor de usuarios");
	}

	private List<String> getAllAlumnos() throws SondeoException {
		LinkedList<String> alumnos = new LinkedList<String>();
		Client client = Client.create();
		String requestURL = "http://localhost:8083/api/usuarios/alumnos";
		WebResource wr = client.resource(requestURL);
		ClientResponse response = wr.method("GET", ClientResponse.class);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
			throw new SondeoException("La petición al servidor de usuarios no ha devuelto ningun dato");
		}
		InputStream inputStream = response.getEntityInputStream();
		JsonParser parser = Json.createParser(inputStream);
		while (parser.hasNext()) {
			JsonParser.Event event = parser.next();
			if (event == JsonParser.Event.KEY_NAME) {
				String key = parser.getString();
				event = parser.next();
				if (key.equals("chars"))
					alumnos.add(parser.getString());
			}
		}
		try {
			parser.close();
			inputStream.close();
		} catch (IOException e) {
			throw new SondeoException("Error en el stream con el servidor de usuarios");
		}
		return alumnos;
	}
}

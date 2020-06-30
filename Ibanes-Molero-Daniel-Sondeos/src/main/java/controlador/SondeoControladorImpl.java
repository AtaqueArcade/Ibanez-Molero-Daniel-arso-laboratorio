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
	public static final String RABBITURI = "amqp://otbzkwgy:MUMAlBAj4iqa0y5ZX63cfDYX1hs7u00u@chinook.rmq.cloudamqp.com/otbzkwgy";
	private static SondeoControlador controlador;
	private SondeoRepository repositorio;
	private Channel channel;
	private String exchangeName;
	private String routingKey;

	public static SondeoControlador getInstance() {
		if (controlador == null) {
			try {
				controlador = new SondeoControladorImpl();
			} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException
					| TimeoutException e) {
				e.printStackTrace();
			}
		}
		return controlador;
	}

	private SondeoControladorImpl()
			throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
		repositorio = SondeoRepository.getInstance();

		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri(RABBITURI);
		Connection connection = factory.newConnection();
		channel = connection.createChannel();

		String queue = "sondeos";
		boolean durable = false; // durable - RabbitMQ will never lose the queue if a crash occurs
		boolean exclusive = false; // exclusive - if queue only will be used by one connection
		boolean autoDelete = false; // autodelete - queue is deleted when last consumer unsubscribes
		channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
		exchangeName = "";
		routingKey = "sondeos";
	}

	@Override
	public String createSondeo(String usuario, String pregunta, List<String> respuestas, String instrucciones,
			String sApertura, String sCierre, int minSeleccion, int maxSeleccion, String visibilidad)
			throws SondeoException {
		if (usuario == null || !getRol(usuario).equals("profesor"))
			throw new IllegalArgumentException("El usuario ha de ser el correo de un profesor valido");
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
		try {
			id = repositorio.saveSondeo(usuario, pregunta, respuestas, instrucciones, apertura, cierre, maxSeleccion,
					minSeleccion, visibilidad);
			channel.basicPublish(exchangeName, routingKey, null, id.getBytes());
		} catch (IOException e) {
			throw new SondeoException("No se ha podido realizar la conexión con la cola de mensajes");
		}
		return id;
	}

	@Override
	public boolean updateRespuestas(String id, List<String> respuestas) {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		if (respuestas == null || respuestas.size() < 1)
			throw new IllegalArgumentException("Formato de respuestas incorrecto en edición");
		return repositorio.updateRespuestas(id, respuestas);
	}

	@Override
	public boolean addEntrada(String id, String correo, String contenido) throws SondeoException {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		if (correo == null || !getRol(correo).equals("estudiante"))
			throw new IllegalArgumentException("El usuario ha de ser el correo de un estudiante valido");
		if (contenido == null || contenido.equals(""))
			throw new IllegalArgumentException("El contenido no puede ser nulo o vacio");
		Entrada e = new Entrada();
		e.setCorreo(correo);
		e.setSeleccion(contenido);
		return repositorio.addEntrada(id, e);
	}

	@Override
	public JsonObject getSondeo(String id) {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		return repositorio.getSondeo(id);
	}

	@Override
	public boolean removeSondeo(String id) {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		return repositorio.removeSondeo(id);
	}

	// Supporting method
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
			JsonParser.Event event = parser.next();
			if (event == JsonParser.Event.VALUE_STRING) {
				try {
					parser.close();
					inputStream.close();
				} catch (IOException e) {
					throw new SondeoException("Error en el stream con el servidor de usuarios");
				}
				return parser.getString();
			}
		}
		throw new SondeoException("Respuesta con formato incorrecto recibida del servidor de usuarios");
	}
}

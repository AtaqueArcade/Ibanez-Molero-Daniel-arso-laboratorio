package controlador;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.json.JsonObject;

import org.bson.types.ObjectId;

import repositorio.SondeoRepository;
import rest.SondeoException;
import com.rabbitmq.client.ConnectionFactory;
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
	public String createSondeo(String pregunta, List<String> respuestas, String instrucciones, String sApertura,
			String sCierre, int minSeleccion, int maxSeleccion, String visibilidad) throws SondeoException {
		if (pregunta == null || pregunta.equals(""))
			throw new IllegalArgumentException("La pregunta del sondeo no puede ser nula o vacia");
		if (instrucciones == null || instrucciones.equals(""))
			throw new IllegalArgumentException("Las instrucciones del sondeo no pueden ser nulas o vacias");
		if (sApertura == null || sApertura.equals(""))
			throw new IllegalArgumentException("La fecha de apertura no puede ser nula");
		if (sCierre == null || sCierre.equals(""))
			throw new IllegalArgumentException("La fecha de cierre no puede ser nula");
		if (minSeleccion <= 0)
			throw new IllegalArgumentException("El número de selecciones mínimas ha de ser mayor a cero");
		if (maxSeleccion < minSeleccion)
			throw new IllegalArgumentException(
					"El número de selecciones máximas ha de ser superior o igual al de mínimas");
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
		if (respuestas == null)
			respuestas = new LinkedList<String>();
		try {
			id = repositorio.saveSondeo(pregunta, respuestas, instrucciones, apertura, cierre, maxSeleccion,
					minSeleccion, visibilidad);
			channel.basicPublish(exchangeName, routingKey, null, id.getBytes());
		} catch (IOException e) {
			throw new SondeoException("No se ha podido realizar la conexión con la cola de mensajes");
		}
		return id;
	}

	@Override
	public boolean updateSondeo(String id, List<String> respuestas) {
		if (!ObjectId.isValid(id))
			throw new IllegalArgumentException("Formato de identificador incorrecto");
		if (respuestas == null || respuestas.size() < 1)
			throw new IllegalArgumentException("Formato de respuestas incorrecto en edición");
		return repositorio.updateSondeo(id, respuestas);
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
}

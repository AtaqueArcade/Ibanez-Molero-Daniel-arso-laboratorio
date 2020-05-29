package controlador;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.json.JsonObject;
import repositorio.SondeoRepository;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class SondeoControladorImpl implements SondeoControlador {
	private static SondeoControladorImpl controlador;
	private static SondeoRepository repositorio;
	private static Channel channel;
	private static String exchangeName;
	private static String routingKey;

	public static SondeoControladorImpl getControlador()
			throws IOException, TimeoutException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
		if (controlador == null) {
			controlador = new SondeoControladorImpl();
			repositorio = SondeoRepository.getInstance();

			ConnectionFactory factory = new ConnectionFactory();
			factory.setUri("amqp://otbzkwgy:MUMAlBAj4iqa0y5ZX63cfDYX1hs7u00u@chinook.rmq.cloudamqp.com/otbzkwgy");
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
		return controlador;
	}

	@Override
	public String createSondeo(String pregunta, List<String> respuestas, String instrucciones, LocalDateTime apertura,
			LocalDateTime cierre, int minSeleccion, int maxSeleccion, String visibilidad) {
		String id = repositorio.saveSondeo(pregunta, respuestas, instrucciones, apertura, cierre, maxSeleccion,
				minSeleccion, visibilidad);
		try {
			channel.basicPublish(exchangeName, routingKey, null, id.getBytes());
			System.out.println(" [x] Sent ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return id;
	}

	@Override
	public void updateSondeo(String id, List<String> respuestas) {
		repositorio.updateSondeo(id, respuestas);
	}

	@Override
	public JsonObject getSondeo(String id) {
		return repositorio.getSondeo(id);
	}

	@Override
	public boolean removeSondeo(String id) {
		return repositorio.removeSondeo(id);
	}

	// Tester
	public static void main(String[] args)
			throws IOException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException, TimeoutException {

		SondeoControlador sc = null;
		try {
			sc = SondeoControladorImpl.getControlador();
		} catch (IOException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

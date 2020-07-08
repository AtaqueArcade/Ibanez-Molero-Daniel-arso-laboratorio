package tareas;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import com.coxautodev.graphql.tools.SchemaParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import graphql.schema.GraphQLSchema;
import graphql.servlet.SimpleGraphQLServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/graphql")
public class GraphQLEndpoint extends SimpleGraphQLServlet {
	private static TareaRepository tareaRepository;
	private static MongoClient client;
	private static Channel channelPendientes;
	private static Channel channelCompletados;

	// Inicia el repositorio y el sistema de colas
	private static void initDB() {
		MongoClient client = MongoClients.create(
				"mongodb+srv://ataquearcade:huWAN4jGusPRjqV@arso-vaorn.mongodb.net/test?retryWrites=true&w=majority");
		MongoDatabase database = client.getDatabase("ArSo");
		tareaRepository = new TareaRepository(database.getCollection("tareas"));
		try {
			setAmqp();
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException
				| TimeoutException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		client.close();
	}

	public GraphQLEndpoint() {
		super(buildSchema());
	}

	private static GraphQLSchema buildSchema() {
		initDB();
		return SchemaParser.newParser().file("schema.graphqls")
				.resolvers(new Query(tareaRepository), new Mutation(tareaRepository)).build().makeExecutableSchema();
	}

	// Supporting methods
	private static void setAmqp()
			throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri("amqp://otbzkwgy:MUMAlBAj4iqa0y5ZX63cfDYX1hs7u00u@chinook.rmq.cloudamqp.com/otbzkwgy");
		Connection connection = factory.newConnection();
		boolean durable = true;
		boolean exclusive = false;
		boolean autoDelete = false;
		channelPendientes = connection.createChannel();
		channelCompletados = connection.createChannel();
		channelPendientes.queueDeclare("ArSoPendientes", durable, exclusive, autoDelete, null);
		// Recibir un mensaje en la cola pendientes creara una tarea en el sistema
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			tareaRepository.saveTarea(parseTarea(new String(delivery.getBody(), "UTF-8")));
		};
		channelPendientes.basicConsume("ArSoPendientes", true, deliverCallback, consumerTag -> {
		});
		// Recibir un mensaje en la cola completados eliminara un receptor de la tarea
		// Cuando no queden receptores en la tarea, la tarea sera eliminada
		channelCompletados.queueDeclare("ArSoCompletados", durable, exclusive, autoDelete, null);
		DeliverCallback completados = (tagCompletados, delivery) -> {
			List<String> params = parseCompletar(new String(delivery.getBody(), "UTF-8"));
			Optional<Tarea> task = tareaRepository.getTareasReceptor(params.get(2)).stream()
					.filter(t -> t.getTipo().equals(params.get(0)) && t.getTareaId().equals(params.get(1))).findFirst();
			if (task.isPresent()) {
				task.get().getReceptores().remove(params.get(2));
				tareaRepository.updateReceptores(task.get().getId(), task.get().getReceptores());
			}
		};
		channelCompletados.basicConsume("ArSoCompletados", true, completados, tagCompletados -> {
		});
	}

	private static Tarea parseTarea(String s) {
		List<String> fields = Arrays.stream(s.split(";")).map(String::intern).collect(Collectors.toList());
		// Tipo, id, creador, receptores, fecha
		return new Tarea(fields.get(0), fields.get(1), fields.get(2),
				Arrays.asList(fields.get(3).replace("[", "").replace("]", "").split("\\s*,\\s*")), fields.get(4));
	}

	private static List<String> parseCompletar(String s) {
		// Tipo, id, receptor
		return Arrays.stream(s.split(";")).map(String::intern).collect(Collectors.toList());
	}
}
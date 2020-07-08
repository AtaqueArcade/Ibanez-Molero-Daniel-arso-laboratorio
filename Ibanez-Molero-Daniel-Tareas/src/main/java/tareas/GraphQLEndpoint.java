package tareas;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
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

	// Inicia el repositorio y el sistema de colas
	private static void initDB()
			throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
		MongoClient client = MongoClients.create(
				"mongodb+srv://ataquearcade:huWAN4jGusPRjqV@arso-vaorn.mongodb.net/test?retryWrites=true&w=majority");
		MongoDatabase database = client.getDatabase("ArSo");
		tareaRepository = new TareaRepository(database.getCollection("tareas"));

		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri("amqp://otbzkwgy:MUMAlBAj4iqa0y5ZX63cfDYX1hs7u00u@chinook.rmq.cloudamqp.com/otbzkwgy");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		String queue = "ArSo";
		boolean durable = false;
		boolean exclusive = false;
		boolean autoDelete = false;
		channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
		// Cada mensaje recibido (Tarea o Sondeo) creara una tarea en el sistema
		// Las tareas pendientes de un alumno podran ser consultadas con TareasByUser
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			tareaRepository.saveTarea(parseTarea(new String(delivery.getBody(), "UTF-8")));
		};
		channel.basicConsume(queue, true, deliverCallback, consumerTag -> {
		});
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
		try {
			initDB();
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException
				| TimeoutException e) {
			e.printStackTrace();
		}
		return SchemaParser.newParser().file("schema.graphqls")
				.resolvers(new Query(tareaRepository), new Mutation(tareaRepository)).build().makeExecutableSchema();
	}

	// Supporting methods
	private static Tarea parseTarea(String s) {
		List<String> fields = Arrays.stream(s.split(";")).map(String::intern).collect(Collectors.toList());
		return new Tarea(fields.get(0), Arrays.asList(fields.get(1).replace("[", "").replace("]", "")), fields.get(2),
				fields.get(3));
	}

}
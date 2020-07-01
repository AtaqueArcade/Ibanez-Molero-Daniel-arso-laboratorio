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
	public static final String RABBITURI = "amqp://otbzkwgy:MUMAlBAj4iqa0y5ZX63cfDYX1hs7u00u@chinook.rmq.cloudamqp.com/otbzkwgy";
	private static TareaRepository tareaRepository;
	private static MongoClient client;

	private static void initDB()
			throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
		MongoClient client = MongoClients.create(
				"mongodb+srv://ataquearcade:huWAN4jGusPRjqV@arso-vaorn.mongodb.net/test?retryWrites=true&w=majority");
		MongoDatabase database = client.getDatabase("ArSo");
		tareaRepository = new TareaRepository(database.getCollection("tareas"));

		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri(RABBITURI);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		String queue = "sondeos";
		boolean durable = false; // durable - RabbitMQ will never lose the queue if a crash occurs
		boolean exclusive = false; // exclusive - if queue only will be used by one connection
		boolean autoDelete = false; // autodelete - queue is deleted when last consumer unsubscribes
		channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
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
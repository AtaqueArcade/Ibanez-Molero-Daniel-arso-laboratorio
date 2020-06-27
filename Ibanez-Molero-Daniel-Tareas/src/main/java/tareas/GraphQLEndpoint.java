package tareas;

import javax.servlet.annotation.WebServlet;
import com.coxautodev.graphql.tools.SchemaParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import graphql.schema.GraphQLSchema;
import graphql.servlet.SimpleGraphQLServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/graphql")
public class GraphQLEndpoint extends SimpleGraphQLServlet {

	private static TareaRepository tareaRepository;
	private static UserRepository userRepository;
	private static MongoClient client;

	private static void initDB() {
		MongoClient client = MongoClients.create(
				"mongodb+srv://ataquearcade:cNyajG9zxdHm2RE@arso-vaorn.mongodb.net/test?retryWrites=true&w=majority");
		MongoDatabase database = client.getDatabase("ArSo");
		tareaRepository = new TareaRepository(database.getCollection("tareas"));
		userRepository = new UserRepository(database.getCollection("usuarios"));
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
		return SchemaParser
				.newParser().file("schema.graphqls").resolvers(new Query(tareaRepository, userRepository),
						new Mutation(tareaRepository, userRepository), new TareaResolver(userRepository))
				.build().makeExecutableSchema();
	}
}
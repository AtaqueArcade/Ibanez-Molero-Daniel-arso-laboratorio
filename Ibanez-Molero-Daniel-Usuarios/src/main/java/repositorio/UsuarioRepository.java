package repositorio;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class UsuarioRepository {
	private static UsuarioRepository instance;
	private MongoCollection<Document> usuarios;

	public static UsuarioRepository getInstance() {
		if (instance == null)
			instance = new UsuarioRepository();
		return instance;

	}

	private UsuarioRepository() {
		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://ataquearcade:huWAN4jGusPRjqV@arso-vaorn.mongodb.net/ArSo?retryWrites=true&w=majority");
		MongoDatabase database = mongoClient.getDatabase("ArSo");
		this.usuarios = database.getCollection("usuarios");
	}

	public String saveUsuario(String correo, String nombre, String rol) {
		Document doc = new Document();
		doc.append("correo", correo);
		doc.append("nombre", nombre);
		doc.append("rol", rol);
		usuarios.insertOne(doc);
		return ((ObjectId) doc.get("_id")).toHexString();
	}

	public JsonObject getUsuario(String correo) {
		Document doc = usuarios.find(Filters.eq("correo", correo)).first();
		if (doc == null)
			return null;
		JsonObject usuario = Json.createObjectBuilder().add("correo", doc.getString("correo"))
				.add("nombre", doc.getString("nombre")).add("rol", doc.getString("rol")).build();
		return usuario;
	}

	public JsonArray getAllByRol(String rol) {
		JsonArrayBuilder result = Json.createArrayBuilder();
		FindIterable<Document> query = usuarios.find(Filters.eq("rol", rol));
		for (Document doc : query) {
			result.add(doc.getString("correo"));
		}
		return result.build();
	}
}
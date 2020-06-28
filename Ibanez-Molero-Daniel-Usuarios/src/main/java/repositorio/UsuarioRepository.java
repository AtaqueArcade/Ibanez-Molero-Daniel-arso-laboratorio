package repositorio;

import java.util.LinkedList;
import java.util.List;
import javax.json.Json;
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
				"mongodb+srv://ataquearcade:cNyajG9zxdHm2RE@arso-vaorn.mongodb.net/test?retryWrites=true&w=majority");
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
		JsonObject usuario = Json.createObjectBuilder().add("correo", doc.getString("correo"))
				.add("nombre", doc.getString("nombre")).add("rol", doc.getString("rol")).build();
		return usuario;
	}

	public List<JsonObject> getAllByRol(String rol) {
		LinkedList<JsonObject> result = new LinkedList<>();
		FindIterable<Document> query = usuarios.find(Filters.eq("rol", rol));
		for (Document doc : query) {
			JsonObject usuario = Json.createObjectBuilder().add("correo", doc.getString("correo"))
					.add("nombre", doc.getString("nombre")).add("rol", doc.getString("rol")).build();
			result.add(usuario);
		}
		return result;
	}
}
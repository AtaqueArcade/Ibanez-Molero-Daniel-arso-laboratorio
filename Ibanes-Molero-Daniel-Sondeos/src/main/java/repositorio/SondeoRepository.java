package repositorio;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import tipos.Entrada;

public class SondeoRepository {
	private static SondeoRepository instance;
	private MongoCollection<Document> sondeos;

	public static SondeoRepository getInstance() {
		if (instance == null)
			instance = new SondeoRepository();
		return instance;
	}

	private SondeoRepository() {
		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://ataquearcade:huWAN4jGusPRjqV@arso-vaorn.mongodb.net/test?retryWrites=true&w=majority");
		MongoDatabase database = mongoClient.getDatabase("ArSo");
		this.sondeos = database.getCollection("sondeos");
	}

	public String saveSondeo(String usuario, String pregunta, List<String> respuestas, String instrucciones,
			LocalDateTime apertura, LocalDateTime cierre, int maxSeleccion, int minSeleccion, String visibilidad) {
		Document doc = new Document();
		doc.append("final", false);
		doc.append("correo", usuario);
		doc.append("pregunta", pregunta);
		doc.append("instrucciones", instrucciones);
		doc.append("apertura", apertura.toString());
		doc.append("cierre", cierre.toString());
		doc.append("maxSeleccion", maxSeleccion);
		doc.append("minSeleccion", minSeleccion);
		doc.append("visibilidad", visibilidad);
		doc.append("entradas", new LinkedList<String>());
		sondeos.insertOne(doc);
		updateRespuestas(doc.get("_id").toString(), respuestas);
		return ((ObjectId) doc.get("_id")).toHexString();
	}

	public boolean updateRespuestas(String id, List<String> respuestas) {
		sondeos.updateOne(Filters.eq("_id", new ObjectId(id)), Updates.set("respuestas", new LinkedList<String>()));
		for (String r : respuestas) {
			DBObject listItem = new BasicDBObject("respuestas", r);
			sondeos.updateOne(Filters.eq("_id", new ObjectId(id)), new Document().append("$push", listItem));
		}
		return true;
	}

	public boolean addEntrada(String id, Entrada e) {
		DBObject listItem = new BasicDBObject("entradas", new BasicDBObject(e.getCorreo(), e.getSeleccion()));
		return sondeos.updateOne(Filters.eq("_id", new ObjectId(id)), new Document().append("$push", listItem))
				.wasAcknowledged();
	}

	public boolean makeFinal(String id) {
		return sondeos.updateOne(Filters.eq("_id", new ObjectId(id)), Updates.set("final", true)).wasAcknowledged();
	}

	@SuppressWarnings("unchecked")
	public JsonObject getSondeo(String id) {
		Document doc = sondeos.find(Filters.eq("_id", new ObjectId(id))).first();
		JsonArrayBuilder respuestas = Json.createArrayBuilder();
		((List<String>) doc.get("respuestas")).forEach(respuesta -> respuestas.add(respuesta));
		JsonObject sondeo = Json.createObjectBuilder().add("final", doc.getBoolean("final").toString())
				.add("correo", doc.getString("correo")).add("pregunta", doc.getString("pregunta"))
				.add("respuestas", respuestas.build()).add("instrucciones", doc.getString("instrucciones"))
				.add("apertura", doc.getString("apertura")).add("cierre", doc.getString("cierre"))
				.add("maxSeleccion", doc.getInteger("maxSeleccion")).add("minSeleccion", doc.getInteger("minSeleccion"))
				.add("visibilidad", doc.getString("visibilidad")).build();
		return sondeo;
	}

	@SuppressWarnings("unchecked")
	public JsonObject getEntradasSondeo(String id) {
		JsonObjectBuilder result = Json.createObjectBuilder();
		Document doc = sondeos.find(Filters.eq("_id", new ObjectId(id))).first();
		JsonArrayBuilder entradas = Json.createArrayBuilder();
		((List<Document>) doc.get("entradas")).forEach(entrada -> entradas.add(entrada.toString()));
		return result.add("entradas", entradas.build()).build();
	}

	public boolean removeSondeo(String id) {
		return sondeos.deleteOne(Filters.eq("_id", new ObjectId(id))).getDeletedCount() > 0;
	}

	public void resetSondeos() {
		sondeos.deleteMany(new Document());
	}
}
package repositorio;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

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
				"mongodb+srv://ataquearcade:cNyajG9zxdHm2RE@arso-vaorn.mongodb.net/test?retryWrites=true&w=majority");
		MongoDatabase database = mongoClient.getDatabase("ArSo");
		this.sondeos = database.getCollection("sondeos");
	}

	public String saveSondeo(String pregunta, List<String> respuestas, String instrucciones, LocalDateTime apertura,
			LocalDateTime cierre, int maxSeleccion, int minSeleccion, String visibilidad) {
		Document doc = new Document();
		doc.append("pregunta", pregunta);
		doc.append("respuestas", respuestas.toString());
		doc.append("instrucciones", instrucciones);
		doc.append("apertura", apertura.toString());
		doc.append("cierre", cierre.toString());
		doc.append("maxSeleccion", maxSeleccion);
		doc.append("minSeleccion", minSeleccion);
		doc.append("visibilidad", visibilidad);
		sondeos.insertOne(doc);
		return ((ObjectId) doc.get("_id")).toHexString();
	}

	public boolean updateSondeo(String id, List<String> respuestas) {
		Document doc = sondeos.find(Filters.eq("_id", new ObjectId(id))).first();
		if (doc != null)
			return (doc.replace("respuestas", respuestas) != null);
		return false;
	}

	public JsonObject getSondeo(String id) {
		Document doc = sondeos.find(Filters.eq("_id", new ObjectId(id))).first();
		JsonArrayBuilder respuestas = Json.createArrayBuilder();
		parseRespuestas(doc.getString("respuestas")).forEach(respuesta -> respuestas.add(respuesta));

		JsonObject sondeo = Json.createObjectBuilder().add("pregunta", doc.getString("pregunta"))
				.add("respuestas", respuestas.build()).add("instrucciones", doc.getString("instrucciones"))
				.add("apertura", doc.getString("apertura")).add("cierre", doc.getString("cierre"))
				.add("maxSeleccion", doc.getInteger("maxSeleccion")).add("minSeleccion", doc.getInteger("minSeleccion"))
				.add("visibilidad", doc.getString("visibilidad")).build();
		return sondeo;
	}

	public boolean removeSondeo(String id) {
		return sondeos.deleteOne(Filters.eq("_id", new ObjectId(id))).getDeletedCount() > 0;
	}

	public void resetSondeos() {
		sondeos.deleteMany(new Document());
	}

	private List<String> parseRespuestas(String s) {
		List<String> list = Arrays.asList(s.substring(1, s.length() - 1).split(", "));
		return list;
	}
}
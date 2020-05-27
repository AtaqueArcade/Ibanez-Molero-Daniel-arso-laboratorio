package repositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import controlador.Utils;
import tipos.TipoSondeo;

public class SondeoRepository {
	private static SondeoRepository instance;
	private MongoCollection<Document> sondeos;

	public static SondeoRepository getInstance() {
		if (instance == null)
			instance = new SondeoRepository();
		return instance;

	}

	private SondeoRepository() {
		initDB();
	}

	private void initDB() {
		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://ataquearcade:cNyajG9zxdHm2RE@arso-vaorn.mongodb.net/test?retryWrites=true&w=majority");
		MongoDatabase database = mongoClient.getDatabase("ArSo");
		this.sondeos = database.getCollection("sondeos");

	}

	public TipoSondeo saveSondeo(TipoSondeo sondeo) {
		Document doc = new Document();
		doc.append("pregunta", sondeo.getPregunta());
		doc.append("respuestas", sondeo.getRespuesta());
		doc.append("urlPublicacion", sondeo.getApretura());
		doc.append("userEmail", sondeo.getCierre());
		sondeos.insertOne(doc);
		return sondeo(doc);
	}

	public void resetSondeos() {
		sondeos.deleteMany(new Document());
	}

	public List<TipoSondeo> getAllSondeos() {
		List<TipoSondeo> allSondeos = new ArrayList<>();
		for (Document doc : sondeos.find()) {
			allSondeos.add(sondeo(doc));
		}
		return allSondeos;
	}

	public TipoSondeo findById(String id) {
		Document doc = sondeos.find(Filters.eq("_id", new ObjectId(id))).first();
		return sondeo(doc);
	}

	private TipoSondeo sondeo(Document doc) {
		TipoSondeo sondeo = new TipoSondeo();
		sondeo.setPregunta(doc.getString("pregunta"));
		sondeo.getRespuesta().addAll(parseRespuestas(doc.getString("respuestas")));
		sondeo.setApretura(Utils.createFecha(parseFecha(doc.getString("apertura"))));
		sondeo.setCierre(Utils.createFecha(parseFecha(doc.getString("cierre"))));
		return sondeo;
	}

	private LocalDateTime parseFecha(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<String> parseRespuestas(String string) {
		// TODO Auto-generated method stub
		return null;
	}
}

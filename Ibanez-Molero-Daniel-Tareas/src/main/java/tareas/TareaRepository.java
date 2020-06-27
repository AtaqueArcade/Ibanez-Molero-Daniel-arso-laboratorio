package tareas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class TareaRepository {
	private final MongoCollection<Document> tareas;

	public TareaRepository(MongoCollection<Document> tareas) {
		this.tareas = tareas;
	}

	public List<Tarea> getAllTareas() {
		List<Tarea> allTareas = new ArrayList<>();
		for (Document doc : tareas.find()) {
			allTareas.add(tarea(doc));
		}
		return allTareas;
	}

	public List<Tarea> getTareasReceptor(String receptor) {
		List<Tarea> tareasReceptor = new ArrayList<>();
		FindIterable<Document> doc = tareas.find();
		for (Document docu : doc) {
			if (parseStringToReceptores(docu.getString("receptores")).contains(receptor))
				tareasReceptor.add(tarea(docu));
		}
		return tareasReceptor;
	}

	public List<Tarea> getTareasAutor(String autor) {
		List<Tarea> tareasAutor = new ArrayList<>();
		FindIterable<Document> doc = tareas.find(Filters.eq("autor", new String(autor)));
		for (Document docu : doc) {
			tareasAutor.add(tarea(docu));
		}
		return tareasAutor;
	}

	public Tarea findById(String id) {
		Document doc = tareas.find(Filters.eq("_id", new ObjectId(id))).first();
		return tarea(doc);
	}

	private Tarea tarea(Document doc) {
		return new Tarea(doc.get("_id").toString(), doc.getString("autor"),
				parseStringToReceptores(doc.getString("receptores")), doc.getString("titulo"),
				doc.getString("contenido"));
	}

	public Tarea saveTarea(Tarea tarea) {
		Document doc = new Document();
		doc.append("autor", tarea.getAutor());
		doc.append("receptores", parseReceptoresToString(tarea.getReceptores()));
		doc.append("titulo", tarea.getTitulo());
		doc.append("contenido", tarea.getContenido());
		tareas.insertOne(doc);
		return tarea(doc);

	}

	public void resetTareas() {
		tareas.deleteMany(new Document());
	}

	// Supporting methods
	private List<String> parseStringToReceptores(String rcpString) {
		List<String> result = null;
		if ((rcpString != null) && !rcpString.equals("")) {
			result = Arrays.stream(rcpString.split("|")).map(String::intern).collect(Collectors.toList());
		}
		return result;
	}

	private String parseReceptoresToString(List<String> receptores) {
		String result = String.join("|", receptores);
		return result;
	}
}

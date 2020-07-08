package tareas;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class TareaRepository {
	private final MongoCollection<Document> tareas;

	public TareaRepository(MongoCollection<Document> tareas) {
		this.tareas = tareas;
	}

	public Tarea saveTarea(Tarea tarea) {
		Document doc = new Document();
		doc.append("tipo", tarea.getTipo());
		doc.append("tareaId", tarea.getTareaId());
		doc.append("autor", tarea.getAutor());
		doc.append("fecha", tarea.getFecha());
		tareas.insertOne(doc);
		updateReceptores(doc.get("_id").toString(), tarea.getReceptores());
		return tarea(doc);
	}

	public void updateReceptores(String id, List<String> receptores) {
		// Si no quedan receptores de la tarea, esta se elimina del sistema
		if (receptores.size() == 0) {
			removeTarea(id);
			return;
		}
		tareas.updateOne(Filters.eq("_id", new ObjectId(id)), Updates.set("receptores", new LinkedList<String>()));
		for (String r : receptores) {
			DBObject listItem = new BasicDBObject("receptores", r);
			tareas.updateOne(Filters.eq("_id", new ObjectId(id)), new Document().append("$push", listItem));
		}
	}

	public boolean removeTarea(String id) {
		return tareas.deleteOne(Filters.eq("_id", new ObjectId(id))).getDeletedCount() > 0;
	}

	public List<Tarea> getAllTareas() {
		List<Tarea> allTareas = new ArrayList<>();
		for (Document doc : tareas.find()) {
			allTareas.add(tarea(doc));
		}
		return allTareas;
	}

	@SuppressWarnings("unchecked")
	public List<Tarea> getTareasReceptor(String receptor) {
		List<Tarea> tareasReceptor = new ArrayList<>();
		FindIterable<Document> doc = tareas.find();
		for (Document docu : doc) {
			LinkedList<String> receptores = new LinkedList<String>();
			((List<String>) docu.get("receptores")).forEach(r -> receptores.add(r));
			if (receptores.contains(receptor))
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

	// Supporting methods
	@SuppressWarnings("unchecked")
	private Tarea tarea(Document doc) {
		LinkedList<String> receptores = new LinkedList<String>();
		((List<String>) doc.get("receptores")).forEach(r -> receptores.add(r));
		return new Tarea(doc.get("_id").toString(), doc.getString("tipo"), doc.getString("tareaId"),
				doc.getString("autor"), receptores, doc.getString("fecha"));
	}
}

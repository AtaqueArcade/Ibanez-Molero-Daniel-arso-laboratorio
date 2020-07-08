package tareas;

import java.util.List;
import com.coxautodev.graphql.tools.GraphQLRootResolver;

public class Mutation implements GraphQLRootResolver {

	private final TareaRepository tareaRepository;

	public Mutation(TareaRepository tareaRepository) {
		this.tareaRepository = tareaRepository;
	}

	public Tarea createTarea(String tipo, String tareaId, String autor, List<String> receptores, String fecha) {
		Tarea tarea = new Tarea(tipo, tareaId, autor, receptores, fecha);
		return tareaRepository.saveTarea(tarea);
	}

	public boolean deleteTarea(String id) {
		return tareaRepository.removeTarea(id);
	}
}
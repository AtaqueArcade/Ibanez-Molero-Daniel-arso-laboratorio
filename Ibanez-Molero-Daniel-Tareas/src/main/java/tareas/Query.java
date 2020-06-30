package tareas;

import java.util.List;
import com.coxautodev.graphql.tools.GraphQLRootResolver;

public class Query implements GraphQLRootResolver {
	private final TareaRepository tareaRepository;

	public Query(TareaRepository tareaRepository) {
		this.tareaRepository = tareaRepository;
	}

	public List<Tarea> allTareas() {
		return tareaRepository.getAllTareas();
	}

	public List<Tarea> tareasByUser(String user) {
		return tareaRepository.getTareasReceptor(user);
	}

	public List<Tarea> tareasByAutor(String user) {
		return tareaRepository.getTareasAutor(user);
	}
}
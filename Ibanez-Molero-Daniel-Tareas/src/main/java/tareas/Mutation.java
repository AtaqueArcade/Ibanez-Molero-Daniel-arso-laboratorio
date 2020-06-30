package tareas;

import java.util.List;
import com.coxautodev.graphql.tools.GraphQLRootResolver;

public class Mutation implements GraphQLRootResolver {

	private final TareaRepository tareaRepository;

	public Mutation(TareaRepository tareaRepository) {
		this.tareaRepository = tareaRepository;
	}

	public Tarea createTarea(String autor, List<String> receptores, String titulo, String contenido) {
		Tarea tarea = new Tarea(autor, receptores, titulo, contenido);
		return tareaRepository.saveTarea(tarea);

	}
}
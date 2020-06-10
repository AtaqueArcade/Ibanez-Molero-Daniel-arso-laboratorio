package tareas;

import java.util.List;
import com.coxautodev.graphql.tools.GraphQLRootResolver;

public class Query implements GraphQLRootResolver {
	private final TareaRepository teareaRepository;
	private final UserRepository userRepository;

	public Query(TareaRepository tareaRepository, UserRepository userRepository) {
		this.teareaRepository = tareaRepository;
		this.userRepository = userRepository;
	}

	public List<Tarea> allTareas() {
		return teareaRepository.getAllTareas();
	}

	public List<User> allUsers() {
		return userRepository.getAllUsers();
	}

	public List<Tarea> tareasByUser(String user) {
		return teareaRepository.getTareasReceptor(user);
	}

	public List<Tarea> tareasByAutor(String user) {
		return teareaRepository.getTareasAutor(user);
	}
}

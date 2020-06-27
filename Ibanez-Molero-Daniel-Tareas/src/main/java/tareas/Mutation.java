package tareas;

import java.util.List;
import com.coxautodev.graphql.tools.GraphQLRootResolver;

public class Mutation implements GraphQLRootResolver {

	private final TareaRepository tareaRepository;
	private final UserRepository userRepository;

	public Mutation(TareaRepository tareaRepository, UserRepository userRepository) {
		this.tareaRepository = tareaRepository;
		this.userRepository = userRepository;
	}

	public Tarea createTarea(String autor, List<String> receptores, String titulo, String contenido) {
		Tarea tarea = new Tarea(autor, receptores, titulo, contenido);
		return tareaRepository.saveTarea(tarea);

	}

	public User createUser(String nombre, String email) {
		User newUser = new User(nombre, email);
		return userRepository.saveUser(newUser);
	}

	public User resetRepositories() {
		tareaRepository.resetTareas();
		userRepository.resetUsers();
		User newUser = new User(null, null);
		return (newUser);
	}
}
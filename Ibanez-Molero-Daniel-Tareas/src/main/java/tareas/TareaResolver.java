package tareas;

import com.coxautodev.graphql.tools.GraphQLResolver;

public class TareaResolver implements GraphQLResolver<Tarea> {

	private final UserRepository userRepository;

	public TareaResolver(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User postedBy(Tarea tarea) {
		return userRepository.findByEmail(tarea.getAutor());
	}

}

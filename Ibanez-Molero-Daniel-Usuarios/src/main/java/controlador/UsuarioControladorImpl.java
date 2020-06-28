package controlador;

import java.util.List;
import javax.json.JsonObject;
import repositorio.UsuarioRepository;

public class UsuarioControladorImpl implements UsuarioControlador {
	private static UsuarioControlador controlador;
	private UsuarioRepository repositorio;

	public static UsuarioControlador getInstance() {
		if (controlador == null) {
			controlador = new UsuarioControladorImpl();
		}
		return controlador;
	}

	private UsuarioControladorImpl() {
		repositorio = UsuarioRepository.getInstance();
	}

	@Override
	public String createUsuario(String correo, String nombre, String rol) {
		if (correo == null || correo.equals(""))
			throw new IllegalArgumentException("El correo del usuario no puede ser nulo o vacio");
		if (nombre == null || nombre.equals(""))
			throw new IllegalArgumentException("El nombre del usuario no pueden ser nulo o vacio");
		if (rol == null || (rol.equals("estudiante") || rol.equals("profesor")))
			throw new IllegalArgumentException("El rol ha de ser 'estudiante' o 'profesor'");

		return repositorio.saveUsuario(correo, nombre, rol);
	}

	@Override
	public JsonObject getUsuario(String correo) {
		return repositorio.getUsuario(correo);
	}

	@Override
	public String getRol(String correo) {
		if (correo == null || correo.equals(""))
			throw new IllegalArgumentException("El correo del usuario no puede ser nulo o vacio");
		JsonObject result = repositorio.getUsuario(correo);
		if (result != null)
			result.get("rol");
		return null;
	}

	@Override
	public List<JsonObject> getAllEstudiantes() {
		return repositorio.getAllByRol("estudiante");
	}

	@Override
	public List<JsonObject> getAllTeachers() {
		return repositorio.getAllByRol("profesor");
	}
}
package controlador;

import java.util.List;
import javax.json.JsonObject;

public interface UsuarioControlador {
	public String createUsuario(String correo, String nombre, String rol);

	public JsonObject getUsuario(String correo);

	public String getRol(String correo);

	public List<JsonObject> getAllEstudiantes();

	public List<JsonObject> getAllTeachers();
}

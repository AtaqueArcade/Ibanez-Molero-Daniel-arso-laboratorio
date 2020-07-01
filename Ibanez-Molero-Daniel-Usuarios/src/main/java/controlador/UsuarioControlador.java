package controlador;

import javax.json.JsonArray;
import javax.json.JsonObject;

public interface UsuarioControlador {
	public String createUsuario(String correo, String nombre, String rol);

	public JsonObject getUsuario(String correo);

	public String getRol(String correo);

	public JsonArray getAllEstudiantes();

	public JsonArray getAllProfesores();
}
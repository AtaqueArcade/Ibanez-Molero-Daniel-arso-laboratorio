package controlador;

import javax.json.JsonArray;
import javax.json.JsonObject;

public interface UsuarioControlador {
	// Crear usuario en la base de datos
	public String createUsuario(String correo, String nombre, String rol);

	// Recuperar usuario como Json de la base de datos
	public JsonObject getUsuario(String correo);

	// Recuperar el usuario. Salidas "estudiante" o "profesor
	public String getRol(String correo);

	// Recuperar todos los correos de usuarios con rol "estudiante" en JsonArray
	public JsonArray getAllEstudiantes();

	// Recuperar todos los correos de usuarios con rol "profesor" en JsonArray
	public JsonArray getAllProfesores();
}

package controlador;

import java.util.List;
import javax.json.JsonObject;

import rest.SondeoException;

public interface SondeoControlador {
	
	// Crea un sondeo para todos los alumnos disponibles
	String createSondeo(String usuario, String pregunta, List<String> respuestas, String instrucciones,
			String sApertura, String sCierre, int minSeleccion, int maxSeleccion, String visibilidad)
			throws SondeoException;

	// Anade respuestas posibles al sondeo
	boolean updateRespuestas(String id, List<String> respuestas);

	// Anade las respuestas emitidas por un alumno
	boolean addEntrada(String id, String correo, String contenido) throws SondeoException;

	// Recupera un sondeo en base a su Id
	public JsonObject getSondeo(String id);

	// Elimina un sondeo en base a su Id
	public boolean removeSondeo(String id);
}

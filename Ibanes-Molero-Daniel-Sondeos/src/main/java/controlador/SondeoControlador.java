package controlador;

import java.util.List;
import javax.json.JsonObject;

import rest.SondeoException;

public interface SondeoControlador {

	// Crea un sondeo para todos los alumnos disponibles
	String createSondeo(String correo, String pregunta, List<String> respuestas, String instrucciones, String sApertura,
			String sCierre, int minSeleccion, int maxSeleccion, String visibilidad) throws SondeoException;

	// Anade respuestas posibles al sondeo
	boolean updateRespuestas(String id, String correo, List<String> respuestas) throws SondeoException;

	// Publica un sondeo a los receptores
	void confirmSondeo(String id, String correo) throws SondeoException;

	// Anade las respuestas emitidas por un alumno
	boolean addEntrada(String id, String correo, List<String> contenido) throws SondeoException;

	// Recupera un sondeo en base a su Id
	public JsonObject getSondeo(String id);

	// Recupera todas las contestaciones de los alumnos en un sondeo
	public JsonObject getEntradas(String id, String correo) throws SondeoException;

	// Elimina un sondeo en base a su Id
	public boolean removeSondeo(String id, String correo) throws SondeoException;;
}

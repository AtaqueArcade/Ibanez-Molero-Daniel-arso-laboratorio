package controlador;

import java.util.List;
import javax.json.JsonObject;

import rest.SondeoException;

public interface SondeoControlador {

	String createSondeo(String usuario, String pregunta, List<String> respuestas, String instrucciones,
			String sApertura, String sCierre, int minSeleccion, int maxSeleccion, String visibilidad)
			throws SondeoException;

	boolean updateRespuestas(String id, List<String> respuestas);

	boolean addEntrada(String id, String correo, String contenido) throws SondeoException;

	public JsonObject getSondeo(String id);

	public boolean removeSondeo(String id);
}

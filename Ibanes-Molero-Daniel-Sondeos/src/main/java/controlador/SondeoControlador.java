package controlador;

import java.util.List;
import javax.json.JsonObject;

import rest.SondeoException;

public interface SondeoControlador {

	public String createSondeo(String pregunta, List<String> respuestas, String instrucciones, String apertura,
			String cierre, int minSeleccion, int maxSeleccion, String visibilidad) throws SondeoException;

	public boolean updateSondeo(String id, List<String> respuestas);

	public JsonObject getSondeo(String id);

	public boolean removeSondeo(String id);
}

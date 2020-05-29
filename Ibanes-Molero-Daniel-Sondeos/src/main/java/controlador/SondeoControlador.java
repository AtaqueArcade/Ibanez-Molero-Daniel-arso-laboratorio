package controlador;

import java.time.LocalDateTime;
import java.util.List;
import javax.json.JsonObject;

public interface SondeoControlador {

	public String createSondeo(String pregunta, List<String> respuestas, String instrucciones, LocalDateTime apertura,
			LocalDateTime cierre, int minSeleccion, int maxSeleccion, String visibilidad);

	public void updateSondeo(String id, List<String> respuestas);

	public JsonObject getSondeo(String id);

	public boolean removeSondeo(String id);
}

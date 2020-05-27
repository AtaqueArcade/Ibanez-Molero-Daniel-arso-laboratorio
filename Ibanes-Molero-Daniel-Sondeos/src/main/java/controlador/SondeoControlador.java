package controlador;

import java.time.LocalDateTime;
import java.util.List;
import tipos.TipoSondeo;

public interface SondeoControlador {

	String createSondeo(String pregunta, List<String> respuesta, LocalDateTime apertura, LocalDateTime cierre);

	void updateSondeo(TipoSondeo sondeo, int minSeleccion, int maxSeleccion, String visibilidad);

	TipoSondeo getSondeo(String id);

	boolean removeSondeo(String id);

}

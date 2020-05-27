package controlador;

import java.time.LocalDateTime;
import java.util.List;

import tipos.TipoSondeo;

public class SondeoControladorImpl implements SondeoControlador {
	private static SondeoControladorImpl controlador = null;

	public static SondeoControladorImpl getControlador() {
		if (controlador == null) {
			controlador = new SondeoControladorImpl();
		}
		return controlador;
	}

	@Override
	public String createSondeo(String pregunta, List<String> respuesta, LocalDateTime apertura, LocalDateTime cierre) {
		TipoSondeo sondeo = new TipoSondeo();
		String id = Utils.createId();
		sondeo.setId(id);
		sondeo.setPregunta(pregunta);
		sondeo.getRespuesta().addAll(respuesta);
		sondeo.setApretura(Utils.createFecha(apertura));
		sondeo.setCierre(Utils.createFecha(cierre));
		return id;
	}

	@Override
	public void updateSondeo(TipoSondeo sondeo, int minSeleccion, int maxSeleccion, String visibilidad) {
		sondeo.setMinSeleccion(minSeleccion);
		sondeo.setMaxSeleccion(maxSeleccion);
		sondeo.setVisibilidad(visibilidad);
	}

	@Override
	public TipoSondeo getSondeo(String id) {
		return null;

	}

	@Override
	public boolean removeSondeo(String id) {
		// TODO Auto-generated method stub
		return false;
	}
}

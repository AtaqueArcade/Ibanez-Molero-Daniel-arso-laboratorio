package tareas;

import java.util.List;

public class Tarea {
	private String id;
	private final String tipo;
	private final String tareaId;
	private final String autor;
	private List<String> receptores;
	private final String fecha;

	public Tarea(String tipo, String tareaId, String autor, List<String> receptores, String fecha) {
		this.tipo = tipo;
		this.tareaId = tareaId;
		this.autor = autor;
		this.receptores = receptores;
		this.fecha = fecha;
	}

	public String getId() {
		return id;
	}

	public String getTipo() {
		return tipo;
	}

	public String getTareaId() {
		return tareaId;
	}

	public String getAutor() {
		return autor;
	}

	public List<String> getReceptores() {
		return receptores;
	}

	public String getFecha() {
		return fecha;
	}

	public Tarea(String id, String tipo, String tareaId, String autor, List<String> receptores, String fecha) {
		this(tipo, tareaId, autor, receptores, fecha);
		this.id = id;
	}

}

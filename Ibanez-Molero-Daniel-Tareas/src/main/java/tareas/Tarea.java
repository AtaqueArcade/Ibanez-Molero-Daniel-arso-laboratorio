package tareas;

import java.util.List;

public class Tarea {
	private String id;
	private final String autor;
	private List<String> receptores;
	private final String titulo;
	private final String contenido;

	public Tarea(String autor, List<String> receptores, String titulo, String contenido) {
		this.autor = autor;
		this.receptores = receptores;
		this.titulo = titulo;
		this.contenido = contenido;
	}

	public Tarea(String id, String autor, List<String> receptores, String titulo, String contenido) {
		this.id = id;
		this.autor = autor;
		this.receptores = receptores;
		this.titulo = titulo;
		this.contenido = contenido;
	}

	public String getId() {
		return id;
	}

	public String getAutor() {
		return autor;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getContenido() {
		return contenido;
	}

	public List<String> getReceptores() {
		return receptores;
	}
}

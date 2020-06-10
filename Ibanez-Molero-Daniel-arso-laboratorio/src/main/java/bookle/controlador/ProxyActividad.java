package bookle.controlador;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import bookle.tipos.Actividad;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProxyActividad {
	// private Actividad actividad;
	private String url;
	private String titulo;

	public ProxyActividad(String url, String titulo) {
		this.url = url;
		this.titulo = titulo;
	}

	public String getUrl() {
		return url;
	}

	public String getTitulo() {
		return titulo;
	}
}
package bookle.controlador;

import java.util.LinkedList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "actividades")
public class ListadoActividades {
	private LinkedList<ProxyActividad> actividad;

	public ListadoActividades() {
		this.actividad = new LinkedList<>();
	}

	public LinkedList<ProxyActividad> getActividad() {
		return actividad;
	}
}
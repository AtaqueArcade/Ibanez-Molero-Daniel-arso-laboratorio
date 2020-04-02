package bookle.controlador;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.LinkedList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import bookle.tipos.Actividad;
import bookle.tipos.DiaActividad;
import bookle.tipos.Reserva;
import bookle.tipos.Turno;

public class BookleControladorImpl implements BookleControlador {
	
	public final static String DIRECTORIO_ACTIVIDADES = "actividades/";
	
	static {
		
		File directorio = new File(DIRECTORIO_ACTIVIDADES);
		
		if (! directorio.exists())
			directorio.mkdir();
	}
	
	/*** Métodos de apoyo ***/

	protected String getDocumento(String id) {
		
		return DIRECTORIO_ACTIVIDADES + id + ".xml";
	}
	
	protected void guardar(Actividad actividad) throws BookleException {
		
		final String documento = getDocumento(actividad.getId());

		final File fichero = new File(documento);
		
		try {

			JAXBContext contexto = JAXBContext.newInstance("bookle.tipos");
			Marshaller marshaller = contexto.createMarshaller();

			marshaller.setProperty("jaxb.formatted.output", true);

			marshaller.marshal(actividad, fichero);

		} catch (Exception e) {
			
			throw new BookleException("Error al guardar la actividad con id: " + actividad.getId(), e);
		}
	}
	
	protected Actividad cargar(String id) throws BookleException {

		final String documento = getDocumento(id);

		try {

			JAXBContext contexto = JAXBContext.newInstance("bookle.tipos");
			Unmarshaller unmarshaller = contexto.createUnmarshaller();

			return (Actividad) unmarshaller.unmarshal(new File(documento));

		} catch (Exception e) {
			throw new BookleException("Error al cargar la actividad con id: " + id, e);
		}		
	}
	
	protected DiaActividad getDia(Actividad actividad, Date fecha) throws BookleException {

		DiaActividad diaActividad = null;

		XMLGregorianCalendar fechaXML = Utils.createFecha(fecha);

		for (DiaActividad dia : actividad.getAgenda()) {

			if (dia.getFecha().equals(fechaXML)) {

				diaActividad = dia;
				break;
			}
		}
		
		return diaActividad;
		
	}
	
	protected Turno getTurno(Actividad actividad, Date fecha, int indice) throws BookleException {

		DiaActividad dia = getDia(actividad, fecha);

		if (dia == null)
			throw new BookleException("La fecha no está en la agenda: " + fecha);
			
		if (indice > dia.getTurno().size())
			throw new BookleException("No existe el turno " + indice + " para la fecha " + fecha);
		
		return dia.getTurno().get(indice - 1);
	}
	
	/*** Fin métodos de apoyo ***/
	
	
	@Override
	public String createActividad(String titulo, String descripcion,
			String profesor, String email) throws BookleException {
		
		Actividad actividad = new Actividad();

		String id = Utils.createId();

		actividad.setId(id);
		actividad.setTitulo(titulo);
		actividad.setDescripcion(descripcion);
		actividad.setProfesor(profesor);
		actividad.setEmail(email);

		guardar(actividad);
				
		return actividad.getId();
	}

	@Override
	public void updateActividad(String id, String titulo, String descripcion, String profesor, String email) throws BookleException {
		
		Actividad actividad = cargar(id);
		
		actividad.setTitulo(titulo);
		actividad.setDescripcion(descripcion);
		actividad.setProfesor(profesor);
		actividad.setEmail(email);
		
		guardar(actividad);
	}
	
	@Override
	public Actividad getActividad(String id) throws BookleException {
		
		return cargar(id);
	}

	@Override
	public boolean removeActividad(String id) throws BookleException {
		
		final String documento = getDocumento(id);

		File fichero = new File(documento);
		
		return fichero.delete();
	}

	@Override
	public void addDiaActividad(String id, Date fecha, int turnos)
			throws BookleException {
		
		// precondición
		if (turnos <= 0)
			throw new BookleException(
					"El número de turnos debe ser mayor o igual que 1");

		Actividad actividad = cargar(id);

		// precondición		
		if (getDia(actividad, fecha) != null)
			throw new BookleException(
					"La fecha establecida ya está en la agenda: " + fecha);
		
		DiaActividad dia = new DiaActividad();
		
		XMLGregorianCalendar fechaXML = Utils.createFecha(fecha);

		dia.setFecha(fechaXML);

		// Crea los slots vacíos

		for (int i = 0; i < turnos; i++) {

			Turno turno = new Turno();
			turno.setHorario(""); // sin especificar
			dia.getTurno().add(turno);
		}

		actividad.getAgenda().add(dia);

		guardar(actividad);		
	}

	@Override
	public boolean removeDiaActividad(String id, Date fecha)
			throws BookleException {
				
		Actividad actividad = cargar(id);
		
		DiaActividad diaEliminar = getDia(actividad, fecha);

		if (diaEliminar != null) {	
			
			actividad.getAgenda().remove(diaEliminar);
			guardar(actividad);
			
			return true;
		}
		else return false;
	}
	
	
	@Override
	public int addTurnoActividad(String id, Date fecha) throws BookleException {
		
		Actividad actividad = cargar(id);

		DiaActividad dia = getDia(actividad, fecha);
		
		if (dia == null)
			throw new BookleException(
					"La fecha establecida no está en la agenda: " + fecha);
		
		Turno turno = new Turno();
		turno.setHorario(""); // sin especificar

		dia.getTurno().add(turno);

		guardar(actividad);
		
		return dia.getTurno().size(); // índice del nuevo turno
	}
	
	public void removeTurnoActividad(String id, Date fecha, int turno) throws BookleException {
		
		Actividad actividad = cargar(id);

		DiaActividad dia = getDia(actividad, fecha);

		if (dia == null)
			throw new BookleException(
					"La fecha establecida no está en la agenda: " + fecha);
		
		// precondición: al menos 1 turno
		
		int turnos = dia.getTurno().size();

		if (turnos == 0)
			throw new BookleException("No existen turnos para la actividad " + id + " en la fecha " + fecha);
		
		if (turno <= 0 || turno > turnos)
			throw new BookleException("Índice de turno incorrecto: " + turno + " en la actividad " + id + " en la fecha " + fecha);
		
		dia.getTurno().remove(turno - 1);

		guardar(actividad);
	}
	
	@Override
	public void setHorario(String id, Date fecha, int indice, String horario)
			throws BookleException {
		
		Actividad actividad = cargar(id);

		getTurno(actividad, fecha, indice).setHorario(horario);

		guardar(actividad);
		
	}

	@Override
	public String createReserva(String id, Date fecha, int indice,
			String alumno, String email) throws BookleException {
		
		Actividad actividad = cargar(id);

		Turno turno = getTurno(actividad, fecha, indice);

		Reserva reserva = new Reserva();
		String ticket = Utils.createId();
		reserva.setId(ticket);
		reserva.setAlumno(alumno);
		reserva.setEmail(email);

		turno.setReserva(reserva);

		guardar(actividad);

		return ticket;
	}

	@Override
	public boolean removeReserva(String id, String ticket) throws BookleException {
		
		Actividad actividad = cargar(id);

		boolean encontrado = false;
		for (DiaActividad dia : actividad.getAgenda()) {
			
			for (Turno turno : dia.getTurno()) {

				if (turno.getReserva() != null
						&& turno.getReserva().getId().equals(ticket)) {
					turno.setReserva(null);
					encontrado = true;
					break;
				}
			}
			if (encontrado)
				break;
		}

		if (encontrado)
			guardar(actividad);
		
		return encontrado;
	}
	
	public LinkedList<Actividad> getActividades() throws BookleException {
		
		LinkedList<Actividad> resultado = new LinkedList<Actividad>();
		
		File directorio = new File(DIRECTORIO_ACTIVIDADES);
		
		File[] actividades = directorio.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				
				return file.isFile() && file.getName().endsWith(".xml");
			}
		});
		
		for (File file : actividades) {
			
			String id = file.getName().substring(0, file.getName().length() - 4);
			
			resultado.add(cargar(id));
		}
		
		return resultado;
	}
	
}

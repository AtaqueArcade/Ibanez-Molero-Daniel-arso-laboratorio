package bookle.controlador;

import java.util.Date;
import java.util.LinkedList;

import bookle.tipos.Actividad;

// Controlador Caso de uso



public interface BookleControlador {

	/** 
	 * Método de creación de una actividad.
	 * Los parámetros email y descripcion son opcionales (aceptan valor nulo).
	 * El método retorna el id de la nueva actividad
	 */
	String createActividad(String titulo, String descripcion, String profesor, String email) throws BookleException;
	
	/**
	 * Método de actualización de la actividad.
	 * En relación al método de creación, añade un primer parámetro con el id de la actividad.
	 */
	void updateActividad(String id, String titulo, String descripcion, String profesor, String email) throws BookleException;
	
	/**
	 * Recupera la información de una actividad utilizando el identificador. 	
	 */
	Actividad getActividad(String id)  throws BookleException;
	
	/**
	 * Elimina una actividad utilizando el identificador.
	 */
	boolean removeActividad(String id)  throws BookleException;
	
	/**
	 * Esta operación añade un día a una actividad.
	 * El día de actividad está identificado por la fecha.
	 * Al menos debe establecerse un turno.
	 */
	void addDiaActividad(String id, Date fecha, int turnos)  throws BookleException;
	
	/**
	 * Elimina un día de una actividad.
	 * El id de la actividad y la fecha identifican el día.
	 */
	boolean removeDiaActividad(String id, Date fecha)  throws BookleException;
	
	/**
	 * Añade un nuevo turno a un día de una actividad.
	 * Los parámetros id y fecha identifican el día.
	 * Retorna el número de turno. El primer turno es el 1.
	 */
	int addTurnoActividad(String id, Date fecha) throws BookleException;
	
	/**
	 * Elimina un turno de un día de la actividad.
	 * El primer turno es el 1.
	 */
	void removeTurnoActividad(String id, Date fecha, int turno) throws BookleException;
	
	/**
	 * Establece la franja horaria de un turno de una actividad.
	 * El primer turno de un día de actividad es el 1.
	 */
	void setHorario(String idActividad, Date fecha, int indice, String horario) throws BookleException;
	
	/**
	 * Realiza la reserva de un turno de la actividad.
	 * Retorna el identificador de la reserva.
	 */
	String createReserva(String idActividad, Date fecha, int indice, String alumno, String email) throws BookleException;
	
	/**
	 * Elimina una reserva de una actividad. 
	 */
	boolean removeReserva(String idActividad, String ticket) throws BookleException;
	
	/**
	 * Método de consulta de todas las actividades.	
	 */
	LinkedList<Actividad> getActividades() throws BookleException;
}

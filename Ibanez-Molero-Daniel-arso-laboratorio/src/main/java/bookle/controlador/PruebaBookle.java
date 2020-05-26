package bookle.controlador;

import java.util.Calendar;
import java.util.Date;


public class PruebaBookle {

	public static void main(String[] args) throws Exception {
		
		BookleControlador bookle = new BookleControladorImpl();
				
		String id = bookle.createActividad("Entrevistas ArSo", "Entrevistas entrega 1", "Marcos", "marcos@um.es");
		
		Calendar calendar = Calendar.getInstance();
		
		// Fija un día de entrevistas, mañana
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		
		Date fecha = calendar.getTime();
		
		bookle.addDiaActividad(id, fecha, 3);
		
		bookle.setHorario(id, fecha, 1, "9:00-9:30");
		bookle.setHorario(id, fecha, 2, "9:30-10:00");
		bookle.setHorario(id, fecha, 3, "10:00-10:30");
		
		String ticket = bookle.createReserva(id, fecha, 3, "Juan González", "juan.gonzalez@gmail.com");
		
		ticket = bookle.createReserva(id, fecha, 2, "José Martínez", "jose.martinez@gmail.com");
		
		bookle.removeReserva(id, ticket);
		
		System.out.println("Fin.");
	}
}

package bookle.controlador;

@SuppressWarnings("serial")
public class BookleException extends Exception {

	public BookleException(String msg, Throwable causa) {		
		super(msg, causa);
	}
	
	public BookleException(String msg) {
		super(msg);		
	}
}

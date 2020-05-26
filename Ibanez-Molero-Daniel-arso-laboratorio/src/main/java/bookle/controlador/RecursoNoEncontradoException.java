package bookle.controlador;

@SuppressWarnings("serial")
public class RecursoNoEncontradoException extends RuntimeException {
	public RecursoNoEncontradoException(String msg) {
		super(msg);
	}
}
package tareas;

public class User {
	private final String nombre;
	private final String email;

	public User(String nombre, String email) {
		this.nombre = nombre;
		this.email = email;
	}

	public String getNombre() {
		return nombre;
	}

	public String getEmail() {
		return email;
	}
}

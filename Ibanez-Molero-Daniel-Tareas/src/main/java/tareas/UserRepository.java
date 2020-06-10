package tareas;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class UserRepository {
	private final MongoCollection<Document> usuarios;
	
	public UserRepository(MongoCollection<Document> usuarios) {
		this.usuarios = usuarios;
	}
	
	private User user(Document doc) {
		return new User(
				doc.getString("nombre"),
				doc.getString("email"));
	}
	
	public List<User> getAllUsers(){
		List<User> allUsers = new ArrayList<>();
		for(Document doc : usuarios.find()) {
			allUsers.add(user(doc));
		}
		return allUsers;
	}
	
	public User findByEmail(String email) {
		Document doc = usuarios.find(Filters.eq("email", new String(email))).first();
		return user(doc);
		
	}
	
	public User saveUser(User usuario) {
		Document doc = new Document();
		doc.append("nombre", usuario.getNombre());
		doc.append("email", usuario.getEmail());
		usuarios.insertOne(doc);
		return user(doc);
	}
	public void resetUsers(){
		usuarios.deleteMany(new Document());
	}
}

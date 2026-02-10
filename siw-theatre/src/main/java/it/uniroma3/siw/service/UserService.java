package it.uniroma3.siw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Client;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.UserRepository;

/**
 * The UserService handles logic for Users.
 */
@Service
public class UserService {

	@Autowired
	protected UserRepository userRepo;

	@Autowired
	protected ClientService clientService;

	/**
	 * This method retrieves a User from the DB based on its ID.
	 * 
	 * @param id the id of the User to retrieve from the DB
	 * @return the retrieved User, or null if no User with the passed ID could be
	 *         found in the DB
	 */
	@Transactional
	public User getUser(Long id) {
		Optional<User> result = this.userRepo.findById(id);
		return result.orElse(null);
	}

	/**
	 * il metodo salva un nuovo utente creando in corrispondenza un oggetto Client
	 */
	@Transactional
	public User saveUser(User user) {
		Client c = new Client();
		c.setName(user.getName());
		c.setSurname(user.getSurname());
		c.setDateOfBirth(null);
		c.setImage(null);
		this.clientService.saveClient(c);
		user.setClient(c);
		return this.userRepo.save(user);
	}

	/**
	 * This method retrieves all Users from the DB.
	 * 
	 * @return a List with all the retrieved Users
	 */
	@Transactional
	public List<User> getAllUsers() {
		List<User> result = new ArrayList<>();
		Iterable<User> iterable = this.userRepo.findAll();
		for (User user : iterable)
			result.add(user);
		return result;
	}

	@Transactional
	public User getUserByClient(Client client) {
		return this.userRepo.findByClient(client);
	}

	@Transactional
	public void deleteUser(User user) {
		this.userRepo.delete(user);
	}
}

package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Chef;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.ChefRepository;
import it.uniroma3.siw.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The UserService handles logic for Users.
 */
@Service
public class UserService {

    @Autowired
    protected UserRepository userRepo;
    
    @Autowired
    protected ChefRepository chefRepo;

    /**
     * This method retrieves a User from the DB based on its ID.
     * @param id the id of the User to retrieve from the DB
     * @return the retrieved User, or null if no User with the passed ID could be found in the DB
     */
    @Transactional
    public User getUser(Long id) {
        Optional<User> result = this.userRepo.findById(id);
        return result.orElse(null);
    }

    /**
     * This method saves a User in the DB.
     * @param user the User to save into the DB
     * @return the saved User
     * @throws DataIntegrityViolationException if a User with the same username
     *                              as the passed User already exists in the DB
     */
    @Transactional
	public User saveUser(User user) {
		Chef c = new Chef();
		c.setName(user.getName());
		c.setSurname(user.getSurname());
		c.setDateOfBirth(null);
		c.setImage(null);
		this.chefRepo.save(c);
		user.setChef(c);
		return this.userRepo.save(user);
	}

    /**
     * This method retrieves all Users from the DB.
     * @return a List with all the retrieved Users
     */
    @Transactional
    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>();
        Iterable<User> iterable = this.userRepo.findAll();
        for(User user : iterable)
            result.add(user);
        return result;
    }

    @Transactional
	public User getUserByChef(Chef chef) {
		return this.userRepo.findByChef(chef);
	}

    @Transactional
	public void deleteUser(User user) {
		this.userRepo.delete(user);
	}
}

package it.uniroma3.siw.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Client;
import it.uniroma3.siw.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
	
	User findByClient(Client client);
	
}
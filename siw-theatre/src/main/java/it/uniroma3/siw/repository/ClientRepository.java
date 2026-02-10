package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Client;

public interface ClientRepository extends CrudRepository<Client, Long> {

	public boolean existsByNameAndSurname(String name, String surname);

	@Query("SELECT c FROM Client c " + "WHERE (:surname IS NULL OR UPPER(c.surname) = UPPER(:surname))")
	public List<Client> findBySurname(@Param("surname") String surname);

	@Query("SELECT c FROM Client c " + "WHERE (:name IS NULL OR UPPER(c.name) = UPPER(:name)) "
			+ "AND (:surname IS NULL OR UPPER(c.surname) = UPPER(:surname))")
	Client findByNameAndSurname(@Param("name") String name, @Param("surname") String surname);

	public Optional<Client> findById(Long id);

}
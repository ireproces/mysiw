package it.uniroma3.siw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.uniroma3.siw.model.Actor;

@Repository
public interface ActorRepository extends CrudRepository<Actor, Long> {

	@Query("SELECT a FROM Actor a " + "WHERE (:surname IS NULL OR UPPER(a.surname) = UPPER(:surname))")
	List<Actor> findBySurname(@Param("surname") String surname);

}

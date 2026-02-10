package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Actor;
import it.uniroma3.siw.model.Opera;

public interface OperaRepository extends CrudRepository<Opera, Long> {

	public boolean existsByName(String name);

	public boolean existsByNameAndYear(String name, String year);

	public Optional<Opera> findById(Long id);

	@Query("SELECT a FROM Actor a WHERE a.id NOT IN (SELECT a.id FROM Opera o JOIN o.actors a WHERE o.id = :operaId)")
	Iterable<Actor> findActorsNotInOpera(@Param("operaId") Long operaId);

	@Query("SELECT o FROM Opera o " + "WHERE (:name IS NULL OR UPPER(o.name) = UPPER(:name))")
	public List<Opera> findByName(@Param("name") String name);

	@Query("SELECT DISTINCT o FROM Opera o JOIN o.tickets t WHERE t.owner IS NULL")
	public Iterable<Opera> findAllOperasWithTicketsAvailable();

}
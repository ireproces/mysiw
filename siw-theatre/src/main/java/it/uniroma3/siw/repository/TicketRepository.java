package it.uniroma3.siw.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Client;
import it.uniroma3.siw.model.Ticket;

public interface TicketRepository extends CrudRepository<Ticket, Long> {

	public boolean existsByDateEventAndType(LocalDate dateEvent, String price);

	@Query("SELECT t FROM Ticket t WHERE t.opera.id = :operaId AND t.owner IS NULL")
	public List<Ticket> findAllTicketsNotOwnedInOpera(@Param("operaId") Long operaId);

//	@Query("SELECT r FROM Recipe r WHERE lower(r.name) like lower(concat('%', :name, '%'))")
//	public List<Ticket> findByName(String name);
//
//	public boolean existsByNameAndInventor(String name, Client chef);

}
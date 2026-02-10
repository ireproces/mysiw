package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Ticket;
import it.uniroma3.siw.repository.TicketRepository;

@Service
public class TicketService {

	@Autowired
	private TicketRepository ticketRepo;

	public Optional<Ticket> getTicketById(Long id) {
		return this.ticketRepo.findById(id);
	}
//
//	public Iterable<Ticket> getAllRecipes() {
//		return this.recipeRepo.findAll();
//	}
//
//	public List<Ticket> getRecipesByName(String name) {
//		return this.recipeRepo.findByName(name);
//	}
//
//	public void deleteRecipeById(Long id) {
//		this.recipeRepo.deleteById(id);
//	}

	public void deleteTicket(Ticket ticket) {
		this.ticketRepo.delete(ticket);
	}

	public void saveTicket(@Valid Ticket ticket) {
		this.ticketRepo.save(ticket);
	}

	public List<Ticket> getAllTicketsNotOwnedInOpera(Long operaId) {
		return this.ticketRepo.findAllTicketsNotOwnedInOpera(operaId);
	}
}

package it.uniroma3.siw.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Actor;
import it.uniroma3.siw.model.Opera;
import it.uniroma3.siw.model.Ticket;
import it.uniroma3.siw.repository.OperaRepository;

@Service
public class OperaService {

	@Autowired
	private OperaRepository operaRepo;

	@Autowired
	private ActorService actorService;

	@Autowired
	private TicketService ticketService;

	public Iterable<Opera> getAllOperas() {
		return this.operaRepo.findAll();
	}

	public void deleteOperaById(Long id) {
		this.operaRepo.deleteById(id);
	}

	public Optional<Opera> getOperaById(Long id) {
		return this.operaRepo.findById(id);
	}

	public void saveOpera(@Valid Opera opera) {
		this.operaRepo.save(opera);
	}

	public void addNewActorToOpera(Long operaId, Long actorId) {
		// recupera l'opera
		Opera o = this.operaRepo.findById(operaId).get();
		// recupera lista attori
		List<Actor> actors = o.getActors();
		// aggiungi attore di cui id
		actors.add(this.actorService.getActorById(actorId).get());
		this.operaRepo.save(o); // salvataggio esplicito per relazioni @MtoM
	}

	public List<Actor> actorsToAddInOpera(Long operaId) {
		List<Actor> actorsToAdd = new LinkedList<>();
		for (Actor a : this.operaRepo.findActorsNotInOpera(operaId)) {
			actorsToAdd.add(a);
		}
		return actorsToAdd;
	}

	public void deleteActorFromOpera(Long operaId, Long actorId) {
		// recupera l'opera
		Opera o = this.operaRepo.findById(operaId).get();
		// recupera attore
		Actor a = this.actorService.getActorById(actorId).get();
		// rimuovilo dalla lista di attori
		List<Actor> actors = o.getActors();
		actors.remove(a);
		this.operaRepo.save(o); // salvataggio esplicito per relazioni @MtoM
	}

	public List<Opera> getOperasByName(String name) {
		return this.operaRepo.findByName(name);
	}

	public void deleteTicketFromOpera(Long operaId, Long ticketId) {
		// recupera l'opera
		Opera o = this.operaRepo.findById(operaId).get();
		// recupera biglietto
		Ticket t = this.ticketService.getTicketById(ticketId).get();
		// rimuovilo dalla lista di attori
		List<Ticket> tickets = o.getTickets();
		tickets.remove(t);
		this.ticketService.deleteTicket(t);
	}

	public Iterable<Opera> getAllOperasWithTicketsAvailable() {
		return this.operaRepo.findAllOperasWithTicketsAvailable();
	}

	public void freeAllTicketsOfOpera(Long id) {
		for(Ticket t: this.operaRepo.findById(id).get().getTickets()) {
			t.setOpera(null);
		}
	}
}

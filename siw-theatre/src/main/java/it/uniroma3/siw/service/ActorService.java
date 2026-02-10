package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Actor;
import it.uniroma3.siw.repository.ActorRepository;

@Service
public class ActorService {

	@Autowired
	private ActorRepository actorRepo;

	public Optional<Actor> getActorById(Long actorId) {
		return this.actorRepo.findById(actorId);
	}

	public Iterable<Actor> getAllActors() {
		return this.actorRepo.findAll();
	}

	public List<Actor> getActorsBySurname(String surname) {
		return this.actorRepo.findBySurname(surname);
	}

	public void deleteActor(Actor actor) {
		this.actorRepo.delete(actor);
	}

	public void saveActor(@Valid Actor actor) {
		this.actorRepo.save(actor);
	}
}

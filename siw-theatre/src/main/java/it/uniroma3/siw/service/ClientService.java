package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Client;
import it.uniroma3.siw.model.Ticket;
import it.uniroma3.siw.repository.ClientRepository;

@Service
public class ClientService {

	@Autowired
	private ClientRepository clientRepo;

	public Client getChefByNameAndSurnameOfUser(String name, String surname) {
		return this.clientRepo.findByNameAndSurname(name, surname);
	}

	public Optional<Client> getChefById(Long id) {
		return this.clientRepo.findById(id);
	}

	public void saveClient(Client client) {
		this.clientRepo.save(client);
	}

	public void deleteClient(Client chef) {
		this.clientRepo.delete(chef);
	}

	public List<Client> getClientsBySurname(String surname) {
		return this.clientRepo.findBySurname(surname);
	}

	public Optional<Client> getClientById(Long clientId) {
		return this.clientRepo.findById(clientId);
	}

	public Iterable<Client> getAllClients() {
		return this.clientRepo.findAll();
	}

	public void freeAllTicketsOfClient(Long id) {
		for (Ticket t : this.clientRepo.findById(id).get().getTickets()) {
			t.setOwner(null);
		}
	}
}

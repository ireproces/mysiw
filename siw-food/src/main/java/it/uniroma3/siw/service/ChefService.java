package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Chef;
import it.uniroma3.siw.repository.ChefRepository;

@Service
public class ChefService {

	@Autowired
	private ChefRepository chefRepo;

	public Chef getChefByNameAndSurnameOfUser(String name, String surname) {
		return this.chefRepo.findByNameAndSurname(name, surname);
	}

	public Optional<Chef> getChefById(Long id) {
		return this.chefRepo.findById(id);
	}

	public void saveChef(Chef chef) {
		this.chefRepo.save(chef);
	}

	public Iterable<Chef> findAllChefs() {
		return this.chefRepo.findAll();
	}

	public void deleteChef(Chef chef) {
		this.chefRepo.delete(chef);
	}

	public List<Chef> getChefsBySurname(String surname) {
		return this.chefRepo.findBySurname(surname);
	}
}

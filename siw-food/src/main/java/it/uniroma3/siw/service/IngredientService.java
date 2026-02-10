package it.uniroma3.siw.service;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Ingredient;
import it.uniroma3.siw.repository.IngredientRepository;

@Service
public class IngredientService {

	@Autowired
	private IngredientRepository ingredientRepo;

	public Iterable<Ingredient> getAllIngredients() {
		return this.ingredientRepo.findAll();
	}

	public void deleteIngredientById(Long id) {
		this.ingredientRepo.deleteById(id);
	}

	public Optional<Ingredient> getIngredientById(Long id) {
		return this.ingredientRepo.findById(id);
	}

	public void saveIngredient(@Valid Ingredient ingredient) {
		this.ingredientRepo.save(ingredient);
	}

}

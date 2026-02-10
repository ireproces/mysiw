package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.repository.RecipeRepository;

@Service
public class RecipeService {

	@Autowired
	private RecipeRepository recipeRepo;

	public Optional<Recipe> getRecipeById(Long id) {
		return this.recipeRepo.findById(id);
	}

	public Iterable<Recipe> getAllRecipes() {
		return this.recipeRepo.findAll();
	}

	public List<Recipe> getRecipesByName(String name) {
		return this.recipeRepo.findByName(name);
	}

	public void deleteRecipeById(Long id) {
		this.recipeRepo.deleteById(id);
	}

	public void deleteRecipe(Recipe recipe) {
		this.recipeRepo.delete(recipe);
	}

	public void saveRecipe(@Valid Recipe recipe) {
		this.recipeRepo.save(recipe);
	}

	public List<Recipe> getAllRecipesInventedByChef(Long id) {
		return this.recipeRepo.findAllRecipesInventedByChef(id);
	}
}

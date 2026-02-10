package it.uniroma3.siw.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.controller.validator.IngredientValidator;
import it.uniroma3.siw.model.Ingredient;
import it.uniroma3.siw.service.IngredientService;

@Controller
public class IngredientController {

	@Autowired
	private IngredientService ingredientService;

	@Autowired
	private IngredientValidator ingredientValidator;

	@GetMapping("/chef/ingredients")
	public String getIngredients(Model model) {
		model.addAttribute("ingredients", this.ingredientService.getAllIngredients());
		return "chef/ingredients.html";
	}

	@PostMapping("/deleteIngredient/{id}")
	public String deleteIngredient(@PathVariable Long id) {
		ingredientService.deleteIngredientById(id);
		return "redirect:/chef/manageIngredients";
	}

	@GetMapping("/chef/manageIngredients")
	public String manageIngredients(Model model) {
		model.addAttribute("ingredients", this.ingredientService.getAllIngredients());
		return "chef/manageIngredients.html";
	}

	@GetMapping(value = "/chef/formNewIngredient")
	public String formNewIngredient(Model model) {
		model.addAttribute("ingredient", new Ingredient());
		return "chef/formNewIngredient.html";
	}

	@GetMapping("/chef/ingredient/{id}")
	public String getRecipe(@PathVariable("id") Long id, Model model) {
		model.addAttribute("ingredient", this.ingredientService.getIngredientById(id).get());
		return "chef/ingredient.html";
	}

	@PostMapping("/chef/ingredient")
	public String newIngredient(@Valid @ModelAttribute("ingredient") Ingredient ingredient, BindingResult bindingResult,
			Model model) {

		this.ingredientValidator.validate(ingredient, bindingResult);
		if (!bindingResult.hasErrors()) {
			this.ingredientService.saveIngredient(ingredient);
			model.addAttribute("ingredient", ingredient);
			return "chef/ingredient.html";
		} else {
			return "chef/formNewIngredient.html";
		}
	}

}

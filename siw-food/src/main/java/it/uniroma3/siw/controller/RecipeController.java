package it.uniroma3.siw.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.model.Chef;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Ingredient;
import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.ChefService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.ImageService;
import it.uniroma3.siw.service.IngredientService;
import it.uniroma3.siw.service.RecipeService;

@Controller
@org.springframework.transaction.annotation.Transactional
public class RecipeController {

	@Autowired
	private RecipeService recipeService;

	@Autowired
	private ImageService imageService;

	@Autowired
	private ChefService chefService;

	@Autowired
	private IngredientService ingredientService;

	@Autowired
	private CredentialsService credentialsService;

	@GetMapping("/public/recipe/{id}")
	public String getRecipe(@PathVariable("id") Long id, Model model) {
		Recipe recipe = this.recipeService.getRecipeById(id).orElse(null);
		if (recipe != null) {
			List<String> base64Strings = recipe.getRecipeImages().stream().filter(Objects::nonNull)
					.map(recipeImage -> Base64Utils.encodeToString(recipeImage.getImageData()))
					.collect(Collectors.toList());
			model.addAttribute("recipe", recipe);
			model.addAttribute("base64Strings", base64Strings);
			return "public/recipe";
		} else {
			return "recipeNotFound";
		}
	}

	@GetMapping("/public/recipes")
	public String getRecipes(Model model) {

		model.addAttribute("recipes", this.recipeService.getAllRecipes());
		return "public/recipes.html";
	}

	@PostMapping("admin/deleteRecipeAdmin/{id}")
	public String deleteRecipeAdmin(@PathVariable Long id) {
		recipeService.deleteRecipeById(id);
		return "redirect:/admin/manageRecipesAdmin";
	}

	@PostMapping("/chef/deleteRecipeChef/{id}")
	public String deleteRecipeChef(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		// Ottieni l'utente autenticato
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		User user = credentials.getUser();

		// Trova lo chef associato all'utente autenticato
		Chef chef = chefService.getChefByNameAndSurnameOfUser(user.getName(), user.getSurname());

		// Trova la ricetta da eliminare
		Recipe recipe = recipeService.getRecipeById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid recipe Id:" + id));

		// Controlla se lo chef è l'inventore della ricetta
		if (!recipe.getInventor().equals(chef)) {
			redirectAttributes.addFlashAttribute("errorMessage", "Non sei autorizzato a cancellare questa ricetta.");
			return "redirect:/chef/manageRecipesChef";
		}

		recipeService.deleteRecipe(recipe);
		return "redirect:/chef/manageRecipesChef";
	}

	@GetMapping("/admin/manageRecipesAdmin")
	public String manageRecipesAdmin(Model model) {
		model.addAttribute("recipes", this.recipeService.getAllRecipes());
		return "admin/manageRecipesAdmin.html";
	}

	@GetMapping("/chef/manageRecipesChef")
	public String manageRecipesChef(Model model) {
		// Ottieni l'utente autenticato
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		User user = credentials.getUser();

		// Trova lo chef associato all'utente autenticato
		Chef chef = chefService.getChefByNameAndSurnameOfUser(user.getName(), user.getSurname());

		model.addAttribute("authenticatedChef", chef);
		model.addAttribute("recipes", recipeService.getAllRecipesInventedByChef(chef.getId()));
		return "chef/manageRecipesChef";
	}

	@GetMapping("/chef/formNewRecipe")
	public String formNewRecipe(Model model) {
		// Ottieni l'utente autenticato
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		User user = credentials.getUser();

		// Trova lo chef associato all'utente autenticato
		Chef chef = this.chefService.getChefByNameAndSurnameOfUser(user.getName(), user.getSurname());

		model.addAttribute("chef", chef);
		model.addAttribute("recipe", new Recipe());
		model.addAttribute("ingredients", ingredientService.getAllIngredients());
		return "chef/formNewRecipe.html";
	}

	@PostMapping("/chef/newRecipe")
	public String createNewRecipe(@Valid @ModelAttribute Recipe recipe,
			@RequestParam Map<String, String> ingredientQuantities,
			@RequestParam("uploadedImages") MultipartFile[] uploadedImages, BindingResult result, Model model,
			@RequestParam("chefId") Long chefId) {

		if (result.hasErrors()) {
			model.addAttribute("ingredients", ingredientService.getAllIngredients());
			return "chef/formNewRecipe";
		}

		// Trova lo chef dal repository utilizzando l'ID passato come parametro
		Chef chef = chefService.getChefById(chefId)
				.orElseThrow(() -> new IllegalArgumentException("Chef non trovato con ID: " + chefId));

		// Imposta lo chef come inventore della ricetta
		recipe.setInventor(chef);

		// Mappa per salvare gli ingredienti e le relative quantità
		Map<Ingredient, Long> ingredientsMap = new HashMap<>();

		// Itera sui parametri del form per ottenere gli ingredienti e le quantità
		for (Map.Entry<String, String> entry : ingredientQuantities.entrySet()) {
			String paramName = entry.getKey();
			if (paramName.startsWith("ingredientQuantities")) {
				Long ingredientId = Long
						.parseLong(paramName.substring(paramName.indexOf("[") + 1, paramName.indexOf("]")));
				Ingredient ingredient = ingredientService.getIngredientById(ingredientId).orElseThrow(
						() -> new IllegalArgumentException("Ingrediente non trovato con ID: " + ingredientId));
				// Controlla se il valore della quantità è vuoto e imposta a 0 se lo è
				Long quantity = entry.getValue().isEmpty() ? 0L : Long.parseLong(entry.getValue());
				ingredientsMap.put(ingredient, quantity);
			}
		}

		// Imposta la mappa degli ingredienti sulla ricetta e salva
		recipe.setIngredients(ingredientsMap);

		try {
			// Salva le immagini
			List<Image> recipeImages = new ArrayList<>();

			for (MultipartFile uploadedImage : uploadedImages) {
				Image recipeImage = new Image();
				byte[] imageBytes = uploadedImage.getBytes();
				recipeImage.setImageData(imageBytes);
				recipeImages.add(recipeImage);
			}

			// Salva l'istanza di Recipe
			recipeService.saveRecipe(recipe);

			// Salva le immagini della ricetta
			imageService.saveAllImages(recipeImages);

			// Aggiorna la lista delle immagini nella ricetta
			recipe.setRecipeImages(recipeImages);

		} catch (IOException e) {
			e.printStackTrace();
			return "redirect:/error";
		}

		return "redirect:/public/recipes";
	}

	@GetMapping("/public/filterRecipes")
	public String filteredRecipesByName(@RequestParam(name = "name", required = false) String name, Model model) {
		List<Recipe> filteredRecipes;

		// Utilizziamo il metodo findByName del repository per cercare le ricette per
		// nome
		if (name != null && !name.isEmpty()) {
			filteredRecipes = recipeService.getRecipesByName(name);
		} else {
			// Se il parametro nome è vuoto o non specificato, restituisci tutte le ricette
			filteredRecipes = (List<Recipe>) recipeService.getAllRecipes();
		}

		model.addAttribute("recipes", filteredRecipes);
		return "public/recipes"; // Assumiamo che ci sia una pagina chiamata recipes.html per visualizzare la
									// lista delle ricette
	}
}

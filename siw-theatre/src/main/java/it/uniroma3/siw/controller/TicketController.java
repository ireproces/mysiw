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

import it.uniroma3.siw.model.Client;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Opera;
import it.uniroma3.siw.model.Ticket;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.ImageService;
import it.uniroma3.siw.service.OperaService;
import it.uniroma3.siw.service.TicketService;

@Controller
public class TicketController {

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private TicketService ticketService;

	@Autowired
	private OperaService operaService;

	/**
	 * metodo che mappa la view per singolo biglietto di cui id
	 */
	@GetMapping("/client/ticket/{id}")
	public String getTicket(@PathVariable("id") Long id, Model model) {
		
		Ticket ticket = this.ticketService.getTicketById(id).get();
		if (ticket != null) {
			model.addAttribute("ticket", ticket);
			return "client/ticket.html";
		} else {
			return "client/indexClient";
		}
	}

//	@GetMapping("/public/recipes")
//	public String getRecipes(Model model) {
//
//		model.addAttribute("recipes", this.recipeService.getAllRecipes());
//		return "public/recipes.html";
//	}
//
//	@GetMapping("public/formSearchRecipes")
//	public String formSearchMovies() {
//		return "public/formSearchRecipes.html";
//	}
//
//	@PostMapping("public/searchRecipes")
//	public String searchMovies(Model model, @RequestParam String name) {
//		model.addAttribute("recipes", this.recipeService.getRecipesByName(name));
//		return "public/foundRecipes.html";
//	}
//
//	@PostMapping("admin/deleteRecipeAdmin/{id}")
//	public String deleteRecipeAdmin(@PathVariable Long id) {
//		recipeService.deleteRecipeById(id);
//		return "redirect:/admin/manageRecipesAdmin";
//	}
//
//	@PostMapping("/chef/deleteRecipeChef/{id}")
//	public String deleteRecipeChef(@PathVariable Long id, RedirectAttributes redirectAttributes) {
//		// Ottieni l'utente autenticato
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
//		User user = credentials.getUser();
//
//		// Trova lo chef associato all'utente autenticato
//		Client chef = chefService.getChefByNameAndSurnameOfUser(user.getName(), user.getSurname());
//
//		// Trova la ricetta da eliminare
//		Ticket recipe = recipeService.getRecipeById(id)
//				.orElseThrow(() -> new IllegalArgumentException("Invalid recipe Id:" + id));
//
//		// Controlla se lo chef è l'inventore della ricetta
//		if (!recipe.getInventor().equals(chef)) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Non sei autorizzato a cancellare questa ricetta.");
//			return "redirect:/chef/manageRecipesChef";
//		}
//
//		recipeService.deleteRecipe(recipe);
//		return "redirect:/chef/manageRecipesChef";
//	}
//
//	@GetMapping("/admin/manageRecipesAdmin")
//	public String manageRecipesAdmin(Model model) {
//		model.addAttribute("recipes", this.recipeService.getAllRecipes());
//		return "admin/manageRecipesAdmin.html";
//	}
//
//	@GetMapping("/chef/manageRecipesChef")
//	public String manageRecipesChef(Model model) {
//		// Ottieni l'utente autenticato
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
//		User user = credentials.getUser();
//
//		// Trova lo chef associato all'utente autenticato
//		Client chef = chefService.getChefByNameAndSurnameOfUser(user.getName(), user.getSurname());
//
//		model.addAttribute("authenticatedChef", chef);
//		model.addAttribute("recipes", recipeService.getAllRecipes());
//		return "chef/manageRecipesChef";
//	}

	/**
	 * metodo che mappa la form per l'aggiunta di un biglietto per l'opera di cui id
	 */
	@GetMapping("/admin/formNewTicket/{operaId}")
	public String formNewTicket(@PathVariable Long operaId, Model model) {

		model.addAttribute("opera", this.operaService.getOperaById(operaId).get());
		model.addAttribute("ticket", new Ticket());
		return "admin/formNewTicket";
	}

	/**
	 * Metodo che mappa l'operazione di aggiunta del biglietto
	 */
	@PostMapping("/admin/newTicket")
	public String createNewTicket(@Valid @ModelAttribute Ticket ticket, BindingResult bindingResult, Model model,
			@RequestParam("operaId") Long operaId) {

		if (bindingResult.hasErrors()) {
			// Gestione degli errori di validazione
			model.addAttribute("opera", this.operaService.getOperaById(operaId)
					.orElseThrow(() -> new IllegalArgumentException("Opera non trovata")));
			return "admin/formNewTicket"; // Torna al form se ci sono errori
		}

		Opera o = this.operaService.getOperaById(operaId).get(); // recupera opera
		ticket.setOpera(o); // imposta opera al biglietto
		this.ticketService.saveTicket(ticket); // salva biglietto
		o.getTickets().add(ticket); // aggiungi nuovo biglietto all'opera

		model.addAttribute("opera", o);
		return "redirect:/admin/manageOperas";
	}

//	@GetMapping("/public/filterRecipes")
//	public String filteredRecipesByName(@RequestParam(name = "name", required = false) String name, Model model) {
//		List<Ticket> filteredRecipes;
//
//		// Utilizziamo il metodo findByName del repository per cercare le ricette per
//		// nome
//		if (name != null && !name.isEmpty()) {
//			filteredRecipes = recipeService.getRecipesByName(name);
//		} else {
//			// Se il parametro nome è vuoto o non specificato, restituisci tutte le ricette
//			filteredRecipes = (List<Ticket>) recipeService.getAllRecipes();
//		}
//
//		model.addAttribute("recipes", filteredRecipes);
//		return "public/recipes"; // Assumiamo che ci sia una pagina chiamata recipes.html per visualizzare la
//									// lista delle ricette
//	}
}

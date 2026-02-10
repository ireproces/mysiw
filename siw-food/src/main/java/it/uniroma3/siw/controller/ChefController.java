package it.uniroma3.siw.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Chef;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.ChefService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.ImageService;
import it.uniroma3.siw.service.UserService;

@Controller
@Transactional
public class ChefController {

	@Autowired
	private ChefService chefService;

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private UserService userService;

	@Autowired
	private ImageService imageService;

	@GetMapping("/chef/updateAccount")
	public String showUpdateProfileForm(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated()) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			User user = credentials.getUser(); // recupero l'utente
			Chef chef = user.getChef(); // recupero lo chef **********

			model.addAttribute("chef", chef); // Passa solo l'oggetto Chef al modello
			return "chef/formUpdateAccount.html"; // Ritorna il template per l'aggiornamento
		} else {
			return "redirect:/public/chefs"; // Se non trova l'utente, reindirizza
		}
	}

	@PostMapping("/chef/updateAcc")
	public String updateChef(@Valid @ModelAttribute("chef") Chef chef, BindingResult bindingResult,
			@RequestPart(value = "uploadedImage", required = false) MultipartFile uploadedImage, Model model,
			@RequestParam("id") Long chefId // Aggiunta del parametro per l'ID dello chef
	) {
		if (!bindingResult.hasErrors()) {
			try {
				// Trova lo chef nel repository *********
				Chef existingChef = this.chefService.getChefById(chefId)
						.orElseThrow(() -> new IllegalArgumentException("Lo chef con l'ID fornito non esiste."));

				// Aggiorna i campi dello chef
				existingChef.setDateOfBirth(chef.getDateOfBirth());

				// Verifica se è stata fornita un'immagine e aggiorna l'immagine solo se c'è un
				// file
				if (uploadedImage != null && !uploadedImage.isEmpty()) {
					byte[] imageData = uploadedImage.getBytes();
					Image chefImage = new Image();
					chefImage.setImageData(imageData);
					imageService.saveImage(chefImage); // Salva l'istanza ChefImage nel repository

					existingChef.setImage(chefImage); // Associa l'immagine allo chef
				}
				// Salva le modifiche **********
				this.chefService.saveChef(existingChef);

				return "redirect:/public/chefs"; // Reindirizza alla lista degli chef dopo l'aggiornamento

			} catch (IOException e) {
				// Gestione dell'errore di IO
				model.addAttribute("errorMessage", "errore nel caricamento dell'imagine");
				return "chef/formUpdateAccount.html"; // Torna alla pagina di aggiornamento in caso di errore
			}
		}
		return "chef/formUpdateAccount.html";
	}

	@GetMapping("/admin/manageChefs")
	public String manageChefs(Model model) {
		model.addAttribute("chefs", this.chefService.findAllChefs()); // *****
		return "admin/manageChefs.html";
	}

	@GetMapping("/public/chefs")
	public String getChefs(Model model) {
		model.addAttribute("chefs", this.chefService.findAllChefs()); // ******
		return "public/chefs.html";
	}

	@PostMapping("admin/deleteChef/{id}")
	public String deleteChef(@PathVariable Long id) {
		// Trova lo chef da eliminare ****
		Chef chef = chefService.getChefById(id).orElse(null);
		if (chef != null) {
			User user = userService.getUserByChef(chef); // recupero utente
			if (user != null) {
				Credentials credentials = this.credentialsService.getCredentialsByUser(user); // recupero credenziali
				if (credentials != null) {
					this.userService.deleteUser(user);
					this.credentialsService.deleteCredentials(credentials);
				}
			}
		} else {
			// Se lo chef non è trovato, reindirizza con un messaggio di errore
			return "redirect:/admin/manageChefs?error=chefNotFound";
		}
		return "redirect:/admin/manageChefs";
	}

	@GetMapping("/public/chef/{id}")
	public String getChefAndRecipes(@PathVariable("id") Long id, Model model) {
		Chef chef = chefService.getChefById(id).orElse(null);

		if (chef != null) {
			// Aggiungi lo chef al modello
			model.addAttribute("chef", chef);

			// Recupera le ricette dello chef
			List<Recipe> recipes = chef.getInventorOf();
			model.addAttribute("recipes", recipes);

			// Recupera l'immagine dello chef e convertila in Base64
			Image chefImage = chef.getImage();
			if (chefImage != null) {
				String base64Image = Base64.getEncoder().encodeToString(chefImage.getImageData());
				model.addAttribute("base64Image", base64Image); // Aggiungi al modello
			} else {
				System.out.println("L'immagine dello chef non è stata trovata");
			}
		} else {
			System.out.println("Chef non trovato con ID: " + id);
		}

		return "public/chef";
	}

	@GetMapping("/chef/indexChef")
	public String chefIndex(Model model) {
		// Ottieni l'autenticazione corrente
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated()) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			User user = credentials.getUser();
			model.addAttribute("nome", user.getName());
			model.addAttribute("cognome", user.getSurname());

		}

		return "chef/indexChef.html"; // Ritorna la pagina dell'index degli chef
	}

	@GetMapping("/public/filterChefs")
	public String filteredChefsBySurname(@RequestParam(name = "surname", required = false) String surname,
			Model model) {
		List<Chef> filteredChefs;

		// Utilizziamo il metodo findBySurname del repository per cercare gli chef per
		// cognome
		if (surname != null && !surname.isEmpty()) {
			filteredChefs = chefService.getChefsBySurname(surname);
		} else {
			// Se il parametro cognome è vuoto o non specificato, restituisci tutti gli chef
			filteredChefs = (List<Chef>) chefService.findAllChefs();
		}

		model.addAttribute("chefs", filteredChefs);
		return "public/chefs";
	}
}

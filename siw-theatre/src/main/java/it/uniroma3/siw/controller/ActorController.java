package it.uniroma3.siw.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Actor;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.service.ActorService;
import it.uniroma3.siw.service.ImageService;

@Controller
public class ActorController {

	@Autowired
	private ActorService actorService;

	@Autowired
	private ImageService imageService;

	/**
	 * mappa la view per la gestione degli attori presenti e nuovi
	 */
	@GetMapping("/admin/manageActors")
	public String manageActors(Model model) {
		model.addAttribute("actors", this.actorService.getAllActors());
		return "admin/manageActors.html";
	}

	/**
	 * mappa la view contente gli attori nel sistema
	 */
	@GetMapping("/public/actors")
	public String getActors(Model model) {
		model.addAttribute("actors", this.actorService.getAllActors());
		return "public/actors.html";
	}

	/**
	 * mappa la view per scheda attore
	 */
	@GetMapping("/public/actor/{id}")
	public String getActor(@PathVariable("id") Long id, Model model) {
		Actor actor = this.actorService.getActorById(id).get();
		if (actor != null) {
			// Aggiungi lo chef al modello
			model.addAttribute("actor", actor);

			// Recupera l'immagine dell'attore e convertila in Base64
			Image image = actor.getImage();
			if (image != null) {
				String base64Image = Base64.getEncoder().encodeToString(image.getImageData());
				model.addAttribute("base64Image", base64Image); // Aggiungi al modello
			} else {
				System.out.println("L'immagine dell'attore non è stata trovata");
			}
		} else {
			System.out.println("Attore non trovato con ID: " + id);
		}

		return "public/actor";
	}

	/**
	 * metodo che mappa la view dopo l'operazione di filtraggio su cognome
	 */
	@GetMapping("/public/filterActors")
	public String filteredActorsBySurname(@RequestParam(name = "surname", required = false) String surname,
			Model model) {
		List<Actor> filteredActors = new LinkedList<>();

		// Utilizziamo il metodo findBySurname del repository per cercare gli chef per
		// cognome
		if (surname != null && !surname.isEmpty()) {
			filteredActors = this.actorService.getActorsBySurname(surname);
		} else {
			// Se il parametro cognome è vuoto o non specificato, restituisci tutti gli chef
			filteredActors = (List<Actor>) this.actorService.getAllActors();
		}

		model.addAttribute("actors", filteredActors);
		return "public/actors";
	}

	/**
	 * mappa l'operazione di cancellazione dell'attore di cui id
	 */
	@PostMapping("admin/deleteActor/{id}")
	public String deleteActor(@PathVariable Long id) {
		// Trova attore da eliminare ****
		Actor actor = actorService.getActorById(id).get();
		if (actor != null) {
			this.actorService.deleteActor(actor);
		} else {
			return "redirect:/admin/manageActors?error=actorNotFound";
		}
		return "redirect:/admin/manageActors";
	}

	/**
	 * metodo che mappa la view-form per inserire un nuovo oggetto Attore
	 */
	@GetMapping(value = "/admin/formNewActor")
	public String formNewActor(Model model) {
		model.addAttribute("actor", new Actor());
		return "admin/formNewActor.html";
	}

	/**
	 * metodo che mappa l'operazione di aggiunta di un nuovo attore
	 */
	@PostMapping("/admin/newActor")
	public String newActor(@Valid @ModelAttribute Actor actor, BindingResult result,
			@RequestPart(value = "uploadedImage", required = false) MultipartFile uploadedImage, Model model,
			@RequestParam("actorId") Long actorId) {

		if (!result.hasErrors()) {
			try {
				if (uploadedImage != null && !uploadedImage.isEmpty()) {
					byte[] imageData = uploadedImage.getBytes();
					Image image = new Image();
					image.setImageData(imageData);
					imageService.saveImage(image); // Salva l'istanza Image nel repository

					actor.setImage(image); // Associa l'immagine all'attore
				}
				this.actorService.saveActor(actor);

			} catch (IOException e) {
				// Gestione dell'errore di IO
				model.addAttribute("errorMessage", "errore nel caricamento dell'imagine");
				return "admin/formUpdateActor.html"; // Torna alla pagina di aggiornamento in caso di errore
			}
			return "redirect:/public/actors";
		}
		return "admin/formUpdateActor.html";
	}
}

package it.uniroma3.siw.controller;

import java.io.IOException;
import java.util.Base64;
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

import it.uniroma3.siw.controller.validator.OperaValidator;
import it.uniroma3.siw.model.Actor;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Opera;
import it.uniroma3.siw.model.Ticket;
import it.uniroma3.siw.service.ActorService;
import it.uniroma3.siw.service.ImageService;
import it.uniroma3.siw.service.OperaService;
import it.uniroma3.siw.service.TicketService;

@Controller
public class OperaController {

	@Autowired
	private OperaService operaService;

	@Autowired
	private ImageService imageService;

	@Autowired
	private ActorService actorService;

	@Autowired
	private OperaValidator operaValidator;

	@Autowired
	private TicketService ticketService;

	/**
	 * metodo per mappare la view della lista di opere
	 */
	@GetMapping("/public/operas")
	public String getOperas(Model model) {
		model.addAttribute("operas", this.operaService.getAllOperas());
		return "public/operas.html";
	}

	@GetMapping("/public/filterOperas")
	public String filteredOperasByName(@RequestParam(name = "name", required = false) String name, Model model) {
		List<Opera> filteredOperas;

		// Utilizziamo il metodo findBySurname del repository per cercare gli chef per
		// cognome
		if (name != null && !name.isEmpty()) {
			filteredOperas = operaService.getOperasByName(name);
		} else {
			// Se il parametro cognome è vuoto o non specificato, restituisci tutti gli chef
			filteredOperas = (List<Opera>) operaService.getAllOperas();
		}

		model.addAttribute("operas", filteredOperas);
		return "public/operas";
	}

	/**
	 * metodo per mappare la view della singola opera
	 */
	@GetMapping("/public/opera/{id}")
	public String getOpera(@PathVariable("id") Long id, Model model) {

		Opera opera = this.operaService.getOperaById(id).get();

		// recupera l'immagine
		Image image = opera.getImage();
		if (image != null) {
			String base64Image = Base64.getEncoder().encodeToString(image.getImageData());
			model.addAttribute("base64Image", base64Image); // Aggiungi al modello
		} else {
			System.out.println("L'immagine dello chef non è stata trovata");
		}
		model.addAttribute("opera", opera);
		return "public/opera.html";
	}

	/**
	 * metodo per mappare la view con la lista delle opere modificabile
	 */
	@GetMapping("/admin/manageOperas")
	public String manageOperas(Model model) {
		model.addAttribute("operas", this.operaService.getAllOperas());
		return "admin/manageOperas.html";
	}

	/**
	 * metodo che mappa la view-form per inserire un nuovo oggetto Opera
	 */
	@GetMapping(value = "/admin/formNewOpera")
	public String formNewOpera(Model model) {
		model.addAttribute("opera", new Opera());
		return "admin/formNewOpera.html";
	}

	/**
	 * metodo che mappa l'operazione di aggiunta di una nuova opera
	 */
	@PostMapping("/admin/newOpera")
	public String newOpera(@Valid @ModelAttribute Opera opera, BindingResult result,
			@RequestPart(value = "uploadedImage", required = false) MultipartFile uploadedImage, Model model,
			@RequestParam("operaId") Long operaId) {

		if (!result.hasErrors()) {
			try {
				if (uploadedImage != null && !uploadedImage.isEmpty()) {
					byte[] imageData = uploadedImage.getBytes();
					Image image = new Image();
					image.setImageData(imageData);
					imageService.saveImage(image); // Salva l'istanza Image nel repository

					opera.setImage(image); // Associa l'immagine all'opera
				}
				this.operaService.saveOpera(opera);

			} catch (IOException e) {
				// Gestione dell'errore di IO
				model.addAttribute("errorMessage", "errore nel caricamento dell'imagine");
				return "admin/formUpdateOpera.html"; // Torna alla pagina di aggiornamento in caso di errore
			}
			return "redirect:/public/operas";
		}
		return "admin/formUpdateOpera.html";
	}

	/**
	 * metodo che mappa la view per aggiungere attori all'Opera
	 */
	@GetMapping("/admin/updateActorsToOpera/{operaId}")
	public String updateActorsToOpera(@PathVariable("operaId") Long operaId, Model model) {
		model.addAttribute("opera", this.operaService.getOperaById(operaId).get());
		// recupero lista di attori
		List<Actor> actorsToAdd = this.operaService.actorsToAddInOpera(operaId);
		model.addAttribute("actorsToAdd", actorsToAdd);
		return "admin/actorsToAdd.html";
	}

	/**
	 * metodo che mappa l'operazione di aggiunta di un attore ad un'opera
	 */
	@PostMapping("/admin/addActorToOpera/{actorId}/{operaId}")
	public String addActorToOpera(@PathVariable("actorId") Long actorId, @PathVariable("operaId") Long operaId,
			Model model) {
		this.operaService.addNewActorToOpera(operaId, actorId);
		model.addAttribute("opera", this.operaService.getOperaById(operaId).get());
		List<Actor> actorsToAdd = this.operaService.actorsToAddInOpera(operaId);
		model.addAttribute("actorsToAdd", actorsToAdd);
		return "admin/actorsToAdd.html";
	}

	/**
	 * metodo che mappa l'operazione di rimozione di un attore ad un'opera
	 */
	@PostMapping("/admin/deleteActorFromOpera/{actorId}/{operaId}")
	public String deleteActorFromOpera(@PathVariable("actorId") Long actorId, @PathVariable("operaId") Long operaId,
			Model model) {
		this.operaService.deleteActorFromOpera(operaId, actorId);
		return "redirect:/admin/updateActorsToOpera/" + operaId;
	}

	/**
	 * metodo che mappa l'operazione di rimozione di una nuova opera
	 */
	@PostMapping("/admin/deleteOpera/{id}")
	public String deleteOpera(@PathVariable Long id) {
		this.operaService.freeAllTicketsOfOpera(id);// cancella biglietti relativi all'opera
		this.operaService.deleteOperaById(id); // cancella opera
		return "redirect:/admin/manageOperas";
	}

	/**
	 * metodo che mappa la view per aggiungere tickets all'Opera
	 */
	@GetMapping("/admin/updateTicketsToOpera/{operaId}")
	public String updateTicketsToOpera(@PathVariable("operaId") Long operaId, Model model) {
		model.addAttribute("opera", this.operaService.getOperaById(operaId).get());
		return "admin/manageTicketsAdmin.html";
	}

	/**
	 * metodo che mappa l'operazione di rimozione di un ticket da un'opera
	 */
	@PostMapping("/admin/deleteTicketFromOpera/{ticketId}/{operaId}")
	public String deleteTicketFromOpera(@PathVariable("ticketId") Long ticketId, @PathVariable("operaId") Long operaId,
			Model model) {
		this.operaService.deleteTicketFromOpera(operaId, ticketId);
		return "redirect:/admin/updateTicketsToOpera/" + operaId;
	}

	/**
	 * metodo che mappa la view per le opere in programma (che almeno almeno 1
	 * biglietto)
	 */
	@GetMapping("/client/operasOnPlan")
	public String getOperasOnPlan(Model model) {
		model.addAttribute("operas", this.operaService.getAllOperasWithTicketsAvailable());
		return "client/operasOnPlan.html";
	}

	/**
	 * metodo che mappa lista di biglietti per l'opera di cui id
	 */
	@GetMapping("/client/tickets/{operaId}")
	public String getTicketsForOpera(@PathVariable Long operaId, Model model) {
		Opera opera = this.operaService.getOperaById(operaId).get(); // recupero opera
		// recupera i biglietti non prenotati dell'opera recuperata
		List<Ticket> ticketsToAdd = this.ticketService.getAllTicketsNotOwnedInOpera(operaId);

		model.addAttribute("opera", opera);
		model.addAttribute("ticketsToAdd", ticketsToAdd);
		return "client/tickets";
	}

}

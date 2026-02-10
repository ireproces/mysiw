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

import it.uniroma3.siw.model.Client;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Ticket;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.ClientService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.ImageService;
import it.uniroma3.siw.service.OperaService;
import it.uniroma3.siw.service.TicketService;
import it.uniroma3.siw.service.UserService;

@Controller
@Transactional
public class ClientController {

	@Autowired
	private ClientService clientService;

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private UserService userService;

	@Autowired
	private ImageService imageService;

	@Autowired
	private OperaService operaService;

	@Autowired
	private TicketService ticketService;

	/**
	 * metodo che mappa la view per l'aggiornamento del profilo
	 */
	@GetMapping("/client/updateAccount")
	public String showUpdateProfileForm(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated()) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			User user = credentials.getUser(); // recupero l'utente
			Client client = user.getClient(); // recupero client

			model.addAttribute("client", client);
			return "client/formUpdateAccount.html";
		} else {
			return "redirect:/client/indexClient"; // Se non trova l'utente, reindirizza ad homepage
		}
	}

	/**
	 * metodo che mappa l'operazione di aggiornamento dei dati mancanti da un
	 * profilo post registrazione
	 */
	@PostMapping("/client/updateAcc")
	public String updateClient(@Valid @ModelAttribute("client") Client client, BindingResult bindingResult,
			@RequestPart(value = "uploadedImage", required = false) MultipartFile uploadedImage, Model model,
			@RequestParam("id") Long clientId) {
		if (!bindingResult.hasErrors()) {
			try {
				// Trova client nel repository *********
				Client foundClient = this.clientService.getClientById(clientId).get();

				// Aggiorna i campi
				foundClient.setDateOfBirth(client.getDateOfBirth());

				// Verifica se è stata fornita un'immagine e aggiorna l'immagine solo se c'è un
				// file
				if (uploadedImage != null && !uploadedImage.isEmpty()) {
					byte[] imageData = uploadedImage.getBytes();
					Image image = new Image();
					image.setImageData(imageData);
					imageService.saveImage(image); // Salva l'istanza ChefImage nel repository

					foundClient.setImage(image); // Associa l'immagine allo chef
				}
				// Salva le modifiche **********
				this.clientService.saveClient(foundClient);

				return "redirect:/client/indexClient"; // Reindirizza alla lista degli chef dopo l'aggiornamento

			} catch (IOException e) {
				// Gestione dell'errore di IO
				model.addAttribute("errorMessage", "errore nel caricamento dell'imagine");
				return "client/formUpdateAccount.html"; // Torna alla pagina di aggiornamento in caso di errore
			}
		}
		return "client/formUpdateAccount.html";
	}

	/**
	 * metodo che mappa la view per il profilo del client
	 */
	@GetMapping("/client/showAccount")
	public String showAccount(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			User user = credentials.getUser(); // recupero l'utente
			Client client = user.getClient(); // recupero client

			// Recupera l'immagine e convertila in Base64
			Image image = client.getImage();
			if (image != null) {
				String base64Image = Base64.getEncoder().encodeToString(image.getImageData());
				model.addAttribute("base64Image", base64Image); // Aggiungi al modello
			} else {
				System.out.println("L'immagine dell'utente non è stata trovata");
			}

			model.addAttribute("client", client);
			return "client/profile.html";
		} else {
			return "redirect:/client/indexClient"; // Se non trova l'utente, reindirizza ad homepage
		}
	}

	/**
	 * metodo per la view di gestione dei clienti
	 */
	@GetMapping("/admin/manageProfiles")
	public String manageClients(Model model) {
		model.addAttribute("clients", this.clientService.getAllClients()); // *****
		return "admin/manageProfiles.html";
	}

	/**
	 * mappa la view con la lista degli utenti
	 */
	@GetMapping("/admin/profiles")
	public String getProfiles(Model model) {
		model.addAttribute("clients", this.clientService.getAllClients()); // ******
		return "admin/profiles.html";
	}

	/**
	 * metodo per la view di ogni singolo profilo di cui id
	 */
	@GetMapping("/client/profile/{id}")
	public String getProfile(@PathVariable("id") Long id, Model model) {

		Client client = clientService.getClientById(id).get();
		if (client != null) {
			// Aggiungi lo chef al modello
			model.addAttribute("client", client);

			// Recupera l'immagine dello chef e convertila in Base64
			Image chefImage = client.getImage();
			if (chefImage != null) {
				String base64Image = Base64.getEncoder().encodeToString(chefImage.getImageData());
				model.addAttribute("base64Image", base64Image); // Aggiungi al modello
			} else {
				System.out.println("L'immagine del profilo non è stata trovata");
			}
		} else {
			System.out.println("Utente non trovato con ID: " + id);
		}
		return "client/profile";
	}

	/**
	 * metodo per mappare la rimozione di un Client
	 */
	@PostMapping("admin/deleteProfile/{id}")
	public String deleteProfile(@PathVariable Long id, Model model) {
		// Trova cliente da eliminare ****
		Client client = clientService.getClientById(id).get();
		if (client != null) {
			User user = userService.getUserByClient(client); // recupero utente
			if (user != null) {
				Credentials credentials = this.credentialsService.getCredentialsByUser(user); // recupero credenziali
				if (credentials != null) {
					this.clientService.freeAllTicketsOfClient(id);
					this.userService.deleteUser(user); // elimina utente
					this.credentialsService.deleteCredentials(credentials); // elimina credenziali
				}
			}
		} else {
			// Se lo chef non è trovato, reindirizza con un messaggio di errore
			return "redirect:/admin/indexAdmin";
		}
		model.addAttribute("clients", this.clientService.getAllClients());
		return "redirect:/admin/manageProfiles";
	}

	/**
	 * metodo che mappa la view indice per gli utenti loggati
	 */
	@GetMapping("/client/indexClient")
	public String chefIndex(Model model) {
		// Ottieni l'autenticazione corrente
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated()) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			User user = credentials.getUser();
			model.addAttribute("name", user.getName());
			model.addAttribute("surname", user.getSurname());

		}

		return "client/indexClient.html"; // Ritorna la pagina dell'index degli chef
	}

	/**
	 * metodo che filtra i profili in base al cognome
	 */
	@GetMapping("/admin/filterProfiles")
	public String filteredProfilesBySurname(@RequestParam(name = "surname", required = false) String surname,
			Model model) {
		List<Client> filteredProfiles;

		// Utilizziamo il metodo findBySurname del repository per cercare gli chef per
		// cognome
		if (surname != null && !surname.isEmpty()) {
			filteredProfiles = this.clientService.getClientsBySurname(surname);
		} else {
			// Se il parametro cognome è vuoto o non specificato, restituisci tutti gli chef
			filteredProfiles = (List<Client>) clientService.getAllClients();
		}

		model.addAttribute("clients", filteredProfiles);
		return "admin/profiles";
	}

	/**
	 * metodo per la view di gestione delle prenotazioni
	 */
	@GetMapping("/client/manageTicketsClient")
	public String manageTicketsClient(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			User user = credentials.getUser(); // recupero l'utente
			Client client = user.getClient(); // recupero client

			model.addAttribute("client", client);
			return "client/manageTicketsClient.html";
		}
		return "public/index";
	}

	/**
	 * metodo per l'operazione di aggiunta di un biglietto di cui id all'opera di
	 * cui id per la prenotazione del cliente loggato
	 */
	@PostMapping("/client/reserveTicketForOperaToClient/{ticketId}/{operaId}")
	public String reserveTicketForOperaToClient(@PathVariable Long ticketId, @PathVariable Long operaId, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			User user = credentials.getUser(); // recupero l'utente
			Client client = user.getClient(); // recupero client

			Ticket t = this.ticketService.getTicketById(ticketId).get(); // recupero biglietto
			client.getTickets().add(t); // aggiungo biglietto all'utente
			t.setOwner(client); // aggiungo utente al biglietto

			model.addAttribute("client", client);
			return "/client/manageTicketsClient";
		}
		model.addAttribute("opera", this.operaService.getOperaById(operaId).get());
		return "client/tickets";
	}

	/**
	 * metodo che mappa l'operazione di rimozione di una prenotazione di un
	 * biglietto
	 */
	@PostMapping("/client/deleteReservationForClient/{ticketId}")
	public String deleteReservationForClient(@PathVariable("ticketId") Long ticketId, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			User user = credentials.getUser(); // recupero l'utente
			Client client = user.getClient(); // recupero client

			Ticket t = this.ticketService.getTicketById(ticketId).get(); // recupero biglietto
			t.setOwner(null);
			client.getTickets().remove(t);

			model.addAttribute("client", client);
			return "client/manageTicketsClient";
		}
		return "public/index";
	}
}

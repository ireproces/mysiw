package it.uniroma3.siw.controller;

import java.util.Locale;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.UserService;

@Controller
public class AuthController {

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private UserService userService;

	@GetMapping(value = "/public/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("credentials", new Credentials());
		return "public/formRegisterUser";
	}

	@GetMapping(value = "/public/login")
	public String showLoginForm(Model model) {
		return "public/formLogin";
	}

	@GetMapping(value = "/")
	public String index() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
			return "public/index.html";
		} else {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
				return "admin/indexAdmin.html";
			} else if (credentials.getRole().equals(Credentials.DEFAULT_ROLE)) {
				return "redirect:/client/indexClient";
			}
		}
		return "public/index.html";
	}

	@PostMapping(value = { "/public/register" })
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult userBindingResult,
			@Valid @ModelAttribute("credentials") Credentials credentials, BindingResult credentialsBindingResult,
			Model model, Locale locale) {

		// Controlla se l'username esiste già
		if (credentialsService.exists(credentials)) {
			String errorMessage = messageSource.getMessage("error.username.duplicate", null, locale);
			credentialsBindingResult.rejectValue("username", "error.username.duplicate", errorMessage);
		}

		if (!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
			userService.saveUser(user);
			credentials.setUser(user);
			credentialsService.saveCredentials(credentials);

			model.addAttribute("user", user);
			return "/public/registrationSuccessful";
		}

		return "/public/registerUser";
	}

}

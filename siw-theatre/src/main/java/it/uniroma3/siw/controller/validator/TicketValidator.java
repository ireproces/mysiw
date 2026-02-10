package it.uniroma3.siw.controller.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.model.Ticket;
import it.uniroma3.siw.repository.TicketRepository;

@Component
public class TicketValidator implements Validator {

	@Autowired
	private TicketRepository ticketRepo;

	@Override
	public void validate(Object o, Errors errors) {
		Ticket recipe = (Ticket) o;
		if (recipe.getDateEvent() != null && recipe.getType() != null
				&& ticketRepo.existsByDateEventAndType(recipe.getDateEvent(), recipe.getPrice())) {
			errors.reject("recipe.duplicate");
		}
	}

	@Override
	public boolean supports(Class<?> aClass) {
		return Ticket.class.equals(aClass);
	}
}

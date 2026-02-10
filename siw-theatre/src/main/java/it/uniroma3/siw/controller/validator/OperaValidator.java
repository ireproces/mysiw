package it.uniroma3.siw.controller.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.model.Opera;
import it.uniroma3.siw.repository.OperaRepository;

@Component
public class OperaValidator implements Validator {

	@Autowired
	private OperaRepository operaRepo;

	@Override
	public void validate(Object o, Errors errors) {
		Opera opera = (Opera) o;
		if (opera.getName() != null && operaRepo.existsByName(opera.getName())) {
			errors.rejectValue("name", "opera.duplicate", "Opera with this name already exists");
		}
	}

	@Override
	public boolean supports(Class<?> aClass) {
		return Opera.class.equals(aClass);
	}
}

package it.uniroma3.siw.model;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotBlank
	private String price;

	@NotNull
	private String type;

	@NotNull
	@Future
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateEvent;

	@ManyToOne
	private Client owner;

	@ManyToOne
	private Opera opera;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDate getDateEvent() {
		return dateEvent;
	}

	public void setDateEvent(LocalDate date) {
		this.dateEvent = date;
	}

	public Client getOwner() {
		return owner;
	}

	public void setOwner(Client owner) {
		this.owner = owner;
	}

	public Opera getOpera() {
		return opera;
	}

	public void setOpera(Opera opera) {
		this.opera = opera;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, opera, price);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ticket other = (Ticket) obj;
		return Objects.equals(type, other.type) && Objects.equals(opera, other.opera)
				&& Objects.equals(price, other.price);
	}

}

package it.uniroma3.siw.model;

import java.util.Base64;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class Image {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Lob
	private byte[] imageData; // Sequenza di byte dell'immagine

	public Image() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public byte[] getImageData() {
		return imageData;
	}

	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}

	// Metodo per ottenere la rappresentazione Base64 dell'immagine
	public String getImageBase64() {
		return Base64.getEncoder().encodeToString(this.imageData);
	}

	// Metodo per convertire una stringa Base64 in byte array
	public static byte[] fromBase64(String base64) {
		return Base64.getDecoder().decode(base64);
	}
}

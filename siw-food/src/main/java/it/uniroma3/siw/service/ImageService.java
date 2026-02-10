package it.uniroma3.siw.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.repository.ImageRepository;

@Service
public class ImageService {

	@Autowired
	private ImageRepository imageRepo;

	public void saveImage(Image image) {
		this.imageRepo.save(image);
	}

	public void saveAllImages(List<Image> images) {
		this.imageRepo.saveAll(images);
	}
}

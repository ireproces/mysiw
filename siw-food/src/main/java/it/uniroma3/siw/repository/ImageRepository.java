package it.uniroma3.siw.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.uniroma3.siw.model.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

	Optional<Image> findById(Long id);
}

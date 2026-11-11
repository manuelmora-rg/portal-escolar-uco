package com.portal.escolar.repositories;

import com.portal.escolar.entities.GalleryAlbum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GalleryAlbumRepository extends JpaRepository<GalleryAlbum, UUID> {

    List<GalleryAlbum> findByPublishedTrueOrderByCreatedAtDesc();
}

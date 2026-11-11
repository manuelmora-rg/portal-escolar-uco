package com.portal.escolar.repositories;

import com.portal.escolar.entities.DomainEnums.NewsStatus;
import com.portal.escolar.entities.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {

    List<News> findByStatusOrderByPublishedAtDesc(NewsStatus status);

    Optional<News> findBySlugAndStatus(String slug, NewsStatus status);

    boolean existsBySlug(String slug);
}

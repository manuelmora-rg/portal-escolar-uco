package com.portal.escolar.entities;

import com.portal.escolar.entities.DomainEnums.NewsStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
public class News extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 160)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 40)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NewsStatus status = NewsStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "author_id")
    private UUID authorId;
}

package com.portal.escolar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "parent_profile")
@Getter
@Setter
@NoArgsConstructor
public class ParentProfile extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "document_number", length = 40)
    private String documentNumber;

    @Column(length = 30)
    private String phone;

    @Column(length = 120)
    private String occupation;

    @Column(length = 200)
    private String address;
}

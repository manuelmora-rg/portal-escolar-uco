package com.portal.escolar.services;

import com.portal.escolar.dto.AdminDtos.AdminUserDto;
import com.portal.escolar.dto.AdminDtos.CreateUserRequest;
import com.portal.escolar.dto.AdminDtos.CreatedUserDto;
import com.portal.escolar.dto.AdminDtos.InvitationCreatedDto;
import com.portal.escolar.dto.AdminDtos.InvitationDto;
import com.portal.escolar.dto.AdminDtos.InvitationInfoDto;
import com.portal.escolar.dto.AdminDtos.InvitationRequest;
import com.portal.escolar.dto.AdminDtos.PasswordResetDto;
import com.portal.escolar.dto.AdminDtos.ResetPasswordRequest;
import com.portal.escolar.dto.AdminDtos.UpdateUserRequest;
import com.portal.escolar.dto.AuthDtos.AcceptInvitationRequest;
import com.portal.escolar.dto.AuthDtos.AcceptedInvitationDto;
import com.portal.escolar.entities.DomainEnums.Role;
import com.portal.escolar.entities.DomainEnums.StudentStatus;
import com.portal.escolar.entities.Invitation;
import com.portal.escolar.entities.ParentProfile;
import com.portal.escolar.entities.StudentProfile;
import com.portal.escolar.entities.TeacherProfile;
import com.portal.escolar.entities.User;
import com.portal.escolar.exceptions.BusinessException;
import com.portal.escolar.exceptions.ConflictException;
import com.portal.escolar.exceptions.NotFoundException;
import com.portal.escolar.repositories.InvitationRepository;
import com.portal.escolar.repositories.ParentProfileRepository;
import com.portal.escolar.repositories.RefreshTokenRepository;
import com.portal.escolar.repositories.StudentProfileRepository;
import com.portal.escolar.repositories.TeacherProfileRepository;
import com.portal.escolar.repositories.UserRepository;
import com.portal.escolar.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Administración de identidad: usuarios, invitaciones y restablecimiento de contraseña.
 * Solo se invoca desde endpoints protegidos para ADMIN, salvo la aceptación de invitación.
 */
@Service
@RequiredArgsConstructor
public class IdentityAdminService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int INVITATION_DAYS = 7;

    private final UserRepository userRepository;
    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherRepository;
    private final ParentProfileRepository parentRepository;
    private final InvitationRepository invitationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    // ===== Usuarios =====

    public List<AdminUserDto> listUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getRole)
                        .thenComparing(User::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(User::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toUserDto)
                .toList();
    }

    public AdminUserDto getUser(UUID userId) {
        return toUserDto(findUser(userId));
    }

    @Transactional
    public CreatedUserDto createUser(UserPrincipal actor, CreateUserRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        Role role = parseRole(request.role());
        validateProfileFields(role, request.documentNumber(), request.employeeCode());

        boolean generated = request.temporaryPassword() == null || request.temporaryPassword().isBlank();
        String temporaryPassword = generated ? randomPassword() : request.temporaryPassword();

        User user = new User();
        user.setEmail(email);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setEnabled(true);
        user.setMustChangePassword(true);
        userRepository.save(user);

        createProfile(user, role, request.documentNumber(), request.phone(),
                request.employeeCode(), request.specialty());

        auditService.record(actor, "USER_CREATED", "app_user", user.getId().toString(),
                Map.of("email", email, "role", role.name()));

        return new CreatedUserDto(toUserDto(user), generated ? temporaryPassword : null);
    }

    @Transactional
    public AdminUserDto updateUser(UserPrincipal actor, UUID userId, UpdateUserRequest request) {
        User user = findUser(userId);
        String email = normalizeEmail(request.email());
        if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        String previousEmail = user.getEmail();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        userRepository.save(user);

        auditService.record(actor, "USER_UPDATED", "app_user", user.getId().toString(),
                Map.of("previousEmail", previousEmail, "email", email));

        return toUserDto(user);
    }

    @Transactional
    public AdminUserDto setStatus(UserPrincipal actor, UUID userId, boolean enabled) {
        User user = findUser(userId);
        if (!enabled && user.getId().equals(actor.id())) {
            throw new BusinessException("No puedes desactivar tu propia cuenta");
        }
        if (!enabled && user.getRole() == Role.ADMIN
                && userRepository.countByRoleAndEnabledTrue(Role.ADMIN) <= 1) {
            throw new BusinessException("Debe existir al menos un administrador activo");
        }
        user.setEnabled(enabled);
        userRepository.save(user);
        if (!enabled) {
            refreshTokenRepository.revokeAllByUserId(user.getId());
        }

        auditService.record(actor, enabled ? "USER_ENABLED" : "USER_DISABLED", "app_user",
                user.getId().toString(), Map.of("email", user.getEmail()));

        return toUserDto(user);
    }

    @Transactional
    public PasswordResetDto resetPassword(UserPrincipal actor, UUID userId, ResetPasswordRequest request) {
        User user = findUser(userId);
        boolean generated = request.temporaryPassword() == null || request.temporaryPassword().isBlank();
        String temporaryPassword = generated ? randomPassword() : request.temporaryPassword();

        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(user.getId());

        auditService.record(actor, "USER_PASSWORD_RESET", "app_user", user.getId().toString(),
                Map.of("email", user.getEmail()));

        return new PasswordResetDto(generated ? temporaryPassword : null);
    }

    // ===== Invitaciones =====

    public List<InvitationDto> listInvitations() {
        return invitationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toInvitationDto)
                .toList();
    }

    @Transactional
    public InvitationCreatedDto createInvitation(UserPrincipal actor, InvitationRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        Role role = parseRole(request.role());

        Instant now = Instant.now();
        for (Invitation previous : invitationRepository
                .findByEmailIgnoreCaseAndUsedAtIsNullAndRevokedAtIsNull(email)) {
            previous.setRevokedAt(now);
            invitationRepository.save(previous);
        }

        String rawToken = randomToken();
        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setTokenHash(hash(rawToken));
        invitation.setExpiresAt(now.plus(INVITATION_DAYS, ChronoUnit.DAYS));
        invitation.setCreatedBy(actor.id());
        invitationRepository.save(invitation);

        auditService.record(actor, "INVITATION_CREATED", "invitation", invitation.getId().toString(),
                Map.of("email", email, "role", role.name()));

        return new InvitationCreatedDto(toInvitationDto(invitation), rawToken);
    }

    public InvitationInfoDto invitationInfo(String token) {
        Invitation invitation = findInvitation(token);
        return new InvitationInfoDto(
                invitation.getEmail(),
                invitation.getRole().name(),
                invitation.getExpiresAt(),
                invitation.isUsable()
        );
    }

    @Transactional
    public AcceptedInvitationDto acceptInvitation(AcceptInvitationRequest request) {
        Invitation invitation = findInvitation(request.token());
        if (!invitation.isUsable()) {
            throw new BusinessException("La invitación expiró o ya fue utilizada");
        }
        String email = invitation.getEmail();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        Role role = invitation.getRole();
        validateProfileFields(role, request.documentNumber(), request.employeeCode());

        User user = new User();
        user.setEmail(email);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setMustChangePassword(false);
        userRepository.save(user);

        createProfile(user, role, request.documentNumber(), request.phone(),
                request.employeeCode(), request.specialty());

        invitation.setUsedAt(Instant.now());
        invitationRepository.save(invitation);

        auditService.record(null, "INVITATION_ACCEPTED", "app_user", user.getId().toString(),
                Map.of("email", email, "role", role.name()));

        return new AcceptedInvitationDto(user.getId(), email, role.name(),
                "Cuenta activada correctamente. Ya puedes iniciar sesión.");
    }

    // ===== Apoyo =====

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private Invitation findInvitation(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("El token de invitación es obligatorio");
        }
        return invitationRepository.findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(hash(token.trim()))
                .orElseThrow(() -> new NotFoundException("Invitación no encontrada o ya utilizada"));
    }

    private void createProfile(User user, Role role, String documentNumber, String phone,
                               String employeeCode, String specialty) {
        switch (role) {
            case STUDENT -> {
                StudentProfile profile = new StudentProfile();
                profile.setUserId(user.getId());
                profile.setDocumentNumber(documentNumber.trim());
                profile.setPhone(trimToNull(phone));
                profile.setStatus(StudentStatus.ACTIVE);
                profile.setAdmissionDate(LocalDate.now());
                studentRepository.save(profile);
            }
            case TEACHER -> {
                TeacherProfile profile = new TeacherProfile();
                profile.setUserId(user.getId());
                profile.setEmployeeCode(employeeCode.trim());
                profile.setPhone(trimToNull(phone));
                profile.setSpecialty(trimToNull(specialty));
                profile.setHireDate(LocalDate.now());
                teacherRepository.save(profile);
            }
            case PARENT -> {
                ParentProfile profile = new ParentProfile();
                profile.setUserId(user.getId());
                profile.setDocumentNumber(trimToNull(documentNumber));
                profile.setPhone(trimToNull(phone));
                parentRepository.save(profile);
            }
            case ADMIN -> {
                // El administrador no requiere perfil adicional.
            }
        }
    }

    private void validateProfileFields(Role role, String documentNumber, String employeeCode) {
        if (role == Role.STUDENT) {
            if (documentNumber == null || documentNumber.isBlank()) {
                throw new BusinessException("El documento del estudiante es obligatorio");
            }
            if (studentRepository.existsByDocumentNumber(documentNumber.trim())) {
                throw new ConflictException("Ya existe un estudiante con ese documento");
            }
        }
        if (role == Role.TEACHER) {
            if (employeeCode == null || employeeCode.isBlank()) {
                throw new BusinessException("El código de empleado del docente es obligatorio");
            }
            if (teacherRepository.existsByEmployeeCode(employeeCode.trim())) {
                throw new ConflictException("Ya existe un docente con ese código de empleado");
            }
        }
    }

    private AdminUserDto toUserDto(User user) {
        return new AdminUserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.isEnabled(),
                user.isMustChangePassword(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }

    private InvitationDto toInvitationDto(Invitation invitation) {
        String createdBy = invitation.getCreatedBy() == null ? null
                : userRepository.findById(invitation.getCreatedBy())
                .map(User::getFullName).orElse(null);
        return new InvitationDto(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getRole().name(),
                invitation.getExpiresAt(),
                invitation.getUsedAt(),
                invitation.getRevokedAt(),
                invitation.getCreatedAt(),
                createdBy
        );
    }

    private Role parseRole(String value) {
        try {
            return Role.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new BusinessException("Rol inválido: " + value);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String randomPassword() {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}

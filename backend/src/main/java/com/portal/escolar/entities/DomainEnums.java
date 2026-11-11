package com.portal.escolar.entities;

/** Enumeraciones del dominio, almacenadas como texto en la base de datos. */
public final class DomainEnums {

    public enum Role { ADMIN, TEACHER, STUDENT, PARENT }

    public enum StudentStatus { ACTIVE, INACTIVE, GRADUATED }

    public enum Relationship { FATHER, MOTHER, GUARDIAN, OTHER }

    public enum EnrollmentStatus { ENROLLED, WITHDRAWN, TRANSFERRED, GRADUATED }

    public enum GradeType { EXAM, HOMEWORK, QUIZ, PROJECT, PARTICIPATION, OTHER }

    public enum AssignmentStatus { DRAFT, PUBLISHED, CLOSED }

    public enum SubmissionStatus { PENDING, SUBMITTED, LATE, GRADED, RETURNED }

    public enum MaterialType { PDF, LINK, VIDEO, DOC, IMAGE, OTHER }

    public enum AttendanceStatus { PRESENT, ABSENT, LATE, EXCUSED }

    public enum ObservationType { ACADEMIC, BEHAVIORAL, PRAISE, FOLLOW_UP }

    public enum ObservationVisibility { STUDENT, PARENT, INTERNAL }

    public enum NewsStatus { DRAFT, PUBLISHED, ARCHIVED }

    public enum EventType { ACADEMIC, HOLIDAY, MEETING, EXAM, OTHER }

    public enum NotificationType { INFO, GRADE, ASSIGNMENT, ATTENDANCE, MEETING, SYSTEM }

    public enum ContactStatus { NEW, READ, ANSWERED, ARCHIVED }

    private DomainEnums() {
    }
}

-- Portal Escolar - Esquema inicial
-- PostgreSQL 18. Esquema `portal` (creado por Flyway).
-- Las claves son UUID generadas por la aplicación.

-- =====================================================================
-- Identidad y acceso
-- =====================================================================

CREATE TABLE app_user (
    id                   uuid PRIMARY KEY,
    email                varchar(160) NOT NULL,
    password_hash        varchar(100) NOT NULL,
    first_name           varchar(80)  NOT NULL,
    last_name            varchar(80)  NOT NULL,
    role                 varchar(20)  NOT NULL,
    enabled              boolean      NOT NULL DEFAULT true,
    must_change_password boolean      NOT NULL DEFAULT false,
    last_login_at        timestamptz,
    created_at           timestamptz  NOT NULL DEFAULT now(),
    updated_at           timestamptz  NOT NULL DEFAULT now(),
    version              bigint       NOT NULL DEFAULT 0,
    CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT', 'PARENT'))
);
CREATE UNIQUE INDEX ux_app_user_email ON app_user (lower(email));

CREATE TABLE student_profile (
    id              uuid PRIMARY KEY,
    user_id         uuid NOT NULL UNIQUE REFERENCES app_user (id),
    document_type   varchar(20),
    document_number varchar(40) NOT NULL,
    birth_date      date,
    gender          varchar(20),
    address         varchar(200),
    phone           varchar(30),
    photo_url       varchar(500),
    status          varchar(20) NOT NULL DEFAULT 'ACTIVE',
    admission_date  date,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    version         bigint NOT NULL DEFAULT 0,
    CONSTRAINT ux_student_document UNIQUE (document_number),
    CONSTRAINT ck_student_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'GRADUATED'))
);

CREATE TABLE teacher_profile (
    id            uuid PRIMARY KEY,
    user_id       uuid NOT NULL UNIQUE REFERENCES app_user (id),
    employee_code varchar(30) NOT NULL UNIQUE,
    hire_date     date,
    specialty     varchar(120),
    phone         varchar(30),
    bio           text,
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    version       bigint NOT NULL DEFAULT 0
);

CREATE TABLE parent_profile (
    id              uuid PRIMARY KEY,
    user_id         uuid NOT NULL UNIQUE REFERENCES app_user (id),
    document_number varchar(40),
    phone           varchar(30),
    occupation      varchar(120),
    address         varchar(200),
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    version         bigint NOT NULL DEFAULT 0
);

CREATE TABLE parent_student (
    id           uuid PRIMARY KEY,
    parent_id    uuid NOT NULL REFERENCES parent_profile (id),
    student_id   uuid NOT NULL REFERENCES student_profile (id),
    relationship varchar(20) NOT NULL,
    authorized   boolean NOT NULL DEFAULT true,
    created_at   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ux_parent_student UNIQUE (parent_id, student_id),
    CONSTRAINT ck_parent_relationship CHECK (relationship IN ('FATHER', 'MOTHER', 'GUARDIAN', 'OTHER'))
);
CREATE INDEX ix_parent_student_student ON parent_student (student_id);

CREATE TABLE refresh_token (
    id         uuid PRIMARY KEY,
    user_id    uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL UNIQUE,
    expires_at timestamptz NOT NULL,
    revoked    boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    user_agent varchar(300),
    ip         varchar(45)
);
CREATE INDEX ix_refresh_token_user ON refresh_token (user_id);

-- =====================================================================
-- Estructura académica
-- =====================================================================

CREATE TABLE academic_year (
    id         uuid PRIMARY KEY,
    name       varchar(20) NOT NULL UNIQUE,
    start_date date NOT NULL,
    end_date   date NOT NULL,
    active     boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version    bigint NOT NULL DEFAULT 0
);

CREATE TABLE grade_level (
    id         uuid PRIMARY KEY,
    name       varchar(40) NOT NULL UNIQUE,
    sort_order integer NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version    bigint NOT NULL DEFAULT 0
);

CREATE TABLE academic_period (
    id               uuid PRIMARY KEY,
    academic_year_id uuid NOT NULL REFERENCES academic_year (id),
    name             varchar(60) NOT NULL,
    sort_order       integer NOT NULL,
    start_date       date NOT NULL,
    end_date         date NOT NULL,
    weight           numeric(5, 2) NOT NULL DEFAULT 25.00,
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now(),
    version          bigint NOT NULL DEFAULT 0,
    CONSTRAINT ux_period_year_order UNIQUE (academic_year_id, sort_order)
);

CREATE TABLE course (
    id                 uuid PRIMARY KEY,
    academic_year_id   uuid NOT NULL REFERENCES academic_year (id),
    grade_level_id     uuid NOT NULL REFERENCES grade_level (id),
    group_name         varchar(10) NOT NULL,
    homeroom_teacher_id uuid REFERENCES teacher_profile (id),
    capacity           integer NOT NULL DEFAULT 35,
    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now(),
    version            bigint NOT NULL DEFAULT 0,
    CONSTRAINT ux_course_year_grade_group UNIQUE (academic_year_id, grade_level_id, group_name)
);

CREATE TABLE subject (
    id          uuid PRIMARY KEY,
    code        varchar(20) NOT NULL UNIQUE,
    name        varchar(100) NOT NULL,
    area        varchar(60),
    description text,
    active      boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),
    version     bigint NOT NULL DEFAULT 0
);

CREATE TABLE course_subject (
    id           uuid PRIMARY KEY,
    course_id    uuid NOT NULL REFERENCES course (id),
    subject_id   uuid NOT NULL REFERENCES subject (id),
    teacher_id   uuid NOT NULL REFERENCES teacher_profile (id),
    weekly_hours integer NOT NULL DEFAULT 4,
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    version      bigint NOT NULL DEFAULT 0,
    CONSTRAINT ux_course_subject_teacher UNIQUE (course_id, subject_id, teacher_id)
);
CREATE INDEX ix_course_subject_teacher ON course_subject (teacher_id);

CREATE TABLE enrollment (
    id               uuid PRIMARY KEY,
    student_id       uuid NOT NULL REFERENCES student_profile (id),
    course_id        uuid NOT NULL REFERENCES course (id),
    academic_year_id uuid NOT NULL REFERENCES academic_year (id),
    enrollment_date  date NOT NULL,
    status           varchar(20) NOT NULL DEFAULT 'ENROLLED',
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now(),
    version          bigint NOT NULL DEFAULT 0,
    CONSTRAINT ux_enrollment_student_year UNIQUE (student_id, academic_year_id),
    CONSTRAINT ck_enrollment_status CHECK (status IN ('ENROLLED', 'WITHDRAWN', 'TRANSFERRED', 'GRADUATED'))
);
CREATE INDEX ix_enrollment_course ON enrollment (course_id);

CREATE TABLE schedule_block (
    id                uuid PRIMARY KEY,
    course_subject_id uuid NOT NULL REFERENCES course_subject (id) ON DELETE CASCADE,
    day_of_week       smallint NOT NULL,
    start_time        time NOT NULL,
    end_time          time NOT NULL,
    classroom         varchar(40),
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_schedule_day CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_schedule_time CHECK (end_time > start_time),
    CONSTRAINT ux_schedule_slot UNIQUE (course_subject_id, day_of_week, start_time)
);
CREATE INDEX ix_schedule_course_subject ON schedule_block (course_subject_id);

-- =====================================================================
-- Evaluación, tareas y asistencia
-- =====================================================================

CREATE TABLE grade (
    id                uuid PRIMARY KEY,
    enrollment_id     uuid NOT NULL REFERENCES enrollment (id),
    course_subject_id uuid NOT NULL REFERENCES course_subject (id),
    period_id         uuid NOT NULL REFERENCES academic_period (id),
    title             varchar(120) NOT NULL,
    type              varchar(20) NOT NULL DEFAULT 'OTHER',
    score             numeric(4, 2) NOT NULL,
    weight            numeric(5, 2) NOT NULL DEFAULT 100.00,
    graded_by         uuid NOT NULL REFERENCES app_user (id),
    comment           varchar(500),
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now(),
    version           bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_grade_score CHECK (score >= 0),
    CONSTRAINT ck_grade_weight CHECK (weight > 0 AND weight <= 100),
    CONSTRAINT ck_grade_type CHECK (type IN ('EXAM', 'HOMEWORK', 'QUIZ', 'PROJECT', 'PARTICIPATION', 'OTHER'))
);
CREATE INDEX ix_grade_enrollment ON grade (enrollment_id);
CREATE INDEX ix_grade_course_subject ON grade (course_subject_id);
CREATE INDEX ix_grade_period ON grade (period_id);

CREATE TABLE grade_audit (
    id          uuid PRIMARY KEY,
    grade_id    uuid NOT NULL REFERENCES grade (id) ON DELETE CASCADE,
    changed_by  uuid NOT NULL REFERENCES app_user (id),
    old_score   numeric(4, 2),
    new_score   numeric(4, 2),
    reason      varchar(300),
    changed_at  timestamptz NOT NULL DEFAULT now(),
    ip          varchar(45)
);
CREATE INDEX ix_grade_audit_grade ON grade_audit (grade_id);

CREATE TABLE assignment (
    id                uuid PRIMARY KEY,
    course_subject_id uuid NOT NULL REFERENCES course_subject (id) ON DELETE CASCADE,
    title             varchar(150) NOT NULL,
    description       text,
    due_at            timestamptz NOT NULL,
    max_score         numeric(5, 2) NOT NULL DEFAULT 5.00,
    status            varchar(20) NOT NULL DEFAULT 'DRAFT',
    attachment_url    varchar(500),
    created_by        uuid NOT NULL REFERENCES app_user (id),
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now(),
    version           bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_assignment_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED'))
);
CREATE INDEX ix_assignment_course_subject ON assignment (course_subject_id);

CREATE TABLE submission (
    id            uuid PRIMARY KEY,
    assignment_id uuid NOT NULL REFERENCES assignment (id) ON DELETE CASCADE,
    student_id    uuid NOT NULL REFERENCES student_profile (id),
    submitted_at  timestamptz,
    file_url      varchar(500),
    comment       varchar(1000),
    score         numeric(5, 2),
    feedback      text,
    status        varchar(20) NOT NULL DEFAULT 'PENDING',
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ux_submission_assignment_student UNIQUE (assignment_id, student_id),
    CONSTRAINT ck_submission_status CHECK (status IN ('PENDING', 'SUBMITTED', 'LATE', 'GRADED', 'RETURNED'))
);

CREATE TABLE material (
    id                 uuid PRIMARY KEY,
    course_subject_id  uuid NOT NULL REFERENCES course_subject (id) ON DELETE CASCADE,
    title              varchar(150) NOT NULL,
    description        text,
    type               varchar(20) NOT NULL DEFAULT 'LINK',
    url                varchar(500) NOT NULL,
    published_at       timestamptz NOT NULL DEFAULT now(),
    visible_to_parents boolean NOT NULL DEFAULT true,
    created_by         uuid NOT NULL REFERENCES app_user (id),
    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_material_type CHECK (type IN ('PDF', 'LINK', 'VIDEO', 'DOC', 'IMAGE', 'OTHER'))
);
CREATE INDEX ix_material_course_subject ON material (course_subject_id);

CREATE TABLE attendance_record (
    id              uuid PRIMARY KEY,
    enrollment_id   uuid NOT NULL REFERENCES enrollment (id) ON DELETE CASCADE,
    attendance_date date NOT NULL,
    status          varchar(20) NOT NULL,
    justification   varchar(300),
    recorded_by     uuid NOT NULL REFERENCES app_user (id),
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ux_attendance_enrollment_date UNIQUE (enrollment_id, attendance_date),
    CONSTRAINT ck_attendance_status CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED'))
);
CREATE INDEX ix_attendance_date ON attendance_record (attendance_date);

CREATE TABLE observation (
    id                uuid PRIMARY KEY,
    student_id        uuid NOT NULL REFERENCES student_profile (id),
    teacher_id        uuid NOT NULL REFERENCES teacher_profile (id),
    course_subject_id uuid REFERENCES course_subject (id),
    type              varchar(20) NOT NULL DEFAULT 'ACADEMIC',
    description       text NOT NULL,
    visibility        varchar(20) NOT NULL DEFAULT 'PARENT',
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_observation_type CHECK (type IN ('ACADEMIC', 'BEHAVIORAL', 'PRAISE', 'FOLLOW_UP')),
    CONSTRAINT ck_observation_visibility CHECK (visibility IN ('STUDENT', 'PARENT', 'INTERNAL'))
);
CREATE INDEX ix_observation_student ON observation (student_id);

-- =====================================================================
-- Comunicación y contenido
-- =====================================================================

CREATE TABLE news (
    id           uuid PRIMARY KEY,
    slug         varchar(160) NOT NULL UNIQUE,
    title        varchar(200) NOT NULL,
    summary      varchar(500),
    content      text NOT NULL,
    image_url    varchar(500),
    category     varchar(40),
    status       varchar(20) NOT NULL DEFAULT 'DRAFT',
    published_at timestamptz,
    author_id    uuid REFERENCES app_user (id),
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    version      bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_news_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);
CREATE INDEX ix_news_published ON news (status, published_at DESC);

CREATE TABLE event (
    id          uuid PRIMARY KEY,
    title       varchar(200) NOT NULL,
    description text,
    starts_at   timestamptz NOT NULL,
    ends_at     timestamptz,
    all_day     boolean NOT NULL DEFAULT false,
    location    varchar(200),
    type        varchar(20) NOT NULL DEFAULT 'ACADEMIC',
    published   boolean NOT NULL DEFAULT true,
    created_by  uuid REFERENCES app_user (id),
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_event_type CHECK (type IN ('ACADEMIC', 'HOLIDAY', 'MEETING', 'EXAM', 'OTHER'))
);
CREATE INDEX ix_event_starts ON event (starts_at);

CREATE TABLE meeting (
    id           uuid PRIMARY KEY,
    title        varchar(200) NOT NULL,
    description  text,
    scheduled_at timestamptz NOT NULL,
    course_id    uuid REFERENCES course (id),
    teacher_id   uuid REFERENCES teacher_profile (id),
    location     varchar(200),
    status       varchar(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_meeting_status CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'CANCELLED', 'FINISHED'))
);

CREATE TABLE meeting_attendance (
    id           uuid PRIMARY KEY,
    meeting_id   uuid NOT NULL REFERENCES meeting (id) ON DELETE CASCADE,
    parent_id    uuid NOT NULL REFERENCES parent_profile (id),
    status       varchar(20) NOT NULL DEFAULT 'PENDING',
    confirmed_at timestamptz,
    CONSTRAINT ux_meeting_parent UNIQUE (meeting_id, parent_id),
    CONSTRAINT ck_meeting_attendance_status CHECK (status IN ('PENDING', 'CONFIRMED', 'DECLINED', 'ATTENDED', 'ABSENT'))
);

CREATE TABLE notification (
    id         uuid PRIMARY KEY,
    user_id    uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    title      varchar(150) NOT NULL,
    message    varchar(500) NOT NULL,
    type       varchar(20) NOT NULL DEFAULT 'INFO',
    link       varchar(300),
    read_at    timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_notification_type CHECK (type IN ('INFO', 'GRADE', 'ASSIGNMENT', 'ATTENDANCE', 'MEETING', 'SYSTEM'))
);
CREATE INDEX ix_notification_user ON notification (user_id, created_at DESC);

CREATE TABLE contact_message (
    id         uuid PRIMARY KEY,
    full_name  varchar(120) NOT NULL,
    email      varchar(160) NOT NULL,
    phone      varchar(30),
    subject    varchar(150) NOT NULL,
    message    text NOT NULL,
    status     varchar(20) NOT NULL DEFAULT 'NEW',
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_contact_status CHECK (status IN ('NEW', 'READ', 'ANSWERED', 'ARCHIVED'))
);

CREATE TABLE gallery_album (
    id          uuid PRIMARY KEY,
    title       varchar(150) NOT NULL,
    description text,
    cover_url   varchar(500),
    published   boolean NOT NULL DEFAULT false,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE gallery_image (
    id        uuid PRIMARY KEY,
    album_id  uuid NOT NULL REFERENCES gallery_album (id) ON DELETE CASCADE,
    image_url varchar(500) NOT NULL,
    caption   varchar(300),
    sort_order integer NOT NULL DEFAULT 0
);

CREATE TABLE site_setting (
    setting_key   varchar(60) PRIMARY KEY,
    setting_value jsonb NOT NULL,
    updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE audit_log (
    id         uuid PRIMARY KEY,
    user_id    uuid REFERENCES app_user (id),
    action     varchar(60) NOT NULL,
    entity     varchar(60),
    entity_id  varchar(60),
    details    jsonb,
    ip         varchar(45),
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_audit_log_created ON audit_log (created_at DESC);
CREATE INDEX ix_audit_log_entity ON audit_log (entity, entity_id);

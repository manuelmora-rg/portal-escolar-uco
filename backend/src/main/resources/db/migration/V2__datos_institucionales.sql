-- Datos iniciales institucionales y estructura académica 2026.
-- No incluye personas: los usuarios de demostración se crean en DemoDataInitializer
-- con contraseñas hasheadas con BCrypt.

INSERT INTO site_setting (setting_key, setting_value) VALUES
('institution', $${
  "name": "Portal Escolar",
  "shortName": "Portal Escolar",
  "slogan": "Aprender para transformar",
  "mission": "Formar personas íntegras, críticas y comprometidas con su comunidad, mediante una educación de calidad que articula el conocimiento académico, el desarrollo humano y el uso responsable de la tecnología.",
  "vision": "Ser una institución educativa reconocida por la excelencia académica, la formación en valores y la innovación pedagógica, contribuyendo al desarrollo sostenible de la región.",
  "history": "La institución nace del compromiso de un grupo de familias y docentes por ofrecer una educación cercana, exigente y humana. A lo largo de su historia ha crecido en infraestructura, propuesta académica y participación comunitaria, manteniendo como centro la formación integral de sus estudiantes.",
  "address": "Dirección institucional pendiente de definir",
  "phone": "+57 000 000 0000",
  "email": "contacto@portalescolar.edu.co",
  "primaryColor": "#0b2545",
  "accentColor": "#38bdf8",
  "logoUrl": null,
  "updatedAt": "2026-11-10"
}$$),
('contact', $${
  "schedule": "Lunes a viernes, 7:00 a.m. - 4:00 p.m.",
  "address": "Dirección institucional pendiente de definir",
  "phone": "+57 000 000 0000",
  "email": "contacto@portalescolar.edu.co",
  "whatsapp": null
}$$),
('social', $${
  "facebook": null,
  "instagram": null,
  "youtube": null,
  "x": null
}$$),
('academic', $${
  "gradingScale": { "min": 0, "max": 5, "passing": 3.0, "decimals": 1 },
  "periodsPerYear": 4
}$$);

INSERT INTO academic_year (id, name, start_date, end_date, active) VALUES
('11111111-1111-1111-1111-111111111101', '2026', '2026-02-02', '2026-11-27', true);

INSERT INTO academic_period (id, academic_year_id, name, sort_order, start_date, end_date, weight) VALUES
('11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111101', 'Periodo 1', 1, '2026-02-02', '2026-03-27', 25.00),
('11111111-1111-1111-1111-111111111112', '11111111-1111-1111-1111-111111111101', 'Periodo 2', 2, '2026-03-30', '2026-06-12', 25.00),
('11111111-1111-1111-1111-111111111113', '11111111-1111-1111-1111-111111111101', 'Periodo 3', 3, '2026-06-15', '2026-09-04', 25.00),
('11111111-1111-1111-1111-111111111114', '11111111-1111-1111-1111-111111111101', 'Periodo 4', 4, '2026-09-07', '2026-11-27', 25.00);

INSERT INTO grade_level (id, name, sort_order) VALUES
('11111111-1111-1111-1111-111111111201', '6°', 6),
('11111111-1111-1111-1111-111111111202', '7°', 7),
('11111111-1111-1111-1111-111111111203', '8°', 8),
('11111111-1111-1111-1111-111111111204', '9°', 9),
('11111111-1111-1111-1111-111111111205', '10°', 10),
('11111111-1111-1111-1111-111111111206', '11°', 11);

INSERT INTO subject (id, code, name, area, description) VALUES
('11111111-1111-1111-1111-111111111301', 'MAT', 'Matemáticas', 'Ciencias exactas', 'Pensamiento numérico, algebraico y geométrico.'),
('11111111-1111-1111-1111-111111111302', 'ESP', 'Lengua Castellana', 'Humanidades', 'Lectura crítica, escritura y comunicación.'),
('11111111-1111-1111-1111-111111111303', 'ING', 'Inglés', 'Idiomas', 'Competencias comunicativas en lengua extranjera.'),
('11111111-1111-1111-1111-111111111304', 'CIE', 'Ciencias Naturales', 'Ciencias naturales', 'Biología, química y física.'),
('11111111-1111-1111-1111-111111111305', 'SOC', 'Ciencias Sociales', 'Humanidades', 'Historia, geografía y ciudadanía.'),
('11111111-1111-1111-1111-111111111306', 'INF', 'Tecnología e Informática', 'Tecnología', 'Pensamiento computacional y ciudadanía digital.'),
('11111111-1111-1111-1111-111111111307', 'EDF', 'Educación Física', 'Deportes', 'Desarrollo corporal, salud y trabajo en equipo.'),
('11111111-1111-1111-1111-111111111308', 'ART', 'Artes', 'Cultura', 'Expresión artística, música y plástica.');

INSERT INTO course (id, academic_year_id, grade_level_id, group_name, capacity) VALUES
('11111111-1111-1111-1111-111111111401', '11111111-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111201', 'A', 35),
('11111111-1111-1111-1111-111111111402', '11111111-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111201', 'B', 35),
('11111111-1111-1111-1111-111111111403', '11111111-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111205', 'A', 35),
('11111111-1111-1111-1111-111111111404', '11111111-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111206', 'A', 35);

INSERT INTO news (id, slug, title, summary, content, image_url, category, status, published_at) VALUES
('11111111-1111-1111-1111-111111111501', 'inicio-del-ano-escolar-2026', 'Inicio del año escolar 2026',
 'Recibimos a nuestras familias con una jornada de bienvenida y presentación de la propuesta académica.',
 'El año escolar 2026 comienza con una apuesta renovada por la lectura, la investigación y el bienestar de nuestros estudiantes. Durante la primera semana se realizaron actividades de integración, presentación de docentes y reconocimiento de los espacios institucionales.

Invitamos a las familias a acompañar este proceso desde casa: revisar la agenda escolar, mantener comunicación con los directores de grupo y participar en las reuniones programadas.',
 '/images/news/bienvenida.svg', 'Institucional', 'PUBLISHED', '2026-02-02T08:00:00-05:00'),
('11111111-1111-1111-1111-111111111502', 'feria-de-ciencia-y-tecnologia', 'Feria de Ciencia y Tecnología',
 'Estudiantes de secundaria presentaron proyectos de robótica, sostenibilidad y ciudadanía digital.',
 'La feria reunió más de veinte proyectos construidos por estudiantes de distintos grados. Los equipos trabajaron durante ocho semanas en la formulación de preguntas, diseño de prototipos y comunicación de resultados.

Felicitamos a los participantes por su curiosidad, rigor y capacidad de trabajo colaborativo.',
 '/images/news/feria.svg', 'Académico', 'PUBLISHED', '2026-09-18T10:00:00-05:00'),
('11111111-1111-1111-1111-111111111503', 'escuela-de-familias', 'Escuela de Familias',
 'Un espacio de formación y diálogo para acompañar el desarrollo académico y emocional de los estudiantes.',
 'La Escuela de Familias es un encuentro periódico entre docentes, orientación escolar y acudientes. En esta ocasión abordaremos hábitos de estudio, uso responsable de pantallas y comunicación asertiva en el hogar.',
 '/images/news/familias.svg', 'Comunidad', 'PUBLISHED', '2026-10-05T09:00:00-05:00');

INSERT INTO event (id, title, description, starts_at, ends_at, all_day, location, type, published) VALUES
('11111111-1111-1111-1111-111111111601', 'Reunión de padres - Periodo 1', 'Entrega de informes y acuerdos pedagógicos.', '2026-03-20T16:00:00-05:00', '2026-03-20T18:00:00-05:00', false, 'Auditorio principal', 'MEETING', true),
('11111111-1111-1111-1111-111111111602', 'Semana de la Ciencia', 'Exposiciones y talleres abiertos a las familias.', '2026-09-14T08:00:00-05:00', '2026-09-18T16:00:00-05:00', true, 'Campus institucional', 'ACADEMIC', true),
('11111111-1111-1111-1111-111111111603', 'Día de la Familia', 'Jornada deportiva y cultural.', '2026-10-17T08:00:00-05:00', '2026-10-17T15:00:00-05:00', true, 'Zona deportiva', 'OTHER', true),
('11111111-1111-1111-1111-111111111604', 'Entrega de informes - Periodo 4', 'Cierre del año escolar 2026.', '2026-11-27T07:00:00-05:00', '2026-11-27T12:00:00-05:00', false, 'Dirección de grupo', 'MEETING', true);

INSERT INTO gallery_album (id, title, description, cover_url, published) VALUES
('11111111-1111-1111-1111-111111111701', 'Vida institucional', 'Momentos de aprendizaje, encuentro y participación comunitaria.', '/images/gallery/vida-institucional.svg', true);

INSERT INTO gallery_image (id, album_id, image_url, caption, sort_order) VALUES
('11111111-1111-1111-1111-111111111702', '11111111-1111-1111-1111-111111111701', '/images/gallery/vida-institucional.svg', 'Jornada de bienvenida', 1),
('11111111-1111-1111-1111-111111111703', '11111111-1111-1111-1111-111111111701', '/images/gallery/feria.svg', 'Feria de Ciencia y Tecnología', 2);

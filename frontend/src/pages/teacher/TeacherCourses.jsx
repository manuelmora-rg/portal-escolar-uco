import { useQuery } from '@tanstack/react-query'
import { BookOpen, Users } from 'lucide-react'
import { useState } from 'react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner } from '../../components/ui'

export default function TeacherCourses() {
  usePageTitle('Mis cursos')
  const [selected, setSelected] = useState(null)

  const { data: courses = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['teacher-courses'],
    queryFn: async () => (await api.get('/teacher/courses')).data,
  })

  const { data: students = [], isLoading: loadingStudents } = useQuery({
    queryKey: ['teacher-course-students', selected],
    queryFn: async () => (await api.get(`/teacher/courses/${selected}/students`)).data,
    enabled: Boolean(selected),
  })

  const { data: courseSubjects = [] } = useQuery({
    queryKey: ['teacher-course-subjects'],
    queryFn: async () => (await api.get('/teacher/course-subjects')).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader title="Mis cursos" description="Cursos asignados y estudiantes matriculados." />

      {courses.length === 0 ? (
        <EmptyState title="Sin cursos" description="No tienes cursos asignados en el año académico activo." icon={BookOpen} />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {courses.map((course) => {
            const subjects = courseSubjects.filter((item) => item.courseId === course.courseId)
            return (
              <button
                key={course.courseId}
                type="button"
                onClick={() => setSelected(course.courseId)}
                className={`card text-left transition hover:border-accent-400 ${selected === course.courseId ? '!border-accent-500 ring-2 ring-accent-500/20' : ''}`}
              >
                <div className="flex items-center justify-between gap-2">
                  <p className="text-lg font-bold text-slate-900 dark:text-white">{course.courseName}</p>
                  <Badge tone="brand">{course.students} estudiantes</Badge>
                </div>
                <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                  {course.gradeLevel} · Grupo {course.group}
                </p>
                <p className="mt-3 text-xs font-semibold tracking-wide text-slate-500 uppercase dark:text-slate-400">
                  {subjects.length} asignaturas
                </p>
                <ul className="mt-1 space-y-0.5 text-sm text-slate-600 dark:text-slate-300">
                  {subjects.slice(0, 4).map((subject) => (
                    <li key={subject.id}>· {subject.subjectName}</li>
                  ))}
                </ul>
              </button>
            )
          })}
        </div>
      )}

      {selected ? (
        <section className="card mt-6">
          <h2 className="card-title flex items-center gap-2">
            <Users size={18} /> Estudiantes del curso
          </h2>
          {loadingStudents ? (
            <Spinner label="Cargando estudiantes…" />
          ) : students.length === 0 ? (
            <p className="mt-4 text-sm text-slate-500 dark:text-slate-400">No hay estudiantes matriculados.</p>
          ) : (
            <div className="mt-4 overflow-x-auto">
              <table className="table-base">
                <thead>
                  <tr>
                    <th>Estudiante</th>
                    <th>Documento</th>
                    <th>Correo</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {students.map((student) => (
                    <tr key={student.studentId}>
                      <td className="font-medium text-slate-800 dark:text-slate-100">{student.fullName}</td>
                      <td className="text-slate-500 dark:text-slate-400">{student.documentNumber}</td>
                      <td className="text-slate-500 dark:text-slate-400">{student.email}</td>
                      <td><Badge tone="green">{student.status}</Badge></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      ) : null}
    </div>
  )
}

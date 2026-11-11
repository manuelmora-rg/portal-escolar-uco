import { useQuery } from '@tanstack/react-query'
import { BookOpen, Layers, Users } from 'lucide-react'
import { useState } from 'react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner, Tabs, formatDate } from '../../components/ui'

const TABS = [
  { value: 'cursos', label: 'Cursos' },
  { value: 'materias', label: 'Materias' },
  { value: 'matriculas', label: 'Matrículas' },
]

export default function AdminCourses() {
  usePageTitle('Cursos y materias')
  const [tab, setTab] = useState('cursos')

  const courses = useQuery({
    queryKey: ['admin-courses'],
    queryFn: async () => (await api.get('/admin/courses')).data,
  })
  const subjects = useQuery({
    queryKey: ['admin-subjects'],
    queryFn: async () => (await api.get('/admin/subjects')).data,
  })
  const enrollments = useQuery({
    queryKey: ['admin-enrollments'],
    queryFn: async () => (await api.get('/admin/enrollments')).data,
  })

  return (
    <div>
      <PageHeader title="Cursos y materias" description="Estructura académica del año en curso." />
      <div className="mb-5">
        <Tabs tabs={TABS} active={tab} onChange={setTab} />
      </div>

      {tab === 'cursos' ? (
        courses.isLoading ? <Spinner /> : courses.isError ? <ErrorState onRetry={courses.refetch} /> : courses.data.length === 0 ? (
          <EmptyState title="Sin cursos" icon={BookOpen} />
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {courses.data.map((course) => (
              <div key={course.id} className="card">
                <div className="flex items-center justify-between gap-2">
                  <p className="text-lg font-bold text-slate-900 dark:text-white">{course.name}</p>
                  <Badge tone="brand">{course.students} estudiantes</Badge>
                </div>
                <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{course.gradeLevel} · Grupo {course.group}</p>
              </div>
            ))}
          </div>
        )
      ) : null}

      {tab === 'materias' ? (
        subjects.isLoading ? <Spinner /> : subjects.isError ? <ErrorState onRetry={subjects.refetch} /> : subjects.data.length === 0 ? (
          <EmptyState title="Sin materias" icon={Layers} />
        ) : (
          <div className="card overflow-x-auto">
            <table className="table-base">
              <thead>
                <tr><th>Código</th><th>Materia</th><th>Área</th></tr>
              </thead>
              <tbody>
                {subjects.data.map((subject) => (
                  <tr key={subject.id}>
                    <td><Badge tone="slate">{subject.code}</Badge></td>
                    <td className="font-medium text-slate-800 dark:text-slate-100">{subject.name}</td>
                    <td className="text-slate-500 dark:text-slate-400">{subject.area}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      ) : null}

      {tab === 'matriculas' ? (
        enrollments.isLoading ? <Spinner /> : enrollments.isError ? <ErrorState onRetry={enrollments.refetch} /> : enrollments.data.length === 0 ? (
          <EmptyState title="Sin matrículas" icon={Users} />
        ) : (
          <div className="card overflow-x-auto">
            <table className="table-base">
              <thead>
                <tr><th>Estudiante</th><th>Curso</th><th>Año</th><th>Fecha</th><th>Estado</th></tr>
              </thead>
              <tbody>
                {enrollments.data.map((enrollment) => (
                  <tr key={enrollment.id}>
                    <td className="font-medium text-slate-800 dark:text-slate-100">{enrollment.studentName}</td>
                    <td className="text-slate-500 dark:text-slate-400">{enrollment.courseName}</td>
                    <td className="text-slate-500 dark:text-slate-400">{enrollment.academicYear}</td>
                    <td className="text-slate-500 dark:text-slate-400">{formatDate(enrollment.enrollmentDate)}</td>
                    <td><Badge tone={enrollment.status === 'ENROLLED' ? 'green' : 'slate'}>{enrollment.status}</Badge></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      ) : null}
    </div>
  )
}

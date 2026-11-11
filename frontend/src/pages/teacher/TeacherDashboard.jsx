import { useQuery } from '@tanstack/react-query'
import { BookOpen, ClipboardList, Layers, Users } from 'lucide-react'
import { Link } from 'react-router-dom'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner, StatCard } from '../../components/ui'

export default function TeacherDashboard() {
  usePageTitle('Panel del docente')
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['teacher-dashboard'],
    queryFn: async () => (await api.get('/teacher/dashboard')).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader
        title={`Hola, ${(data.teacherName || '').split(' ')[0] || 'docente'}`}
        description="Resumen de tus cursos, estudiantes y entregas pendientes."
      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Cursos" value={data.courses} icon={BookOpen} tone="brand" />
        <StatCard label="Estudiantes" value={data.students} icon={Users} tone="sky" />
        <StatCard label="Asignaturas" value={data.courseSubjects?.length || 0} icon={Layers} tone="green" />
        <StatCard label="Entregas por calificar" value={data.pendingSubmissions} icon={ClipboardList} tone="amber" />
      </div>

      <section className="card mt-6">
        <div className="flex items-center justify-between">
          <h2 className="card-title">Mis asignaturas</h2>
          <Link to="/app/docente/cuaderno" className="text-sm font-semibold text-brand-700 hover:underline dark:text-accent-300">
            Ir al cuaderno de notas
          </Link>
        </div>
        <div className="mt-4">
          {data.courseSubjects?.length ? (
            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
              {data.courseSubjects.map((item) => (
                <div key={item.id} className="rounded-xl border border-slate-200 p-4 dark:border-slate-800">
                  <div className="flex items-center justify-between gap-2">
                    <p className="font-semibold text-slate-900 dark:text-white">{item.subjectName}</p>
                    <Badge tone="brand">{item.weeklyHours} h</Badge>
                  </div>
                  <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{item.courseName}</p>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState title="Sin asignaturas asignadas" description="Contacta a coordinación académica si esto no corresponde." />
          )}
        </div>
      </section>
    </div>
  )
}

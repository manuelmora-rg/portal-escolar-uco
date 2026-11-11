import { useQuery } from '@tanstack/react-query'
import {
  BookOpen,
  CalendarCheck,
  ClipboardList,
  GraduationCap,
  TrendingUp,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import {
  Badge,
  EmptyState,
  ErrorState,
  PageHeader,
  Spinner,
  StatCard,
  formatDate,
  statusTone,
} from '../../components/ui'

export default function StudentDashboard() {
  usePageTitle('Panel del estudiante')
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['student-dashboard'],
    queryFn: async () => (await api.get('/student/dashboard')).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  const student = data.student
  const average = data.average == null ? '—' : Number(data.average).toFixed(1)

  return (
    <div>
      <PageHeader
        title={`Hola, ${student.fullName.split(' ')[0]}`}
        description={`${student.gradeLevel} · Grupo ${student.groupName} · Documento ${student.documentNumber}`}
      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Promedio general" value={average} hint="Escala 0.0 – 5.0" icon={TrendingUp} tone="green" />
        <StatCard label="Asistencia" value={`${Number(data.attendanceRate ?? 0).toFixed(1)}%`} hint="Últimos 30 días" icon={CalendarCheck} tone="sky" />
        <StatCard label="Tareas pendientes" value={data.pendingAssignments} hint="Sin entregar" icon={ClipboardList} tone="amber" />
        <StatCard label="Notificaciones" value={data.unreadNotifications} hint="Sin leer" icon={BookOpen} tone="brand" />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-[1.4fr_0.6fr]">
        <section className="card">
          <div className="flex items-center justify-between">
            <h2 className="card-title">Próximas entregas</h2>
            <Link to="/app/estudiante/tareas" className="text-sm font-semibold text-brand-700 hover:underline dark:text-accent-300">
              Ver tareas
            </Link>
          </div>
          <div className="mt-4">
            {data.upcomingAssignments?.length ? (
              <div className="space-y-2">
                {data.upcomingAssignments.map((assignment) => (
                  <div key={assignment.id} className="flex items-center justify-between gap-3 rounded-xl border border-slate-200 px-4 py-3 dark:border-slate-800">
                    <div className="min-w-0">
                      <p className="truncate font-medium text-slate-900 dark:text-white">{assignment.title}</p>
                      <p className="text-xs text-slate-500 dark:text-slate-400">
                        {assignment.subjectName} · vence {formatDate(assignment.dueAt)}
                      </p>
                    </div>
                    <Badge tone={assignment.submission ? 'green' : 'amber'}>
                      {assignment.submission ? 'Entregada' : 'Pendiente'}
                    </Badge>
                  </div>
                ))}
              </div>
            ) : (
              <EmptyState title="Sin entregas próximas" description="No tienes tareas pendientes por ahora." />
            )}
          </div>
        </section>

        <section className="card">
          <h2 className="card-title">Accesos rápidos</h2>
          <div className="mt-4 grid gap-2">
            {[
              { to: '/app/estudiante/calificaciones', label: 'Calificaciones', icon: GraduationCap },
              { to: '/app/estudiante/horario', label: 'Horario de clases', icon: CalendarCheck },
              { to: '/app/estudiante/tareas', label: 'Tareas y entregas', icon: ClipboardList },
              { to: '/app/estudiante/asistencia', label: 'Mi asistencia', icon: CalendarCheck },
            ].map(({ to, label, icon: Icon }) => (
              <Link
                key={to}
                to={to}
                className="flex items-center gap-3 rounded-xl border border-slate-200 px-4 py-3 text-sm font-medium text-slate-700 transition hover:border-accent-400 hover:bg-slate-50 dark:border-slate-800 dark:text-slate-200 dark:hover:bg-slate-800"
              >
                <Icon size={18} className="text-accent-600 dark:text-accent-400" />
                {label}
              </Link>
            ))}
          </div>
        </section>
      </div>
    </div>
  )
}

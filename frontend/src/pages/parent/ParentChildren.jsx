import { useQuery } from '@tanstack/react-query'
import {
  CalendarCheck,
  CalendarDays,
  ClipboardList,
  GraduationCap,
  MessageSquare,
  TrendingUp,
  Users,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import {
  Badge,
  EmptyState,
  ErrorState,
  PageHeader,
  Spinner,
  StatCard,
  Tabs,
  DAY_NAMES,
  formatDate,
  formatDateTime,
  formatTime,
  statusTone,
} from '../../components/ui'

const TABS = [
  { value: 'resumen', label: 'Resumen' },
  { value: 'calificaciones', label: 'Calificaciones' },
  { value: 'horario', label: 'Horario' },
  { value: 'tareas', label: 'Tareas' },
  { value: 'asistencia', label: 'Asistencia' },
  { value: 'observaciones', label: 'Observaciones' },
]

const ATTENDANCE_LABEL = { PRESENT: 'Presente', ABSENT: 'Ausente', LATE: 'Tarde', EXCUSED: 'Excusado' }

export default function ParentChildren() {
  usePageTitle('Mis hijos')
  const [searchParams, setSearchParams] = useSearchParams()
  const [tab, setTab] = useState('resumen')
  const [periodId, setPeriodId] = useState('')

  const { data: children = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['parent-children'],
    queryFn: async () => (await api.get('/parent/children')).data,
  })

  const { data: periods = [] } = useQuery({
    queryKey: ['parent-periods'],
    queryFn: async () => (await api.get('/parent/periods')).data,
  })

  const selectedId = searchParams.get('child') || children[0]?.studentId || ''
  const selected = children.find((child) => child.studentId === selectedId)

  useEffect(() => {
    if (!searchParams.get('child') && children.length > 0) {
      setSearchParams({ child: children[0].studentId }, { replace: true })
    }
  }, [children, searchParams, setSearchParams])

  const overview = useQuery({
    queryKey: ['parent-child-overview', selectedId],
    queryFn: async () => (await api.get(`/parent/children/${selectedId}/overview`)).data,
    enabled: Boolean(selectedId),
  })

  const grades = useQuery({
    queryKey: ['parent-child-grades', selectedId, periodId],
    queryFn: async () =>
      (await api.get(`/parent/children/${selectedId}/grades`, { params: periodId ? { periodId } : {} })).data,
    enabled: Boolean(selectedId) && tab === 'calificaciones',
  })

  const schedule = useQuery({
    queryKey: ['parent-child-schedule', selectedId],
    queryFn: async () => (await api.get(`/parent/children/${selectedId}/schedule`)).data,
    enabled: Boolean(selectedId) && tab === 'horario',
  })

  const assignments = useQuery({
    queryKey: ['parent-child-assignments', selectedId],
    queryFn: async () => (await api.get(`/parent/children/${selectedId}/assignments`)).data,
    enabled: Boolean(selectedId) && tab === 'tareas',
  })

  const attendance = useQuery({
    queryKey: ['parent-child-attendance', selectedId],
    queryFn: async () => (await api.get(`/parent/children/${selectedId}/attendance`)).data,
    enabled: Boolean(selectedId) && tab === 'asistencia',
  })

  const observations = useQuery({
    queryKey: ['parent-child-observations', selectedId],
    queryFn: async () => (await api.get(`/parent/children/${selectedId}/observations`)).data,
    enabled: Boolean(selectedId) && tab === 'observaciones',
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  if (children.length === 0) {
    return (
      <div>
        <PageHeader title="Mis hijos" />
        <EmptyState title="Sin hijos vinculados" description="No tienes estudiantes autorizados en tu cuenta." icon={Users} />
      </div>
    )
  }

  return (
    <div>
      <PageHeader title="Mis hijos" description="Selecciona un hijo para consultar su información académica." />

      <div className="mb-6 flex flex-wrap gap-2">
        {children.map((child) => (
          <button
            key={child.studentId}
            type="button"
            onClick={() => {
              setSearchParams({ child: child.studentId })
              setPeriodId('')
            }}
            className={`rounded-xl border px-4 py-2.5 text-left transition ${
              child.studentId === selectedId
                ? 'border-accent-500 bg-accent-500/10'
                : 'border-slate-200 bg-white hover:border-slate-300 dark:border-slate-800 dark:bg-slate-900'
            }`}
          >
            <p className="text-sm font-semibold text-slate-900 dark:text-white">{child.fullName}</p>
            <p className="text-xs text-slate-500 dark:text-slate-400">{child.gradeLevel} · {child.groupName}</p>
          </button>
        ))}
      </div>

      {selected ? (
        <>
          <div className="mb-4">
            <Tabs tabs={TABS} active={tab} onChange={setTab} />
          </div>

          {tab === 'resumen' ? (
            overview.isLoading ? (
              <Spinner />
            ) : overview.isError ? (
              <ErrorState onRetry={overview.refetch} />
            ) : (
              <div className="space-y-6">
                <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                  <StatCard label="Promedio" value={overview.data.average == null ? '—' : Number(overview.data.average).toFixed(1)} icon={TrendingUp} tone="green" />
                  <StatCard label="Asistencia" value={`${Number(overview.data.attendanceRate ?? 0).toFixed(1)}%`} icon={CalendarCheck} tone="sky" />
                  <StatCard label="Tareas pendientes" value={overview.data.pendingAssignments} icon={ClipboardList} tone="amber" />
                  <StatCard label="Notificaciones" value={overview.data.unreadNotifications} icon={MessageSquare} tone="brand" />
                </div>
                <section className="card">
                  <h2 className="card-title">Datos del estudiante</h2>
                  <dl className="mt-4 grid gap-4 sm:grid-cols-2">
                    <div>
                      <dt className="text-xs font-semibold tracking-wide text-slate-500 uppercase dark:text-slate-400">Nombre</dt>
                      <dd className="mt-1 text-slate-800 dark:text-slate-100">{overview.data.fullName}</dd>
                    </div>
                    <div>
                      <dt className="text-xs font-semibold tracking-wide text-slate-500 uppercase dark:text-slate-400">Documento</dt>
                      <dd className="mt-1 text-slate-800 dark:text-slate-100">{overview.data.documentNumber}</dd>
                    </div>
                    <div>
                      <dt className="text-xs font-semibold tracking-wide text-slate-500 uppercase dark:text-slate-400">Curso</dt>
                      <dd className="mt-1 text-slate-800 dark:text-slate-100">{overview.data.gradeLevel} · {overview.data.groupName}</dd>
                    </div>
                    <div>
                      <dt className="text-xs font-semibold tracking-wide text-slate-500 uppercase dark:text-slate-400">Parentesco</dt>
                      <dd className="mt-1 text-slate-800 dark:text-slate-100">{overview.data.relationship}</dd>
                    </div>
                  </dl>
                </section>
              </div>
            )
          ) : null}

          {tab === 'calificaciones' ? (
            grades.isLoading ? (
              <Spinner />
            ) : grades.isError ? (
              <ErrorState onRetry={grades.refetch} />
            ) : (
              <div className="space-y-4">
                <select className="input max-w-xs" value={periodId} onChange={(event) => setPeriodId(event.target.value)}>
                  <option value="">Todos los periodos</option>
                  {periods.map((period) => (
                    <option key={period.id} value={period.id}>{period.name}</option>
                  ))}
                </select>
                {(grades.data || []).length === 0 ? (
                  <EmptyState title="Sin calificaciones" icon={GraduationCap} />
                ) : (
                  <div className="grid gap-4 lg:grid-cols-2">
                    {grades.data.map((subject) => (
                      <section key={subject.courseSubjectId} className="card">
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <h2 className="font-semibold text-slate-900 dark:text-white">{subject.subjectName}</h2>
                            <p className="text-xs text-slate-500 dark:text-slate-400">{subject.teacherName}</p>
                          </div>
                          <div className="text-right">
                            <p className="text-2xl font-bold text-slate-900 dark:text-white">
                              {subject.average == null ? '—' : Number(subject.average).toFixed(1)}
                            </p>
                            <p className="text-[11px] text-slate-500 dark:text-slate-400">Promedio</p>
                          </div>
                        </div>
                        {subject.grades.length === 0 ? (
                          <p className="mt-3 text-sm text-slate-500 dark:text-slate-400">Sin notas registradas.</p>
                        ) : (
                          <div className="mt-3 space-y-2">
                            {subject.grades.map((grade) => (
                              <div key={grade.id} className="flex items-center justify-between gap-2 rounded-lg border border-slate-200 px-3 py-2 dark:border-slate-800">
                                <div className="min-w-0">
                                  <p className="truncate text-sm font-medium text-slate-800 dark:text-slate-100">{grade.title}</p>
                                  <p className="text-xs text-slate-500 dark:text-slate-400">{formatDate(grade.date)} · peso {Number(grade.weight).toFixed(0)}%</p>
                                </div>
                                <span className={`font-semibold ${Number(grade.score) < 3 ? 'text-rose-600 dark:text-rose-400' : 'text-brand-700 dark:text-brand-300'}`}>
                                  {Number(grade.score).toFixed(1)}
                                </span>
                              </div>
                            ))}
                          </div>
                        )}
                      </section>
                    ))}
                  </div>
                )}
              </div>
            )
          ) : null}

          {tab === 'horario' ? (
            schedule.isLoading ? (
              <Spinner />
            ) : schedule.isError ? (
              <ErrorState onRetry={schedule.refetch} />
            ) : (schedule.data || []).length === 0 ? (
              <EmptyState title="Sin horario" icon={CalendarDays} />
            ) : (
              <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                {[1, 2, 3, 4, 5, 6].map((day) => {
                  const items = (schedule.data || [])
                    .filter((item) => item.dayOfWeek === day)
                    .sort((a, b) => String(a.startTime).localeCompare(String(b.startTime)))
                  if (items.length === 0) return null
                  return (
                    <section key={day} className="card">
                      <h2 className="card-title">{DAY_NAMES[day]}</h2>
                      <div className="mt-3 space-y-2">
                        {items.map((item) => (
                          <div key={item.id} className="rounded-xl border border-slate-200 px-3 py-2 dark:border-slate-800">
                            <div className="flex items-center justify-between gap-2">
                              <p className="text-sm font-medium text-slate-900 dark:text-white">{item.subjectName}</p>
                              <span className="badge bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                                {formatTime(item.startTime)}–{formatTime(item.endTime)}
                              </span>
                            </div>
                            <p className="text-xs text-slate-500 dark:text-slate-400">{item.teacherName}{item.classroom ? ` · Aula ${item.classroom}` : ''}</p>
                          </div>
                        ))}
                      </div>
                    </section>
                  )
                })}
              </div>
            )
          ) : null}

          {tab === 'tareas' ? (
            assignments.isLoading ? (
              <Spinner />
            ) : assignments.isError ? (
              <ErrorState onRetry={assignments.refetch} />
            ) : (assignments.data || []).length === 0 ? (
              <EmptyState title="Sin tareas" icon={ClipboardList} />
            ) : (
              <div className="space-y-3">
                {assignments.data.map((assignment) => (
                  <div key={assignment.id} className="card">
                    <div className="flex flex-wrap items-center gap-2">
                      <h2 className="font-semibold text-slate-900 dark:text-white">{assignment.title}</h2>
                      <Badge tone={assignment.submission ? statusTone(assignment.submission.status) : 'amber'}>
                        {assignment.submission ? (assignment.submission.status === 'GRADED' ? 'Calificada' : 'Entregada') : 'Pendiente'}
                      </Badge>
                    </div>
                    <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                      {assignment.subjectName} · vence {formatDateTime(assignment.dueAt)}
                    </p>
                    {assignment.description ? <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{assignment.description}</p> : null}
                    {assignment.submission?.score != null ? (
                      <p className="mt-2 text-sm font-semibold text-brand-700 dark:text-brand-300">
                        Nota: {Number(assignment.submission.score).toFixed(1)}
                      </p>
                    ) : null}
                  </div>
                ))}
              </div>
            )
          ) : null}

          {tab === 'asistencia' ? (
            attendance.isLoading ? (
              <Spinner />
            ) : attendance.isError ? (
              <ErrorState onRetry={attendance.refetch} />
            ) : (
              <div className="space-y-4">
                <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                  <StatCard label="Asistencia" value={`${Number(attendance.data.summary.rate).toFixed(1)}%`} icon={CalendarCheck} tone="green" />
                  <StatCard label="Presentes" value={attendance.data.summary.present} tone="green" />
                  <StatCard label="Ausentes" value={attendance.data.summary.absent} tone="red" />
                  <StatCard label="Tardanzas / excusas" value={attendance.data.summary.late + attendance.data.summary.excused} tone="amber" />
                </div>
                <section className="card overflow-x-auto">
                  {attendance.data.records.length === 0 ? (
                    <EmptyState title="Sin registros" />
                  ) : (
                    <table className="table-base">
                      <thead>
                        <tr><th>Fecha</th><th>Estado</th><th>Justificación</th></tr>
                      </thead>
                      <tbody>
                        {attendance.data.records.map((record) => (
                          <tr key={record.id}>
                            <td>{formatDate(record.date)}</td>
                            <td><Badge tone={statusTone(record.status)}>{ATTENDANCE_LABEL[record.status] || record.status}</Badge></td>
                            <td className="text-slate-500 dark:text-slate-400">{record.justification || '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </section>
              </div>
            )
          ) : null}

          {tab === 'observaciones' ? (
            observations.isLoading ? (
              <Spinner />
            ) : observations.isError ? (
              <ErrorState onRetry={observations.refetch} />
            ) : (observations.data || []).length === 0 ? (
              <EmptyState title="Sin observaciones" icon={MessageSquare} />
            ) : (
              <div className="space-y-3">
                {observations.data.map((item) => (
                  <div key={item.id} className="card">
                    <div className="flex flex-wrap items-center gap-2">
                      <Badge tone="brand">{item.type}</Badge>
                      <span className="text-xs text-slate-500 dark:text-slate-400">{formatDateTime(item.createdAt)}</span>
                    </div>
                    <p className="mt-2 text-slate-700 dark:text-slate-200">{item.description}</p>
                    <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">Docente: {item.teacherName || '—'}</p>
                  </div>
                ))}
              </div>
            )
          ) : null}
        </>
      ) : null}
    </div>
  )
}

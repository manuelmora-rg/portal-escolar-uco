import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { CalendarCheck, Save } from 'lucide-react'
import { useEffect, useState } from 'react'
import api, { apiErrorMessage } from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, Field, PageHeader, Spinner, formatDate } from '../../components/ui'

const STATUS_OPTIONS = [
  { value: 'PRESENT', label: 'Presente', tone: 'green' },
  { value: 'ABSENT', label: 'Ausente', tone: 'red' },
  { value: 'LATE', label: 'Tarde', tone: 'amber' },
  { value: 'EXCUSED', label: 'Excusado', tone: 'sky' },
]

function today() {
  return new Date().toISOString().slice(0, 10)
}

export default function TeacherAttendance() {
  usePageTitle('Asistencia')
  const queryClient = useQueryClient()
  const [courseSubjectId, setCourseSubjectId] = useState('')
  const [date, setDate] = useState(today())
  const [statuses, setStatuses] = useState({})
  const [justifications, setJustifications] = useState({})
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const { data: courseSubjects = [] } = useQuery({
    queryKey: ['teacher-course-subjects'],
    queryFn: async () => (await api.get('/teacher/course-subjects')).data,
  })

  useEffect(() => {
    if (!courseSubjectId && courseSubjects.length > 0) {
      setCourseSubjectId(courseSubjects[0].id)
    }
  }, [courseSubjects, courseSubjectId])

  const { data: sheet, isLoading, isError, refetch } = useQuery({
    queryKey: ['teacher-attendance', courseSubjectId, date],
    queryFn: async () =>
      (await api.get(`/teacher/course-subjects/${courseSubjectId}/attendance`, { params: { date } })).data,
    enabled: Boolean(courseSubjectId),
  })

  useEffect(() => {
    if (!sheet?.rows) return
    const nextStatuses = {}
    const nextJustifications = {}
    for (const row of sheet.rows) {
      nextStatuses[row.enrollmentId] = row.status || 'PRESENT'
      nextJustifications[row.enrollmentId] = row.justification || ''
    }
    setStatuses(nextStatuses)
    setJustifications(nextJustifications)
    setMessage('')
    setError('')
  }, [sheet])

  const save = useMutation({
    mutationFn: async () =>
      (await api.post(`/teacher/course-subjects/${courseSubjectId}/attendance`, {
        date,
        entries: (sheet?.rows || []).map((row) => ({
          enrollmentId: row.enrollmentId,
          status: statuses[row.enrollmentId] || 'PRESENT',
          justification: justifications[row.enrollmentId]?.trim() || null,
        })),
      })).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['teacher-attendance'] })
      setMessage('Asistencia guardada correctamente.')
      setError('')
    },
    onError: (requestError) => {
      setMessage('')
      setError(apiErrorMessage(requestError, 'No fue posible guardar la asistencia.'))
    },
  })

  const rows = sheet?.rows || []
  const counts = rows.reduce((acc, row) => {
    const status = statuses[row.enrollmentId] || 'PRESENT'
    acc[status] = (acc[status] || 0) + 1
    return acc
  }, {})

  return (
    <div>
      <PageHeader
        title="Asistencia"
        description="Registra la asistencia diaria por asignatura."
        actions={
          <button type="button" className="btn-primary" onClick={() => save.mutate()} disabled={!rows.length || save.isPending}>
            <Save size={16} /> {save.isPending ? 'Guardando…' : 'Guardar asistencia'}
          </button>
        }
      />

      <div className="card mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Field label="Asignatura / curso" htmlFor="courseSubject">
          <select id="courseSubject" className="input" value={courseSubjectId} onChange={(event) => setCourseSubjectId(event.target.value)}>
            {courseSubjects.length === 0 ? <option value="">Sin asignaturas</option> : null}
            {courseSubjects.map((item) => (
              <option key={item.id} value={item.id}>
                {item.courseName} · {item.subjectName}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Fecha" htmlFor="date">
          <input id="date" type="date" className="input" value={date} onChange={(event) => setDate(event.target.value)} />
        </Field>
        <div className="sm:col-span-2 flex flex-wrap items-end gap-2">
          {STATUS_OPTIONS.map((option) => (
            <Badge key={option.value} tone={option.tone}>
              {option.label}: {counts[option.value] || 0}
            </Badge>
          ))}
        </div>
      </div>

      {message ? (
        <p className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700 dark:border-emerald-900/60 dark:bg-emerald-950/40 dark:text-emerald-200">
          {message}
        </p>
      ) : null}
      {error ? (
        <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-200">
          {error}
        </p>
      ) : null}

      {!courseSubjectId ? (
        <EmptyState title="Selecciona una asignatura" icon={CalendarCheck} />
      ) : isLoading ? (
        <Spinner />
      ) : isError ? (
        <ErrorState onRetry={refetch} />
      ) : rows.length === 0 ? (
        <EmptyState title="Sin estudiantes" description="El curso no tiene estudiantes matriculados." />
      ) : (
        <div className="card overflow-x-auto">
          <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
            <h2 className="card-title">{sheet.courseName}</h2>
            <span className="text-xs text-slate-500 dark:text-slate-400">{formatDate(sheet.date)}</span>
          </div>
          <table className="table-base">
            <thead>
              <tr>
                <th>Estudiante</th>
                <th>Documento</th>
                <th>Estado</th>
                <th>Justificación</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.enrollmentId}>
                  <td className="font-medium text-slate-800 dark:text-slate-100">{row.fullName}</td>
                  <td className="text-slate-500 dark:text-slate-400">{row.documentNumber}</td>
                  <td>
                    <select
                      className="input !py-1.5"
                      value={statuses[row.enrollmentId] || 'PRESENT'}
                      onChange={(event) => setStatuses({ ...statuses, [row.enrollmentId]: event.target.value })}
                    >
                      {STATUS_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>{option.label}</option>
                      ))}
                    </select>
                  </td>
                  <td>
                    <input
                      className="input !py-1.5"
                      placeholder="Opcional"
                      value={justifications[row.enrollmentId] || ''}
                      onChange={(event) => setJustifications({ ...justifications, [row.enrollmentId]: event.target.value })}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

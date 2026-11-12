import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ClipboardList, Pencil, Plus } from 'lucide-react'
import { useEffect, useState } from 'react'
import api, { apiErrorMessage } from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import {
  Badge,
  EmptyState,
  ErrorState,
  Field,
  Modal,
  PageHeader,
  Spinner,
  formatDate,
} from '../../components/ui'

const GRADE_TYPES = [
  { value: 'EXAM', label: 'Examen' },
  { value: 'HOMEWORK', label: 'Tarea' },
  { value: 'QUIZ', label: 'Quiz' },
  { value: 'PROJECT', label: 'Proyecto' },
  { value: 'PARTICIPATION', label: 'Participación' },
  { value: 'OTHER', label: 'Otra' },
]

const EMPTY_FORM = { title: '', type: 'EXAM', score: '', weight: '', comment: '' }

export default function TeacherGradebook() {
  usePageTitle('Cuaderno de notas')
  const queryClient = useQueryClient()
  const [courseSubjectId, setCourseSubjectId] = useState('')
  const [periodId, setPeriodId] = useState('')
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
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

  const { data: periods = [] } = useQuery({
    queryKey: ['teacher-periods'],
    queryFn: async () => (await api.get('/teacher/periods')).data,
  })

  const { data: gradebook, isLoading, isError, refetch } = useQuery({
    queryKey: ['teacher-gradebook', courseSubjectId, periodId],
    queryFn: async () =>
      (await api.get(`/teacher/course-subjects/${courseSubjectId}/gradebook`, {
        params: periodId ? { periodId } : {},
      })).data,
    enabled: Boolean(courseSubjectId),
  })

  const activePeriodId = periodId || gradebook?.period?.id || ''
  const activePeriod = periods.find((period) => period.id === activePeriodId) || gradebook?.period

  const save = useMutation({
    mutationFn: async (payload) => {
      if (editing?.gradeId) {
        return (await api.put(`/teacher/grades/${editing.gradeId}`, payload)).data
      }
      return (await api.post(`/teacher/course-subjects/${courseSubjectId}/grades`, { entries: [payload] })).data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['teacher-gradebook'] })
      closeModal()
    },
    onError: (requestError) => setError(apiErrorMessage(requestError, 'No fue posible guardar la calificación.')),
  })

  function closeModal() {
    setEditing(null)
    setForm(EMPTY_FORM)
    setError('')
  }

  function openAdd(student) {
    setEditing({ enrollmentId: student.enrollmentId, studentName: student.fullName, gradeId: null })
    setForm(EMPTY_FORM)
    setError('')
  }

  function openEdit(student, grade) {
    setEditing({ enrollmentId: student.enrollmentId, studentName: student.fullName, gradeId: grade.id })
    setForm({
      title: grade.title,
      type: grade.type,
      score: String(grade.score),
      weight: String(grade.weight),
      comment: grade.comment || '',
    })
    setError('')
  }

  function submit(event) {
    event.preventDefault()
    if (!form.title.trim()) return setError('Escribe el nombre de la actividad.')
    if (form.score === '' || Number(form.score) < 0 || Number(form.score) > 5) {
      return setError('La nota debe estar entre 0.0 y 5.0.')
    }
    if (form.weight === '' || Number(form.weight) <= 0 || Number(form.weight) > 100) {
      return setError('El peso debe estar entre 0.01 y 100.')
    }
    if (!activePeriodId) return setError('Selecciona un periodo académico.')

    save.mutate({
      enrollmentId: editing.enrollmentId,
      periodId: activePeriodId,
      title: form.title.trim(),
      type: form.type,
      score: Number(form.score),
      weight: Number(form.weight),
      comment: form.comment.trim() || null,
    })
  }

  const students = gradebook?.students || []

  return (
    <div>
      <PageHeader
        title="Cuaderno de notas"
        description="Registra y actualiza calificaciones por asignatura y periodo."
      />

      <div className="card mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Field label="Asignatura / curso" htmlFor="courseSubject">
          <select
            id="courseSubject"
            className="input"
            value={courseSubjectId}
            onChange={(event) => {
              setCourseSubjectId(event.target.value)
              setPeriodId('')
            }}
          >
            {courseSubjects.length === 0 ? <option value="">Sin asignaturas</option> : null}
            {courseSubjects.map((item) => (
              <option key={item.id} value={item.id}>
                {item.courseName} · {item.subjectName}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Periodo académico" htmlFor="period">
          <select
            id="period"
            className="input"
            value={activePeriodId}
            onChange={(event) => setPeriodId(event.target.value)}
          >
            {periods.map((period) => (
              <option key={period.id} value={period.id}>
                {period.name}
              </option>
            ))}
          </select>
        </Field>
        {activePeriod ? (
          <div className="flex items-end">
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Periodo {activePeriod.name}: {formatDate(activePeriod.startDate)} – {formatDate(activePeriod.endDate)} · Peso {Number(activePeriod.weight).toFixed(0)}%
            </p>
          </div>
        ) : null}
      </div>

      {!courseSubjectId ? (
        <EmptyState title="Selecciona una asignatura" description="Elige una asignatura para cargar el cuaderno de notas." icon={ClipboardList} />
      ) : isLoading ? (
        <Spinner />
      ) : isError ? (
        <ErrorState onRetry={refetch} />
      ) : students.length === 0 ? (
        <EmptyState title="Sin estudiantes" description="El curso no tiene estudiantes matriculados." />
      ) : (
        <div className="space-y-3">
          {students.map((student) => (
            <section key={student.enrollmentId} className="card">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h2 className="font-semibold text-slate-900 dark:text-white">{student.fullName}</h2>
                  <p className="text-xs text-slate-500 dark:text-slate-400">Documento {student.documentNumber}</p>
                </div>
                <div className="flex items-center gap-3">
                  <div className="text-right">
                    <p className="text-lg font-bold text-slate-900 dark:text-white">
                      {student.average == null ? '—' : Number(student.average).toFixed(1)}
                    </p>
                    <p className="text-[11px] text-slate-500 dark:text-slate-400">Promedio</p>
                  </div>
                  <button type="button" className="btn-secondary" onClick={() => openAdd(student)}>
                    <Plus size={16} /> Nota
                  </button>
                </div>
              </div>

              {student.grades.length === 0 ? (
                <p className="mt-3 text-sm text-slate-500 dark:text-slate-400">Sin notas en este periodo.</p>
              ) : (
                <div className="mt-3 overflow-x-auto">
                  <table className="table-base">
                    <thead>
                      <tr>
                        <th>Actividad</th>
                        <th>Tipo</th>
                        <th>Nota</th>
                        <th>Peso</th>
                        <th>Fecha</th>
                        <th />
                      </tr>
                    </thead>
                    <tbody>
                      {student.grades.map((grade) => (
                        <tr key={grade.id}>
                          <td className="font-medium text-slate-800 dark:text-slate-100">{grade.title}</td>
                          <td><Badge tone="slate">{grade.type}</Badge></td>
                          <td className={Number(grade.score) < 3 ? 'font-semibold text-rose-600 dark:text-rose-400' : 'font-semibold text-brand-700 dark:text-brand-300'}>
                            {Number(grade.score).toFixed(1)}
                          </td>
                          <td className="text-slate-500 dark:text-slate-400">{Number(grade.weight).toFixed(0)}%</td>
                          <td className="text-xs text-slate-500 dark:text-slate-400">{formatDate(grade.date)}</td>
                          <td className="text-right">
                            <button type="button" className="btn-ghost !px-2 !py-1" onClick={() => openEdit(student, grade)} title="Editar nota">
                              <Pencil size={15} />
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          ))}
        </div>
      )}

      <Modal
        open={Boolean(editing)}
        title={editing?.gradeId ? 'Editar calificación' : 'Registrar calificación'}
        onClose={closeModal}
        footer={
          <>
            <button type="button" className="btn-secondary" onClick={closeModal}>Cancelar</button>
            <button type="submit" form="grade-form" className="btn-primary" disabled={save.isPending}>
              {save.isPending ? 'Guardando…' : 'Guardar'}
            </button>
          </>
        }
      >
        <form id="grade-form" className="space-y-4" onSubmit={submit}>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Estudiante: <span className="font-semibold text-slate-800 dark:text-slate-100">{editing?.studentName}</span>
            {activePeriod ? ` · ${activePeriod.name}` : ''}
          </p>
          <Field label="Actividad" htmlFor="title">
            <input id="title" className="input" value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} />
          </Field>
          <div className="grid gap-4 sm:grid-cols-3">
            <Field label="Tipo" htmlFor="type">
              <select id="type" className="input" value={form.type} onChange={(event) => setForm({ ...form, type: event.target.value })}>
                {GRADE_TYPES.map((type) => (
                  <option key={type.value} value={type.value}>{type.label}</option>
                ))}
              </select>
            </Field>
            <Field label="Nota (0.0 – 5.0)" htmlFor="score">
              <input
                id="score"
                type="number"
                step="0.1"
                min="0"
                max="5"
                className="input"
                value={form.score}
                onChange={(event) => setForm({ ...form, score: event.target.value })}
              />
            </Field>
            <Field label="Peso (%)" htmlFor="weight">
              <input
                id="weight"
                type="number"
                step="1"
                min="0.01"
                max="100"
                className="input"
                value={form.weight}
                onChange={(event) => setForm({ ...form, weight: event.target.value })}
              />
            </Field>
          </div>
          <Field label="Comentario" htmlFor="comment">
            <textarea
              id="comment"
              rows={3}
              className="input resize-y"
              value={form.comment}
              onChange={(event) => setForm({ ...form, comment: event.target.value })}
            />
          </Field>
          {error ? <p className="text-sm font-medium text-rose-600 dark:text-rose-400">{error}</p> : null}
        </form>
      </Modal>
    </div>
  )
}

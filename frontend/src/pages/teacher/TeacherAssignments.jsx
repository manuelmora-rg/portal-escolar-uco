import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { FileText, Plus, Send } from 'lucide-react'
import { useState } from 'react'
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
  formatDateTime,
  statusTone,
} from '../../components/ui'

const EMPTY = { courseSubjectId: '', title: '', description: '', dueAt: '', maxScore: '5.0', attachmentUrl: '', publish: true }

export default function TeacherAssignments() {
  usePageTitle('Tareas')
  const queryClient = useQueryClient()
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState('')
  const [submissionsFor, setSubmissionsFor] = useState(null)
  const [gradeForm, setGradeForm] = useState({ submissionId: '', studentName: '', score: '', feedback: '' })

  const { data: courseSubjects = [] } = useQuery({
    queryKey: ['teacher-course-subjects'],
    queryFn: async () => (await api.get('/teacher/course-subjects')).data,
  })

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['teacher-assignments'],
    queryFn: async () => (await api.get('/teacher/assignments')).data,
  })

  const { data: submissions = [], isLoading: loadingSubmissions } = useQuery({
    queryKey: ['teacher-submissions', submissionsFor],
    queryFn: async () => (await api.get(`/teacher/assignments/${submissionsFor}/submissions`)).data,
    enabled: Boolean(submissionsFor),
  })

  const create = useMutation({
    mutationFn: async (payload) => (await api.post(`/teacher/course-subjects/${payload.courseSubjectId}/assignments`, payload)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['teacher-assignments'] })
      setCreating(false)
      setForm(EMPTY)
      setError('')
    },
    onError: (requestError) => setError(apiErrorMessage(requestError, 'No fue posible crear la tarea.')),
  })

  const grade = useMutation({
    mutationFn: async (payload) => (await api.put(`/teacher/submissions/${payload.submissionId}/grade`, {
      score: payload.score,
      feedback: payload.feedback,
    })).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['teacher-submissions', submissionsFor] })
      queryClient.invalidateQueries({ queryKey: ['teacher-assignments'] })
      setGradeForm({ submissionId: '', studentName: '', score: '', feedback: '' })
    },
    onError: (requestError) => setError(apiErrorMessage(requestError, 'No fue posible calificar la entrega.')),
  })

  function openCreate() {
    setForm({ ...EMPTY, courseSubjectId: courseSubjects[0]?.id || '' })
    setError('')
    setCreating(true)
  }

  function submitCreate(event) {
    event.preventDefault()
    if (!form.courseSubjectId) return setError('Selecciona una asignatura.')
    if (!form.title.trim()) return setError('Escribe el título de la tarea.')
    if (!form.dueAt) return setError('Indica la fecha de entrega.')
    if (!form.maxScore || Number(form.maxScore) <= 0) return setError('El puntaje máximo debe ser mayor que cero.')
    create.mutate({
      courseSubjectId: form.courseSubjectId,
      title: form.title.trim(),
      description: form.description.trim() || null,
      dueAt: new Date(form.dueAt).toISOString(),
      maxScore: Number(form.maxScore),
      attachmentUrl: form.attachmentUrl.trim() || null,
      publish: form.publish,
    })
  }

  function submitGrade(event) {
    event.preventDefault()
    if (gradeForm.score === '') return setError('Ingresa la nota.')
    grade.mutate({
      submissionId: gradeForm.submissionId,
      score: Number(gradeForm.score),
      feedback: gradeForm.feedback.trim() || null,
    })
  }

  return (
    <div>
      <PageHeader
        title="Tareas"
        description="Publica tareas y califica las entregas de tus estudiantes."
        actions={
          <button type="button" className="btn-primary" onClick={openCreate}>
            <Plus size={16} /> Nueva tarea
          </button>
        }
      />

      {error && !creating && !gradeForm.submissionId ? (
        <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-200">{error}</p>
      ) : null}

      {isLoading ? (
        <Spinner />
      ) : isError ? (
        <ErrorState onRetry={refetch} />
      ) : data.length === 0 ? (
        <EmptyState title="Sin tareas" description="Crea la primera tarea para tus cursos." icon={FileText} />
      ) : (
        <div className="space-y-3">
          {data.map((assignment) => (
            <div key={assignment.id} className="card">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="font-semibold text-slate-900 dark:text-white">{assignment.title}</h2>
                    <Badge tone={statusTone(assignment.status)}>{assignment.status}</Badge>
                  </div>
                  <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                    {assignment.courseName} · {assignment.subjectName} · vence {formatDateTime(assignment.dueAt)} · máx. {Number(assignment.maxScore).toFixed(1)}
                  </p>
                  {assignment.description ? (
                    <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{assignment.description}</p>
                  ) : null}
                  <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">
                    {assignment.submissions} entregas · {assignment.graded} calificadas
                  </p>
                </div>
                <button type="button" className="btn-secondary shrink-0" onClick={() => setSubmissionsFor(assignment.id)}>
                  <Send size={16} /> Ver entregas
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={creating}
        title="Nueva tarea"
        onClose={() => setCreating(false)}
        footer={
          <>
            <button type="button" className="btn-secondary" onClick={() => setCreating(false)}>Cancelar</button>
            <button type="submit" form="assignment-form" className="btn-primary" disabled={create.isPending}>
              {create.isPending ? 'Guardando…' : 'Crear tarea'}
            </button>
          </>
        }
      >
        <form id="assignment-form" className="space-y-4" onSubmit={submitCreate}>
          <Field label="Asignatura / curso" htmlFor="assignment-courseSubject">
            <select
              id="assignment-courseSubject"
              className="input"
              value={form.courseSubjectId}
              onChange={(event) => setForm({ ...form, courseSubjectId: event.target.value })}
            >
              {courseSubjects.length === 0 ? <option value="">Sin asignaturas</option> : null}
              {courseSubjects.map((item) => (
                <option key={item.id} value={item.id}>{item.courseName} · {item.subjectName}</option>
              ))}
            </select>
          </Field>
          <Field label="Título" htmlFor="assignment-title">
            <input id="assignment-title" className="input" value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} />
          </Field>
          <Field label="Descripción" htmlFor="assignment-description">
            <textarea
              id="assignment-description"
              rows={3}
              className="input resize-y"
              value={form.description}
              onChange={(event) => setForm({ ...form, description: event.target.value })}
            />
          </Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Fecha y hora de entrega" htmlFor="assignment-dueAt">
              <input
                id="assignment-dueAt"
                type="datetime-local"
                className="input"
                value={form.dueAt}
                onChange={(event) => setForm({ ...form, dueAt: event.target.value })}
              />
            </Field>
            <Field label="Puntaje máximo" htmlFor="assignment-maxScore">
              <input
                id="assignment-maxScore"
                type="number"
                step="0.1"
                min="0.1"
                max="100"
                className="input"
                value={form.maxScore}
                onChange={(event) => setForm({ ...form, maxScore: event.target.value })}
              />
            </Field>
          </div>
          <Field label="Enlace de material (opcional)" htmlFor="assignment-attachment">
            <input
              id="assignment-attachment"
              className="input"
              value={form.attachmentUrl}
              onChange={(event) => setForm({ ...form, attachmentUrl: event.target.value })}
              placeholder="https://…"
            />
          </Field>
          <label className="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
            <input
              type="checkbox"
              className="size-4 rounded border-slate-300"
              checked={form.publish}
              onChange={(event) => setForm({ ...form, publish: event.target.checked })}
            />
            Publicar inmediatamente
          </label>
          {error ? <p className="text-sm font-medium text-rose-600 dark:text-rose-400">{error}</p> : null}
        </form>
      </Modal>

      <Modal
        open={Boolean(submissionsFor)}
        title="Entregas de la tarea"
        onClose={() => setSubmissionsFor(null)}
      >
        {loadingSubmissions ? (
          <Spinner label="Cargando entregas…" />
        ) : submissions.length === 0 ? (
          <EmptyState title="Sin entregas" description="Aún no hay entregas para esta tarea." />
        ) : (
          <div className="space-y-3">
            {submissions.map((submission) => (
              <div key={submission.id} className="rounded-xl border border-slate-200 p-4 dark:border-slate-800">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <p className="font-semibold text-slate-900 dark:text-white">{submission.studentName}</p>
                  <Badge tone={statusTone(submission.status)}>{submission.status}</Badge>
                </div>
                <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                  Entregada {formatDateTime(submission.submittedAt)}
                </p>
                {submission.comment ? <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{submission.comment}</p> : null}
                {submission.fileUrl ? (
                  <a href={submission.fileUrl} target="_blank" rel="noreferrer" className="mt-2 inline-block text-sm font-semibold text-brand-700 hover:underline dark:text-accent-300">
                    Ver archivo
                  </a>
                ) : null}
                {submission.score != null ? (
                  <p className="mt-2 text-sm font-semibold text-emerald-600 dark:text-emerald-400">
                    Nota: {Number(submission.score).toFixed(1)}
                  </p>
                ) : null}

                {gradeForm.submissionId === submission.id ? (
                  <form className="mt-3 space-y-2" onSubmit={submitGrade}>
                    <input
                      type="number"
                      step="0.1"
                      min="0"
                      className="input"
                      placeholder="Nota"
                      value={gradeForm.score}
                      onChange={(event) => setGradeForm({ ...gradeForm, score: event.target.value })}
                    />
                    <input
                      className="input"
                      placeholder="Retroalimentación"
                      value={gradeForm.feedback}
                      onChange={(event) => setGradeForm({ ...gradeForm, feedback: event.target.value })}
                    />
                    <div className="flex gap-2">
                      <button type="submit" className="btn-primary" disabled={grade.isPending}>
                        {grade.isPending ? 'Guardando…' : 'Calificar'}
                      </button>
                      <button type="button" className="btn-secondary" onClick={() => setGradeForm({ submissionId: '', studentName: '', score: '', feedback: '' })}>
                        Cancelar
                      </button>
                    </div>
                  </form>
                ) : (
                  <button
                    type="button"
                    className="btn-secondary mt-3"
                    onClick={() => {
                      setError('')
                      setGradeForm({
                        submissionId: submission.id,
                        studentName: submission.studentName,
                        score: submission.score != null ? String(submission.score) : '',
                        feedback: submission.feedback || '',
                      })
                    }}
                  >
                    {submission.score != null ? 'Actualizar nota' : 'Calificar'}
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
        {error ? <p className="mt-3 text-sm font-medium text-rose-600 dark:text-rose-400">{error}</p> : null}
      </Modal>
    </div>
  )
}

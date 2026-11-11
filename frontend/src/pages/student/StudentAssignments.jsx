import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { CheckCircle2, ClipboardList, FileText, Upload } from 'lucide-react'
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

export default function StudentAssignments() {
  usePageTitle('Tareas')
  const queryClient = useQueryClient()
  const [selected, setSelected] = useState(null)
  const [comment, setComment] = useState('')
  const [fileUrl, setFileUrl] = useState('')
  const [error, setError] = useState('')

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['student-assignments'],
    queryFn: async () => (await api.get('/student/assignments')).data,
  })

  const submit = useMutation({
    mutationFn: async ({ id, payload }) => (await api.post(`/student/assignments/${id}/submit`, payload)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['student-assignments'] })
      queryClient.invalidateQueries({ queryKey: ['student-dashboard'] })
      setSelected(null)
      setComment('')
      setFileUrl('')
      setError('')
    },
    onError: (requestError) => setError(apiErrorMessage(requestError, 'No fue posible registrar la entrega.')),
  })

  function open(assignment) {
    setSelected(assignment)
    setComment(assignment.submission?.comment || '')
    setFileUrl(assignment.submission?.fileUrl || '')
    setError('')
  }

  function send(event) {
    event.preventDefault()
    if (!comment.trim() && !fileUrl.trim()) {
      setError('Agrega un comentario o el enlace del archivo.')
      return
    }
    submit.mutate({ id: selected.id, payload: { comment: comment.trim() || null, fileUrl: fileUrl.trim() || null } })
  }

  const pending = data.filter((item) => !item.submission)
  const done = data.filter((item) => item.submission)

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader
        title="Tareas"
        description={`${pending.length} pendientes · ${done.length} entregadas`}
      />

      {data.length === 0 ? (
        <EmptyState title="Sin tareas asignadas" description="Cuando tus docentes publiquen tareas aparecerán aquí." icon={ClipboardList} />
      ) : (
        <div className="space-y-3">
          {data.map((assignment) => {
            const overdue = !assignment.submission && new Date(assignment.dueAt) < new Date()
            return (
              <div key={assignment.id} className="card">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <h2 className="font-semibold text-slate-900 dark:text-white">{assignment.title}</h2>
                      <Badge tone={statusTone(assignment.status)}>{assignment.status}</Badge>
                      {assignment.submission ? (
                        <Badge tone={statusTone(assignment.submission.status)}>
                          {assignment.submission.status === 'GRADED' ? 'Calificada' : 'Entregada'}
                        </Badge>
                      ) : overdue ? (
                        <Badge tone="red">Vencida</Badge>
                      ) : (
                        <Badge tone="amber">Pendiente</Badge>
                      )}
                    </div>
                    <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                      {assignment.subjectName} · entrega hasta {formatDateTime(assignment.dueAt)} · puntaje máximo {Number(assignment.maxScore).toFixed(1)}
                    </p>
                    {assignment.description ? (
                      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{assignment.description}</p>
                    ) : null}
                    {assignment.submission ? (
                      <div className="mt-3 rounded-xl bg-slate-50 px-3 py-2 text-sm dark:bg-slate-800/60">
                        {assignment.submission.score != null ? (
                          <p className="font-semibold text-emerald-600 dark:text-emerald-400">
                            Nota: {Number(assignment.submission.score).toFixed(1)}
                          </p>
                        ) : null}
                        {assignment.submission.feedback ? (
                          <p className="text-slate-600 dark:text-slate-300">Retroalimentación: {assignment.submission.feedback}</p>
                        ) : null}
                        <p className="text-xs text-slate-500 dark:text-slate-400">
                          Entregada {formatDateTime(assignment.submission.submittedAt)}
                        </p>
                      </div>
                    ) : null}
                  </div>
                  <div className="flex shrink-0 gap-2">
                    {assignment.attachmentUrl ? (
                      <a href={assignment.attachmentUrl} target="_blank" rel="noreferrer" className="btn-secondary">
                        <FileText size={16} /> Material
                      </a>
                    ) : null}
                    <button type="button" className="btn-primary" onClick={() => open(assignment)}>
                      {assignment.submission ? <CheckCircle2 size={16} /> : <Upload size={16} />}
                      {assignment.submission ? 'Reenviar' : 'Entregar'}
                    </button>
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      )}

      <Modal
        open={Boolean(selected)}
        title={selected ? `Entregar: ${selected.title}` : ''}
        onClose={() => setSelected(null)}
        footer={
          <>
            <button type="button" className="btn-secondary" onClick={() => setSelected(null)}>
              Cancelar
            </button>
            <button type="submit" form="submission-form" className="btn-primary" disabled={submit.isPending}>
              {submit.isPending ? 'Enviando…' : 'Registrar entrega'}
            </button>
          </>
        }
      >
        <form id="submission-form" className="space-y-4" onSubmit={send}>
          <Field label="Comentario" htmlFor="comment">
            <textarea
              id="comment"
              rows={4}
              className="input resize-y"
              value={comment}
              onChange={(event) => setComment(event.target.value)}
              placeholder="Describe tu entrega o deja una nota para el docente."
            />
          </Field>
          <Field label="Enlace del archivo" htmlFor="fileUrl" hint="Pega la URL del documento o evidencia (Drive, OneDrive, etc.).">
            <input
              id="fileUrl"
              className="input"
              value={fileUrl}
              onChange={(event) => setFileUrl(event.target.value)}
              placeholder="https://…"
            />
          </Field>
          {error ? <p className="text-sm font-medium text-rose-600 dark:text-rose-400">{error}</p> : null}
        </form>
      </Modal>
    </div>
  )
}

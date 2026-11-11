import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ExternalLink, FolderOpen, Plus } from 'lucide-react'
import { useState } from 'react'
import api, { apiErrorMessage } from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, Field, Modal, PageHeader, Spinner, formatDate } from '../../components/ui'

const TYPE_OPTIONS = [
  { value: 'PDF', label: 'PDF' },
  { value: 'LINK', label: 'Enlace' },
  { value: 'VIDEO', label: 'Video' },
  { value: 'DOC', label: 'Documento' },
  { value: 'IMAGE', label: 'Imagen' },
  { value: 'OTHER', label: 'Otro' },
]

const EMPTY = { courseSubjectId: '', title: '', description: '', type: 'PDF', url: '', visibleToParents: true }

export default function TeacherMaterials() {
  usePageTitle('Materiales')
  const queryClient = useQueryClient()
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState('')

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['teacher-materials'],
    queryFn: async () => (await api.get('/teacher/materials')).data,
  })

  const { data: courseSubjects = [] } = useQuery({
    queryKey: ['teacher-course-subjects'],
    queryFn: async () => (await api.get('/teacher/course-subjects')).data,
  })

  const create = useMutation({
    mutationFn: async (payload) =>
      (await api.post(`/teacher/course-subjects/${payload.courseSubjectId}/materials`, payload)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['teacher-materials'] })
      setCreating(false)
      setForm(EMPTY)
      setError('')
    },
    onError: (requestError) => setError(apiErrorMessage(requestError, 'No fue posible publicar el material.')),
  })

  function submit(event) {
    event.preventDefault()
    if (!form.courseSubjectId) return setError('Selecciona una asignatura.')
    if (!form.title.trim()) return setError('Escribe el título.')
    if (!form.url.trim()) return setError('Ingresa el enlace del recurso.')
    create.mutate({
      courseSubjectId: form.courseSubjectId,
      title: form.title.trim(),
      description: form.description.trim() || null,
      type: form.type,
      url: form.url.trim(),
      visibleToParents: form.visibleToParents,
    })
  }

  return (
    <div>
      <PageHeader
        title="Materiales"
        description="Comparte recursos de estudio con tus estudiantes."
        actions={
          <button type="button" className="btn-primary" onClick={() => { setForm({ ...EMPTY, courseSubjectId: courseSubjects[0]?.id || '' }); setError(''); setCreating(true) }}>
            <Plus size={16} /> Nuevo material
          </button>
        }
      />

      {isLoading ? (
        <Spinner />
      ) : isError ? (
        <ErrorState onRetry={refetch} />
      ) : data.length === 0 ? (
        <EmptyState title="Sin materiales" description="Publica el primer recurso para tus cursos." icon={FolderOpen} />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {data.map((material) => (
            <div key={material.id} className="card flex flex-col">
              <div className="flex items-center justify-between gap-2">
                <Badge tone="brand">{material.type}</Badge>
                <span className="text-xs text-slate-400">{formatDate(material.publishedAt)}</span>
              </div>
              <h2 className="mt-3 font-semibold text-slate-900 dark:text-white">{material.title}</h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">{material.courseName} · {material.subjectName}</p>
              {material.description ? (
                <p className="mt-2 flex-1 text-sm text-slate-600 dark:text-slate-400">{material.description}</p>
              ) : null}
              <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">
                {material.visibleToParents ? 'Visible para acudientes' : 'Solo para estudiantes'}
              </p>
              <a href={material.url} target="_blank" rel="noreferrer" className="btn-secondary mt-4">
                <ExternalLink size={16} /> Abrir recurso
              </a>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={creating}
        title="Nuevo material"
        onClose={() => setCreating(false)}
        footer={
          <>
            <button type="button" className="btn-secondary" onClick={() => setCreating(false)}>Cancelar</button>
            <button type="submit" form="material-form" className="btn-primary" disabled={create.isPending}>
              {create.isPending ? 'Publicando…' : 'Publicar'}
            </button>
          </>
        }
      >
        <form id="material-form" className="space-y-4" onSubmit={submit}>
          <Field label="Asignatura / curso" htmlFor="material-courseSubject">
            <select
              id="material-courseSubject"
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
          <Field label="Título" htmlFor="material-title">
            <input id="material-title" className="input" value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} />
          </Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Tipo" htmlFor="material-type">
              <select id="material-type" className="input" value={form.type} onChange={(event) => setForm({ ...form, type: event.target.value })}>
                {TYPE_OPTIONS.map((type) => (
                  <option key={type.value} value={type.value}>{type.label}</option>
                ))}
              </select>
            </Field>
            <Field label="Enlace" htmlFor="material-url">
              <input id="material-url" className="input" value={form.url} onChange={(event) => setForm({ ...form, url: event.target.value })} placeholder="https://…" />
            </Field>
          </div>
          <Field label="Descripción" htmlFor="material-description">
            <textarea
              id="material-description"
              rows={3}
              className="input resize-y"
              value={form.description}
              onChange={(event) => setForm({ ...form, description: event.target.value })}
            />
          </Field>
          <label className="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
            <input
              type="checkbox"
              className="size-4 rounded border-slate-300"
              checked={form.visibleToParents}
              onChange={(event) => setForm({ ...form, visibleToParents: event.target.checked })}
            />
            Visible para acudientes
          </label>
          {error ? <p className="text-sm font-medium text-rose-600 dark:text-rose-400">{error}</p> : null}
        </form>
      </Modal>
    </div>
  )
}

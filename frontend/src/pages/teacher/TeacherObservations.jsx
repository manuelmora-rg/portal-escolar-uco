import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { MessageSquare, Plus } from 'lucide-react'
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
} from '../../components/ui'

const TYPE_OPTIONS = [
  { value: 'ACADEMIC', label: 'Académica' },
  { value: 'BEHAVIORAL', label: 'Convivencia' },
  { value: 'PRAISE', label: 'Reconocimiento' },
  { value: 'FOLLOW_UP', label: 'Seguimiento' },
]

const VISIBILITY_OPTIONS = [
  { value: 'STUDENT', label: 'Visible para el estudiante' },
  { value: 'PARENT', label: 'Visible para el acudiente' },
  { value: 'INTERNAL', label: 'Solo docentes y administración' },
]

const EMPTY = { studentId: '', courseSubjectId: '', type: 'ACADEMIC', visibility: 'PARENT', description: '' }

export default function TeacherObservations() {
  usePageTitle('Observaciones')
  const queryClient = useQueryClient()
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState('')

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['teacher-observations'],
    queryFn: async () => (await api.get('/teacher/observations')).data,
  })

  const { data: courseSubjects = [] } = useQuery({
    queryKey: ['teacher-course-subjects'],
    queryFn: async () => (await api.get('/teacher/course-subjects')).data,
  })

  const { data: courses = [] } = useQuery({
    queryKey: ['teacher-courses'],
    queryFn: async () => (await api.get('/teacher/courses')).data,
  })

  const { data: students = [] } = useQuery({
    queryKey: ['teacher-all-students', courses.map((course) => course.courseId).join(',')],
    queryFn: async () => {
      const lists = await Promise.all(
        courses.map(async (course) => (await api.get(`/teacher/courses/${course.courseId}/students`)).data),
      )
      const map = new Map()
      for (const list of lists) {
        for (const student of list) {
          map.set(student.studentId, student)
        }
      }
      return Array.from(map.values()).sort((a, b) => a.fullName.localeCompare(b.fullName))
    },
    enabled: courses.length > 0,
  })

  const create = useMutation({
    mutationFn: async (payload) => (await api.post('/teacher/observations', payload)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['teacher-observations'] })
      setCreating(false)
      setForm(EMPTY)
      setError('')
    },
    onError: (requestError) => setError(apiErrorMessage(requestError, 'No fue posible registrar la observación.')),
  })

  function submit(event) {
    event.preventDefault()
    if (!form.studentId) return setError('Selecciona un estudiante.')
    if (!form.description.trim()) return setError('Escribe la observación.')
    create.mutate({
      studentId: form.studentId,
      courseSubjectId: form.courseSubjectId || null,
      type: form.type,
      visibility: form.visibility,
      description: form.description.trim(),
    })
  }

  return (
    <div>
      <PageHeader
        title="Observaciones"
        description="Registra observaciones académicas y de convivencia."
        actions={
          <button type="button" className="btn-primary" onClick={() => { setForm(EMPTY); setError(''); setCreating(true) }}>
            <Plus size={16} /> Nueva observación
          </button>
        }
      />

      {isLoading ? (
        <Spinner />
      ) : isError ? (
        <ErrorState onRetry={refetch} />
      ) : data.length === 0 ? (
        <EmptyState title="Sin observaciones" description="Registra la primera observación de tus estudiantes." icon={MessageSquare} />
      ) : (
        <div className="space-y-3">
          {data.map((item) => (
            <div key={item.id} className="card">
              <div className="flex flex-wrap items-center gap-2">
                <p className="font-semibold text-slate-900 dark:text-white">{item.studentName}</p>
                <Badge tone="brand">{TYPE_OPTIONS.find((type) => type.value === item.type)?.label || item.type}</Badge>
                <Badge tone={item.visibility === 'INTERNAL' ? 'slate' : 'sky'}>{item.visibility}</Badge>
                <span className="text-xs text-slate-500 dark:text-slate-400">{formatDateTime(item.createdAt)}</span>
              </div>
              <p className="mt-2 text-slate-700 dark:text-slate-200">{item.description}</p>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={creating}
        title="Nueva observación"
        onClose={() => setCreating(false)}
        footer={
          <>
            <button type="button" className="btn-secondary" onClick={() => setCreating(false)}>Cancelar</button>
            <button type="submit" form="observation-form" className="btn-primary" disabled={create.isPending}>
              {create.isPending ? 'Guardando…' : 'Registrar'}
            </button>
          </>
        }
      >
        <form id="observation-form" className="space-y-4" onSubmit={submit}>
          <Field label="Estudiante" htmlFor="observation-student">
            <select
              id="observation-student"
              className="input"
              value={form.studentId}
              onChange={(event) => setForm({ ...form, studentId: event.target.value })}
            >
              <option value="">Selecciona un estudiante</option>
              {students.map((student) => (
                <option key={student.studentId} value={student.studentId}>{student.fullName}</option>
              ))}
            </select>
          </Field>
          <Field label="Asignatura (opcional)" htmlFor="observation-courseSubject">
            <select
              id="observation-courseSubject"
              className="input"
              value={form.courseSubjectId}
              onChange={(event) => setForm({ ...form, courseSubjectId: event.target.value })}
            >
              <option value="">Sin asignatura específica</option>
              {courseSubjects.map((item) => (
                <option key={item.id} value={item.id}>{item.courseName} · {item.subjectName}</option>
              ))}
            </select>
          </Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Tipo" htmlFor="observation-type">
              <select id="observation-type" className="input" value={form.type} onChange={(event) => setForm({ ...form, type: event.target.value })}>
                {TYPE_OPTIONS.map((type) => (
                  <option key={type.value} value={type.value}>{type.label}</option>
                ))}
              </select>
            </Field>
            <Field label="Visibilidad" htmlFor="observation-visibility">
              <select id="observation-visibility" className="input" value={form.visibility} onChange={(event) => setForm({ ...form, visibility: event.target.value })}>
                {VISIBILITY_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>
            </Field>
          </div>
          <Field label="Descripción" htmlFor="observation-description">
            <textarea
              id="observation-description"
              rows={4}
              className="input resize-y"
              value={form.description}
              onChange={(event) => setForm({ ...form, description: event.target.value })}
            />
          </Field>
          {error ? <p className="text-sm font-medium text-rose-600 dark:text-rose-400">{error}</p> : null}
        </form>
      </Modal>
    </div>
  )
}

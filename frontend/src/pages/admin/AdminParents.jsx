import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { HeartHandshake, Link2 } from 'lucide-react'
import { useState } from 'react'
import api, { apiErrorMessage } from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, Field, Modal, PageHeader, Spinner } from '../../components/ui'

const RELATIONSHIPS = [
  { value: 'FATHER', label: 'Padre' },
  { value: 'MOTHER', label: 'Madre' },
  { value: 'GUARDIAN', label: 'Acudiente' },
  { value: 'OTHER', label: 'Otro' },
]

export default function AdminParents() {
  usePageTitle('Acudientes')
  const queryClient = useQueryClient()
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({ parentEmail: '', studentDocument: '', relationship: 'MOTHER' })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['admin-parents'],
    queryFn: async () => (await api.get('/admin/parents')).data,
  })

  const link = useMutation({
    mutationFn: async (payload) => (await api.post('/admin/parent-links', payload)).data,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['admin-parents'] })
      setOpen(false)
      setForm({ parentEmail: '', studentDocument: '', relationship: 'MOTHER' })
      setError('')
      setSuccess(data.message || 'Vínculo registrado.')
    },
    onError: (requestError) => setError(apiErrorMessage(requestError, 'No fue posible registrar el vínculo.')),
  })

  function submit(event) {
    event.preventDefault()
    if (!form.parentEmail.trim()) return setError('Ingresa el correo del acudiente.')
    if (!form.studentDocument.trim()) return setError('Ingresa el documento del estudiante.')
    link.mutate({
      parentEmail: form.parentEmail.trim(),
      studentDocument: form.studentDocument.trim(),
      relationship: form.relationship,
    })
  }

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader
        title="Acudientes"
        description={`${data.length} acudientes registrados.`}
        actions={
          <button type="button" className="btn-primary" onClick={() => { setError(''); setSuccess(''); setOpen(true) }}>
            <Link2 size={16} /> Vincular estudiante
          </button>
        }
      />

      {success ? (
        <p className="mb-4 rounded-xl border border-brand-200 bg-brand-50 px-4 py-3 text-sm text-brand-700 dark:border-brand-800 dark:bg-brand-900/40 dark:text-brand-100">
          {success}
        </p>
      ) : null}

      {data.length === 0 ? (
        <EmptyState title="Sin acudientes" icon={HeartHandshake} />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table-base">
            <thead>
              <tr>
                <th>Acudiente</th>
                <th>Correo</th>
                <th>Documento</th>
                <th>Teléfono</th>
                <th>Hijos vinculados</th>
              </tr>
            </thead>
            <tbody>
              {data.map((parent) => (
                <tr key={parent.userId}>
                  <td className="font-medium text-slate-800 dark:text-slate-100">{parent.fullName}</td>
                  <td className="text-slate-500 dark:text-slate-400">{parent.email}</td>
                  <td className="text-slate-500 dark:text-slate-400">{parent.documentNumber || '—'}</td>
                  <td className="text-slate-500 dark:text-slate-400">{parent.phone || '—'}</td>
                  <td><Badge tone={parent.children > 0 ? 'green' : 'amber'}>{parent.children}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        open={open}
        title="Vincular estudiante a un acudiente"
        onClose={() => setOpen(false)}
        footer={
          <>
            <button type="button" className="btn-secondary" onClick={() => setOpen(false)}>Cancelar</button>
            <button type="submit" form="parent-link-form" className="btn-primary" disabled={link.isPending}>
              {link.isPending ? 'Guardando…' : 'Vincular'}
            </button>
          </>
        }
      >
        <form id="parent-link-form" className="space-y-4" onSubmit={submit}>
          <Field label="Correo del acudiente" htmlFor="parentEmail">
            <input
              id="parentEmail"
              type="email"
              className="input"
              value={form.parentEmail}
              onChange={(event) => setForm({ ...form, parentEmail: event.target.value })}
              placeholder="acudiente@portalescolar.edu.co"
            />
          </Field>
          <Field label="Documento del estudiante" htmlFor="studentDocument">
            <input
              id="studentDocument"
              className="input"
              value={form.studentDocument}
              onChange={(event) => setForm({ ...form, studentDocument: event.target.value })}
            />
          </Field>
          <Field label="Parentesco" htmlFor="relationship">
            <select
              id="relationship"
              className="input"
              value={form.relationship}
              onChange={(event) => setForm({ ...form, relationship: event.target.value })}
            >
              {RELATIONSHIPS.map((option) => (
                <option key={option.value} value={option.value}>{option.label}</option>
              ))}
            </select>
          </Field>
          {error ? <p className="text-sm font-medium text-rose-600 dark:text-rose-400">{error}</p> : null}
        </form>
      </Modal>
    </div>
  )
}

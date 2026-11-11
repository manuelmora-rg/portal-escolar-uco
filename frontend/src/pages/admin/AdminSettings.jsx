import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Save } from 'lucide-react'
import { useEffect, useState } from 'react'
import api, { apiErrorMessage } from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { ErrorState, Field, PageHeader, Spinner } from '../../components/ui'

const EMPTY = {
  name: '',
  shortName: '',
  slogan: '',
  mission: '',
  vision: '',
  history: '',
  address: '',
  phone: '',
  email: '',
  primaryColor: '#0b2545',
  accentColor: '#38bdf8',
  logoUrl: '',
}

export default function AdminSettings() {
  usePageTitle('Configuración institucional')
  const queryClient = useQueryClient()
  const [form, setForm] = useState(EMPTY)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['admin-institution'],
    queryFn: async () => (await api.get('/admin/settings/institution')).data,
  })

  useEffect(() => {
    if (data) {
      setForm({
        name: data.name || '',
        shortName: data.shortName || '',
        slogan: data.slogan || '',
        mission: data.mission || '',
        vision: data.vision || '',
        history: data.history || '',
        address: data.address || '',
        phone: data.phone || '',
        email: data.email || '',
        primaryColor: data.primaryColor || '#0b2545',
        accentColor: data.accentColor || '#38bdf8',
        logoUrl: data.logoUrl || '',
      })
    }
  }, [data])

  const save = useMutation({
    mutationFn: async (payload) => (await api.put('/admin/settings/institution', payload)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-institution'] })
      queryClient.invalidateQueries({ queryKey: ['public-institution'] })
      setMessage('Configuración institucional actualizada.')
      setError('')
    },
    onError: (requestError) => {
      setMessage('')
      setError(apiErrorMessage(requestError, 'No fue posible actualizar la configuración.'))
    },
  })

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function submit(event) {
    event.preventDefault()
    if (!form.name.trim()) {
      setError('El nombre de la institución es obligatorio.')
      setMessage('')
      return
    }
    save.mutate(form)
  }

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader title="Configuración institucional" description="Información pública que aparece en el sitio web." />

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

      <form className="space-y-6" onSubmit={submit}>
        <section className="card grid gap-4 sm:grid-cols-2">
          <Field label="Nombre de la institución *" htmlFor="name">
            <input id="name" className="input" value={form.name} onChange={(event) => update('name', event.target.value)} />
          </Field>
          <Field label="Nombre corto" htmlFor="shortName">
            <input id="shortName" className="input" value={form.shortName} onChange={(event) => update('shortName', event.target.value)} />
          </Field>
          <Field label="Eslogan" htmlFor="slogan">
            <input id="slogan" className="input" value={form.slogan} onChange={(event) => update('slogan', event.target.value)} />
          </Field>
          <Field label="Correo institucional" htmlFor="email">
            <input id="email" type="email" className="input" value={form.email} onChange={(event) => update('email', event.target.value)} />
          </Field>
          <Field label="Teléfono" htmlFor="phone">
            <input id="phone" className="input" value={form.phone} onChange={(event) => update('phone', event.target.value)} />
          </Field>
          <Field label="Dirección" htmlFor="address">
            <input id="address" className="input" value={form.address} onChange={(event) => update('address', event.target.value)} />
          </Field>
          <Field label="URL del logo" htmlFor="logoUrl">
            <input id="logoUrl" className="input" value={form.logoUrl} onChange={(event) => update('logoUrl', event.target.value)} />
          </Field>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Color primario" htmlFor="primaryColor">
              <input id="primaryColor" type="color" className="input h-11 !p-1" value={form.primaryColor} onChange={(event) => update('primaryColor', event.target.value)} />
            </Field>
            <Field label="Color de acento" htmlFor="accentColor">
              <input id="accentColor" type="color" className="input h-11 !p-1" value={form.accentColor} onChange={(event) => update('accentColor', event.target.value)} />
            </Field>
          </div>
        </section>

        <section className="card grid gap-4">
          <Field label="Misión" htmlFor="mission">
            <textarea id="mission" rows={3} className="input resize-y" value={form.mission} onChange={(event) => update('mission', event.target.value)} />
          </Field>
          <Field label="Visión" htmlFor="vision">
            <textarea id="vision" rows={3} className="input resize-y" value={form.vision} onChange={(event) => update('vision', event.target.value)} />
          </Field>
          <Field label="Historia" htmlFor="history">
            <textarea id="history" rows={4} className="input resize-y" value={form.history} onChange={(event) => update('history', event.target.value)} />
          </Field>
        </section>

        <button type="submit" className="btn-primary" disabled={save.isPending}>
          <Save size={16} /> {save.isPending ? 'Guardando…' : 'Guardar cambios'}
        </button>
      </form>
    </div>
  )
}

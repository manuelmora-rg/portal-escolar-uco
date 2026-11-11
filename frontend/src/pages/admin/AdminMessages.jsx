import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Mail } from 'lucide-react'
import api, { apiErrorMessage } from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner, formatDateTime, statusTone } from '../../components/ui'

const STATUS_OPTIONS = [
  { value: 'NEW', label: 'Nuevo' },
  { value: 'READ', label: 'Leído' },
  { value: 'ANSWERED', label: 'Respondido' },
  { value: 'ARCHIVED', label: 'Archivado' },
]

export default function AdminMessages() {
  usePageTitle('Mensajes de contacto')
  const queryClient = useQueryClient()

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['admin-contact-messages'],
    queryFn: async () => (await api.get('/admin/contact-messages')).data,
  })

  const update = useMutation({
    mutationFn: async ({ id, status }) => (await api.patch(`/admin/contact-messages/${id}/status`, { status })).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-contact-messages'] }),
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader title="Mensajes de contacto" description={`${data.length} mensajes recibidos desde el sitio público.`} />

      {update.isError ? (
        <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-200">
          {apiErrorMessage(update.error, 'No fue posible actualizar el estado.')}
        </p>
      ) : null}

      {data.length === 0 ? (
        <EmptyState title="Sin mensajes" description="Los mensajes enviados desde el formulario de contacto aparecerán aquí." icon={Mail} />
      ) : (
        <div className="space-y-3">
          {data.map((message) => (
            <div key={message.id} className="card">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="font-semibold text-slate-900 dark:text-white">{message.subject}</h2>
                    <Badge tone={statusTone(message.status)}>{message.status}</Badge>
                  </div>
                  <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                    {message.fullName} · {message.email}
                    {message.phone ? ` · ${message.phone}` : ''}
                  </p>
                  <p className="mt-1 text-xs text-slate-400">{formatDateTime(message.createdAt)}</p>
                  <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{message.message}</p>
                </div>
                <select
                  className="input max-w-[180px] shrink-0"
                  value={message.status}
                  onChange={(event) => update.mutate({ id: message.id, status: event.target.value })}
                  disabled={update.isPending}
                >
                  {STATUS_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>{option.label}</option>
                  ))}
                </select>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

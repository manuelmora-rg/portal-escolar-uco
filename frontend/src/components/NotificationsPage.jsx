import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bell, CheckCheck } from 'lucide-react'
import api from '../lib/api'
import { EmptyState, ErrorState, PageHeader, Spinner, formatDateTime } from './ui'

const TYPE_TONE = {
  GRADE: 'bg-brand-100 text-brand-700 dark:bg-brand-900/60 dark:text-brand-100',
  ASSIGNMENT: 'bg-amber-100 text-amber-800 dark:bg-amber-900/50 dark:text-amber-200',
  ATTENDANCE: 'bg-rose-100 text-rose-700 dark:bg-rose-900/50 dark:text-rose-200',
  MEETING: 'bg-brand-100 text-brand-800 dark:bg-brand-900/60 dark:text-brand-100',
  SYSTEM: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200',
}

export default function NotificationsPage({ basePath, title = 'Notificaciones' }) {
  const queryClient = useQueryClient()
  const queryKey = [`${basePath}-notifications`]

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey,
    queryFn: async () => (await api.get(`${basePath}/notifications`)).data,
  })

  const markRead = useMutation({
    mutationFn: async (id) => api.patch(`${basePath}/notifications/${id}/read`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey }),
  })

  const unread = data.filter((item) => !item.read).length

  return (
    <div>
      <PageHeader
        title={title}
        description={unread > 0 ? `${unread} sin leer de ${data.length} notificaciones.` : 'Todas tus notificaciones están al día.'}
        actions={
          unread > 0 ? (
            <button
              type="button"
              className="btn-secondary"
              onClick={() => data.filter((item) => !item.read).forEach((item) => markRead.mutate(item.id))}
              disabled={markRead.isPending}
            >
              <CheckCheck size={16} /> Marcar todas como leídas
            </button>
          ) : null
        }
      />

      {isLoading ? (
        <Spinner />
      ) : isError ? (
        <ErrorState onRetry={refetch} />
      ) : data.length === 0 ? (
        <EmptyState title="Sin notificaciones" description="Aquí aparecerán las novedades relacionadas con tu actividad." icon={Bell} />
      ) : (
        <div className="space-y-2">
          {data.map((item) => (
            <div
              key={item.id}
              className={`card flex items-start gap-4 !p-4 ${item.read ? '' : 'border-l-4 !border-l-accent-500'}`}
            >
              <span className={`grid size-10 shrink-0 place-items-center rounded-xl ${TYPE_TONE[item.type] || TYPE_TONE.SYSTEM}`}>
                <Bell size={18} />
              </span>
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <p className="font-semibold text-slate-900 dark:text-white">{item.title}</p>
                  {!item.read ? <span className="badge bg-accent-500/15 text-accent-700 dark:text-accent-300">Nueva</span> : null}
                </div>
                <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">{item.message}</p>
                <p className="mt-1 text-xs text-slate-400">{formatDateTime(item.createdAt)}</p>
              </div>
              {!item.read ? (
                <button
                  type="button"
                  className="btn-ghost !px-2.5 !py-1.5 text-xs"
                  onClick={() => markRead.mutate(item.id)}
                  disabled={markRead.isPending}
                >
                  Marcar leída
                </button>
              ) : null}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

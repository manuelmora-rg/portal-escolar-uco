import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { CalendarClock, Check, MapPin, X } from 'lucide-react'
import api, { apiErrorMessage } from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner, formatDateTime, statusTone } from '../../components/ui'

const CONFIRM_LABEL = {
  PENDING: 'Sin responder',
  CONFIRMED: 'Confirmada',
  DECLINED: 'No asistiré',
}

export default function ParentMeetings() {
  usePageTitle('Reuniones')
  const queryClient = useQueryClient()

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['parent-meetings'],
    queryFn: async () => (await api.get('/parent/meetings')).data,
  })

  const confirm = useMutation({
    mutationFn: async ({ id, status }) => (await api.post(`/parent/meetings/${id}/confirm`, { status })).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['parent-meetings'] }),
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader title="Reuniones" description="Confirma tu asistencia a las reuniones programadas por la institución." />

      {confirm.isError ? (
        <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-200">
          {apiErrorMessage(confirm.error, 'No fue posible registrar la confirmación.')}
        </p>
      ) : null}

      {data.length === 0 ? (
        <EmptyState title="Sin reuniones" description="No hay reuniones programadas por ahora." icon={CalendarClock} />
      ) : (
        <div className="space-y-3">
          {data.map((meeting) => (
            <div key={meeting.id} className="card">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="font-semibold text-slate-900 dark:text-white">{meeting.title}</h2>
                    <Badge tone={statusTone(meeting.confirmationStatus)}>
                      {CONFIRM_LABEL[meeting.confirmationStatus] || meeting.confirmationStatus}
                    </Badge>
                  </div>
                  <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                    {formatDateTime(meeting.scheduledAt)}
                    {meeting.courseName ? ` · ${meeting.courseName}` : ''}
                    {meeting.teacherName ? ` · ${meeting.teacherName}` : ''}
                  </p>
                  {meeting.location ? (
                    <p className="mt-1 flex items-center gap-1.5 text-sm text-slate-500 dark:text-slate-400">
                      <MapPin size={14} /> {meeting.location}
                    </p>
                  ) : null}
                  {meeting.description ? <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{meeting.description}</p> : null}
                </div>
                <div className="flex shrink-0 gap-2">
                  <button
                    type="button"
                    className="btn-primary"
                    onClick={() => confirm.mutate({ id: meeting.id, status: 'CONFIRMED' })}
                    disabled={confirm.isPending || meeting.confirmationStatus === 'CONFIRMED'}
                  >
                    <Check size={16} /> Asistiré
                  </button>
                  <button
                    type="button"
                    className="btn-secondary"
                    onClick={() => confirm.mutate({ id: meeting.id, status: 'DECLINED' })}
                    disabled={confirm.isPending || meeting.confirmationStatus === 'DECLINED'}
                  >
                    <X size={16} /> No podré
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

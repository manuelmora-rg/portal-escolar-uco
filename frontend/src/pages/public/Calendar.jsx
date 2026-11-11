import { useQuery } from '@tanstack/react-query'
import { CalendarDays, Clock, MapPin } from 'lucide-react'
import { useMemo } from 'react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { EmptyState, ErrorState, Spinner, formatDate, formatDateTime } from '../../components/ui'

const MONTHS = [
  'enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
  'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre',
]

export default function Calendar() {
  usePageTitle('Calendario')
  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['public-events'],
    queryFn: async () => (await api.get('/public/events')).data,
  })

  const grouped = useMemo(() => {
    const groups = new Map()
    const sorted = [...data].sort((a, b) => new Date(a.startsAt) - new Date(b.startsAt))
    for (const event of sorted) {
      const date = new Date(event.startsAt)
      const key = `${date.getFullYear()}-${date.getMonth()}`
      if (!groups.has(key)) {
        groups.set(key, { label: `${MONTHS[date.getMonth()]} ${date.getFullYear()}`, items: [] })
      }
      groups.get(key).items.push(event)
    }
    return Array.from(groups.values())
  }, [data])

  return (
    <div>
      <section className="bg-brand-950 py-16 text-white">
        <div className="container-page">
          <p className="text-xs font-semibold tracking-wide text-accent-300 uppercase">Agenda</p>
          <h1 className="mt-2 text-3xl font-extrabold sm:text-4xl">Calendario institucional</h1>
          <p className="mt-4 max-w-2xl text-slate-300">
            Fechas clave, actividades académicas, culturales y encuentros con familias.
          </p>
        </div>
      </section>

      <section className="container-page py-12">
        {isLoading ? (
          <Spinner />
        ) : isError ? (
          <ErrorState onRetry={refetch} />
        ) : grouped.length === 0 ? (
          <EmptyState
            title="Sin eventos programados"
            description="Cuando se publiquen nuevas actividades aparecerán en esta agenda."
            icon={CalendarDays}
          />
        ) : (
          <div className="space-y-10">
            {grouped.map((group) => (
              <div key={group.label}>
                <h2 className="mb-4 flex items-center gap-2 text-lg font-bold text-slate-900 capitalize dark:text-white">
                  <CalendarDays size={18} className="text-accent-600 dark:text-accent-400" />
                  {group.label}
                </h2>
                <div className="space-y-3">
                  {group.items.map((event) => (
                    <div key={event.id} className="card flex flex-col gap-4 sm:flex-row sm:items-start">
                      <div className="grid size-14 shrink-0 place-items-center rounded-xl bg-brand-900 text-white dark:bg-accent-500 dark:text-slate-950">
                        <span className="text-lg font-bold leading-none">{new Date(event.startsAt).getDate()}</span>
                        <span className="text-[10px] uppercase">{MONTHS[new Date(event.startsAt).getMonth()].slice(0, 3)}</span>
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <h3 className="font-semibold text-slate-900 dark:text-white">{event.title}</h3>
                          <span className="badge bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                            {event.type || 'Institucional'}
                          </span>
                        </div>
                        {event.description ? (
                          <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">{event.description}</p>
                        ) : null}
                        <div className="mt-2 flex flex-wrap gap-4 text-xs text-slate-500 dark:text-slate-400">
                          <span className="inline-flex items-center gap-1.5">
                            <Clock size={13} />
                            {event.allDay ? formatDate(event.startsAt) : formatDateTime(event.startsAt)}
                          </span>
                          {event.location ? (
                            <span className="inline-flex items-center gap-1.5">
                              <MapPin size={13} /> {event.location}
                            </span>
                          ) : null}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}

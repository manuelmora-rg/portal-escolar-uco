import { useQuery } from '@tanstack/react-query'
import { CalendarDays } from 'lucide-react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { EmptyState, ErrorState, PageHeader, Spinner, DAY_NAMES, formatTime } from '../../components/ui'

const DAYS = [1, 2, 3, 4, 5, 6]

export default function StudentSchedule() {
  usePageTitle('Horario')
  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['student-schedule'],
    queryFn: async () => (await api.get('/student/schedule')).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  const byDay = DAYS.map((day) => ({
    day,
    items: data
      .filter((item) => item.dayOfWeek === day)
      .sort((a, b) => String(a.startTime).localeCompare(String(b.startTime))),
  })).filter((group) => group.items.length > 0)

  return (
    <div>
      <PageHeader title="Horario de clases" description="Distribución semanal de asignaturas y docentes." />

      {byDay.length === 0 ? (
        <EmptyState title="Sin horario configurado" description="La institución aún no ha publicado tu horario." icon={CalendarDays} />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {byDay.map((group) => (
            <section key={group.day} className="card">
              <h2 className="card-title">{DAY_NAMES[group.day]}</h2>
              <div className="mt-3 space-y-2">
                {group.items.map((item) => (
                  <div key={item.id} className="rounded-xl border border-slate-200 px-3 py-2.5 dark:border-slate-800">
                    <div className="flex items-center justify-between gap-2">
                      <p className="font-medium text-slate-900 dark:text-white">{item.subjectName}</p>
                      <span className="badge bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                        {formatTime(item.startTime)} – {formatTime(item.endTime)}
                      </span>
                    </div>
                    <p className="mt-0.5 text-xs text-slate-500 dark:text-slate-400">
                      {item.teacherName}
                      {item.classroom ? ` · Aula ${item.classroom}` : ''}
                    </p>
                  </div>
                ))}
              </div>
            </section>
          ))}
        </div>
      )}
    </div>
  )
}

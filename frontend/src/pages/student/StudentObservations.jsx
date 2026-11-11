import { useQuery } from '@tanstack/react-query'
import { MessageSquare } from 'lucide-react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner, formatDateTime } from '../../components/ui'

const TYPE_LABEL = {
  ACADEMIC: 'Académica',
  BEHAVIOR: 'Convivencia',
  ATTENDANCE: 'Asistencia',
  POSITIVE: 'Reconocimiento',
  GENERAL: 'General',
}

export default function StudentObservations() {
  usePageTitle('Observaciones')
  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['student-observations'],
    queryFn: async () => (await api.get('/student/observations')).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader title="Observaciones" description="Comentarios registrados por tus docentes." />

      {data.length === 0 ? (
        <EmptyState title="Sin observaciones" description="No tienes observaciones registradas." icon={MessageSquare} />
      ) : (
        <div className="space-y-3">
          {data.map((item) => (
            <div key={item.id} className="card">
              <div className="flex flex-wrap items-center gap-2">
                <Badge tone="brand">{TYPE_LABEL[item.type] || item.type}</Badge>
                <span className="text-xs text-slate-500 dark:text-slate-400">{formatDateTime(item.createdAt)}</span>
              </div>
              <p className="mt-2 text-slate-700 dark:text-slate-200">{item.description}</p>
              <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">Registrada por {item.teacherName || 'docente'}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

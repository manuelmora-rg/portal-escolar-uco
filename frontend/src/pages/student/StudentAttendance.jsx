import { useQuery } from '@tanstack/react-query'
import { CalendarCheck } from 'lucide-react'
import { useState } from 'react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import {
  Badge,
  EmptyState,
  ErrorState,
  PageHeader,
  Spinner,
  StatCard,
  formatDate,
  statusTone,
} from '../../components/ui'

const STATUS_LABEL = {
  PRESENT: 'Presente',
  ABSENT: 'Ausente',
  LATE: 'Tarde',
  EXCUSED: 'Excusado',
}

export default function StudentAttendance() {
  usePageTitle('Asistencia')
  const [range, setRange] = useState('30')

  const from = (() => {
    const date = new Date()
    date.setDate(date.getDate() - Number(range))
    return date.toISOString().slice(0, 10)
  })()

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['student-attendance', range],
    queryFn: async () => (await api.get('/student/attendance', { params: { from } })).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  const summary = data?.summary || { present: 0, absent: 0, late: 0, excused: 0, total: 0, rate: 0 }
  const records = data?.records || []

  return (
    <div>
      <PageHeader
        title="Asistencia"
        description="Registro de asistencia a clases."
        actions={
          <select className="input max-w-xs" value={range} onChange={(event) => setRange(event.target.value)}>
            <option value="7">Últimos 7 días</option>
            <option value="30">Últimos 30 días</option>
            <option value="60">Últimos 60 días</option>
            <option value="120">Últimos 120 días</option>
          </select>
        }
      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Asistencia" value={`${Number(summary.rate).toFixed(1)}%`} icon={CalendarCheck} tone="green" />
        <StatCard label="Presentes" value={summary.present} tone="green" />
        <StatCard label="Ausentes" value={summary.absent} tone="red" />
        <StatCard label="Tardanzas / excusas" value={summary.late + summary.excused} tone="amber" />
      </div>

      <section className="card mt-6">
        <h2 className="card-title">Detalle de registros</h2>
        {records.length === 0 ? (
          <div className="mt-4">
            <EmptyState title="Sin registros" description="No hay registros de asistencia en el periodo seleccionado." />
          </div>
        ) : (
          <div className="mt-4 overflow-x-auto">
            <table className="table-base">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Estado</th>
                  <th>Justificación</th>
                </tr>
              </thead>
              <tbody>
                {records.map((record) => (
                  <tr key={record.id}>
                    <td className="text-slate-700 dark:text-slate-200">{formatDate(record.date)}</td>
                    <td><Badge tone={statusTone(record.status)}>{STATUS_LABEL[record.status] || record.status}</Badge></td>
                    <td className="text-slate-500 dark:text-slate-400">{record.justification || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}

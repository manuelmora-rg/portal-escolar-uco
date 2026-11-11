import { useQuery } from '@tanstack/react-query'
import { GraduationCap } from 'lucide-react'
import { useState } from 'react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner, formatDate } from '../../components/ui'

export default function StudentGrades() {
  usePageTitle('Calificaciones')
  const [periodId, setPeriodId] = useState('')

  const { data: periods = [] } = useQuery({
    queryKey: ['student-periods'],
    queryFn: async () => (await api.get('/student/periods')).data,
  })

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['student-grades', periodId],
    queryFn: async () =>
      (await api.get('/student/grades', { params: periodId ? { periodId } : {} })).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader
        title="Calificaciones"
        description="Consulta tus notas por periodo y asignatura."
        actions={
          <select className="input max-w-xs" value={periodId} onChange={(event) => setPeriodId(event.target.value)}>
            <option value="">Todos los periodos</option>
            {periods.map((period) => (
              <option key={period.id} value={period.id}>
                {period.name}
              </option>
            ))}
          </select>
        }
      />

      {data.length === 0 ? (
        <EmptyState title="Sin calificaciones" description="Aún no hay notas registradas para el filtro seleccionado." icon={GraduationCap} />
      ) : (
        <div className="grid gap-4 lg:grid-cols-2">
          {data.map((subject) => (
            <section key={subject.courseSubjectId} className="card">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 className="font-semibold text-slate-900 dark:text-white">{subject.subjectName}</h2>
                  <p className="text-xs text-slate-500 dark:text-slate-400">{subject.teacherName}</p>
                </div>
                <div className="text-right">
                  <p className="text-2xl font-bold text-slate-900 dark:text-white">
                    {subject.average == null ? '—' : Number(subject.average).toFixed(1)}
                  </p>
                  <p className="text-[11px] text-slate-500 dark:text-slate-400">Promedio</p>
                </div>
              </div>

              {subject.grades.length === 0 ? (
                <p className="mt-4 text-sm text-slate-500 dark:text-slate-400">Sin notas registradas.</p>
              ) : (
                <div className="mt-4 overflow-x-auto">
                  <table className="table-base">
                    <thead>
                      <tr>
                        <th>Actividad</th>
                        <th>Tipo</th>
                        <th>Nota</th>
                        <th>Peso</th>
                        <th>Fecha</th>
                      </tr>
                    </thead>
                    <tbody>
                      {subject.grades.map((grade) => (
                        <tr key={grade.id}>
                          <td>
                            <p className="font-medium text-slate-800 dark:text-slate-100">{grade.title}</p>
                            {grade.comment ? <p className="text-xs text-slate-400">{grade.comment}</p> : null}
                          </td>
                          <td><Badge tone="slate">{grade.type}</Badge></td>
                          <td>
                            <span className={`font-semibold ${Number(grade.score) < 3 ? 'text-rose-600 dark:text-rose-400' : 'text-emerald-600 dark:text-emerald-400'}`}>
                              {Number(grade.score).toFixed(1)}
                            </span>
                          </td>
                          <td className="text-slate-500 dark:text-slate-400">{Number(grade.weight).toFixed(0)}%</td>
                          <td className="text-xs text-slate-500 dark:text-slate-400">{formatDate(grade.date)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          ))}
        </div>
      )}
    </div>
  )
}

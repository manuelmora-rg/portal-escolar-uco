import { useQuery } from '@tanstack/react-query'
import { UserCog } from 'lucide-react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner } from '../../components/ui'

export default function AdminTeachers() {
  usePageTitle('Docentes')
  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['admin-teachers'],
    queryFn: async () => (await api.get('/admin/teachers')).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader title="Docentes" description={`${data.length} docentes registrados.`} />

      {data.length === 0 ? (
        <EmptyState title="Sin docentes" icon={UserCog} />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table-base">
            <thead>
              <tr>
                <th>Docente</th>
                <th>Correo</th>
                <th>Código</th>
                <th>Especialidad</th>
                <th>Cuenta</th>
              </tr>
            </thead>
            <tbody>
              {data.map((teacher) => (
                <tr key={teacher.userId}>
                  <td className="font-medium text-slate-800 dark:text-slate-100">{teacher.fullName}</td>
                  <td className="text-slate-500 dark:text-slate-400">{teacher.email}</td>
                  <td className="text-slate-500 dark:text-slate-400">{teacher.employeeCode || '—'}</td>
                  <td className="text-slate-500 dark:text-slate-400">{teacher.specialty || '—'}</td>
                  <td><Badge tone={teacher.enabled ? 'green' : 'red'}>{teacher.enabled ? 'Activa' : 'Inactiva'}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

import { useQuery } from '@tanstack/react-query'
import { GraduationCap, Search } from 'lucide-react'
import { useMemo, useState } from 'react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner } from '../../components/ui'

export default function AdminStudents() {
  usePageTitle('Estudiantes')
  const [search, setSearch] = useState('')

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['admin-students'],
    queryFn: async () => (await api.get('/admin/students')).data,
  })

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase()
    if (!term) return data
    return data.filter((student) =>
      [student.fullName, student.email, student.documentNumber, student.gradeLevel, student.groupName]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(term)),
    )
  }, [data, search])

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader title="Estudiantes" description={`${data.length} estudiantes registrados.`} />

      <div className="relative mb-4 max-w-sm">
        <Search className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-slate-400" size={16} />
        <input
          type="search"
          className="input pl-9"
          placeholder="Buscar por nombre, documento o curso…"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
      </div>

      {filtered.length === 0 ? (
        <EmptyState title="Sin resultados" description="No hay estudiantes que coincidan con la búsqueda." icon={GraduationCap} />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table-base">
            <thead>
              <tr>
                <th>Estudiante</th>
                <th>Documento</th>
                <th>Correo</th>
                <th>Curso</th>
                <th>Estado</th>
                <th>Cuenta</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((student) => (
                <tr key={student.userId}>
                  <td className="font-medium text-slate-800 dark:text-slate-100">{student.fullName}</td>
                  <td className="text-slate-500 dark:text-slate-400">{student.documentNumber || '—'}</td>
                  <td className="text-slate-500 dark:text-slate-400">{student.email}</td>
                  <td className="text-slate-500 dark:text-slate-400">
                    {student.gradeLevel ? `${student.gradeLevel} · ${student.groupName}` : 'Sin matrícula'}
                  </td>
                  <td><Badge tone={student.status === 'ACTIVE' ? 'green' : 'slate'}>{student.status || '—'}</Badge></td>
                  <td><Badge tone={student.enabled ? 'green' : 'red'}>{student.enabled ? 'Activa' : 'Inactiva'}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

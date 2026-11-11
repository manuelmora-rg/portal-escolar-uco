import { useQuery } from '@tanstack/react-query'
import { Bell, HeartHandshake, Users } from 'lucide-react'
import { Link } from 'react-router-dom'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner, StatCard } from '../../components/ui'

export default function ParentDashboard() {
  usePageTitle('Panel del acudiente')
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['parent-dashboard'],
    queryFn: async () => (await api.get('/parent/dashboard')).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader
        title={`Hola, ${(data.parentName || '').split(' ')[0] || 'acudiente'}`}
        description="Consulta la información autorizada de tus hijos."
      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <StatCard label="Hijos vinculados" value={data.children?.length || 0} icon={Users} tone="brand" />
        <StatCard label="Notificaciones sin leer" value={data.unreadNotifications} icon={Bell} tone="amber" />
        <StatCard label="Acceso" value="Autorizado" hint="Solo información de tus hijos" icon={HeartHandshake} tone="green" />
      </div>

      <section className="mt-6">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold text-slate-900 dark:text-white">Mis hijos</h2>
          <Link to="/app/padre/hijos" className="text-sm font-semibold text-brand-700 hover:underline dark:text-accent-300">
            Ver detalle
          </Link>
        </div>

        <div className="mt-4">
          {data.children?.length ? (
            <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
              {data.children.map((child) => (
                <Link key={child.studentId} to={`/app/padre/hijos?child=${child.studentId}`} className="card transition hover:border-accent-400">
                  <div className="flex items-center gap-4">
                    <span className="grid size-14 shrink-0 place-items-center rounded-full bg-brand-100 text-lg font-bold text-brand-900 dark:bg-slate-800 dark:text-accent-300">
                      {(child.fullName?.[0] || '') + (child.fullName?.split(' ')[1]?.[0] || '')}
                    </span>
                    <div className="min-w-0">
                      <p className="truncate font-semibold text-slate-900 dark:text-white">{child.fullName}</p>
                      <p className="text-sm text-slate-500 dark:text-slate-400">
                        {child.gradeLevel} · Grupo {child.groupName}
                      </p>
                      <Badge tone="sky" className="mt-1">{child.relationship}</Badge>
                    </div>
                  </div>
                  <p className="mt-3 text-xs text-slate-500 dark:text-slate-400">Documento {child.documentNumber}</p>
                </Link>
              ))}
            </div>
          ) : (
            <EmptyState
              title="Sin hijos vinculados"
              description="Si crees que falta un vínculo, comunícate con la administración del colegio."
              icon={Users}
            />
          )}
        </div>
      </section>
    </div>
  )
}

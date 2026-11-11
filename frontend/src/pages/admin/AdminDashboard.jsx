import { useQuery } from '@tanstack/react-query'
import { BookOpen, GraduationCap, Newspaper, UserCog, Users } from 'lucide-react'
import { Link } from 'react-router-dom'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { EmptyState, ErrorState, PageHeader, Spinner, StatCard, formatDate } from '../../components/ui'

export default function AdminDashboard() {
  usePageTitle('Panel administrativo')
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: async () => (await api.get('/admin/dashboard')).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader title="Panel administrativo" description="Resumen general de la institución." />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <StatCard label="Estudiantes" value={data.students} icon={GraduationCap} tone="brand" />
        <StatCard label="Docentes" value={data.teachers} icon={UserCog} tone="sky" />
        <StatCard label="Acudientes" value={data.parents} icon={Users} tone="green" />
        <StatCard label="Cursos" value={data.courses} icon={BookOpen} tone="amber" />
        <StatCard label="Matrículas activas" value={data.enrollments} icon={Users} tone="slate" />
        <StatCard label="Noticias publicadas" value={data.publishedNews} icon={Newspaper} tone="brand" />
      </div>

      <section className="card mt-6">
        <div className="flex items-center justify-between">
          <h2 className="card-title">Últimas noticias publicadas</h2>
          <Link to="/app/admin/noticias" className="text-sm font-semibold text-brand-700 hover:underline dark:text-accent-300">
            Gestionar noticias
          </Link>
        </div>
        <div className="mt-4">
          {data.latestNews?.length ? (
            <div className="divide-y divide-slate-100 dark:divide-slate-800">
              {data.latestNews.map((news) => (
                <div key={news.id} className="flex items-center justify-between gap-3 py-3">
                  <div className="min-w-0">
                    <p className="truncate font-medium text-slate-900 dark:text-white">{news.title}</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400">
                      {news.category || 'Institucional'} · {formatDate(news.publishedAt)}
                    </p>
                  </div>
                  <Link to="/app/admin/noticias" className="text-xs font-semibold text-brand-700 hover:underline dark:text-accent-300">
                    Editar
                  </Link>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState title="Sin noticias publicadas" description="Publica la primera noticia institucional." />
          )}
        </div>
      </section>
    </div>
  )
}

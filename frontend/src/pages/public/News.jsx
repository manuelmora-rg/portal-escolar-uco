import { useQuery } from '@tanstack/react-query'
import { Newspaper, Search } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { EmptyState, ErrorState, Spinner, formatDate } from '../../components/ui'

export default function News() {
  usePageTitle('Noticias')
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('all')

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['public-news'],
    queryFn: async () => (await api.get('/public/news')).data,
  })

  const categories = useMemo(
    () => Array.from(new Set(data.map((item) => item.category).filter(Boolean))),
    [data],
  )

  const filtered = useMemo(
    () =>
      data.filter((item) => {
        const matchesCategory = category === 'all' || item.category === category
        const term = search.trim().toLowerCase()
        const matchesSearch =
          !term ||
          item.title.toLowerCase().includes(term) ||
          (item.summary || '').toLowerCase().includes(term)
        return matchesCategory && matchesSearch
      }),
    [data, category, search],
  )

  return (
    <div>
      <section className="bg-brand-950 py-16 text-white">
        <div className="container-page">
          <p className="text-xs font-semibold tracking-wide text-accent-300 uppercase">Actualidad</p>
          <h1 className="mt-2 text-3xl font-extrabold sm:text-4xl">Noticias institucionales</h1>
          <p className="mt-4 max-w-2xl text-slate-300">
            Conoce las novedades, logros y actividades de nuestra comunidad educativa.
          </p>
        </div>
      </section>

      <section className="container-page py-12">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="relative max-w-sm flex-1">
            <Search className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-slate-400" size={16} />
            <input
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Buscar noticias…"
              className="input pl-9"
              aria-label="Buscar noticias"
            />
          </div>
          <div className="flex flex-wrap gap-1.5">
            <button
              type="button"
              onClick={() => setCategory('all')}
              className={`badge cursor-pointer ${category === 'all' ? 'bg-brand-900 text-white dark:bg-accent-500 dark:text-slate-950' : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300'}`}
            >
              Todas
            </button>
            {categories.map((item) => (
              <button
                key={item}
                type="button"
                onClick={() => setCategory(item)}
                className={`badge cursor-pointer ${category === item ? 'bg-brand-900 text-white dark:bg-accent-500 dark:text-slate-950' : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300'}`}
              >
                {item}
              </button>
            ))}
          </div>
        </div>

        <div className="mt-8">
          {isLoading ? (
            <Spinner />
          ) : isError ? (
            <ErrorState onRetry={refetch} />
          ) : filtered.length === 0 ? (
            <EmptyState title="No hay noticias" description="Aún no hay publicaciones que coincidan con la búsqueda." icon={Newspaper} />
          ) : (
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {filtered.map((item) => (
                <Link key={item.id} to={`/noticias/${item.slug}`} className="group card flex flex-col overflow-hidden !p-0">
                  <div className="aspect-[16/10] overflow-hidden bg-gradient-to-br from-brand-900 to-brand-700 dark:from-slate-800 dark:to-slate-700">
                    {item.imageUrl ? (
                      <img
                        src={item.imageUrl}
                        alt=""
                        className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
                        onError={(event) => {
                          event.currentTarget.style.display = 'none'
                        }}
                      />
                    ) : (
                      <div className="grid h-full place-items-center text-white/40">
                        <Newspaper size={36} />
                      </div>
                    )}
                  </div>
                  <div className="flex flex-1 flex-col p-5">
                    <p className="text-xs font-semibold text-accent-600 dark:text-accent-400">{item.category || 'Institucional'}</p>
                    <h2 className="mt-1 line-clamp-2 text-lg font-semibold text-slate-900 group-hover:text-brand-700 dark:text-white dark:group-hover:text-accent-300">
                      {item.title}
                    </h2>
                    <p className="mt-2 line-clamp-3 flex-1 text-sm text-slate-600 dark:text-slate-400">{item.summary}</p>
                    <p className="mt-4 text-xs text-slate-400">{formatDate(item.publishedAt)}</p>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  )
}

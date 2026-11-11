import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, CalendarDays, User } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { ErrorState, Spinner, formatDate } from '../../components/ui'

export default function NewsDetail() {
  const { slug } = useParams()
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['public-news', slug],
    queryFn: async () => (await api.get(`/public/news/${slug}`)).data,
    retry: false,
  })

  usePageTitle(data?.title || 'Noticia')

  if (isLoading) return <div className="container-page py-20"><Spinner /></div>
  if (isError || !data) {
    return (
      <div className="container-page py-20">
        <ErrorState message="No encontramos esta noticia o ya no está publicada." onRetry={refetch} />
        <div className="mt-6 text-center">
          <Link to="/noticias" className="btn-secondary">
            <ArrowLeft size={16} /> Volver a noticias
          </Link>
        </div>
      </div>
    )
  }

  const paragraphs = String(data.content || '').split(/\n{2,}/).filter(Boolean)

  return (
    <article>
      <div className="bg-brand-950 py-12 text-white">
        <div className="container-page">
          <Link to="/noticias" className="inline-flex items-center gap-2 text-sm text-accent-300 hover:text-accent-200">
            <ArrowLeft size={16} /> Noticias
          </Link>
          <p className="mt-6 text-xs font-semibold tracking-wide text-accent-300 uppercase">
            {data.category || 'Institucional'}
          </p>
          <h1 className="mt-2 max-w-4xl text-3xl font-extrabold sm:text-4xl">{data.title}</h1>
          <div className="mt-5 flex flex-wrap items-center gap-5 text-sm text-slate-300">
            <span className="inline-flex items-center gap-1.5">
              <CalendarDays size={15} /> {formatDate(data.publishedAt)}
            </span>
            {data.authorName ? (
              <span className="inline-flex items-center gap-1.5">
                <User size={15} /> {data.authorName}
              </span>
            ) : null}
          </div>
        </div>
      </div>

      <div className="container-page max-w-3xl py-12">
        {data.imageUrl ? (
          <div className="mb-8 overflow-hidden rounded-2xl bg-slate-100 dark:bg-slate-800">
            <img
              src={data.imageUrl}
              alt={data.title}
              className="max-h-[420px] w-full object-cover"
              onError={(event) => {
                event.currentTarget.parentElement.style.display = 'none'
              }}
            />
          </div>
        ) : null}

        {data.summary ? (
          <p className="mb-6 border-l-4 border-accent-500 pl-4 text-lg font-medium text-slate-700 dark:text-slate-200">
            {data.summary}
          </p>
        ) : null}

        <div className="space-y-4 text-base leading-relaxed text-slate-700 dark:text-slate-300">
          {paragraphs.length > 0 ? (
            paragraphs.map((paragraph, index) => <p key={index}>{paragraph}</p>)
          ) : (
            <p>{data.content}</p>
          )}
        </div>

        <div className="mt-10 border-t border-slate-200 pt-6 dark:border-slate-800">
          <Link to="/noticias" className="btn-secondary">
            <ArrowLeft size={16} /> Ver más noticias
          </Link>
        </div>
      </div>
    </article>
  )
}

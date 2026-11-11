import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Newspaper, Pencil, Plus, Send } from 'lucide-react'
import { useState } from 'react'
import api, { apiErrorMessage } from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, Field, Modal, PageHeader, Spinner, formatDate, statusTone } from '../../components/ui'

const EMPTY = { title: '', summary: '', content: '', imageUrl: '', category: 'Institucional', publish: false }

export default function AdminNews() {
  usePageTitle('Noticias')
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState('')

  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['admin-news'],
    queryFn: async () => (await api.get('/admin/news')).data,
  })

  const save = useMutation({
    mutationFn: async (payload) => {
      if (editing?.id) {
        return (await api.put(`/admin/news/${editing.id}`, payload)).data
      }
      return (await api.post('/admin/news', payload)).data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-news'] })
      queryClient.invalidateQueries({ queryKey: ['admin-dashboard'] })
      queryClient.invalidateQueries({ queryKey: ['public-news'] })
      close()
    },
    onError: (requestError) => setError(apiErrorMessage(requestError, 'No fue posible guardar la noticia.')),
  })

  const publish = useMutation({
    mutationFn: async (id) => (await api.patch(`/admin/news/${id}/publish`)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-news'] })
      queryClient.invalidateQueries({ queryKey: ['admin-dashboard'] })
      queryClient.invalidateQueries({ queryKey: ['public-news'] })
    },
  })

  function close() {
    setEditing(null)
    setForm(EMPTY)
    setError('')
  }

  function openCreate() {
    setForm(EMPTY)
    setError('')
    setEditing({ id: null })
  }

  function openEdit(news) {
    setForm({
      title: news.title,
      summary: news.summary || '',
      content: news.content || '',
      imageUrl: news.imageUrl || '',
      category: news.category || 'Institucional',
      publish: news.status === 'PUBLISHED',
    })
    setError('')
    setEditing(news)
  }

  function submit(event) {
    event.preventDefault()
    if (!form.title.trim()) return setError('Escribe el título.')
    if (!form.content.trim()) return setError('Escribe el contenido.')
    save.mutate({
      title: form.title.trim(),
      summary: form.summary.trim() || null,
      content: form.content.trim(),
      imageUrl: form.imageUrl.trim() || null,
      category: form.category.trim() || null,
      publish: form.publish,
    })
  }

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader
        title="Noticias"
        description="Crea, edita y publica noticias institucionales."
        actions={
          <button type="button" className="btn-primary" onClick={openCreate}>
            <Plus size={16} /> Nueva noticia
          </button>
        }
      />

      {data.length === 0 ? (
        <EmptyState title="Sin noticias" description="Crea la primera noticia institucional." icon={Newspaper} />
      ) : (
        <div className="space-y-3">
          {data.map((news) => (
            <div key={news.id} className="card">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="font-semibold text-slate-900 dark:text-white">{news.title}</h2>
                    <Badge tone={statusTone(news.status)}>{news.status}</Badge>
                    {news.category ? <Badge tone="slate">{news.category}</Badge> : null}
                  </div>
                  <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                    {news.publishedAt ? `Publicada ${formatDate(news.publishedAt)}` : 'Sin publicar'}
                    {news.authorName ? ` · ${news.authorName}` : ''}
                  </p>
                  {news.summary ? <p className="mt-2 line-clamp-2 text-sm text-slate-600 dark:text-slate-300">{news.summary}</p> : null}
                </div>
                <div className="flex shrink-0 gap-2">
                  {news.status !== 'PUBLISHED' ? (
                    <button type="button" className="btn-secondary" onClick={() => publish.mutate(news.id)} disabled={publish.isPending}>
                      <Send size={16} /> Publicar
                    </button>
                  ) : null}
                  <button type="button" className="btn-secondary" onClick={() => openEdit(news)}>
                    <Pencil size={16} /> Editar
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={Boolean(editing)}
        title={editing?.id ? 'Editar noticia' : 'Nueva noticia'}
        onClose={close}
        footer={
          <>
            <button type="button" className="btn-secondary" onClick={close}>Cancelar</button>
            <button type="submit" form="news-form" className="btn-primary" disabled={save.isPending}>
              {save.isPending ? 'Guardando…' : 'Guardar'}
            </button>
          </>
        }
      >
        <form id="news-form" className="space-y-4" onSubmit={submit}>
          <Field label="Título" htmlFor="news-title">
            <input id="news-title" className="input" value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} />
          </Field>
          <Field label="Resumen" htmlFor="news-summary">
            <textarea
              id="news-summary"
              rows={2}
              className="input resize-y"
              value={form.summary}
              onChange={(event) => setForm({ ...form, summary: event.target.value })}
            />
          </Field>
          <Field label="Contenido" htmlFor="news-content" hint="Separa los párrafos con una línea en blanco.">
            <textarea
              id="news-content"
              rows={8}
              className="input resize-y"
              value={form.content}
              onChange={(event) => setForm({ ...form, content: event.target.value })}
            />
          </Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Categoría" htmlFor="news-category">
              <input id="news-category" className="input" value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} />
            </Field>
            <Field label="Imagen (URL)" htmlFor="news-image">
              <input id="news-image" className="input" value={form.imageUrl} onChange={(event) => setForm({ ...form, imageUrl: event.target.value })} placeholder="https://…" />
            </Field>
          </div>
          <label className="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
            <input
              type="checkbox"
              className="size-4 rounded border-slate-300"
              checked={form.publish}
              onChange={(event) => setForm({ ...form, publish: event.target.checked })}
            />
            Publicar al guardar
          </label>
          {error ? <p className="text-sm font-medium text-rose-600 dark:text-rose-400">{error}</p> : null}
        </form>
      </Modal>
    </div>
  )
}

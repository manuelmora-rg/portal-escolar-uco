import { useQuery } from '@tanstack/react-query'
import { ExternalLink, FolderOpen } from 'lucide-react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Badge, EmptyState, ErrorState, PageHeader, Spinner, formatDate } from '../../components/ui'

export default function StudentMaterials() {
  usePageTitle('Materiales')
  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['student-materials'],
    queryFn: async () => (await api.get('/student/materials')).data,
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorState onRetry={refetch} />

  return (
    <div>
      <PageHeader title="Materiales de estudio" description="Recursos compartidos por tus docentes." />

      {data.length === 0 ? (
        <EmptyState title="Sin materiales" description="Tus docentes aún no han publicado recursos." icon={FolderOpen} />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {data.map((material) => (
            <div key={material.id} className="card flex flex-col">
              <div className="flex items-center justify-between gap-2">
                <Badge tone="brand">{material.type}</Badge>
                <span className="text-xs text-slate-400">{formatDate(material.publishedAt)}</span>
              </div>
              <h2 className="mt-3 font-semibold text-slate-900 dark:text-white">{material.title}</h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">{material.subjectName}</p>
              {material.description ? (
                <p className="mt-2 flex-1 text-sm text-slate-600 dark:text-slate-400">{material.description}</p>
              ) : null}
              <a href={material.url} target="_blank" rel="noreferrer" className="btn-secondary mt-4">
                <ExternalLink size={16} /> Abrir recurso
              </a>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

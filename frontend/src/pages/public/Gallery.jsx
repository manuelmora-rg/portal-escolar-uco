import { useQuery } from '@tanstack/react-query'
import { ImageIcon } from 'lucide-react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { EmptyState, ErrorState, Spinner } from '../../components/ui'

export default function Gallery() {
  usePageTitle('Galería')
  const { data = [], isLoading, isError, refetch } = useQuery({
    queryKey: ['public-gallery'],
    queryFn: async () => (await api.get('/public/gallery')).data,
  })

  return (
    <div>
      <section className="bg-brand-950 py-16 text-white">
        <div className="container-page">
          <p className="text-xs font-semibold tracking-wide text-accent-300 uppercase">Memorias</p>
          <h1 className="mt-2 text-3xl font-extrabold sm:text-4xl">Galería institucional</h1>
          <p className="mt-4 max-w-2xl text-slate-300">
            Momentos de aprendizaje, encuentros y celebraciones de nuestra comunidad.
          </p>
        </div>
      </section>

      <section className="container-page py-12">
        {isLoading ? (
          <Spinner />
        ) : isError ? (
          <ErrorState onRetry={refetch} />
        ) : data.length === 0 ? (
          <EmptyState
            title="Galería en construcción"
            description="Pronto publicaremos los álbumes de nuestras actividades institucionales."
            icon={ImageIcon}
          />
        ) : (
          <div className="space-y-12">
            {data.map((album) => (
              <div key={album.id}>
                <div className="flex flex-wrap items-end justify-between gap-2">
                  <div>
                    <h2 className="text-xl font-bold text-slate-900 dark:text-white">{album.title}</h2>
                    {album.description ? (
                      <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{album.description}</p>
                    ) : null}
                  </div>
                  <span className="badge bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                    {album.images?.length || 0} imágenes
                  </span>
                </div>

                <div className="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                  {(album.images || []).map((image) => (
                    <figure key={image.id} className="group overflow-hidden rounded-2xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
                      <div className="aspect-[4/3] overflow-hidden bg-gradient-to-br from-brand-900 to-brand-700 dark:from-slate-800 dark:to-slate-700">
                        {image.imageUrl ? (
                          <img
                            src={image.imageUrl}
                            alt={image.caption || album.title}
                            loading="lazy"
                            className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
                            onError={(event) => {
                              event.currentTarget.style.display = 'none'
                            }}
                          />
                        ) : (
                          <div className="grid h-full place-items-center text-white/40">
                            <ImageIcon size={32} />
                          </div>
                        )}
                      </div>
                      {image.caption ? (
                        <figcaption className="px-4 py-3 text-sm text-slate-600 dark:text-slate-300">
                          {image.caption}
                        </figcaption>
                      ) : null}
                    </figure>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}

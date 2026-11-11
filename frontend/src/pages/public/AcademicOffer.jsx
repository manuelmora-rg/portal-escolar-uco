import { useQuery } from '@tanstack/react-query'
import { BookOpen, Layers, Sparkles } from 'lucide-react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { EmptyState, Spinner } from '../../components/ui'

export default function AcademicOffer() {
  usePageTitle('Oferta académica')
  const { data, isLoading } = useQuery({
    queryKey: ['public-academic-offer'],
    queryFn: async () => (await api.get('/public/academic-offer')).data,
  })

  if (isLoading) return <div className="container-page py-20"><Spinner /></div>

  const levels = data?.levels || []
  const subjects = data?.subjects || []

  return (
    <div>
      <section className="bg-brand-950 py-16 text-white">
        <div className="container-page">
          <p className="text-xs font-semibold tracking-wide text-accent-300 uppercase">Propuesta académica</p>
          <h1 className="mt-2 text-3xl font-extrabold sm:text-4xl">Oferta académica</h1>
          <p className="mt-4 max-w-2xl text-slate-300">
            Acompañamos el tránsito por la educación básica secundaria y la media, con énfasis en lectura crítica,
            pensamiento científico y ciudadanía digital.
          </p>
        </div>
      </section>

      <section className="container-page py-16">
        <div className="flex items-center gap-3">
          <span className="grid size-10 place-items-center rounded-xl bg-brand-50 text-brand-900 dark:bg-brand-900/50 dark:text-accent-300">
            <Layers size={20} />
          </span>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Niveles</h2>
        </div>
        {levels.length === 0 ? (
          <div className="mt-6"><EmptyState title="Sin niveles configurados" /></div>
        ) : (
          <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {levels.map((level) => (
              <div key={level.name} className="card flex items-center gap-4">
                <span className="grid size-12 place-items-center rounded-xl bg-brand-900 text-lg font-bold text-white dark:bg-accent-500 dark:text-slate-950">
                  {level.order}
                </span>
                <div>
                  <p className="text-lg font-bold text-slate-900 dark:text-white">{level.name}</p>
                  <p className="text-xs text-slate-500 dark:text-slate-400">Educación básica y media</p>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="mt-14 flex items-center gap-3">
          <span className="grid size-10 place-items-center rounded-xl bg-brand-50 text-brand-900 dark:bg-brand-900/50 dark:text-accent-300">
            <BookOpen size={20} />
          </span>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Áreas y asignaturas</h2>
        </div>
        {subjects.length === 0 ? (
          <div className="mt-6"><EmptyState title="Sin asignaturas configuradas" /></div>
        ) : (
          <div className="mt-6 grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            {subjects.map((subject) => (
              <div key={subject.code} className="card">
                <div className="flex items-center justify-between gap-2">
                  <span className="badge bg-brand-100 text-brand-800 dark:bg-brand-900/60 dark:text-brand-100">{subject.code}</span>
                  <Sparkles size={16} className="text-accent-500" />
                </div>
                <p className="mt-3 font-semibold text-slate-900 dark:text-white">{subject.name}</p>
                <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">{subject.area}</p>
              </div>
            ))}
          </div>
        )}

        <div className="mt-14 grid gap-6 rounded-3xl bg-slate-100 p-8 dark:bg-slate-900 md:grid-cols-3">
          <div>
            <p className="text-3xl font-extrabold text-brand-900 dark:text-accent-300">4</p>
            <p className="mt-1 text-sm font-semibold text-slate-700 dark:text-slate-200">Periodos académicos</p>
            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">Con informes periódicos a las familias.</p>
          </div>
          <div>
            <p className="text-3xl font-extrabold text-brand-900 dark:text-accent-300">0.0 – 5.0</p>
            <p className="mt-1 text-sm font-semibold text-slate-700 dark:text-slate-200">Escala de valoración</p>
            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">Nota mínima de aprobación: 3.0.</p>
          </div>
          <div>
            <p className="text-3xl font-extrabold text-brand-900 dark:text-accent-300">8</p>
            <p className="mt-1 text-sm font-semibold text-slate-700 dark:text-slate-200">Asignaturas</p>
            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">Articuladas por áreas del conocimiento.</p>
          </div>
        </div>
      </section>
    </div>
  )
}

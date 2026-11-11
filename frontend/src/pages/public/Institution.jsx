import { useQuery } from '@tanstack/react-query'
import { Compass, Flag, HeartHandshake, History, MapPin, Target } from 'lucide-react'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Spinner } from '../../components/ui'

const VALUES = [
  { title: 'Respeto', text: 'Reconocer y valorar la dignidad de cada persona.' },
  { title: 'Responsabilidad', text: 'Asumir con compromiso las decisiones y sus consecuencias.' },
  { title: 'Solidaridad', text: 'Actuar por el bienestar de la comunidad.' },
  { title: 'Curiosidad', text: 'Preguntar, investigar y aprender de forma continua.' },
]

export default function Institution() {
  usePageTitle('La institución')
  const { data, isLoading } = useQuery({
    queryKey: ['public-institution'],
    queryFn: async () => (await api.get('/public/institution')).data,
  })

  if (isLoading) return <div className="container-page py-20"><Spinner /></div>

  return (
    <div>
      <section className="bg-brand-950 py-16 text-white">
        <div className="container-page">
          <p className="text-xs font-semibold tracking-wide text-accent-300 uppercase">Nuestra institución</p>
          <h1 className="mt-2 max-w-3xl text-3xl font-extrabold sm:text-4xl">
            {data?.name || 'Portal Escolar'}
          </h1>
          <p className="mt-4 max-w-2xl text-slate-300">
            {data?.slogan || 'Aprender para transformar'} · Una propuesta educativa centrada en las personas.
          </p>
        </div>
      </section>

      <section className="container-page grid gap-8 py-16 lg:grid-cols-[1fr_0.8fr]">
        <div className="space-y-6">
          <article className="card">
            <div className="flex items-center gap-3">
              <span className="grid size-10 place-items-center rounded-xl bg-brand-50 text-brand-900 dark:bg-brand-900/50 dark:text-accent-300">
                <History size={20} />
              </span>
              <h2 className="text-xl font-bold text-slate-900 dark:text-white">Nuestra historia</h2>
            </div>
            <p className="mt-4 leading-relaxed text-slate-600 dark:text-slate-300">
              {data?.history || 'La institución nace del compromiso de un grupo de familias y docentes por ofrecer una educación cercana, exigente y humana.'}
            </p>
          </article>

          <div className="grid gap-6 md:grid-cols-2">
            <article className="card">
              <div className="flex items-center gap-3">
                <span className="grid size-10 place-items-center rounded-xl bg-emerald-100 text-emerald-700 dark:bg-emerald-900/50 dark:text-emerald-200">
                  <Flag size={20} />
                </span>
                <h2 className="text-lg font-bold text-slate-900 dark:text-white">Misión</h2>
              </div>
              <p className="mt-4 text-sm leading-relaxed text-slate-600 dark:text-slate-300">
                {data?.mission || 'Formar personas íntegras, críticas y comprometidas con su comunidad.'}
              </p>
            </article>
            <article className="card">
              <div className="flex items-center gap-3">
                <span className="grid size-10 place-items-center rounded-xl bg-sky-100 text-sky-700 dark:bg-sky-900/50 dark:text-sky-200">
                  <Compass size={20} />
                </span>
                <h2 className="text-lg font-bold text-slate-900 dark:text-white">Visión</h2>
              </div>
              <p className="mt-4 text-sm leading-relaxed text-slate-600 dark:text-slate-300">
                {data?.vision || 'Ser una institución reconocida por su excelencia académica y formación en valores.'}
              </p>
            </article>
          </div>

          <article className="card">
            <div className="flex items-center gap-3">
              <span className="grid size-10 place-items-center rounded-xl bg-amber-100 text-amber-700 dark:bg-amber-900/50 dark:text-amber-200">
                <Target size={20} />
              </span>
              <h2 className="text-xl font-bold text-slate-900 dark:text-white">Nuestros valores</h2>
            </div>
            <div className="mt-5 grid gap-4 sm:grid-cols-2">
              {VALUES.map((value) => (
                <div key={value.title} className="rounded-xl border border-slate-200 p-4 dark:border-slate-800">
                  <p className="font-semibold text-slate-900 dark:text-white">{value.title}</p>
                  <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{value.text}</p>
                </div>
              ))}
            </div>
          </article>
        </div>

        <aside className="space-y-6">
          <div className="card">
            <h2 className="card-title">Información de contacto</h2>
            <ul className="mt-4 space-y-3 text-sm text-slate-600 dark:text-slate-300">
              <li className="flex items-start gap-2">
                <MapPin size={16} className="mt-0.5 shrink-0 text-accent-600 dark:text-accent-400" />
                {data?.address || 'Dirección institucional pendiente de definir'}
              </li>
              <li className="flex items-start gap-2">
                <HeartHandshake size={16} className="mt-0.5 shrink-0 text-accent-600 dark:text-accent-400" />
                {data?.phone || '+57 000 000 0000'}
              </li>
              <li className="flex items-start gap-2">
                <Compass size={16} className="mt-0.5 shrink-0 text-accent-600 dark:text-accent-400" />
                {data?.email || 'contacto@portalescolar.edu.co'}
              </li>
            </ul>
          </div>
          <div className="rounded-2xl bg-brand-900 p-6 text-white">
            <p className="text-sm font-semibold text-accent-300">Horario de atención</p>
            <p className="mt-2 text-sm text-slate-300">Lunes a viernes</p>
            <p className="text-2xl font-bold">7:00 a.m. – 4:00 p.m.</p>
            <p className="mt-3 text-xs text-slate-400">
              Para consultas académicas, comunícate con la coordinación correspondiente.
            </p>
          </div>
        </aside>
      </section>
    </div>
  )
}

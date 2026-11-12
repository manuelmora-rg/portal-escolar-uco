import { useQuery } from '@tanstack/react-query'
import {
  ArrowRight,
  BookOpen,
  CalendarDays,
  GraduationCap,
  HeartHandshake,
  MapPin,
  Newspaper,
  ShieldCheck,
  Sparkles,
  Users,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import api from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { formatDate } from '../../components/ui'

const FEATURES = [
  {
    icon: BookOpen,
    title: 'Formación académica',
    text: 'Un currículo articulado por áreas, con acompañamiento cercano y evaluación formativa.',
  },
  {
    icon: HeartHandshake,
    title: 'Comunidad y valores',
    text: 'Familias y docentes trabajan juntos por el bienestar y el desarrollo integral de cada estudiante.',
  },
  {
    icon: ShieldCheck,
    title: 'Información segura',
    text: 'Cada familia accede únicamente a la información de sus hijos, con controles de seguridad.',
  },
]

export default function Home() {
  usePageTitle('Inicio')
  const { data: institution } = useQuery({
    queryKey: ['public-institution'],
    queryFn: async () => (await api.get('/public/institution')).data,
  })
  const { data: news = [] } = useQuery({
    queryKey: ['public-news'],
    queryFn: async () => (await api.get('/public/news')).data,
  })
  const { data: events = [] } = useQuery({
    queryKey: ['public-events'],
    queryFn: async () => (await api.get('/public/events')).data,
  })

  const name = institution?.name || 'Portal Escolar'
  const upcoming = events.slice(0, 4)
  const latest = news.slice(0, 3)

  return (
    <div>
      <section className="relative overflow-hidden bg-brand-950 text-white">
        <div className="hero-glow absolute inset-0" />
        <div className="container-page relative grid gap-12 py-20 lg:grid-cols-[1.15fr_0.85fr] lg:py-28">
          <div>
            <span className="badge bg-white/10 text-accent-300 ring-1 ring-white/15">
              <Sparkles size={14} />
              {institution?.slogan || 'Aprender para transformar'}
            </span>
            <h1 className="mt-5 max-w-2xl text-4xl font-extrabold tracking-tight sm:text-5xl">
              {name}
              <span className="mt-3 block text-accent-400">Una comunidad que aprende y se cuida.</span>
            </h1>
            <p className="mt-5 max-w-xl text-base text-slate-300">
              {institution?.mission ||
                'Formamos personas íntegras, críticas y comprometidas con su comunidad, articulando el conocimiento académico, el desarrollo humano y el uso responsable de la tecnología.'}
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/oferta-academica" className="btn-primary !bg-accent-500 !text-slate-950 hover:!bg-accent-400">
                Conoce la oferta académica
                <ArrowRight size={16} />
              </Link>
              <Link to="/contacto" className="btn-secondary !border-white/20 !bg-white/10 !text-white hover:!bg-white/20">
                Agenda una visita
              </Link>
            </div>
            <dl className="mt-10 grid max-w-lg grid-cols-3 gap-4 border-t border-white/10 pt-6">
              <div>
                <dt className="text-xs tracking-wide text-slate-400 uppercase">Niveles</dt>
                <dd className="mt-1 text-2xl font-bold">6° a 11°</dd>
              </div>
              <div>
                <dt className="text-xs tracking-wide text-slate-400 uppercase">Jornada</dt>
                <dd className="mt-1 text-2xl font-bold">Única</dd>
              </div>
              <div>
                <dt className="text-xs tracking-wide text-slate-400 uppercase">Enfoque</dt>
                <dd className="mt-1 text-2xl font-bold">Integral</dd>
              </div>
            </dl>
          </div>

          <div className="relative hidden lg:block">
            <div className="absolute -top-6 right-6 size-64 rounded-full bg-accent-500/20 blur-3xl" />
            <div className="relative grid gap-4">
              <div className="rounded-2xl border border-white/10 bg-white/5 p-5 backdrop-blur">
                <div className="flex items-center gap-3">
                  <span className="grid size-11 place-items-center rounded-xl bg-accent-500 text-slate-950">
                    <GraduationCap size={22} />
                  </span>
                  <div>
                    <p className="text-sm font-semibold">Acompañamiento académico</p>
                    <p className="text-xs text-slate-400">Seguimiento de notas, tareas y asistencia.</p>
                  </div>
                </div>
              </div>
              <div className="ml-8 rounded-2xl border border-white/10 bg-white/5 p-5 backdrop-blur">
                <div className="flex items-center gap-3">
                  <span className="grid size-11 place-items-center rounded-xl bg-white/10 text-accent-300">
                    <Users size={22} />
                  </span>
                  <div>
                    <p className="text-sm font-semibold">Familias conectadas</p>
                    <p className="text-xs text-slate-400">Consulta autorizada por hijo y por acudiente.</p>
                  </div>
                </div>
              </div>
              <div className="ml-16 rounded-2xl border border-white/10 bg-white/5 p-5 backdrop-blur">
                <div className="flex items-center gap-3">
                  <span className="grid size-11 place-items-center rounded-xl bg-white/10 text-accent-300">
                    <ShieldCheck size={22} />
                  </span>
                  <div>
                    <p className="text-sm font-semibold">Acceso seguro</p>
                    <p className="text-xs text-slate-400">Roles, validación y sesiones protegidas.</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="container-page py-16">
        <div className="grid gap-5 md:grid-cols-3">
          {FEATURES.map(({ icon: Icon, title, text }) => (
            <div key={title} className="card">
              <span className="grid size-11 place-items-center rounded-xl bg-brand-50 text-brand-900 dark:bg-brand-900/50 dark:text-accent-300">
                <Icon size={22} />
              </span>
              <h2 className="mt-4 text-lg font-semibold text-slate-900 dark:text-white">{title}</h2>
              <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">{text}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="border-y border-slate-200 bg-white py-16 dark:border-slate-800 dark:bg-slate-900/40">
        <div className="container-page grid gap-10 lg:grid-cols-[1.2fr_0.8fr]">
          <div>
            <div className="flex items-end justify-between gap-4">
              <div>
                <p className="text-xs font-semibold tracking-wide text-accent-600 uppercase dark:text-accent-400">Actualidad</p>
                <h2 className="mt-1 text-2xl font-bold text-slate-900 dark:text-white">Noticias recientes</h2>
              </div>
              <Link to="/noticias" className="text-sm font-semibold text-brand-700 hover:underline dark:text-accent-300">
                Ver todas
              </Link>
            </div>
            <div className="mt-6 grid gap-5 sm:grid-cols-3">
              {latest.length === 0 ? (
                <p className="text-sm text-slate-500 dark:text-slate-400">Aún no hay noticias publicadas.</p>
              ) : (
                latest.map((item) => (
                  <Link key={item.id} to={`/noticias/${item.slug}`} className="group card overflow-hidden !p-0">
                    <div className="aspect-[16/10] overflow-hidden bg-slate-100 dark:bg-slate-800">
                      {item.imageUrl ? (
                        <img
                          src={item.imageUrl}
                          alt=""
                          className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
                          onError={(event) => {
                            event.currentTarget.style.display = 'none'
                          }}
                        />
                      ) : null}
                    </div>
                    <div className="p-4">
                      <p className="text-xs font-semibold text-accent-600 dark:text-accent-400">{item.category || 'Institucional'}</p>
                      <h3 className="mt-1 line-clamp-2 font-semibold text-slate-900 group-hover:text-brand-700 dark:text-white dark:group-hover:text-accent-300">
                        {item.title}
                      </h3>
                      <p className="mt-2 line-clamp-2 text-xs text-slate-500 dark:text-slate-400">{item.summary}</p>
                      <p className="mt-3 text-[11px] text-slate-400">{formatDate(item.publishedAt)}</p>
                    </div>
                  </Link>
                ))
              )}
            </div>
          </div>

          <div>
            <div className="flex items-end justify-between gap-4">
              <div>
                <p className="text-xs font-semibold tracking-wide text-accent-600 uppercase dark:text-accent-400">Agenda</p>
                <h2 className="mt-1 text-2xl font-bold text-slate-900 dark:text-white">Próximos eventos</h2>
              </div>
              <Link to="/calendario" className="text-sm font-semibold text-brand-700 hover:underline dark:text-accent-300">
                Calendario
              </Link>
            </div>
            <div className="mt-6 space-y-3">
              {upcoming.length === 0 ? (
                <p className="text-sm text-slate-500 dark:text-slate-400">No hay eventos programados.</p>
              ) : (
                upcoming.map((event) => (
                  <div key={event.id} className="card flex gap-4 !p-4">
                    <div className="grid size-12 shrink-0 place-items-center rounded-xl bg-brand-900 text-white dark:bg-accent-500 dark:text-slate-950">
                      <CalendarDays size={20} />
                    </div>
                    <div className="min-w-0">
                      <p className="truncate font-semibold text-slate-900 dark:text-white">{event.title}</p>
                      <p className="mt-0.5 text-xs text-slate-500 dark:text-slate-400">{formatDate(event.startsAt)}</p>
                      {event.location ? (
                        <p className="mt-1 flex items-center gap-1 text-xs text-slate-500 dark:text-slate-400">
                          <MapPin size={12} /> {event.location}
                        </p>
                      ) : null}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </section>

      <section className="container-page py-16">
        <div className="relative overflow-hidden rounded-3xl bg-brand-900 px-6 py-12 text-white sm:px-12">
          <div className="absolute -top-16 -right-10 size-64 rounded-full bg-accent-500/20 blur-3xl" />
          <div className="relative flex flex-col items-start justify-between gap-6 md:flex-row md:items-center">
            <div>
              <p className="text-xs font-semibold tracking-wide text-accent-300 uppercase">Comunidad educativa</p>
              <h2 className="mt-2 text-2xl font-bold sm:text-3xl">¿Ya eres parte de la institución?</h2>
              <p className="mt-2 max-w-xl text-sm text-slate-300">
                Ingresa al portal para consultar calificaciones, tareas, asistencia, reuniones y comunicaciones.
              </p>
            </div>
            <div className="flex flex-wrap gap-3">
              <Link to="/login" className="btn-primary !bg-accent-500 !text-slate-950 hover:!bg-accent-400">
                <Newspaper size={16} />
                Ir al portal
              </Link>
              <Link to="/contacto" className="btn-secondary !border-white/20 !bg-white/10 !text-white hover:!bg-white/20">
                Solicitar información
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}

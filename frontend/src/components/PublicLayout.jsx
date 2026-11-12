import { useQuery } from '@tanstack/react-query'
import { GraduationCap, LogIn, Menu, X } from 'lucide-react'
import { useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import api from '../lib/api'
import { useAuth, ROLE_HOME } from '../lib/auth'
import ThemeToggle from './ThemeToggle'

const NAV = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/institucion', label: 'Institución' },
  { to: '/oferta-academica', label: 'Oferta académica' },
  { to: '/noticias', label: 'Noticias' },
  { to: '/galeria', label: 'Galería' },
  { to: '/calendario', label: 'Calendario' },
  { to: '/contacto', label: 'Contacto' },
]

export default function PublicLayout() {
  const [open, setOpen] = useState(false)
  const { user } = useAuth()
  const { data: institution } = useQuery({
    queryKey: ['public-institution'],
    queryFn: async () => (await api.get('/public/institution')).data,
    staleTime: 10 * 60 * 1000,
  })

  const name = institution?.name || 'Portal Escolar'
  const year = new Date().getFullYear()

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-40 border-b border-slate-200/80 bg-white/90 backdrop-blur dark:border-slate-800 dark:bg-slate-950/90">
        <div className="container-page flex h-16 items-center justify-between gap-4">
          <Link to="/" className="flex items-center gap-2.5" onClick={() => setOpen(false)}>
            <span className="grid size-10 place-items-center rounded-xl bg-brand-900 text-white dark:bg-accent-500 dark:text-slate-950">
              <GraduationCap size={22} />
            </span>
            <span className="leading-tight">
              <span className="block text-sm font-bold text-slate-900 dark:text-white">{name}</span>
              <span className="block text-[11px] text-slate-500 dark:text-slate-400">Portal institucional</span>
            </span>
          </Link>

          <nav className="hidden items-center gap-1 lg:flex">
            {NAV.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  `rounded-lg px-3 py-2 text-sm font-medium transition ${
                    isActive
                      ? 'bg-brand-100 text-brand-900 dark:bg-brand-900/70 dark:text-brand-50'
                      : 'text-slate-600 hover:bg-brand-50 hover:text-brand-900 dark:text-slate-300 dark:hover:bg-brand-900/40 dark:hover:text-white'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="flex items-center gap-1.5">
            <ThemeToggle />
            {user ? (
              <Link to={ROLE_HOME[user.role] || '/'} className="btn-primary hidden sm:inline-flex">
                Ir al panel
              </Link>
            ) : (
              <Link to="/login" className="btn-primary hidden sm:inline-flex">
                <LogIn size={16} />
                Ingresar
              </Link>
            )}
            <button
              type="button"
              className="btn-ghost !px-2.5 lg:hidden"
              onClick={() => setOpen((value) => !value)}
              aria-label="Abrir menú"
            >
              {open ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>

        {open ? (
          <div className="border-t border-slate-200 bg-white px-4 py-3 lg:hidden dark:border-slate-800 dark:bg-slate-950">
            <nav className="grid gap-1">
              {NAV.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.end}
                  onClick={() => setOpen(false)}
                  className={({ isActive }) =>
                    `rounded-lg px-3 py-2 text-sm font-medium ${
                      isActive
                        ? 'bg-brand-100 text-brand-900 dark:bg-brand-900/70 dark:text-brand-50'
                        : 'text-slate-600 dark:text-slate-300'
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
              <Link
                to={user ? ROLE_HOME[user.role] : '/login'}
                className="btn-primary mt-2"
                onClick={() => setOpen(false)}
              >
                {user ? 'Ir al panel' : 'Ingresar'}
              </Link>
            </nav>
          </div>
        ) : null}
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <footer className="border-t border-slate-200 bg-brand-950 text-slate-300 dark:border-slate-800">
        <div className="container-page grid gap-8 py-10 sm:grid-cols-2 lg:grid-cols-4">
          <div>
            <div className="flex items-center gap-2 text-white">
              <GraduationCap size={20} />
              <span className="font-semibold">{name}</span>
            </div>
            <p className="mt-3 text-sm text-slate-400">
              {institution?.slogan || 'Educación con sentido, comunidad y futuro.'}
            </p>
          </div>
          <div>
            <p className="text-sm font-semibold text-white">Contacto</p>
            <ul className="mt-3 space-y-1.5 text-sm text-slate-400">
              <li>{institution?.address || 'Dirección institucional'}</li>
              <li>{institution?.phone || '+57 000 000 0000'}</li>
              <li>{institution?.email || 'contacto@portalescolar.edu.co'}</li>
            </ul>
          </div>
          <div>
            <p className="text-sm font-semibold text-white">Enlaces</p>
            <ul className="mt-3 space-y-1.5 text-sm">
              <li><Link className="hover:text-white" to="/institucion">La institución</Link></li>
              <li><Link className="hover:text-white" to="/oferta-academica">Oferta académica</Link></li>
              <li><Link className="hover:text-white" to="/calendario">Calendario</Link></li>
              <li><Link className="hover:text-white" to="/contacto">Contacto</Link></li>
            </ul>
          </div>
          <div>
            <p className="text-sm font-semibold text-white">Acceso</p>
            <p className="mt-3 text-sm text-slate-400">
              Estudiantes, docentes, acudientes y administradores acceden con las credenciales entregadas por la institución.
            </p>
            <Link to="/login" className="btn-secondary mt-3 !border-white/20 !bg-white/10 !text-white hover:!bg-white/20">
              Iniciar sesión
            </Link>
          </div>
        </div>
        <div className="border-t border-white/10 py-4 text-center text-xs text-slate-500">
          © {year} {name}. Todos los derechos reservados.
        </div>
      </footer>
    </div>
  )
}

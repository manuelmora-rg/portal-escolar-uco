import { GraduationCap, Lock, LogIn, Mail } from 'lucide-react'
import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { apiErrorMessage } from '../../lib/api'
import { ROLE_HOME, useAuth } from '../../lib/auth'
import { usePageTitle } from '../../lib/usePageTitle'
import { Field } from '../../components/ui'
import ThemeToggle from '../../components/ThemeToggle'

const DEMO = [
  { role: 'Administrador', email: 'admin@portalescolar.edu.co' },
  { role: 'Docente', email: 'laura.gomez@portalescolar.edu.co' },
  { role: 'Estudiante', email: 'sofia.mora@portalescolar.edu.co' },
  { role: 'Acudiente', email: 'maria.giraldo@portalescolar.edu.co' },
]

export default function Login() {
  usePageTitle('Iniciar sesión')
  const { user, loading, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (!loading && user) {
    return <Navigate to={ROLE_HOME[user.role] || '/'} replace />
  }

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    if (!email.trim() || !password) {
      setError('Ingresa tu correo y contraseña.')
      return
    }
    setSubmitting(true)
    try {
      const logged = await login(email.trim(), password)
      const target = location.state?.from || ROLE_HOME[logged.role] || '/'
      navigate(target, { replace: true })
    } catch (requestError) {
      setError(apiErrorMessage(requestError, 'Credenciales incorrectas o cuenta no disponible.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="relative hidden overflow-hidden bg-brand-950 p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="hero-glow absolute inset-0" />
        <Link to="/" className="relative flex items-center gap-3">
          <span className="grid size-11 place-items-center rounded-xl bg-accent-500 text-slate-950">
            <GraduationCap size={24} />
          </span>
          <span className="text-lg font-bold">Portal Escolar</span>
        </Link>
        <div className="relative">
          <p className="text-xs font-semibold tracking-wide text-accent-300 uppercase">Comunidad educativa</p>
          <h1 className="mt-3 max-w-md text-4xl font-extrabold">
            Toda la información académica en un solo lugar.
          </h1>
          <p className="mt-4 max-w-md text-slate-300">
            Calificaciones, tareas, asistencia, reuniones y comunicaciones con acceso seguro según tu rol.
          </p>
          <ul className="mt-8 space-y-3 text-sm text-slate-300">
            <li className="flex items-center gap-2"><span className="size-1.5 rounded-full bg-accent-400" /> Estudiantes: seguimiento de su proceso.</li>
            <li className="flex items-center gap-2"><span className="size-1.5 rounded-full bg-accent-400" /> Docentes: notas, asistencia y materiales.</li>
            <li className="flex items-center gap-2"><span className="size-1.5 rounded-full bg-accent-400" /> Acudientes: información autorizada por hijo.</li>
            <li className="flex items-center gap-2"><span className="size-1.5 rounded-full bg-accent-400" /> Administradores: gestión institucional.</li>
          </ul>
        </div>
        <p className="relative text-xs text-slate-500">© {new Date().getFullYear()} Portal Escolar</p>
      </div>

      <div className="flex flex-col bg-slate-50 dark:bg-slate-950">
        <div className="flex items-center justify-between px-6 py-5 lg:justify-end">
          <Link to="/" className="flex items-center gap-2 lg:hidden">
            <span className="grid size-9 place-items-center rounded-xl bg-brand-900 text-white dark:bg-accent-500 dark:text-slate-950">
              <GraduationCap size={20} />
            </span>
            <span className="font-bold text-slate-900 dark:text-white">Portal Escolar</span>
          </Link>
          <ThemeToggle />
        </div>

        <div className="flex flex-1 items-center justify-center px-6 pb-12">
          <div className="w-full max-w-md">
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Iniciar sesión</h2>
            <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
              Usa las credenciales entregadas por la institución.
            </p>

            <form className="mt-8 space-y-4" onSubmit={onSubmit} noValidate>
              <Field label="Correo electrónico" htmlFor="email">
                <div className="relative">
                  <Mail className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-slate-400" size={16} />
                  <input
                    id="email"
                    type="email"
                    className="input pl-9"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    autoComplete="email"
                    placeholder="nombre@portalescolar.edu.co"
                  />
                </div>
              </Field>

              <Field label="Contraseña" htmlFor="password">
                <div className="relative">
                  <Lock className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-slate-400" size={16} />
                  <input
                    id="password"
                    type="password"
                    className="input pl-9"
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    autoComplete="current-password"
                    placeholder="••••••••"
                  />
                </div>
              </Field>

              {error ? (
                <p className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-200">
                  {error}
                </p>
              ) : null}

              <button type="submit" className="btn-primary w-full" disabled={submitting}>
                <LogIn size={16} />
                {submitting ? 'Ingresando…' : 'Ingresar'}
              </button>
            </form>

            <div className="mt-8 rounded-xl border border-dashed border-slate-300 p-4 dark:border-slate-700">
              <p className="text-xs font-semibold tracking-wide text-slate-500 uppercase dark:text-slate-400">
                Accesos de demostración
              </p>
              <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                Contraseña: <code className="rounded bg-slate-100 px-1 py-0.5 dark:bg-slate-800">Portal2026*</code>
              </p>
              <div className="mt-3 grid gap-1.5">
                {DEMO.map((item) => (
                  <button
                    key={item.email}
                    type="button"
                    onClick={() => {
                      setEmail(item.email)
                      setPassword('Portal2026*')
                      setError('')
                    }}
                    className="flex items-center justify-between gap-2 rounded-lg px-2 py-1.5 text-left text-xs text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
                  >
                    <span className="font-semibold">{item.role}</span>
                    <span className="truncate text-slate-400">{item.email}</span>
                  </button>
                ))}
              </div>
            </div>

            <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
              <Link to="/" className="font-semibold text-brand-700 hover:underline dark:text-accent-300">
                Volver al sitio público
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

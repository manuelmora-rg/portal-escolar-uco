import {
  Bell,
  BookOpen,
  CalendarCheck,
  CalendarClock,
  CalendarDays,
  ClipboardList,
  FileText,
  FolderOpen,
  GraduationCap,
  HeartHandshake,
  LayoutDashboard,
  LogOut,
  Mail,
  Menu,
  MessageSquare,
  Newspaper,
  Settings,
  UserCog,
  Users,
  X,
} from 'lucide-react'
import { useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { ROLE_LABEL, useAuth } from '../lib/auth'
import ThemeToggle from './ThemeToggle'

const NAV_BY_ROLE = {
  ADMIN: [
    { to: '/app/admin', label: 'Panel', icon: LayoutDashboard, end: true },
    { to: '/app/admin/estudiantes', label: 'Estudiantes', icon: Users },
    { to: '/app/admin/docentes', label: 'Docentes', icon: UserCog },
    { to: '/app/admin/acudientes', label: 'Acudientes', icon: HeartHandshake },
    { to: '/app/admin/cursos', label: 'Cursos', icon: BookOpen },
    { to: '/app/admin/noticias', label: 'Noticias', icon: Newspaper },
    { to: '/app/admin/mensajes', label: 'Mensajes', icon: Mail },
    { to: '/app/admin/configuracion', label: 'Configuración', icon: Settings },
  ],
  TEACHER: [
    { to: '/app/docente', label: 'Panel', icon: LayoutDashboard, end: true },
    { to: '/app/docente/cursos', label: 'Cursos', icon: BookOpen },
    { to: '/app/docente/cuaderno', label: 'Cuaderno de notas', icon: ClipboardList },
    { to: '/app/docente/asistencia', label: 'Asistencia', icon: CalendarCheck },
    { to: '/app/docente/tareas', label: 'Tareas', icon: FileText },
    { to: '/app/docente/observaciones', label: 'Observaciones', icon: MessageSquare },
    { to: '/app/docente/materiales', label: 'Materiales', icon: FolderOpen },
    { to: '/app/docente/notificaciones', label: 'Notificaciones', icon: Bell },
  ],
  STUDENT: [
    { to: '/app/estudiante', label: 'Panel', icon: LayoutDashboard, end: true },
    { to: '/app/estudiante/calificaciones', label: 'Calificaciones', icon: GraduationCap },
    { to: '/app/estudiante/horario', label: 'Horario', icon: CalendarDays },
    { to: '/app/estudiante/tareas', label: 'Tareas', icon: FileText },
    { to: '/app/estudiante/materiales', label: 'Materiales', icon: FolderOpen },
    { to: '/app/estudiante/asistencia', label: 'Asistencia', icon: CalendarCheck },
    { to: '/app/estudiante/observaciones', label: 'Observaciones', icon: MessageSquare },
    { to: '/app/estudiante/notificaciones', label: 'Notificaciones', icon: Bell },
  ],
  PARENT: [
    { to: '/app/padre', label: 'Panel', icon: LayoutDashboard, end: true },
    { to: '/app/padre/hijos', label: 'Hijos', icon: Users },
    { to: '/app/padre/reuniones', label: 'Reuniones', icon: CalendarClock },
    { to: '/app/padre/notificaciones', label: 'Notificaciones', icon: Bell },
  ],
}

export default function AppLayout() {
  const { user, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const items = NAV_BY_ROLE[user?.role] || []

  const sidebar = (
    <div className="flex h-full flex-col bg-brand-900 dark:bg-slate-900">
      <Link to="/" className="flex items-center gap-2.5 px-5 py-5">
        <span className="grid size-9 place-items-center rounded-xl bg-white/10 text-white ring-1 ring-white/15">
          <GraduationCap size={20} />
        </span>
        <span className="leading-tight">
          <span className="block text-sm font-bold text-white">Portal Escolar</span>
          <span className="block text-[11px] text-brand-100/80">{ROLE_LABEL[user?.role]}</span>
        </span>
      </Link>
      <nav className="flex-1 space-y-1 overflow-y-auto px-3 pb-4">
        {items.map(({ to, label, icon: Icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            onClick={() => setOpen(false)}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition ${
                isActive
                  ? 'bg-white text-brand-900 shadow-sm'
                  : 'text-brand-50/80 hover:bg-white/10 hover:text-white'
              }`
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="border-t border-white/10 px-3 py-3">
        <button
          type="button"
          onClick={logout}
          className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-brand-100/80 transition hover:bg-rose-500/20 hover:text-white"
        >
          <LogOut size={18} />
          Cerrar sesión
        </button>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-brand-50 dark:bg-slate-950">
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 border-r border-brand-800 bg-brand-900 lg:block dark:border-slate-800 dark:bg-slate-900">
        {sidebar}
      </aside>

      {open ? (
        <div className="fixed inset-0 z-40 lg:hidden">
          <button
            type="button"
            className="absolute inset-0 bg-slate-950/50 backdrop-blur-sm"
            onClick={() => setOpen(false)}
            aria-label="Cerrar menú"
          />
          <aside className="relative h-full w-72 border-r border-brand-800 bg-brand-900 dark:border-slate-800 dark:bg-slate-900">
            {sidebar}
          </aside>
        </div>
      ) : null}

      <div className="lg:pl-64">
        <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur dark:border-slate-800 dark:bg-slate-900/90">
          <div className="flex h-16 items-center justify-between gap-3 px-4 sm:px-6">
            <button
              type="button"
              className="btn-ghost !px-2.5 lg:hidden"
              onClick={() => setOpen(true)}
              aria-label="Abrir menú"
            >
              <Menu size={20} />
            </button>
            <div className="hidden text-sm text-slate-500 sm:block dark:text-slate-400">
              {new Intl.DateTimeFormat('es-CO', { dateStyle: 'full' }).format(new Date())}
            </div>
            <div className="flex items-center gap-2">
              <ThemeToggle />
              <div className="hidden text-right sm:block">
                <p className="text-sm font-semibold text-slate-900 dark:text-white">{user?.fullName}</p>
                <p className="text-xs text-slate-500 dark:text-slate-400">{ROLE_LABEL[user?.role]}</p>
              </div>
              <span className="grid size-9 place-items-center rounded-full bg-brand-100 text-sm font-bold text-brand-900 dark:bg-brand-900/60 dark:text-brand-100">
                {(user?.firstName?.[0] || '') + (user?.lastName?.[0] || '')}
              </span>
            </div>
          </div>
        </header>

        <main className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

import { GraduationCap, Home } from 'lucide-react'
import { Link } from 'react-router-dom'
import { usePageTitle } from '../lib/usePageTitle'

export default function NotFound() {
  usePageTitle('Página no encontrada')
  return (
    <div className="grid min-h-screen place-items-center bg-slate-50 px-6 dark:bg-slate-950">
      <div className="text-center">
        <span className="mx-auto grid size-14 place-items-center rounded-2xl bg-brand-900 text-white dark:bg-accent-500 dark:text-slate-950">
          <GraduationCap size={28} />
        </span>
        <p className="mt-6 text-sm font-semibold tracking-wide text-accent-600 uppercase dark:text-accent-400">Error 404</p>
        <h1 className="mt-2 text-3xl font-extrabold text-slate-900 dark:text-white">Página no encontrada</h1>
        <p className="mt-2 text-slate-500 dark:text-slate-400">
          La dirección que buscas no existe o fue movida.
        </p>
        <Link to="/" className="btn-primary mt-6">
          <Home size={16} /> Volver al inicio
        </Link>
      </div>
    </div>
  )
}

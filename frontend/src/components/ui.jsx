import { AlertCircle, Inbox, Loader2, X } from 'lucide-react'

const TONES = {
  slate: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200',
  brand: 'bg-brand-50 text-brand-800 dark:bg-brand-900/40 dark:text-brand-100',
  green: 'bg-brand-100 text-brand-700 dark:bg-brand-900/60 dark:text-brand-100',
  amber: 'bg-amber-100 text-amber-800 dark:bg-amber-900/50 dark:text-amber-200',
  red: 'bg-rose-100 text-rose-700 dark:bg-rose-900/50 dark:text-rose-200',
  sky: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300',
}

export function Badge({ children, tone = 'slate', className = '' }) {
  return <span className={`badge ${TONES[tone] || TONES.slate} ${className}`}>{children}</span>
}

export function Card({ children, className = '' }) {
  return <div className={`card ${className}`}>{children}</div>
}

export function PageHeader({ title, description, actions }) {
  return (
    <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-brand-900 dark:text-brand-50">{title}</h1>
        {description ? <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{description}</p> : null}
      </div>
      {actions ? <div className="flex flex-wrap items-center gap-2">{actions}</div> : null}
    </div>
  )
}

export function StatCard({ label, value, hint, icon: Icon, tone = 'brand' }) {
  return (
    <div className="card flex items-start gap-4">
      {Icon ? (
        <span className={`grid size-11 shrink-0 place-items-center rounded-xl ${TONES[tone]}`}>
          <Icon size={20} />
        </span>
      ) : null}
      <div className="min-w-0">
        <p className="text-xs font-semibold tracking-wide text-slate-500 uppercase dark:text-slate-400">{label}</p>
        <p className="mt-1 text-2xl font-bold text-slate-900 dark:text-white">{value}</p>
        {hint ? <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">{hint}</p> : null}
      </div>
    </div>
  )
}

export function Spinner({ label = 'Cargando información…' }) {
  return (
    <div className="flex items-center justify-center gap-3 py-12 text-sm text-slate-500 dark:text-slate-400">
      <Loader2 className="animate-spin" size={20} />
      {label}
    </div>
  )
}

export function EmptyState({ title = 'Sin información', description, icon: Icon = Inbox }) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 rounded-2xl border border-dashed border-slate-300 px-6 py-12 text-center dark:border-slate-700">
      <Icon className="text-slate-400" size={28} />
      <p className="font-semibold text-slate-700 dark:text-slate-200">{title}</p>
      {description ? <p className="max-w-md text-sm text-slate-500 dark:text-slate-400">{description}</p> : null}
    </div>
  )
}

export function ErrorState({ message = 'No fue posible cargar la información.', onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-2xl border border-rose-200 bg-rose-50 px-6 py-10 text-center dark:border-rose-900/60 dark:bg-rose-950/40">
      <AlertCircle className="text-rose-500" size={28} />
      <p className="text-sm text-rose-700 dark:text-rose-200">{message}</p>
      {onRetry ? (
        <button type="button" className="btn-secondary" onClick={onRetry}>
          Reintentar
        </button>
      ) : null}
    </div>
  )
}

export function Field({ label, error, hint, children, htmlFor }) {
  return (
    <div>
      {label ? (
        <label className="label" htmlFor={htmlFor}>
          {label}
        </label>
      ) : null}
      {children}
      {hint && !error ? <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">{hint}</p> : null}
      {error ? <p className="mt-1 text-xs font-medium text-rose-600 dark:text-rose-400">{error}</p> : null}
    </div>
  )
}

export function Modal({ open, title, onClose, children, footer }) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-slate-950/60 p-4 backdrop-blur-sm sm:items-center">
      <div className="w-full max-w-lg rounded-2xl border border-slate-200 bg-white shadow-2xl dark:border-slate-800 dark:bg-slate-900">
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4 dark:border-slate-800">
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white">{title}</h2>
          <button type="button" className="btn-ghost !px-2 !py-1" onClick={onClose} aria-label="Cerrar">
            <X size={18} />
          </button>
        </div>
        <div className="max-h-[70vh] overflow-y-auto px-5 py-4">{children}</div>
        {footer ? <div className="flex justify-end gap-2 border-t border-slate-200 px-5 py-4 dark:border-slate-800">{footer}</div> : null}
      </div>
    </div>
  )
}

export function Tabs({ tabs, active, onChange }) {
  return (
    <div className="flex flex-wrap gap-1 rounded-xl bg-slate-100 p-1 dark:bg-slate-800/70">
      {tabs.map((tab) => (
        <button
          key={tab.value}
          type="button"
          onClick={() => onChange(tab.value)}
          className={`rounded-lg px-3 py-1.5 text-sm font-medium transition ${
            active === tab.value
              ? 'bg-white text-brand-900 shadow-sm dark:bg-slate-900 dark:text-brand-50'
              : 'text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-100'
          }`}
        >
          {tab.label}
        </button>
      ))}
    </div>
  )
}

export function statusTone(status) {
  const normalized = String(status || '').toUpperCase()
  if (['PRESENT', 'CONFIRMED', 'GRADED', 'PUBLISHED', 'ENROLLED', 'ACTIVE'].includes(normalized)) return 'green'
  if (['ABSENT', 'DECLINED', 'CANCELLED', 'WITHDRAWN', 'INACTIVE'].includes(normalized)) return 'red'
  if (['LATE', 'PENDING', 'SUBMITTED', 'DRAFT', 'SCHEDULED'].includes(normalized)) return 'amber'
  if (['EXCUSED', 'RETURNED', 'CLOSED', 'ARCHIVED', 'GRADUATED'].includes(normalized)) return 'sky'
  return 'slate'
}

export function formatDate(value, options = {}) {
  if (!value) return '—'
  const dateOnly = typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value)
  const date = dateOnly
    ? new Date(Number(value.slice(0, 4)), Number(value.slice(5, 7)) - 1, Number(value.slice(8, 10)))
    : new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    ...options,
  }).format(date)
}

export function formatDateTime(value) {
  return formatDate(value, { hour: '2-digit', minute: '2-digit' })
}

export function formatTime(value) {
  if (!value) return '—'
  const [hour, minute] = String(value).split(':')
  const date = new Date()
  date.setHours(Number(hour), Number(minute || 0), 0, 0)
  return new Intl.DateTimeFormat('es-CO', { hour: '2-digit', minute: '2-digit' }).format(date)
}

export const DAY_NAMES = ['', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']

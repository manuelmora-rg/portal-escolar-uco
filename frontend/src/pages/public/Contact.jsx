import { useMutation, useQuery } from '@tanstack/react-query'
import { CheckCircle2, Clock, Mail, MapPin, Phone, Send } from 'lucide-react'
import { useState } from 'react'
import api, { apiErrorMessage } from '../../lib/api'
import { usePageTitle } from '../../lib/usePageTitle'
import { Field } from '../../components/ui'

const EMPTY = { fullName: '', email: '', phone: '', subject: '', message: '' }

export default function Contact() {
  usePageTitle('Contacto')
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [sent, setSent] = useState(false)

  const { data: institution } = useQuery({
    queryKey: ['public-institution'],
    queryFn: async () => (await api.get('/public/institution')).data,
  })

  const mutation = useMutation({
    mutationFn: async (payload) => (await api.post('/public/contact', payload)).data,
    onSuccess: () => {
      setSent(true)
      setForm(EMPTY)
      setErrors({})
    },
  })

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function validate() {
    const next = {}
    if (!form.fullName.trim()) next.fullName = 'Escribe tu nombre completo.'
    if (!form.email.trim()) next.email = 'Escribe tu correo electrónico.'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) next.email = 'El correo no es válido.'
    if (!form.subject.trim()) next.subject = 'Indica el asunto.'
    if (!form.message.trim()) next.message = 'Escribe tu mensaje.'
    else if (form.message.length > 2000) next.message = 'El mensaje no puede superar 2000 caracteres.'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  function onSubmit(event) {
    event.preventDefault()
    if (!validate()) return
    mutation.mutate({
      fullName: form.fullName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim() || null,
      subject: form.subject.trim(),
      message: form.message.trim(),
    })
  }

  return (
    <div>
      <section className="bg-brand-950 py-16 text-white">
        <div className="container-page">
          <p className="text-xs font-semibold tracking-wide text-accent-300 uppercase">Estamos para escucharte</p>
          <h1 className="mt-2 text-3xl font-extrabold sm:text-4xl">Contacto</h1>
          <p className="mt-4 max-w-2xl text-slate-300">
            Escríbenos para solicitar información sobre admisiones, servicios o actividades institucionales.
          </p>
        </div>
      </section>

      <section className="container-page grid gap-8 py-12 lg:grid-cols-[0.85fr_1.15fr]">
        <aside className="space-y-4">
          <div className="card">
            <h2 className="card-title">Datos de la institución</h2>
            <ul className="mt-4 space-y-4 text-sm text-slate-600 dark:text-slate-300">
              <li className="flex items-start gap-3">
                <MapPin size={18} className="mt-0.5 shrink-0 text-accent-600 dark:text-accent-400" />
                <span>{institution?.address || 'Dirección institucional pendiente de definir'}</span>
              </li>
              <li className="flex items-start gap-3">
                <Phone size={18} className="mt-0.5 shrink-0 text-accent-600 dark:text-accent-400" />
                <span>{institution?.phone || '+57 000 000 0000'}</span>
              </li>
              <li className="flex items-start gap-3">
                <Mail size={18} className="mt-0.5 shrink-0 text-accent-600 dark:text-accent-400" />
                <span>{institution?.email || 'contacto@portalescolar.edu.co'}</span>
              </li>
              <li className="flex items-start gap-3">
                <Clock size={18} className="mt-0.5 shrink-0 text-accent-600 dark:text-accent-400" />
                <span>Lunes a viernes, 7:00 a.m. – 4:00 p.m.</span>
              </li>
            </ul>
          </div>
          <div className="rounded-2xl bg-brand-900 p-6 text-white">
            <p className="text-sm font-semibold text-accent-300">Admisiones</p>
            <p className="mt-2 text-sm text-slate-300">
              Las solicitudes de admisión se atienden durante todo el año según disponibilidad de cupos por nivel.
            </p>
          </div>
        </aside>

        <div className="card">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">Envíanos un mensaje</h2>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Los campos marcados con * son obligatorios.
          </p>

          {sent ? (
            <div className="mt-6 rounded-xl border border-brand-200 bg-brand-50 p-5 dark:border-brand-800 dark:bg-brand-900/40">
              <p className="flex items-center gap-2 font-semibold text-brand-700 dark:text-brand-100">
                <CheckCircle2 size={18} /> Mensaje enviado
              </p>
              <p className="mt-1 text-sm text-brand-700 dark:text-brand-200">
                Gracias por contactarnos. Nuestro equipo responderá a la mayor brevedad.
              </p>
              <button type="button" className="btn-secondary mt-4" onClick={() => setSent(false)}>
                Enviar otro mensaje
              </button>
            </div>
          ) : (
            <form className="mt-6 space-y-4" onSubmit={onSubmit} noValidate>
              <div className="grid gap-4 sm:grid-cols-2">
                <Field label="Nombre completo *" error={errors.fullName} htmlFor="fullName">
                  <input
                    id="fullName"
                    className="input"
                    value={form.fullName}
                    onChange={(event) => update('fullName', event.target.value)}
                    autoComplete="name"
                  />
                </Field>
                <Field label="Correo electrónico *" error={errors.email} htmlFor="email">
                  <input
                    id="email"
                    type="email"
                    className="input"
                    value={form.email}
                    onChange={(event) => update('email', event.target.value)}
                    autoComplete="email"
                  />
                </Field>
                <Field label="Teléfono" error={errors.phone} htmlFor="phone">
                  <input
                    id="phone"
                    className="input"
                    value={form.phone}
                    onChange={(event) => update('phone', event.target.value)}
                    autoComplete="tel"
                  />
                </Field>
                <Field label="Asunto *" error={errors.subject} htmlFor="subject">
                  <input
                    id="subject"
                    className="input"
                    value={form.subject}
                    onChange={(event) => update('subject', event.target.value)}
                  />
                </Field>
              </div>
              <Field label="Mensaje *" error={errors.message} htmlFor="message" hint={`${form.message.length}/2000`}>
                <textarea
                  id="message"
                  rows={6}
                  className="input resize-y"
                  value={form.message}
                  onChange={(event) => update('message', event.target.value)}
                />
              </Field>

              {mutation.isError ? (
                <p className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-200">
                  {apiErrorMessage(mutation.error, 'No fue posible enviar el mensaje.')}
                </p>
              ) : null}

              <button type="submit" className="btn-primary" disabled={mutation.isPending}>
                <Send size={16} />
                {mutation.isPending ? 'Enviando…' : 'Enviar mensaje'}
              </button>
            </form>
          )}
        </div>
      </section>
    </div>
  )
}

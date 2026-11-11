import { useEffect } from 'react'

export function usePageTitle(title) {
  useEffect(() => {
    document.title = title ? `${title} · Portal Escolar` : 'Portal Escolar'
  }, [title])
}

import { ref, watch } from 'vue'

const isDark = ref(true)
let hasHydrated = false

interface SimpleStorage {
  getItem(key: string): string | null
  setItem(key: string, value: string): void
}

const getLocalStorage = (): SimpleStorage | undefined => {
  if (typeof globalThis !== 'undefined' && 'localStorage' in globalThis) {
    return (globalThis as unknown as { localStorage: SimpleStorage }).localStorage
  }
  return undefined
}

const hasWindow = (): boolean => {
  return typeof globalThis !== 'undefined' && 'window' in globalThis
}

const loadTheme = () => {
  const storage = getLocalStorage()
  if (!storage || hasHydrated) return

  const savedTheme = storage.getItem('shopbee_theme')
  if (savedTheme) {
    isDark.value = savedTheme === 'dark'
  } else {
    // Default to dark mode for shopbee
    isDark.value = true
  }

  hasHydrated = true
  updateDOM()
}

const updateDOM = () => {
  if (typeof document !== 'undefined') {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }
}

const saveTheme = () => {
  const storage = getLocalStorage()
  if (!storage) return
  storage.setItem('shopbee_theme', isDark.value ? 'dark' : 'light')
  updateDOM()
}

if (hasWindow()) {
  watch(isDark, saveTheme)
}

export function useTheme() {
  if (hasWindow() && !hasHydrated) {
    loadTheme()
  }

  const initTheme = () => {
    if (hasWindow()) {
      loadTheme()
    }
  }

  const toggleTheme = () => {
    isDark.value = !isDark.value
  }

  return {
    isDark,
    initTheme,
    toggleTheme
  }
}

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useProducts } from '~/composables/useProducts'
import { useUserProfile, type UserAccount } from '~/composables/useUserProfile'
import { useTheme } from '~/composables/useTheme'

const { searchQuery, cartCount } = useProducts()
const { accounts, activeAccountId, userProfile, switchAccount, userBalance } =
  useUserProfile()
const { isDark, toggleTheme } = useTheme()

if (import.meta.server || accounts.value.length === 0) {
  const { data } = await useFetch('/api/accounts')
  if (data.value && accounts.value.length === 0) {
    accounts.value = data.value as UserAccount[]
  }
}

const clearSearch = () => {
  searchQuery.value = ''
}

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(val)
}

const userMenuRef = ref<{ toggle: (event: Event) => void; hide: () => void } | null>(null)

const toggleUserMenu = (event: Event) => {
  userMenuRef.value?.toggle(event)
}

const hideUserMenu = () => {
  userMenuRef.value?.hide()
}

const menuItems = computed(() => [
  {
    type: 'profile'
  },
  {
    type: 'switcher'
  },
  {
    label: 'My Orders',
    icon: 'pi pi-compass',
    route: '/orders'
  },
  {
    label: 'Voucher Hub',
    icon: 'pi pi-ticket',
    route: '/vouchers',
    badge: true
  },
  {
    separator: true
  },
  {
    label: 'My Profile',
    icon: 'pi pi-user',
    route: '/profile'
  },
  {
    label: 'Settings',
    icon: 'pi pi-cog',
    items: [
      {
        icon: isDark.value ? 'pi pi-sun' : 'pi pi-moon',
        type: 'theme-toggle',
        command: () => {
          toggleTheme()
          hideUserMenu()
        }
      }
    ]
  },
  {
    label: 'Sign Out',
    icon: 'pi pi-sign-out',
    disabled: true,
    class: 'opacity-50 cursor-not-allowed'
  }
])
</script>

<template>
  <div
    class="min-h-screen relative flex flex-col font-sans selection:bg-indigo-500/30 selection:text-white"
  >
    <!-- Skip to content -->
    <a
      href="#main-content"
      class="sr-only focus:not-sr-only focus:absolute focus:z-[100] focus:top-4 focus:left-4 focus:px-4 focus:py-2 focus:bg-indigo-600 focus:text-white focus:rounded-lg focus:font-bold focus:text-sm"
    >
      Skip to main content
    </a>

    <!-- Ambient backgrounds -->
    <div
      class="absolute inset-0 z-0 overflow-hidden pointer-events-none"
      aria-hidden="true"
    >
      <div
        class="absolute rounded-full blur-[160px] opacity-15 top-[-10%] left-[10%] w-[500px] h-[500px] bg-[radial-gradient(circle,#6366f1_0%,transparent_70%)]"
      ></div>
      <div
        class="absolute rounded-full blur-[160px] opacity-15 bottom-[20%] right-[5%] w-[600px] h-[600px] bg-[radial-gradient(circle,#3b82f6_0%,transparent_70%)]"
      ></div>
      <div
        class="absolute rounded-full blur-[140px] opacity-10 top-[40%] right-[30%] w-[400px] h-[400px] bg-[radial-gradient(circle,#ec4899_0%,transparent_70%)]"
      ></div>
    </div>

    <!-- Header Navigation -->
    <header
      role="banner"
      class="sticky top-0 z-50 w-full backdrop-blur-md bg-white/75 dark:bg-slate-950/75 border-b border-slate-200 dark:border-white/5 transition-all duration-300"
    >
      <div
        class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex justify-between items-center gap-4"
      >
        <!-- Logo -->
        <NuxtLink
          to="/"
          class="flex items-center gap-3 shrink-0 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 rounded-lg p-1 group"
        >
          <div
            class="relative flex items-center justify-center w-10 h-10 rounded-xl bg-linear-to-br from-indigo-500 to-blue-600 shadow-[0_0_20px_rgba(99,102,241,0.3)] group-hover:scale-105 transition-all duration-300"
          >
            <i
              class="pi pi-bolt text-xl text-white drop-shadow-[0_2px_4px_rgba(0,0,0,0.2)]"
            ></i>
            <div
              class="absolute inset-0 rounded-xl bg-white/10 opacity-0 group-hover:opacity-100 transition-opacity duration-300"
            ></div>
          </div>
          <span
            class="text-xl font-extrabold tracking-tight bg-linear-to-r from-slate-900 via-slate-700 to-slate-500 dark:from-white dark:via-slate-100 dark:to-slate-400 bg-clip-text text-transparent group-hover:to-slate-900 dark:group-hover:to-white transition-all duration-300"
          >
            Shopbee
          </span>
        </NuxtLink>

        <!-- Search input (Desktop) -->
        <div class="hidden md:flex relative flex-1 max-w-md mx-4">
          <label for="search-desktop" class="sr-only">Search products</label>
          <span
            class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none"
          >
            <i class="pi pi-search text-slate-400 text-sm"></i>
          </span>
          <input
            id="search-desktop"
            v-model="searchQuery"
            type="text"
            placeholder="Search premium products..."
            class="w-full bg-slate-100/60 dark:bg-slate-900/60 hover:bg-slate-100 dark:hover:bg-slate-900 border border-slate-200 dark:border-white/10 hover:border-slate-300 dark:hover:border-white/20 focus:border-indigo-500 text-slate-800 dark:text-slate-200 placeholder-slate-400 dark:placeholder-slate-500 py-2.5 pl-10 pr-9 rounded-xl font-medium text-sm transition-all focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
          />
          <button
            v-if="searchQuery"
            aria-label="Clear search"
            class="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 transition-colors"
            @click="clearSearch"
          >
            <i class="pi pi-times-circle text-sm" aria-hidden="true"></i>
          </button>
        </div>

        <!-- Right Side Nav Actions -->
        <div class="flex items-center gap-4 shrink-0">
          <nav
            class="hidden lg:flex items-center gap-6"
            aria-label="Primary navigation"
          >
            <NuxtLink
              to="/"
              class="text-sm font-semibold text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white transition-colors py-2 px-1 relative group"
            >
              Catalog
              <span
                class="absolute bottom-0 left-0 w-0 h-0.5 bg-indigo-500 transition-all duration-300 group-hover:w-full"
              ></span>
            </NuxtLink>
            <span
              class="text-sm font-semibold text-slate-400 cursor-not-allowed py-2 px-1"
              >Deals</span
            >
            <span
              class="text-sm font-semibold text-slate-400 cursor-not-allowed py-2 px-1"
              >Brands</span
            >
            <span
              class="text-sm font-semibold text-slate-400 cursor-not-allowed py-2 px-1"
              >Support</span
            >
          </nav>

          <div class="h-6 w-px bg-slate-200 dark:bg-white/10 hidden lg:block"></div>

          <!-- Cart button -->
          <NuxtLink
            to="/cart"
            :aria-label="`Shopping cart${cartCount > 0 ? ', ' + cartCount + ' items' : ', empty'}`"
            class="relative flex items-center justify-center p-2.5 rounded-xl border border-slate-200 dark:border-white/5 bg-slate-100/40 dark:bg-slate-900/40 hover:bg-slate-100 dark:hover:bg-slate-900 hover:border-slate-300 dark:hover:border-white/10 active:scale-95 transition-all group focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
          >
            <i
              class="pi pi-shopping-cart text-lg text-slate-600 dark:text-slate-300 group-hover:text-slate-900 group-hover:dark:text-white transition-colors"
            ></i>
            <span
              v-if="cartCount > 0"
              class="absolute -top-1.5 -right-1.5 flex h-5 min-w-[20px] px-1 items-center justify-center rounded-full bg-linear-to-r from-pink-500 to-rose-500 text-[10px] font-extrabold text-white border border-white dark:border-slate-950 shadow-[0_0_10px_rgba(244,63,94,0.4)] animate-bounce"
            >
              {{ cartCount }}
            </span>
          </NuxtLink>

          <!-- User Menu Dropdown -->
          <div class="relative">
            <!-- User avatar -->
            <button
              aria-label="Open user account menu"
              class="flex items-center justify-center w-10 h-10 rounded-xl border border-slate-200 dark:border-white/10 bg-slate-100/60 dark:bg-slate-900/60 overflow-hidden hover:border-indigo-500/40 active:scale-95 transition-all focus:outline-none focus:ring-2 focus:ring-indigo-500/20 cursor-pointer text-slate-600 dark:text-slate-300"
              aria-haspopup="true"
              @click="toggleUserMenu($event)"
            >
              <i class="pi pi-user text-sm"></i>
            </button>

            <!-- PrimeVue TieredMenu -->
            <TieredMenu
              ref="userMenuRef"
              :model="menuItems"
              popup
              class="w-60 rounded-2xl border border-slate-200 dark:border-white/10 bg-white dark:bg-slate-950/90 backdrop-blur-xl shadow-2xl p-2 z-50"
            >
              <template #item="{ item, props, hasSubmenu }">
                <!-- User Profile Header -->
                <NuxtLink
                  v-if="item.type === 'profile'"
                  to="/profile"
                  class="block px-3 py-2 border-b border-slate-200 dark:border-white/5 mb-1.5 hover:bg-slate-50 dark:hover:bg-white/5 transition-colors rounded-lg group"
                  @click="hideUserMenu"
                >
                  <p
                    class="text-[10px] uppercase tracking-wider text-slate-400 font-bold"
                  >
                    Signed in as
                  </p>
                  <p
                    class="text-sm font-semibold text-slate-700 dark:text-slate-200 truncate group-hover:text-slate-900 dark:group-hover:text-white transition-colors"
                  >
                    {{ userProfile.name }}
                  </p>
                  <p class="text-[10px] text-indigo-500 dark:text-indigo-400 font-mono mt-0.5">
                    Balance: {{ formatCurrency(userBalance) }}
                  </p>
                </NuxtLink>

                <!-- Account Switcher -->
                <div
                  v-else-if="item.type === 'switcher'"
                  class="px-3 py-1.5 border-b border-slate-200 dark:border-white/5 mb-1.5 flex flex-col gap-1"
                >
                  <span
                    class="text-[9px] uppercase tracking-wider text-slate-400 font-bold"
                  >
                    Switch Active Account
                  </span>
                  <Select
                    v-model="activeAccountId"
                    :options="accounts"
                    option-label="profile.name"
                    option-value="id"
                    class="w-full text-xs switcher-select"
                    @change="switchAccount($event.value)"
                  >
                    <template #value="slotProps">
                      <div v-if="slotProps.value" class="flex items-center">
                        <span class="font-bold text-slate-700 dark:text-slate-200 text-xs truncate">
                          {{
                            accounts.find((a) => a.id === slotProps.value)
                              ?.profile.name
                          }}
                        </span>
                      </div>
                    </template>
                    <template #option="slotProps">
                      <div class="flex flex-col text-xs py-0.5">
                        <span class="font-bold text-slate-700 dark:text-slate-200">
                          {{ slotProps.option.profile.name }}
                        </span>
                        <span class="text-[10px] text-slate-500 font-mono">
                          {{ formatCurrency(slotProps.option.balance) }}
                        </span>
                      </div>
                    </template>
                  </Select>
                </div>

                <!-- Theme Toggle -->
                <div
                  v-else-if="item.type === 'theme-toggle'"
                  v-bind="props.action"
                  class="flex items-center justify-center p-2 cursor-pointer w-full text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-white/5 rounded-lg"
                >
                  <i :class="[item.icon, 'text-lg']" />
                </div>

                <!-- Standard Links with route -->
                <NuxtLink
                  v-else-if="item.route"
                  :to="item.route"
                  class="flex items-center justify-between px-3 py-2 rounded-lg text-sm text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-white/5 transition-all group cursor-pointer"
                  @click="hideUserMenu"
                >
                  <div class="flex items-center gap-2.5">
                    <i
                      :class="[item.icon, 'text-slate-400 group-hover:text-indigo-500 dark:group-hover:text-indigo-400 transition-colors']"
                    ></i>
                    <span>{{ item.label }}</span>
                  </div>
                  <!-- Voucher hub ping badge indicator -->
                  <span v-if="item.badge" class="flex h-2 w-2 relative">
                    <span
                      class="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"
                    ></span>
                    <span
                      class="relative inline-flex rounded-full h-2 w-2 bg-indigo-500"
                    ></span>
                  </span>
                </NuxtLink>

                <!-- Standard Submenus / Parent menu items (like Settings, Sign Out) -->
                <div
                  v-else
                  :class="[
                    'flex items-center justify-between px-3 py-2 rounded-lg text-sm transition-all group cursor-pointer',
                    item.disabled ? 'text-slate-400 dark:text-slate-500 opacity-50 cursor-not-allowed' : 'text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-white/5'
                  ]"
                  v-bind="props.action"
                >
                  <div class="flex items-center gap-2.5">
                    <i
                      :class="[item.icon, item.disabled ? 'text-slate-400 dark:text-slate-600' : 'text-slate-400 group-hover:text-indigo-500 dark:group-hover:text-indigo-400 transition-colors']"
                    ></i>
                    <span>{{ item.label }}</span>
                  </div>
                  <i v-if="hasSubmenu" class="pi pi-angle-right text-xs text-slate-400 ml-auto" />
                </div>
              </template>
            </TieredMenu>
          </div>
        </div>
      </div>

      <!-- Search input (Mobile) -->
      <div class="md:hidden px-4 pb-4 flex">
        <div class="relative w-full">
          <label for="search-mobile" class="sr-only">Search products</label>
          <span
            class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"
          >
            <i class="pi pi-search text-slate-400 text-sm"></i>
          </span>
          <input
            id="search-mobile"
            v-model="searchQuery"
            type="text"
            placeholder="Search products..."
            class="w-full bg-slate-100/60 dark:bg-slate-900/60 border border-slate-200 dark:border-white/10 text-slate-800 dark:text-slate-200 placeholder-slate-400 dark:placeholder-slate-500 py-2 pl-9 pr-8 rounded-lg font-medium text-xs transition-all focus:outline-none focus:border-indigo-500"
          />
          <button
            v-if="searchQuery"
            aria-label="Clear search"
            class="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-500 hover:text-slate-800 dark:hover:text-slate-200"
            @click="clearSearch"
          >
            <i class="pi pi-times-circle text-xs" aria-hidden="true"></i>
          </button>
        </div>
      </div>
    </header>

    <!-- Main Content Slot -->
    <main
      id="main-content"
      class="relative z-10 flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex flex-col"
    >
      <slot />
    </main>

    <!-- Footer -->
    <footer
      role="contentinfo"
      class="relative z-10 mt-auto border-t border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-slate-950/50 backdrop-blur-sm py-8 text-center text-slate-500 dark:text-slate-400 text-xs"
    >
      <div
        class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row justify-between items-center gap-4"
      >
        <div class="flex items-center gap-2">
          <i class="pi pi-bolt text-indigo-500 dark:text-indigo-400"></i>
          <span class="font-semibold text-slate-600 dark:text-slate-400"
            >Shopbee Storefront Prototype</span
          >
        </div>
        <div class="flex gap-4">
          <span class="hover:text-slate-800 dark:hover:text-slate-300 cursor-pointer transition-colors"
            >Privacy Policy</span
          >
          <span class="hover:text-slate-800 dark:hover:text-slate-300 cursor-pointer transition-colors"
            >Terms of Service</span
          >
          <span class="hover:text-slate-800 dark:hover:text-slate-300 cursor-pointer transition-colors"
            >API Docs</span
          >
        </div>
        <div>
          <p>&copy; 2026 Shopbee Engineering. Powered by Nuxt & PrimeVue.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

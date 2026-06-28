<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useProducts } from '~/composables/useProducts'
import type { Voucher } from '~/composables/useProducts'

const router = useRouter()
const toast = useToast()
const { vouchersList, collectVoucher } = useProducts()

if (vouchersList.value.length === 0) {
  const { data: serverVouchers } = await useFetch('/api/vouchers')
  if (serverVouchers.value) {
    vouchersList.value = serverVouchers.value as Voucher[]
  }
}

// Filtering vouchers by type
const platformVouchers = computed(() => {
  return vouchersList.value.filter((v) => v.type === 'platform')
})

const merchantVouchers = computed(() => {
  return vouchersList.value.filter((v) => v.type === 'merchant')
})

const eventVouchers = computed(() => {
  return vouchersList.value.filter((v) => v.type === 'event')
})

// --- Lucky Card Draw Event Game ---
const isDrawing = ref(false)
const flippedCardIndex = ref<number | null>(null)
const drawnVoucher = ref<Voucher | null>(null)
const gamePlayed = ref(false)

const handleCardClick = async (cardIndex: number) => {
  if (gamePlayed.value || isDrawing.value) return

  isDrawing.value = true
  flippedCardIndex.value = cardIndex

  // Play animation latency
  await new Promise((resolve) => setTimeout(resolve, 800))

  // Pick a random event voucher (v6, v7, or v8)
  const availableEventVouchers = eventVouchers.value
  const randomIndex = Math.floor(Math.random() * availableEventVouchers.length)
  const selectedVoucher = availableEventVouchers[randomIndex]

  if (selectedVoucher) {
    await collectVoucher(selectedVoucher.id)
    drawnVoucher.value = selectedVoucher
    toast.add({
      severity: 'success',
      summary: 'Lucky Draw Winner!',
      detail: `You won the ${selectedVoucher.title} (${selectedVoucher.code})!`,
      life: 4000
    })
  }

  isDrawing.value = false
  gamePlayed.value = true
}

const resetLuckyDraw = () => {
  flippedCardIndex.value = null
  drawnVoucher.value = null
  gamePlayed.value = false
}

// --- General Collect Action ---
const handleCollect = async (voucher: Voucher) => {
  const success = await collectVoucher(voucher.id)
  if (success) {
    toast.add({
      severity: 'success',
      summary: 'Voucher Collected',
      detail: `Voucher "${voucher.code}" has been saved to your account.`,
      life: 3000
    })
  }
}

const goToCart = () => router.push('/cart')
const goHome = () => router.push('/')

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(val)
}
</script>

<template>
  <div class="flex-1 flex flex-col gap-8 animate-slide-up">
    <!-- Back Button -->
    <div>
      <button
        class="group flex items-center gap-2.5 text-xs font-bold text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-colors focus:outline-none cursor-pointer"
        @click="goHome"
      >
        <i
          class="pi pi-arrow-left group-hover:-translate-x-1 transition-transform"
        />
        Back to Catalog
      </button>
    </div>

    <!-- Page Title -->
    <div>
      <h1 class="text-2xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
        Shopbee Voucher Hub
      </h1>
      <p class="text-xs text-slate-500 mt-1">
        Collect exclusive platform, brand, and event vouchers to save big on
        your purchases.
      </p>
    </div>

    <!-- ─── EVENT SECTION: LUCKY CARD DRAW ─── -->
    <section
      class="bg-linear-to-br from-indigo-950/40 via-slate-900/40 to-slate-950/45 border border-slate-200 dark:border-white/5 rounded-3xl p-6 sm:p-8 relative overflow-hidden shadow-2xl backdrop-blur-md"
    >
      <!-- Background Ambient Glow -->
      <div
        class="absolute -top-[50%] -right-[30%] w-[350px] h-[350px] bg-indigo-500/10 rounded-full blur-3xl pointer-events-none"
      ></div>
      <div
        class="absolute -bottom-[50%] -left-[20%] w-[350px] h-[350px] bg-pink-500/5 rounded-full blur-3xl pointer-events-none"
      ></div>

      <div class="relative z-10 flex flex-col gap-6">
        <div
          class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4"
        >
          <div>
            <span
              class="px-2.5 py-0.5 rounded-full bg-amber-500/10 border border-amber-500/20 text-[9px] font-bold uppercase tracking-wider text-amber-400"
            >
              Interactive Event
            </span>
            <h2 class="text-lg sm:text-xl font-extrabold text-slate-900 dark:text-white mt-1.5">
              Summer Festival Lucky Draw
            </h2>
            <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Choose and flip one lucky card to claim a high-value event
              voucher! Limit one flip per visit.
            </p>
          </div>
          <Button
            v-if="gamePlayed"
            label="Try Another Flip"
            icon="pi pi-refresh"
            size="small"
            severity="secondary"
            class="text-xs py-1.5 px-3 border-slate-200 dark:border-white/10 hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white rounded-xl font-bold transition-all whitespace-nowrap cursor-pointer"
            @click="resetLuckyDraw"
          />
        </div>

        <!-- Cards Deck Container -->
        <div
          class="grid grid-cols-1 sm:grid-cols-3 gap-6 max-w-2xl mx-auto w-full py-4 perspective-1000"
        >
          <!-- Loop 3 cards -->
          <div
            v-for="idx in [0, 1, 2]"
            :key="idx"
            class="card-container aspect-[3/4.5] w-full cursor-pointer relative"
            @click="handleCardClick(idx)"
          >
            <!-- 3D Card Inner Wrapper -->
            <div
              class="card-inner w-full h-full rounded-2xl relative transition-transform duration-700 transform-style-3d border border-slate-200 dark:border-white/10 shadow-2xl"
              :class="{
                'rotate-y-180': flippedCardIndex === idx && gamePlayed,
                'opacity-40 pointer-events-none scale-95':
                  gamePlayed && flippedCardIndex !== idx
              }"
            >
              <!-- CARD FRONT (Face Down) -->
              <div
                class="card-front absolute inset-0 w-full h-full rounded-2xl flex flex-col items-center justify-center bg-linear-to-br from-indigo-950 via-slate-900 to-slate-950 backface-hidden z-10"
              >
                <!-- Gold Foil border decoration -->
                <div
                  class="absolute inset-1.5 rounded-[10px] border border-amber-500/15"
                ></div>
                <!-- Center Icon -->
                <div
                  class="w-12 h-12 rounded-full bg-linear-to-br from-amber-500 to-yellow-600 flex items-center justify-center text-slate-900 dark:text-white shadow-[0_0_15px_rgba(245,158,11,0.3)] animate-pulse"
                >
                  <i class="pi pi-bolt text-lg" />
                </div>
                <span
                  class="text-[10px] font-extrabold uppercase text-amber-500 tracking-widest mt-4 font-mono"
                  >Lucky Flip</span
                >
              </div>

              <!-- CARD BACK (Face Up - Revealed) -->
              <div
                class="card-back absolute inset-0 w-full h-full rounded-2xl flex flex-col items-center justify-between p-5 bg-linear-to-br from-slate-900 via-indigo-950 to-slate-900 rotate-y-180 backface-hidden"
              >
                <div
                  class="absolute inset-1.5 rounded-[10px] border border-indigo-500/25"
                ></div>

                <!-- Card Header -->
                <div class="flex items-center gap-1.5 relative z-10">
                  <i class="pi pi-gift text-indigo-650 dark:text-indigo-400 text-xs" />
                  <span
                    class="text-[9px] uppercase tracking-wider text-slate-500 dark:text-slate-400 font-bold"
                    >You Won!</span
                  >
                </div>

                <!-- Reward Details -->
                <div
                  v-if="drawnVoucher"
                  class="flex flex-col items-center text-center relative z-10 w-full"
                >
                  <span
                    class="text-2xl font-black bg-linear-to-r from-amber-400 to-yellow-500 bg-clip-text text-transparent"
                  >
                    {{
                      drawnVoucher.discountType === 'percentage'
                        ? `${drawnVoucher.discountValue}%`
                        : `$${drawnVoucher.discountValue}`
                    }}
                  </span>
                  <span
                    class="text-[9px] uppercase tracking-wider text-slate-500 font-bold mt-0.5"
                    >OFF</span
                  >
                  <p
                    class="text-[11px] font-bold text-slate-750 dark:text-slate-200 mt-3 line-clamp-2 leading-relaxed px-1"
                  >
                    {{ drawnVoucher.title }}
                  </p>
                  <span
                    class="px-2.5 py-1 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/10 rounded-lg text-xs font-mono font-bold text-indigo-650 dark:text-indigo-400 tracking-wider mt-4"
                  >
                    {{ drawnVoucher.code }}
                  </span>
                </div>

                <!-- Card Footer Action -->
                <button
                  class="relative z-10 w-full py-1.5 bg-linear-to-r from-indigo-500 to-blue-600 text-[10px] font-extrabold uppercase tracking-wider text-slate-900 dark:text-white border-0 rounded-xl hover:shadow-[0_0_10px_rgba(99,102,241,0.4)] transition-all cursor-pointer"
                  @click.stop="goToCart"
                >
                  Apply to Cart
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ─── PLATFORM VOUCHERS ─── -->
    <section class="flex flex-col gap-4">
      <div>
        <h2
          class="text-sm sm:text-base font-extrabold uppercase tracking-wider text-slate-900 dark:text-white"
        >
          Shopbee Platform Vouchers
        </h2>
        <p class="text-xs text-slate-500 mt-0.5">
          General vouchers that apply to your entire order subtotal.
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div
          v-for="voucher in platformVouchers"
          :key="voucher.id"
          class="ticket-card relative rounded-3xl overflow-hidden border border-slate-200 dark:border-white/5 bg-white dark:bg-slate-100 dark:bg-slate-900/30 hover:border-indigo-500/10 hover:bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/50 backdrop-blur-md flex shadow-xl transition-all duration-300 group"
        >
          <!-- Left side ticket notched edge spacer -->
          <div
            class="absolute left-[-10px] top-1/2 -translate-y-1/2 w-5 h-5 rounded-full bg-slate-100 dark:bg-slate-950 border-r border-slate-200 dark:border-white/5 z-20"
          ></div>
          <!-- Right side ticket notched edge spacer -->
          <div
            class="absolute right-[-10px] top-1/2 -translate-y-1/2 w-5 h-5 rounded-full bg-slate-100 dark:bg-slate-950 border-l border-slate-200 dark:border-white/5 z-20"
          ></div>

          <!-- Color gradient accent bar -->
          <div
            class="w-3 shrink-0 bg-linear-to-b"
            :class="voucher.colorClass"
          ></div>

          <!-- Ticket Contents -->
          <div
            class="flex-1 flex flex-col sm:flex-row divide-y sm:divide-y-0 sm:divide-x divide-dashed divide-slate-200 dark:divide-white/10 p-5 gap-4"
          >
            <!-- Details Area -->
            <div
              class="flex-1 flex flex-col justify-between min-w-0 pr-0 sm:pr-4"
            >
              <div>
                <span
                  class="text-[9px] uppercase tracking-wider text-indigo-650 dark:text-indigo-400 font-bold"
                >
                  Shopbee Storewide
                </span>
                <h3
                  class="text-sm sm:text-base font-bold text-slate-900 dark:text-white mt-1 leading-tight truncate"
                >
                  {{ voucher.title }}
                </h3>
                <p
                  class="text-xs text-slate-500 dark:text-slate-400 mt-1 leading-relaxed line-clamp-2 font-medium"
                >
                  {{ voucher.description }}
                </p>
              </div>

              <div class="flex items-center gap-2 mt-4">
                <span class="text-[10px] font-semibold text-slate-500 uppercase"
                  >Min. Spend:</span
                >
                <span class="text-[10px] font-bold text-slate-600 dark:text-slate-300">
                  {{
                    voucher.minSubtotal > 0
                      ? formatCurrency(voucher.minSubtotal)
                      : 'None'
                  }}
                </span>
              </div>
            </div>

            <!-- CTA / Reward Area -->
            <div
              class="shrink-0 flex flex-col justify-between items-center sm:items-end gap-3 pt-4 sm:pt-0 pl-0 sm:pl-4 min-w-[110px]"
            >
              <div
                class="flex flex-col items-center sm:items-end text-center sm:text-right"
              >
                <span
                  class="text-2xl font-black bg-linear-to-br bg-clip-text text-transparent"
                  :class="voucher.colorClass"
                >
                  {{
                    voucher.discountType === 'percentage'
                      ? `${voucher.discountValue}%`
                      : `$${voucher.discountValue}`
                  }}
                </span>
                <span
                  class="text-[9px] font-bold text-slate-500 uppercase tracking-widest mt-0.5"
                  >discount</span
                >
              </div>

              <!-- Action button -->
              <Button
                v-if="!voucher.collected"
                label="Collect"
                size="small"
                class="w-full py-1.5 bg-indigo-600/20 hover:bg-indigo-600 text-indigo-650 dark:text-indigo-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white border border-indigo-500/20 hover:shadow-[0_0_10px_rgba(99,102,241,0.3)] rounded-xl font-bold text-[10px] uppercase tracking-wider transition-all cursor-pointer"
                @click="handleCollect(voucher)"
              />
              <div
                v-else
                class="w-full flex items-center justify-center gap-1.5 py-1.5 rounded-xl border border-emerald-500/15 bg-emerald-500/10 text-emerald-400 font-extrabold text-[10px] uppercase tracking-wider"
              >
                <i class="pi pi-check text-[9px]" />
                Collected
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ─── MERCHANT VOUCHERS ─── -->
    <section class="flex flex-col gap-4">
      <div>
        <h2
          class="text-sm sm:text-base font-extrabold uppercase tracking-wider text-slate-900 dark:text-white"
        >
          Brand Partner Store Vouchers
        </h2>
        <p class="text-xs text-slate-500 mt-0.5">
          Vouchers provided by merchant brands, applicable to their products
          only.
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div
          v-for="voucher in merchantVouchers"
          :key="voucher.id"
          class="ticket-card relative rounded-3xl overflow-hidden border border-slate-200 dark:border-white/5 bg-white dark:bg-slate-100 dark:bg-slate-900/30 hover:border-indigo-500/10 hover:bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/50 backdrop-blur-md flex shadow-xl transition-all duration-300 group"
        >
          <!-- Left side ticket notched edge spacer -->
          <div
            class="absolute left-[-10px] top-1/2 -translate-y-1/2 w-5 h-5 rounded-full bg-slate-100 dark:bg-slate-950 border-r border-slate-200 dark:border-white/5 z-20"
          ></div>
          <!-- Right side ticket notched edge spacer -->
          <div
            class="absolute right-[-10px] top-1/2 -translate-y-1/2 w-5 h-5 rounded-full bg-slate-100 dark:bg-slate-950 border-l border-slate-200 dark:border-white/5 z-20"
          ></div>

          <!-- Color gradient accent bar -->
          <div
            class="w-3 shrink-0 bg-linear-to-b"
            :class="voucher.colorClass"
          ></div>

          <!-- Ticket Contents -->
          <div
            class="flex-1 flex flex-col sm:flex-row divide-y sm:divide-y-0 sm:divide-x divide-dashed divide-slate-200 dark:divide-white/10 p-5 gap-4"
          >
            <!-- Details Area -->
            <div
              class="flex-1 flex flex-col justify-between min-w-0 pr-0 sm:pr-4"
            >
              <div>
                <span
                  class="text-[9px] uppercase tracking-wider text-sky-400 font-bold"
                >
                  {{ voucher.merchantBrand }} Exclusive
                </span>
                <h3
                  class="text-sm sm:text-base font-bold text-slate-900 dark:text-white mt-1 leading-tight truncate"
                >
                  {{ voucher.title }}
                </h3>
                <p
                  class="text-xs text-slate-500 dark:text-slate-400 mt-1 leading-relaxed line-clamp-2 font-medium"
                >
                  {{ voucher.description }}
                </p>
              </div>

              <div class="flex items-center gap-2 mt-4">
                <span class="text-[10px] font-semibold text-slate-500 uppercase"
                  >Min. Spend:</span
                >
                <span class="text-[10px] font-bold text-slate-600 dark:text-slate-300">
                  {{
                    voucher.minSubtotal > 0
                      ? formatCurrency(voucher.minSubtotal)
                      : 'None'
                  }}
                </span>
              </div>
            </div>

            <!-- CTA / Reward Area -->
            <div
              class="shrink-0 flex flex-col justify-between items-center sm:items-end gap-3 pt-4 sm:pt-0 pl-0 sm:pl-4 min-w-[110px]"
            >
              <div
                class="flex flex-col items-center sm:items-end text-center sm:text-right"
              >
                <span
                  class="text-2xl font-black bg-linear-to-br bg-clip-text text-transparent"
                  :class="voucher.colorClass"
                >
                  {{
                    voucher.discountType === 'percentage'
                      ? `${voucher.discountValue}%`
                      : `$${voucher.discountValue}`
                  }}
                </span>
                <span
                  class="text-[9px] font-bold text-slate-500 uppercase tracking-widest mt-0.5"
                  >discount</span
                >
              </div>

              <!-- Action button -->
              <Button
                v-if="!voucher.collected"
                label="Collect"
                size="small"
                class="w-full py-1.5 bg-indigo-600/20 hover:bg-indigo-600 text-indigo-650 dark:text-indigo-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white border border-indigo-500/20 hover:shadow-[0_0_10px_rgba(99,102,241,0.3)] rounded-xl font-bold text-[10px] uppercase tracking-wider transition-all cursor-pointer"
                @click="handleCollect(voucher)"
              />
              <div
                v-else
                class="w-full flex items-center justify-center gap-1.5 py-1.5 rounded-xl border border-emerald-500/15 bg-emerald-500/10 text-emerald-400 font-extrabold text-[10px] uppercase tracking-wider"
              >
                <i class="pi pi-check text-[9px]" />
                Collected
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* 3D Lucky Draw perspective rules */
.perspective-1000 {
  perspective: 1000px;
}

.transform-style-3d {
  transform-style: preserve-3d;
}

.backface-hidden {
  backface-visibility: hidden;
}

.rotate-y-180 {
  transform: rotateY(180deg);
}

.card-container {
  perspective: 1000px;
}

.card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  transition: transform 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.card-container:hover .card-inner:not(.rotate-y-180) {
  transform: translateY(-8px) scale(1.02);
  border-color: rgba(245, 158, 11, 0.4);
}

/* Glass ticket notches styling and shadows */
.ticket-card {
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.5);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.ticket-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px -10px rgba(0, 0, 0, 0.7);
}
</style>

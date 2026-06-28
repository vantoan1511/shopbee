import type {
  Product,
  Voucher,
  Review
} from '../../app/composables/useProducts'
import type { UserAccount } from '../../app/composables/useUserProfile'
import type { Order } from '../../app/composables/useOrders'

export const mockProducts: Product[] = [
  {
    id: 1,
    name: 'Beexel 9 Pro',
    description:
      'Next-gen flagship smartphone with a stunning LTPO AMOLED display, professional-grade triple camera array, and blazing-fast performance.',
    price: 899.0,
    sku: 'BX-9PRO-5G',
    active: true,
    brand: 'Beexel',
    category: 'Electronics',
    stock: 12,
    imageUrl:
      'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&auto=format&fit=crop&q=80',
    images: [
      'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1565849904461-04a58ad377e0?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1580910051074-3eb694886505?w=400&auto=format&fit=crop&q=80'
    ],
    rating: 4.5,
    reviewsCount: 18,
    features: ['5G', 'Waterproof', 'Wireless', 'Touch Control'],
    specs: {
      Display: '6.8" LTPO AMOLED, 120Hz',
      Processor: 'Beexel X2 Octa-core 3.2GHz',
      RAM: '12 GB LPDDR5X',
      Storage: '256 GB UFS 4.0',
      'Rear Camera': '200MP + 12MP + 10MP Triple Array',
      'Front Camera': '32MP f/2.2',
      Battery: '5100 mAh',
      Charging: '65W Wired, 45W Wireless',
      OS: 'BeexelOS 14 (Android 15)',
      Network: '5G Sub-6 / mmWave',
      'Water Resistance': 'IP68 (2m / 30 min)',
      Dimensions: '163.4 × 74.8 × 8.6 mm'
    },
    variants: [
      {
        name: 'Color',
        options: ['Obsidian Black', 'Snow White', 'Mint Green']
      },
      { name: 'Storage', options: ['128 GB', '256 GB'] }
    ]
  },
  {
    id: 2,
    name: 'AeroBook 14',
    description:
      'Ultra-thin, feather-light laptop featuring a gorgeous 4K OLED display, exceptional battery life, and powerful multi-threaded processing power.',
    price: 1249.0,
    sku: 'AR-BK14-X',
    active: true,
    brand: 'Aero',
    category: 'Electronics',
    stock: 4,
    imageUrl:
      'https://images.unsplash.com/photo-1496181130204-755241524eab?w=400&auto=format&fit=crop&q=80',
    images: [
      'https://images.unsplash.com/photo-1496181130204-755241524eab?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&auto=format&fit=crop&q=80'
    ],
    rating: 4.8,
    reviewsCount: 4,
    features: ['Wireless', '4K Display'],
    specs: {
      Display: '14" 4K OLED, 90Hz, 400 nits',
      Processor: 'Intel Core Ultra 9 185H',
      RAM: '32 GB LPDDR5',
      Storage: '1 TB NVMe PCIe 4.0',
      Graphics: 'Intel Arc Graphics 140V',
      Battery: '75 Wh',
      'Battery Life': 'Up to 18 hours',
      Ports: '2× Thunderbolt 4, 1× USB-A 3.2, HDMI 2.1',
      Keyboard: 'Backlit, 1.5mm travel',
      Weight: '1.19 kg',
      OS: 'Windows 11 Pro',
      Dimensions: '313 × 220 × 13.8 mm'
    },
    variants: [{ name: 'Storage', options: ['512 GB', '1 TB'] }]
  },
  {
    id: 3,
    name: 'SoundSync Buds Air',
    description:
      'True wireless earbuds with advanced Active Noise Cancellation, high-fidelity audio drivers, crystal-clear call quality, and IPX7 rating.',
    price: 129.99,
    sku: 'SS-BAIR-ANC',
    active: true,
    brand: 'SoundSync',
    category: 'Audio',
    stock: 25,
    imageUrl:
      'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400&auto=format&fit=crop&q=80',
    images: [
      'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1606220588913-b3aacb4d2f37?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1608156639585-b3a032ef9689?w=400&auto=format&fit=crop&q=80'
    ],
    rating: 4.2,
    reviewsCount: 45,
    features: ['ANC', 'Wireless', 'Waterproof', 'Touch Control'],
    specs: {
      'Driver Size': '11mm Dynamic Driver',
      'Frequency Response': '20Hz – 20kHz',
      ANC: 'Hybrid Active Noise Cancellation (−40dB)',
      Connectivity: 'Bluetooth 5.3 Multipoint',
      Codec: 'LDAC, AAC, SBC',
      'Earbud Battery': '8 hours (ANC on)',
      'Case Battery': '32 hours additional',
      Charging: 'USB-C + Wireless Qi',
      'Water Resistance': 'IPX7',
      Microphones: '3-mic array per earbud',
      Controls: 'Capacitive touch + voice control',
      Weight: '5.4g per earbud'
    },
    variants: [{ name: 'Color', options: ['Charcoal Black', 'Pearl White'] }]
  },
  {
    id: 4,
    name: 'FitStep Active',
    description:
      'Rugged smart fitness tracker with continuous heart rate monitoring, GPS tracking, sleep coaching, and a bright sunlight-readable touch display.',
    price: 199.99,
    sku: 'FS-ACT-WTC',
    active: true,
    brand: 'FitStep',
    category: 'Accessories',
    stock: 0,
    imageUrl:
      'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=400&auto=format&fit=crop&q=80',
    images: [
      'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1610945264803-c22b62d2a7b3?w=400&auto=format&fit=crop&q=80'
    ],
    rating: 4.0,
    reviewsCount: 12,
    features: ['Waterproof', 'Wireless', 'Touch Control'],
    specs: {
      Display: '1.45" AMOLED MIP, 450 nits',
      GPS: 'Multi-band GPS + GLONASS + Galileo',
      'Heart Rate': 'Optical HR, SpO2, Skin Temp',
      'Battery Life': 'Up to 14 days standard',
      'GPS Battery': 'Up to 20 hours',
      Connectivity: 'Bluetooth 5.0, ANT+',
      'Water Resistance': '5 ATM (50m)',
      Sensors: 'Accelerometer, Gyroscope, Altimeter',
      Storage: '32 MB onboard activity storage',
      Strap: '22mm quick-release silicone',
      Compatibility: 'iOS 14+ / Android 9+',
      Weight: '42g with strap'
    },
    variants: [
      { name: 'Strap Color', options: ['Graphite', 'Amber', 'Emerald'] }
    ]
  },
  {
    id: 5,
    name: 'Beexel Tab S',
    description:
      'Versatile 11-inch productivity tablet with high-accuracy stylus support, long-lasting battery, and robust security integrations.',
    price: 649.99,
    sku: 'BX-TABS-W',
    active: true,
    brand: 'Beexel',
    category: 'Electronics',
    stock: 7,
    imageUrl:
      'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400&auto=format&fit=crop&q=80',
    images: [
      'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1561154464-82e9adf32764?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1589739900243-4b52cd9b104e?w=400&auto=format&fit=crop&q=80'
    ],
    rating: 4.4,
    reviewsCount: 22,
    features: ['Wireless', 'Touch Control'],
    specs: {
      Display: '11" IPS LCD, 2560×1600, 120Hz',
      Processor: 'Beexel X1 Octa-core 2.8GHz',
      RAM: '8 GB',
      Storage: '128 GB UFS 3.1 (microSD up to 1TB)',
      'Rear Camera': '13MP AF',
      'Front Camera': '8MP',
      Battery: '8000 mAh',
      Charging: '45W USB-C',
      Stylus: 'BeexelPen 2 (sold separately)',
      Connectivity: 'Wi-Fi 6E, Bluetooth 5.3',
      OS: 'BeexelOS Tab 14',
      Weight: '499g'
    },
    variants: [{ name: 'Color', options: ['Graphite', 'Silver'] }]
  },
  {
    id: 6,
    name: 'SoundSync Wave',
    description:
      'Premium over-ear wireless headphones delivering master-quality audio with hybrid ANC, custom EQ, and plush memory foam earcups.',
    price: 249.99,
    sku: 'SS-WAVE-ANC',
    active: true,
    brand: 'SoundSync',
    category: 'Audio',
    stock: 2,
    imageUrl:
      'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&auto=format&fit=crop&q=80',
    images: [
      'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1487215078519-e21cc028cb29?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1524678606370-a47ad25cb82a?w=400&auto=format&fit=crop&q=80'
    ],
    rating: 4.7,
    reviewsCount: 31,
    features: ['ANC', 'Wireless', 'Touch Control'],
    specs: {
      'Driver Size': '40mm Planar Magnetic',
      'Frequency Response': '4Hz – 40kHz',
      ANC: 'Hybrid ANC (−45dB)',
      Connectivity: 'Bluetooth 5.2, USB-C audio, 3.5mm jack',
      Codec: 'LDAC, aptX HD, AAC, SBC',
      'Battery Life': '30 hours (ANC on), 50 hours (ANC off)',
      Charging: 'USB-C, 15-min charge = 3hr play',
      Microphones: '6-mic beamforming array',
      Controls: 'Touch pad + physical button',
      Foldable: 'Yes, with carry case included',
      Weight: '254g',
      Colors: 'Midnight Black, Pearl White, Sage Green'
    },
    variants: [
      {
        name: 'Color',
        options: ['Midnight Black', 'Pearl White', 'Sage Green']
      }
    ]
  },
  {
    id: 7,
    name: 'Aero Hub Pro',
    description:
      'High-performance 8-in-1 USB-C docking station featuring dual 4K HDMI ports, high-speed power delivery, and robust card readers.',
    price: 79.99,
    sku: 'AR-HUB8-P',
    active: true,
    brand: 'Aero',
    category: 'Accessories',
    stock: 15,
    imageUrl:
      'https://images.unsplash.com/photo-1468495244123-6c6c332eeece?w=400&auto=format&fit=crop&q=80',
    images: [
      'https://images.unsplash.com/photo-1468495244123-6c6c332eeece?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1625895197185-efcec01cffe0?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=400&auto=format&fit=crop&q=80'
    ],
    rating: 4.6,
    reviewsCount: 50,
    features: ['Wireless'],
    specs: {
      Ports: '8-in-1 (2× HDMI, 3× USB-A, 1× USB-C, SD, microSD)',
      'Video Output': 'Dual 4K@60Hz HDMI',
      'USB-A Speed': 'USB 3.2 Gen 1 (5Gbps)',
      'USB-C (Pass-through)': '100W Power Delivery',
      'Card Reader': 'UHS-I SD + microSD',
      'Cable Length': '20cm integrated USB-C',
      'Host Interface': 'USB-C 3.2 Gen 2 (10Gbps)',
      'Max Resolution': '3840 × 2160 @ 60Hz',
      Build: 'Aluminum alloy shell',
      Dimensions: '110 × 43 × 14 mm',
      Weight: '98g',
      Compatibility: 'USB-C devices, Thunderbolt 3/4'
    },
    variants: [{ name: 'Color', options: ['Silver', 'Space Grey'] }]
  },
  {
    id: 8,
    name: 'Aero Stand',
    description:
      'Ergonomic brushed aluminum laptop elevator designed to optimize viewing height, heat dissipation, and desk workspace neatness.',
    price: 49.99,
    sku: 'AR-STAND-ER',
    active: true,
    brand: 'Aero',
    category: 'Office & Study',
    stock: 8,
    imageUrl:
      'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400&auto=format&fit=crop&q=80',
    images: [
      'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=400&auto=format&fit=crop&q=80',
      'https://images.unsplash.com/photo-1547394765-185e1e68f34e?w=400&auto=format&fit=crop&q=80'
    ],
    rating: 4.5,
    reviewsCount: 14,
    features: [],
    specs: {
      Material: 'Brushed 6061 Aluminum Alloy',
      Compatibility: 'Laptops 10"–17"',
      'Viewing Angle': 'Fixed 15° ergonomic elevation',
      'Height Gain': '+15.2 cm above desk surface',
      'Anti-Slip': 'Silicone base + top grip pads',
      'Max Load': '15 kg',
      Foldable: 'Yes, flat-pack for travel',
      'Cable Pass': 'Built-in rear cable channel',
      'Dimensions (open)': '270 × 225 × 152 mm',
      'Dimensions (folded)': '270 × 225 × 22 mm',
      Weight: '680g',
      Colors: 'Silver, Space Grey'
    },
    variants: [{ name: 'Color', options: ['Silver', 'Space Grey'] }]
  }
]

export const vouchersList: Voucher[] = [
  {
    id: 'v1',
    code: 'BEEWELCOME10',
    title: 'Shopbee Welcome Discount',
    description: 'Enjoy 10% off your entire order. Welcome to Shopbee!',
    type: 'platform',
    discountType: 'percentage',
    discountValue: 10,
    minSubtotal: 50,
    collected: false,
    colorClass: 'from-indigo-600 via-indigo-700 to-indigo-850',
    expiryDate: '2026-12-31'
  },
  {
    id: 'v2',
    code: 'BEESUPER50',
    title: 'Shopbee Super Saver',
    description: 'Get $50 off when you spend $300 or more platform-wide.',
    type: 'platform',
    discountType: 'fixed',
    discountValue: 50,
    minSubtotal: 300,
    collected: false,
    colorClass: 'from-violet-600 via-violet-700 to-violet-850',
    expiryDate: '2026-09-30'
  },
  {
    id: 'v3',
    code: 'AEROPRO30',
    title: 'Aero Brand Exclusive',
    description:
      '15% discount on all Aero premium devices. Minimum spend $100.',
    type: 'merchant',
    discountType: 'percentage',
    discountValue: 15,
    minSubtotal: 100,
    merchantBrand: 'Aero',
    collected: false,
    colorClass: 'from-cyan-600 via-sky-750 to-blue-800',
    expiryDate: '2026-08-31'
  },
  {
    id: 'v4',
    code: 'BEEXEL50',
    title: 'Beexel Store Voucher',
    description:
      'Save $50 on any Beexel product with order subtotal over $200.',
    type: 'merchant',
    discountType: 'fixed',
    discountValue: 50,
    minSubtotal: 200,
    merchantBrand: 'Beexel',
    collected: false,
    colorClass: 'from-emerald-600 via-teal-700 to-teal-850',
    expiryDate: '2026-07-31'
  },
  {
    id: 'v5',
    code: 'SOUNDSYNC20',
    title: 'SoundSync Audio Promo',
    description: 'Get 20% off high-fidelity SoundSync audio accessories.',
    type: 'merchant',
    discountType: 'percentage',
    discountValue: 20,
    minSubtotal: 0,
    merchantBrand: 'SoundSync',
    collected: false,
    colorClass: 'from-rose-600 via-pink-700 to-pink-850',
    expiryDate: '2026-10-31'
  },
  {
    id: 'v6',
    code: 'GAMETIME25',
    title: 'E-Sports Event Reward',
    description:
      '$25 off anything on Shopbee during the Gaming Event. Min spend $150.',
    type: 'event',
    discountType: 'fixed',
    discountValue: 25,
    minSubtotal: 150,
    collected: false,
    colorClass: 'from-amber-500 via-orange-600 to-red-750',
    expiryDate: '2026-06-30'
  },
  {
    id: 'v7',
    code: 'SUMMERSALE15',
    title: 'Summer Solstice Voucher',
    description:
      'Celebrate summer with a 15% site-wide discount. Min spend $120.',
    type: 'event',
    discountType: 'percentage',
    discountValue: 15,
    minSubtotal: 120,
    collected: false,
    colorClass: 'from-yellow-500 via-orange-500 to-pink-600',
    expiryDate: '2026-07-15'
  },
  {
    id: 'v8',
    code: 'LUCKYDRAW99',
    title: 'Golden Ticket Winner',
    description:
      '99% off your cart (maximum discount value of $100). Min spend $100.',
    type: 'event',
    discountType: 'percentage',
    discountValue: 99,
    minSubtotal: 100,
    collected: false,
    colorClass: 'from-yellow-400 via-amber-500 to-yellow-600',
    expiryDate: '2026-06-20'
  }
]

export const initialReviews: Review[] = [
  {
    id: 1,
    productId: 1,
    author: 'Sarah Jenkins',
    rating: 5,
    title: 'Absolutely stunning phone',
    comment:
      'Exceeded my expectations completely. The AMOLED display is breathtaking, battery life is outstanding, and the camera system rivals any flagship. Build quality feels genuinely premium.',
    date: '2026-05-14',
    verified: true
  },
  {
    id: 2,
    productId: 1,
    author: 'Michael Chen',
    rating: 4,
    title: 'Solid upgrade from last gen',
    comment:
      'Very solid performance leap from my previous device. Setup was seamless and the camera AI processing is impressively fast. Docked one star only because the charger is sold separately.',
    date: '2026-04-28',
    verified: true
  },
  {
    id: 3,
    productId: 1,
    author: 'Priya Nair',
    rating: 5,
    title: 'Best smartphone I have ever owned',
    comment:
      'The 5G speeds are incredible and I love the waterproofing. Dropped it in a puddle and it survived without a scratch. Highly recommend for power users.',
    date: '2026-03-10',
    verified: false
  },
  {
    id: 4,
    productId: 2,
    author: 'James Whitfield',
    rating: 5,
    title: 'Perfect ultrabook',
    comment:
      'This is genuinely the best laptop I have ever owned. The OLED screen reproduces colors beautifully and the battery actually lasts a full workday. Incredibly light for the performance on tap.',
    date: '2026-05-02',
    verified: true
  },
  {
    id: 5,
    productId: 2,
    author: 'Lena Hoffmann',
    rating: 5,
    title: 'Worth every cent',
    comment:
      'Runs demanding creative software with ease and stays cool. The keyboard travel is excellent and the trackpad is buttery smooth. Premium build with zero flex.',
    date: '2026-04-19',
    verified: true
  },
  {
    id: 6,
    productId: 3,
    author: 'Tom Reeves',
    rating: 4,
    title: 'Great ANC for the price',
    comment:
      'ANC is impressively effective on public transit. Call quality is crystal clear even in busy environments. Fit and comfort is excellent for long listening sessions.',
    date: '2026-05-21',
    verified: true
  },
  {
    id: 7,
    productId: 3,
    author: 'Yui Tanaka',
    rating: 3,
    title: 'Good but bass is light',
    comment:
      'I love the compact case and the multipoint connection is super convenient. However the bass response feels thin for my taste. Overall still a solid product at this price.',
    date: '2026-04-05',
    verified: true
  },
  {
    id: 8,
    productId: 3,
    author: 'Carlos Mendez',
    rating: 5,
    title: 'Incredible sound quality',
    comment:
      'LDAC codec with a good DAP is absolutely mind-blowing. These earbuds punch way above their class. IPX7 rating gives peace of mind at the gym too.',
    date: '2026-03-29',
    verified: false
  },
  {
    id: 9,
    productId: 4,
    author: 'Amara Osei',
    rating: 4,
    title: 'Excellent fitness companion',
    comment:
      'GPS lock is very fast and the heart rate tracking is accurate compared to my chest strap. Sleep coaching feature has genuinely improved my rest. Battery life is impressive for GPS-heavy users.',
    date: '2026-05-08',
    verified: true
  },
  {
    id: 10,
    productId: 4,
    author: 'David Park',
    rating: 4,
    title: 'Sturdy and reliable',
    comment:
      'Survived multiple trail runs and one accidental swim without issues. The sunlight-readable display is excellent outdoors. App integration could be improved but overall a great tracker.',
    date: '2026-04-16',
    verified: true
  },
  {
    id: 11,
    productId: 5,
    author: 'Ingrid Svensson',
    rating: 5,
    title: 'My go-to creative device',
    comment:
      'Paired with the BeexelPen 2 this tablet is a dream for digital sketching and note-taking. Display is vivid and the stylus latency is near zero. Highly productive device.',
    date: '2026-05-18',
    verified: true
  },
  {
    id: 12,
    productId: 5,
    author: 'Omar Hassan',
    rating: 4,
    title: 'Great tablet for the price',
    comment:
      'The 120Hz display makes scrolling and drawing feel very responsive. Battery lasts two full days of moderate use. The pen is sold separately which bumps the total cost.',
    date: '2026-04-30',
    verified: true
  },
  {
    id: 13,
    productId: 5,
    author: 'Mei Lin',
    rating: 4,
    title: 'Solid all-rounder',
    comment:
      'Good performance for study apps, streaming, and light creative work. MicroSD expansion is a big plus. I would love more RAM but for the price it is excellent value.',
    date: '2026-03-22',
    verified: false
  },
  {
    id: 14,
    productId: 6,
    author: 'Felix Wagner',
    rating: 5,
    title: 'Audiophile level at consumer price',
    comment:
      'The planar magnetic drivers produce an incredibly detailed and airy soundstage. ANC is among the best I have tested. Comfort over long sessions is exceptional thanks to the memory foam pads.',
    date: '2026-05-25',
    verified: true
  },
  {
    id: 15,
    productId: 6,
    author: 'Nina Johansson',
    rating: 5,
    title: 'Replaced my studio headphones',
    comment:
      'I bought these for travel but ended up using them in the studio. Frequency extension down to 4Hz is audible on well-recorded content. Build feels tank-like yet elegant.',
    date: '2026-04-14',
    verified: true
  },
  {
    id: 16,
    productId: 6,
    author: 'Ryan Kowalski',
    rating: 4,
    title: 'Excellent but heavy',
    comment:
      'Sound quality is genuinely reference-grade and the ANC is phenomenal. My only gripe is the weight during long walks. For desk listening or flights they are absolutely perfect.',
    date: '2026-03-07',
    verified: true
  },
  {
    id: 17,
    productId: 7,
    author: 'Sophie Laurent',
    rating: 5,
    title: 'Transformed my desk setup',
    comment:
      'Running dual 4K monitors and charging my laptop simultaneously without any issues. Plug and play on both Mac and Windows. The aluminium build looks stunning next to any premium laptop.',
    date: '2026-05-30',
    verified: true
  },
  {
    id: 18,
    productId: 7,
    author: 'Arjun Sharma',
    rating: 4,
    title: 'Reliable every day driver',
    comment:
      'All 8 ports work exactly as advertised. The SD card slot reads at full UHS-I speed. Only minor complaint is the cable could be a touch longer but everything else is flawless.',
    date: '2026-04-22',
    verified: true
  },
  {
    id: 19,
    productId: 7,
    author: 'Brooke Nielsen',
    rating: 5,
    title: 'Best hub I have tried',
    comment:
      'Previously tried three cheaper hubs that all had dropouts. This one has been rock solid for six weeks. Power delivery keeps my laptop topped up even during intensive video rendering.',
    date: '2026-03-15',
    verified: false
  },
  {
    id: 20,
    productId: 8,
    author: 'Hannah Becker',
    rating: 5,
    title: 'Neck saver, worth every penny',
    comment:
      'After one week my neck ache is gone. The stand is stable, looks gorgeous, and folds flat for my bag. Anti-slip pads keep my laptop perfectly in place even on a glass desk.',
    date: '2026-05-11',
    verified: true
  },
  {
    id: 21,
    productId: 8,
    author: 'Daniel Okonkwo',
    rating: 4,
    title: 'Quality build, great ergonomics',
    comment:
      'Brushed aluminium finish is beautiful and the angle is spot on for my seated eye level. Would love an adjustable height option but at this price the fixed angle works wonderfully.',
    date: '2026-04-03',
    verified: true
  }
]

export const initialAccounts: UserAccount[] = [
  {
    id: 'user-1',
    balance: 1500.0,
    profile: {
      name: 'Hannah Becker',
      email: 'hannah@example.com',
      phone: '+1 (555) 019-2834'
    },
    addresses: [
      {
        id: 'addr-1-1',
        name: 'Hannah Becker',
        address: '128 Magnolia Dr',
        city: 'Cupertino',
        zip: '95014',
        type: 'home',
        isDefault: true
      },
      {
        id: 'addr-1-2',
        name: 'Hannah Becker',
        address: '1 Infinite Loop, Ste 100',
        city: 'Cupertino',
        zip: '95014',
        type: 'office',
        isDefault: false
      }
    ],
    paymentMethods: [
      {
        id: 'pay-1-1',
        type: 'credit_card',
        cardDetails: {
          cardNumber: '4111 2222 3333 4444',
          cardName: 'Hannah Becker',
          expiry: '12/29',
          cvc: '123'
        },
        isDefault: true
      },
      {
        id: 'pay-1-2',
        type: 'shopbee_pay',
        isDefault: false
      }
    ]
  },
  {
    id: 'user-2',
    balance: 500.0,
    profile: {
      name: 'Liam Thompson',
      email: 'liam@example.com',
      phone: '+1 (555) 234-5678'
    },
    addresses: [
      {
        id: 'addr-2-1',
        name: 'Liam Thompson',
        address: '104 Applewood Dr',
        city: 'Cupertino',
        zip: '95014',
        type: 'office',
        isDefault: true
      },
      {
        id: 'addr-2-2',
        name: 'Liam Thompson',
        address: '456 Pine Ave',
        city: 'San Jose',
        zip: '95112',
        type: 'home',
        isDefault: false
      }
    ],
    paymentMethods: [
      {
        id: 'pay-2-1',
        type: 'shopbee_pay',
        isDefault: true
      },
      {
        id: 'pay-2-2',
        type: 'credit_card',
        cardDetails: {
          cardNumber: '5222 3333 4444 5555',
          cardName: 'Liam Thompson',
          expiry: '08/28',
          cvc: '456'
        },
        isDefault: false
      }
    ]
  },
  {
    id: 'user-3',
    balance: 50.0,
    profile: {
      name: 'Sarah Jenkins',
      email: 'sarah@example.com',
      phone: '+1 (555) 876-5432'
    },
    addresses: [
      {
        id: 'addr-3-1',
        name: 'Sarah Jenkins',
        address: '742 Evergreen Terrace',
        city: 'Springfield',
        zip: '97477',
        type: 'home',
        isDefault: true
      }
    ],
    paymentMethods: [
      {
        id: 'pay-3-1',
        type: 'cod',
        isDefault: true
      },
      {
        id: 'pay-3-2',
        type: 'credit_card',
        cardDetails: {
          cardNumber: '4222 3333 4444 6666',
          cardName: 'Sarah Jenkins',
          expiry: '11/27',
          cvc: '789'
        },
        isDefault: false
      }
    ]
  }
]

export const initialOrders: Order[] = [
  {
    orderNumber: 'SB-2026-104928',
    datePlaced: '2026-06-12',
    status: 'processing',
    shippingAddress: {
      name: 'Hannah Becker',
      email: 'hannah@example.com',
      address: '128 Magnolia Dr',
      city: 'Cupertino',
      zip: '95014'
    },
    items: [
      {
        productId: 2,
        name: 'AeroBook 14',
        price: 1249.0,
        quantity: 1,
        selectedVariants: { Storage: '1 TB' },
        imageUrl:
          'https://images.unsplash.com/photo-1496181130204-755241524eab?w=400&auto=format&fit=crop&q=80'
      }
    ],
    subtotal: 1249.0,
    shippingFee: 0,
    tax: 99.92,
    discount: 0,
    totalPaid: 1348.92,
    paymentMethod: 'credit_card',
    carrier: 'FedEx',
    trackingNumber: 'FX-820491823',
    timeline: [
      {
        status: 'placed',
        title: 'Order Placed',
        description:
          'Thank you for your purchase. We have received your order details.',
        timestamp: '2026-06-12 14:30'
      },
      {
        status: 'processing',
        title: 'Payment Confirmed & Processing',
        description:
          'Your payment was approved and the warehouse is preparing your packaging details.',
        timestamp: '2026-06-12 16:00'
      }
    ]
  },
  {
    orderNumber: 'SB-2026-491820',
    datePlaced: '2026-06-11',
    status: 'shipped',
    shippingAddress: {
      name: 'Liam Thompson',
      email: 'liam@example.com',
      address: '104 Applewood Dr',
      city: 'Cupertino',
      zip: '95014'
    },
    items: [
      {
        productId: 3,
        name: 'SoundSync Buds Air',
        price: 129.99,
        quantity: 2,
        selectedVariants: { Color: 'Charcoal Black' },
        imageUrl:
          'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400&auto=format&fit=crop&q=80'
      },
      {
        productId: 8,
        name: 'Aero Stand',
        price: 49.99,
        quantity: 1,
        selectedVariants: { Color: 'Space Grey' },
        imageUrl:
          'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400&auto=format&fit=crop&q=80'
      }
    ],
    subtotal: 309.97,
    shippingFee: 0,
    tax: 24.8,
    discount: 0,
    totalPaid: 334.77,
    paymentMethod: 'credit_card',
    carrier: 'DHL Express',
    trackingNumber: 'DHL-9081234567',
    timeline: [
      {
        status: 'placed',
        title: 'Order Placed',
        description:
          'Thank you for your purchase. We have received your order details.',
        timestamp: '2026-06-11 09:15'
      },
      {
        status: 'processing',
        title: 'Payment Confirmed',
        description: 'Payment verified successfully.',
        timestamp: '2026-06-11 09:20'
      },
      {
        status: 'processing',
        title: 'Package Prepared',
        description:
          'Order items packed and handed over to transit dispatch team.',
        timestamp: '2026-06-11 15:30'
      },
      {
        status: 'shipped',
        title: 'Dispatched from Warehouse',
        description:
          'Shipped from warehouse via DHL Express. Transit code: DHL-9081234567.',
        timestamp: '2026-06-12 08:00'
      },
      {
        status: 'shipped',
        title: 'In Transit',
        description: 'Arrived at sorting facility: San Jose Hub, CA.',
        timestamp: '2026-06-12 15:45'
      }
    ]
  },
  {
    orderNumber: 'SB-2026-820491',
    datePlaced: '2026-06-09',
    status: 'delivered',
    shippingAddress: {
      name: 'Sarah Jenkins',
      email: 'sarah@example.com',
      address: '742 Evergreen Terrace',
      city: 'Springfield',
      zip: '97477'
    },
    items: [
      {
        productId: 1,
        name: 'Beexel 9 Pro',
        price: 899.0,
        quantity: 1,
        selectedVariants: { Color: 'Obsidian Black', Storage: '256 GB' },
        imageUrl:
          'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&auto=format&fit=crop&q=80'
      }
    ],
    subtotal: 899.0,
    shippingFee: 0,
    tax: 71.92,
    discount: 0,
    totalPaid: 970.92,
    paymentMethod: 'credit_card',
    carrier: 'UPS',
    trackingNumber: '1Z999AA10123456784',
    timeline: [
      {
        status: 'placed',
        title: 'Order Placed',
        description:
          'Thank you for your purchase. We have received your order details.',
        timestamp: '2026-06-09 10:10'
      },
      {
        status: 'processing',
        title: 'Payment Confirmed',
        description: 'Payment verified successfully.',
        timestamp: '2026-06-09 10:15'
      },
      {
        status: 'shipped',
        title: 'Dispatched from Warehouse',
        description:
          'Shipped from warehouse via UPS. Tracking code: 1Z999AA10123456784.',
        timestamp: '2026-06-10 08:30'
      },
      {
        status: 'out_for_delivery',
        title: 'Out for Delivery',
        description:
          'Your courier has loaded package for local deliveries today.',
        timestamp: '2026-06-11 09:00'
      },
      {
        status: 'delivered',
        title: 'Delivered',
        description:
          'Delivered successfully. Left on front porch. Signed by recipient.',
        timestamp: '2026-06-11 14:12'
      }
    ]
  }
]

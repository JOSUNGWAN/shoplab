export type DashboardSummary = {
  todaySalesAmount: number
  todayOrderCount: number
  yesterdaySalesAmount: number
  paidCount: number
  preparingCount: number
  shippingCount: number
  soldOutProductCount: number
  lowStockProductCount: number
  lowStockThreshold: number
}

export type DailySales = {
  date: string // yyyy-MM-dd
  orderCount: number
  salesAmount: number
}

export type TopProduct = {
  rank: number
  productId: number
  productName: string
  quantity: number
  salesAmount: number
}

export type DashboardPeriod = {
  from: string
  to: string
}
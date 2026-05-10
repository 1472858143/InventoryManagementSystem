<template>
  <div class="dashboard">
    <div class="page-title">
      <h2>首页概览</h2>
      <p>欢迎使用超市库存管理系统</p>
    </div>

    <el-row :gutter="16">
      <el-col v-for="card in statCards" :key="card.key" :xs="24" :sm="12" :xl="6">
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon-wrap" :style="{ background: card.color }">
            <el-icon size="26" color="#ffffff"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ loading ? '—' : card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>近 7 天入库趋势</template>
          <div ref="inboundChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>近 7 天出库趋势</template>
          <div ref="outboundChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { Download, Goods, Upload, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { getProducts } from '../../api/product'
import { getInbounds } from '../../api/inbound'
import { getOutbounds } from '../../api/outbound'
import { getStocks } from '../../api/stock'

const loading = ref(true)
const stats = reactive({ productCount: 0, warningCount: 0, inboundCount: 0, outboundCount: 0 })

const statCards = computed(() => [
  { key: 'product', label: '商品总数', value: stats.productCount, icon: Goods, color: 'var(--color-primary)' },
  { key: 'warning', label: '库存预警', value: stats.warningCount, icon: Warning, color: 'var(--color-danger)' },
  { key: 'inbound', label: '入库总笔数', value: stats.inboundCount, icon: Download, color: 'var(--color-success)' },
  { key: 'outbound', label: '出库总笔数', value: stats.outboundCount, icon: Upload, color: 'var(--color-warning)' },
])

const inboundChartRef = ref(null)
const outboundChartRef = ref(null)
let inboundChart = null
let outboundChart = null

function getLast7Days() {
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - (6 - i))
    return d.toISOString().slice(0, 10)
  })
}

function aggregateByDay(records, dateField) {
  const days = getLast7Days()
  const counts = Object.fromEntries(days.map(d => [d, 0]))
  for (const r of records) {
    const day = String(r[dateField] || '').slice(0, 10)
    if (day in counts) counts[day]++
  }
  return { days, values: days.map(d => counts[d]) }
}

function makeLineOption(data, color) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 16, bottom: 32 },
    xAxis: {
      type: 'category',
      data: data.days.map(d => d.slice(5)),
      axisLabel: { fontSize: 11 },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '笔数',
      type: 'line',
      data: data.values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: { color },
      areaStyle: { color, opacity: 0.08 },
    }],
  }
}

function handleResize() {
  inboundChart?.resize()
  outboundChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  try {
    const [products, stocks, inbounds, outbounds] = await Promise.all([
      getProducts(),
      getStocks(),
      getInbounds(),
      getOutbounds(),
    ])
    stats.productCount = Array.isArray(products) ? products.length : 0
    const stockArr = Array.isArray(stocks) ? stocks : []
    stats.warningCount = stockArr.filter(s => s.shelfStatus === '缺货' || s.quantity < s.minStock).length
    stats.inboundCount = Array.isArray(inbounds) ? inbounds.length : 0
    stats.outboundCount = Array.isArray(outbounds) ? outbounds.length : 0

    const rootStyle = getComputedStyle(document.documentElement)
    const primaryColor = rootStyle.getPropertyValue('--color-primary').trim() || '#6366F1'
    const successColor = rootStyle.getPropertyValue('--color-success').trim() || '#22C55E'

    inboundChart = echarts.init(inboundChartRef.value)
    inboundChart.setOption(makeLineOption(aggregateByDay(inbounds || [], 'createTime'), primaryColor))

    outboundChart = echarts.init(outboundChartRef.value)
    outboundChart.setOption(makeLineOption(aggregateByDay(outbounds || [], 'createTime'), successColor))
  } catch (e) {
    ElMessage.error(e.message || '加载数据失败')
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  inboundChart?.dispose()
  outboundChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-title h2 {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.page-title p {
  font-size: 14px;
  color: #8c8c8c;
}

.stat-card {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-card);
  margin-bottom: 16px;
  min-height: 90px;
}

.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body { flex: 1; }

.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.1;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 4px;
}

.chart-box { height: 260px; }
</style>

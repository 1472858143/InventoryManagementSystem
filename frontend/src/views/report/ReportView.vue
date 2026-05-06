<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>报表统计</h2>
        <p>库存分布与入出库趋势图表（仅供查看）</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
    </div>

    <div v-loading="loading" class="charts-wrap">
      <el-card>
        <template #header>库存总览（当前库存量，红色表示低于下限）</template>
        <div ref="stockChartRef" class="chart-tall"></div>
      </el-card>

      <el-row :gutter="16">
        <el-col :xs="24" :md="12">
          <el-card>
            <template #header>近 30 天入库趋势</template>
            <div ref="inboundChartRef" class="chart-normal"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-card>
            <template #header>近 30 天出库趋势</template>
            <div ref="outboundChartRef" class="chart-normal"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { onMounted, onUnmounted, ref } from 'vue'
import { getInbounds } from '../../api/inbound'
import { getOutbounds } from '../../api/outbound'
import { getStocks } from '../../api/stock'

const loading = ref(false)
const stockChartRef = ref(null)
const inboundChartRef = ref(null)
const outboundChartRef = ref(null)
let stockChart = null
let inboundChart = null
let outboundChart = null

function getLast30Days() {
  return Array.from({ length: 30 }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - (29 - i))
    return d.toISOString().slice(0, 10)
  })
}

function aggregateByDay(records, dateField) {
  const days = getLast30Days()
  const counts = Object.fromEntries(days.map(d => [d, 0]))
  for (const r of records) {
    const day = String(r[dateField] || '').slice(0, 10)
    if (day in counts) counts[day]++
  }
  return { days, values: days.map(d => counts[d]) }
}

function renderStockChart(stocks) {
  const sorted = [...stocks].sort((a, b) => b.quantity - a.quantity).slice(0, 20)
  stockChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '22%', right: '5%', top: 16, bottom: 16 },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: sorted.map(s => s.productName),
      axisLabel: { fontSize: 11, width: 120, overflow: 'truncate' },
    },
    series: [{
      name: '库存量',
      type: 'bar',
      data: sorted.map(s => ({
        value: s.quantity,
        itemStyle: { color: s.quantity < s.minStock ? '#ff4d4f' : '#1890ff' },
      })),
    }],
  }, true)
}

function makeTrendOption(data, color) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 16, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data.days.map(d => d.slice(5)),
      axisLabel: { rotate: 45, fontSize: 10 },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '笔数',
      type: 'line',
      data: data.values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      itemStyle: { color },
      areaStyle: { color, opacity: 0.08 },
    }],
  }
}

async function loadData() {
  loading.value = true
  try {
    const [stocks, inbounds, outbounds] = await Promise.all([getStocks(), getInbounds(), getOutbounds()])

    if (!stockChart) stockChart = echarts.init(stockChartRef.value)
    if (!inboundChart) inboundChart = echarts.init(inboundChartRef.value)
    if (!outboundChart) outboundChart = echarts.init(outboundChartRef.value)

    renderStockChart(Array.isArray(stocks) ? stocks : [])
    inboundChart.setOption(makeTrendOption(aggregateByDay(inbounds || [], 'createTime'), '#52c41a'), true)
    outboundChart.setOption(makeTrendOption(aggregateByDay(outbounds || [], 'createTime'), '#fa8c16'), true)
  } catch (e) {
    ElMessage.error(e.message || '加载报表数据失败')
  } finally {
    loading.value = false
  }
}

function handleResize() {
  stockChart?.resize()
  inboundChart?.resize()
  outboundChart?.resize()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  stockChart?.dispose()
  inboundChart?.dispose()
  outboundChart?.dispose()
})
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
.charts-wrap { display: flex; flex-direction: column; gap: 16px; }
.chart-tall { height: 360px; }
.chart-normal { height: 260px; }
</style>

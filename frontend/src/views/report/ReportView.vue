<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>报表分析</h2>
        <p>多维度商业智能仪表盘</p>
      </div>
      <el-button size="small" :loading="loading" @click="loadAll">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab" type="card" class="report-tabs" @tab-change="onTabChange">

      <!-- Tab 1: 库存健康度 -->
      <el-tab-pane label="库存健康度" name="health">
        <el-row :gutter="16" v-loading="loading">
          <el-col :span="12">
            <el-card shadow="never" class="chart-card">
              <template #header>陈列状态分布</template>
              <div ref="statusPieRef" class="chart-box"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" class="chart-card">
              <template #header>各分类库存数量</template>
              <div ref="categoryBarRef" class="chart-box"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- Tab 2: 销售分析 -->
      <el-tab-pane label="销售分析" name="sales">
        <el-row :gutter="16" v-loading="loading">
          <el-col :span="14">
            <el-card shadow="never" class="chart-card">
              <template #header>近 30 天出库 Top 10 商品</template>
              <div ref="hotspotBarRef" class="chart-box"></div>
            </el-card>
          </el-col>
          <el-col :span="10">
            <el-card shadow="never" class="chart-card">
              <template #header>近 30 天出库趋势</template>
              <div ref="outboundTrendRef" class="chart-box"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- Tab 3: 决策辅助 -->
      <el-tab-pane label="决策辅助" name="decision">
        <el-row :gutter="16" v-loading="loading">
          <el-col :span="12">
            <el-card shadow="never">
              <template #header>补货建议（低于下限或货架缺货）</template>
              <el-table :data="restockSuggestions" size="small" max-height="360">
                <el-table-column prop="productCode" label="编号" width="110" />
                <el-table-column prop="productName" label="商品" />
                <el-table-column prop="quantity" label="库存" width="70" align="right" />
                <el-table-column prop="minStock" label="下限" width="60" align="right" />
                <el-table-column label="陈列" width="80">
                  <template #default="{ row }">
                    <el-tag :type="statusType(row.shelfStatus)" size="small">{{ row.shelfStatus }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="!restockSuggestions.length" class="empty-tip">暂无需要补货的商品</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never">
              <template #header>滞销商品（近 30 天无出库）</template>
              <el-table :data="slowMoving" size="small" max-height="360">
                <el-table-column prop="productCode" label="编号" width="110" />
                <el-table-column prop="productName" label="商品" />
                <el-table-column prop="quantity" label="库存" width="80" align="right" />
              </el-table>
              <div v-if="!slowMoving.length" class="empty-tip">近 30 天无滞销商品</div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- Tab 4: 流转明细 -->
      <el-tab-pane label="流转明细" name="flow">
        <el-row :gutter="16" v-loading="loading">
          <el-col :span="12">
            <el-card shadow="never" class="chart-card">
              <template #header>近 30 天入库趋势</template>
              <div ref="inboundTrendRef" class="chart-box"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" class="chart-card">
              <template #header>近 30 天出库趋势（明细）</template>
              <div ref="outboundTrendRef2" class="chart-box"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import {
  getCategoryDistribution, getInboundTrend, getOutboundTrend,
  getRestockSuggestions, getSalesHotspot, getShelfStatusDistribution, getSlowMoving,
} from '../../api/report'

const activeTab = ref('health')
const loading = ref(false)

const restockSuggestions = ref([])
const slowMoving = ref([])

const statusPieRef = ref(null)
const categoryBarRef = ref(null)
const hotspotBarRef = ref(null)
const outboundTrendRef = ref(null)
const inboundTrendRef = ref(null)
const outboundTrendRef2 = ref(null)

let charts = []

function statusType(status) {
  return { '充足': 'success', '较少': 'warning', '缺货': 'danger', '未上架': 'info' }[status] || 'info'
}

function getLast30Days() {
  return Array.from({ length: 30 }, (_, i) => {
    const d = new Date(); d.setDate(d.getDate() - (29 - i))
    return d.toISOString().slice(0, 10)
  })
}

function mergeTrend(data) {
  const days = getLast30Days()
  const map = Object.fromEntries((data || []).map(i => [i.date, i.count]))
  return { days, values: days.map(d => map[d] || 0) }
}

function makeLineOption(data, color) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 16, bottom: 40 },
    xAxis: { type: 'category', data: data.days.map(d => d.slice(5)), axisLabel: { rotate: 45, fontSize: 10 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'line', data: data.values, smooth: true, symbol: 'circle', symbolSize: 5,
      itemStyle: { color }, areaStyle: { color, opacity: 0.08 } }],
  }
}

function initChart(el) {
  const c = echarts.init(el)
  charts.push(c)
  return c
}

async function loadAll() {
  loading.value = true
  try {
    const [statusDist, catDist, hotspot, outTrend, inTrend, restock, slow] = await Promise.all([
      getShelfStatusDistribution(),
      getCategoryDistribution(),
      getSalesHotspot(30, 10),
      getOutboundTrend(),
      getInboundTrend(),
      getRestockSuggestions(),
      getSlowMoving(30),
    ])

    restockSuggestions.value = Array.isArray(restock) ? restock : []
    slowMoving.value = Array.isArray(slow) ? slow : []

    await nextTick()

    if (statusPieRef.value) {
      const c = initChart(statusPieRef.value)
      const statusColors = { '充足': '#10B981', '较少': '#F59E0B', '缺货': '#EF4444', '未上架': '#94A3B8' }
      c.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie', radius: ['40%', '65%'],
          data: (Array.isArray(statusDist) ? statusDist : []).map(i => ({
            name: i.shelfStatus, value: i.count,
            itemStyle: { color: statusColors[i.shelfStatus] || '#ccc' },
          })),
        }],
      })
    }

    if (categoryBarRef.value) {
      const c = initChart(categoryBarRef.value)
      const cats = Array.isArray(catDist) ? catDist : []
      c.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '25%', right: '5%', top: 16, bottom: 16 },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: cats.map(c => c.categoryName),
          axisLabel: { fontSize: 11, width: 90, overflow: 'truncate' } },
        series: [{ type: 'bar', data: cats.map(c => c.totalQuantity),
          itemStyle: { color: '#6366F1' } }],
      })
    }

    if (hotspotBarRef.value) {
      const c = initChart(hotspotBarRef.value)
      const items = (Array.isArray(hotspot) ? hotspot : []).slice(0, 10).reverse()
      c.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '30%', right: '5%', top: 16, bottom: 16 },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: items.map(i => i.productName),
          axisLabel: { fontSize: 11, width: 110, overflow: 'truncate' } },
        series: [{ type: 'bar', data: items.map(i => i.totalOutbound),
          itemStyle: { color: '#10B981' } }],
      })
    }

    if (outboundTrendRef.value) {
      const c = initChart(outboundTrendRef.value)
      c.setOption(makeLineOption(mergeTrend(outTrend), '#F59E0B'))
    }

    if (inboundTrendRef.value) {
      const c = initChart(inboundTrendRef.value)
      c.setOption(makeLineOption(mergeTrend(inTrend), '#6366F1'))
    }
    if (outboundTrendRef2.value) {
      const c = initChart(outboundTrendRef2.value)
      c.setOption(makeLineOption(mergeTrend(outTrend), '#EF4444'))
    }

  } catch (e) {
    ElMessage.error(e.message || '加载报表数据失败')
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  nextTick(() => charts.forEach(c => c.resize()))
}

function handleResize() { charts.forEach(c => c.resize()) }

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadAll()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
  charts = []
})
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: var(--color-text); margin-bottom: 4px; }
.page-header p { font-size: 13px; color: var(--color-text-secondary); }
.report-tabs { --el-tabs-header-height: 38px; }
.chart-card { border: 1px solid var(--color-border); }
.chart-box { height: 300px; }
.empty-tip { text-align: center; color: var(--color-text-secondary); padding: 24px; font-size: 13px; }
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>{{ title }}</h2>
        <p>{{ description }}</p>
      </div>
      <div class="page-actions">
        <el-input-number v-model="operatorId" :min="1" :precision="0" controls-position="right" style="width: 128px" />
        <el-button :icon="RefreshCw" :loading="loading" @click="loadOrders">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="router.push({ name: createRouteName })">新建</el-button>
      </div>
    </div>

    <el-card>
      <OrderQueryBar v-model="filters" :subjects="subjects" :subject-label="subjectLabel" @search="handleSearch" />
    </el-card>

    <el-card>
      <el-table :data="orders" v-loading="loading" stripe row-key="id" empty-text="暂无单据">
        <el-table-column :prop="numberField" label="单据号" min-width="190" />
        <el-table-column :label="subjectLabel" min-width="150">
          <template #default="{ row }">{{ subjectName(row[subjectField]) }}</template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="数量" width="90" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">¥{{ amountText(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'NORMAL' ? 'success' : 'info'" size="small">
              {{ row.status === 'NORMAL' ? '正常' : '已作废' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
              link
              type="danger"
              :disabled="row.status !== 'NORMAL'"
              :loading="cancelingId === row.id"
              @click="cancel(row)"
            >作废</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-row">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @current-change="loadOrders"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <OrderDetailDrawer
      v-model="detailVisible"
      :order="detail"
      :products="products"
      :subjects="subjects"
      :title="`${title}详情`"
      :subject-label="subjectLabel"
      :subject-field="subjectField"
      :number-field="numberField"
      :source-field="sourceField"
      :remark-field="remarkField"
      :remark-label="remarkLabel"
    />
  </div>
</template>

<script setup>
import { Plus, RefreshCw } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authState } from '../../../stores/auth'
import OrderDetailDrawer from './OrderDetailDrawer.vue'
import OrderQueryBar from './OrderQueryBar.vue'

const props = defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
  subjectLabel: { type: String, required: true },
  subjectField: { type: String, required: true },
  subjectParamKey: { type: String, required: true },
  numberField: { type: String, required: true },
  sourceField: { type: String, default: '' },
  remarkField: { type: String, default: 'remark' },
  remarkLabel: { type: String, default: '备注' },
  createRouteName: { type: String, required: true },
  loadSubjects: { type: Function, required: true },
  loadProducts: { type: Function, required: true },
  listOrders: { type: Function, required: true },
  getDetail: { type: Function, required: true },
  cancelOrder: { type: Function, required: true },
})

const router = useRouter()
const subjects = ref([])
const products = ref([])
const filters = ref({})
const orders = ref([])
const detail = ref(null)
const detailVisible = ref(false)
const loading = ref(false)
const cancelingId = ref(null)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const operatorId = ref(Number(authState.currentUser?.userId) || 1)

onMounted(async () => {
  const [subjectResult, productResult] = await Promise.allSettled([props.loadSubjects(), props.loadProducts()])
  subjects.value = Array.isArray(subjectResult.value) ? subjectResult.value : []
  products.value = Array.isArray(productResult.value) ? productResult.value : []
  await loadOrders()
})

async function loadOrders() {
  loading.value = true
  try {
    const params = {
      keyword: filters.value.keyword,
      [props.subjectParamKey]: filters.value.subjectId,
      startDate: filters.value.startDate,
      endDate: filters.value.endDate,
      page: page.value,
      pageSize: pageSize.value,
    }
    const result = await props.listOrders(params)
    orders.value = Array.isArray(result?.items) ? result.items : []
    total.value = Number(result?.total || 0)
  } catch (e) {
    ElMessage.error(e.message || '查询失败')
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch(nextFilters) {
  filters.value = nextFilters
  page.value = 1
  loadOrders()
}

function handleSizeChange() {
  page.value = 1
  loadOrders()
}

async function openDetail(row) {
  try {
    detail.value = await props.getDetail(row.id)
    detailVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || '加载详情失败')
  }
}

async function cancel(row) {
  try {
    await ElMessageBox.confirm(`确认作废 ${row[props.numberField]}？`, '作废确认', { type: 'warning' })
  } catch {
    return
  }
  cancelingId.value = row.id
  try {
    await props.cancelOrder(row.id, operatorId.value)
    ElMessage.success('作废成功')
    await loadOrders()
  } catch (e) {
    ElMessage.error(e.message || '作废失败')
  } finally {
    cancelingId.value = null
  }
}

function subjectName(id) {
  return subjects.value.find(item => item.id === id)?.name || id || '-'
}

function formatTime(value) {
  return value ? String(value).slice(0, 19).replace('T', ' ') : '-'
}

function amountText(value) {
  return Number(value || 0).toFixed(2)
}
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
.page-actions { display: flex; gap: 8px; align-items: center; flex-shrink: 0; }
.pagination-row { display: flex; justify-content: flex-end; padding-top: 14px; }
</style>

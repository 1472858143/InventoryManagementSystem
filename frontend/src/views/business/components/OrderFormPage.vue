<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>{{ title }}</h2>
        <p>{{ description }}</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Back" @click="router.push({ name: successRouteName })">返回查询</el-button>
        <el-button type="primary" :icon="Check" :loading="submitting" @click="submit">保存</el-button>
      </div>
    </div>

    <el-card>
      <el-form label-width="86px" class="order-form">
        <el-row :gutter="16">
          <el-col :xs="24" :md="8">
            <el-form-item :label="subjectLabel" required>
              <el-select v-model="form.subjectId" :placeholder="subjectPlaceholder" filterable style="width:100%">
                <el-option v-for="subject in enabledSubjects" :key="subject.id" :label="subject.name" :value="subject.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="sourceOrderKey" :xs="24" :md="8">
            <el-form-item :label="sourceOrderLabel">
              <el-input-number v-model="form.sourceOrderId" :min="1" :precision="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="操作人" required>
              <el-input-number v-model="form.operatorId" :min="1" :precision="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="sourceOrderKey ? 24 : 8">
            <el-form-item :label="remarkLabel">
              <el-input v-model.trim="form.remark" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <OrderItemEditor v-model="items" :products="products" :price-field="priceField" />
      <OrderSummaryBar :items="items" />
    </el-card>
  </div>
</template>

<script setup>
import { Check, ChevronLeft as Back } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authState } from '../../../stores/auth'
import OrderItemEditor from './OrderItemEditor.vue'
import OrderSummaryBar from './OrderSummaryBar.vue'

const props = defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
  subjectLabel: { type: String, required: true },
  subjectPlaceholder: { type: String, default: '请选择' },
  subjectKey: { type: String, required: true },
  remarkKey: { type: String, default: 'remark' },
  remarkLabel: { type: String, default: '备注' },
  sourceOrderKey: { type: String, default: '' },
  sourceOrderLabel: { type: String, default: '来源单ID' },
  priceField: { type: String, default: 'purchasePrice' },
  successRouteName: { type: String, required: true },
  successMessage: { type: String, default: '保存成功' },
  loadSubjects: { type: Function, required: true },
  loadProducts: { type: Function, required: true },
  createOrder: { type: Function, required: true },
})

const router = useRouter()
const subjects = ref([])
const products = ref([])
const submitting = ref(false)
const form = reactive({
  subjectId: null,
  sourceOrderId: null,
  operatorId: Number(authState.currentUser?.userId) || 1,
  remark: '',
})
const items = ref([newItem()])

const enabledSubjects = computed(() => subjects.value.filter(subject => subject.status === 1))

onMounted(async () => {
  const [subjectResult, productResult] = await Promise.allSettled([props.loadSubjects(), props.loadProducts()])
  subjects.value = Array.isArray(subjectResult.value) ? subjectResult.value : []
  products.value = Array.isArray(productResult.value) ? productResult.value : []
})

function newItem() {
  return { rowKey: `${Date.now()}-${Math.random().toString(16).slice(2)}`, productId: null, quantity: 1, unitPrice: 0, remark: '' }
}

function hasDuplicateProduct() {
  const selected = items.value.map(item => item.productId).filter(Boolean)
  return new Set(selected).size !== selected.length
}

function validate() {
  if (!form.subjectId) return `${props.subjectLabel}不能为空`
  if (!form.operatorId) return '操作人不能为空'
  if (!items.value.length) return '请至少添加一条商品明细'
  if (hasDuplicateProduct()) return '同一张单据不能重复选择商品'
  const invalid = items.value.find(item => !item.productId || !item.quantity || item.quantity <= 0 || item.unitPrice === null || item.unitPrice === undefined || item.unitPrice < 0)
  return invalid ? '请完善商品、数量和单价' : ''
}

async function submit() {
  const error = validate()
  if (error) {
    ElMessage.warning(error)
    return
  }

  const payload = {
    [props.subjectKey]: form.subjectId,
    operatorId: form.operatorId,
    [props.remarkKey]: form.remark || undefined,
    items: items.value.map(item => ({
      productId: item.productId,
      quantity: item.quantity,
      unitPrice: item.unitPrice,
      remark: item.remark || undefined,
    })),
  }
  if (props.sourceOrderKey) payload[props.sourceOrderKey] = form.sourceOrderId || undefined

  submitting.value = true
  try {
    await props.createOrder(payload)
    ElMessage.success(props.successMessage)
    router.push({ name: props.successRouteName })
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
.page-actions { display: flex; gap: 8px; flex-shrink: 0; }
.order-form { margin-bottom: 10px; }
</style>

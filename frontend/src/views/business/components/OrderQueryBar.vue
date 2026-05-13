<template>
  <div class="query-bar">
    <el-input
      v-model.trim="draft.keyword"
      placeholder="单据号"
      clearable
      style="width: 220px"
      @keyup.enter="emitSearch"
    />
    <el-select v-model="draft.subjectId" :placeholder="subjectLabel" clearable filterable style="width: 220px">
      <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
    </el-select>
    <el-date-picker
      v-model="dateRange"
      type="daterange"
      range-separator="至"
      start-placeholder="开始日期"
      end-placeholder="结束日期"
      value-format="YYYY-MM-DD"
      style="width: 260px"
    />
    <el-button :icon="Search" type="primary" @click="emitSearch">查询</el-button>
    <el-button :icon="RotateCcw" @click="reset">重置</el-button>
  </div>
</template>

<script setup>
import { RotateCcw, Search } from 'lucide-vue-next'
import { reactive, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
  subjects: { type: Array, default: () => [] },
  subjectLabel: { type: String, default: '往来单位' },
})

const emit = defineEmits(['update:modelValue', 'search'])
const draft = reactive({ keyword: '', subjectId: null })
const dateRange = ref([])

watch(() => props.modelValue, value => {
  draft.keyword = value.keyword || ''
  draft.subjectId = value.subjectId || null
  dateRange.value = value.startDate && value.endDate ? [value.startDate, value.endDate] : []
}, { immediate: true, deep: true })

function currentValue() {
  return {
    keyword: draft.keyword || undefined,
    subjectId: draft.subjectId || undefined,
    startDate: dateRange.value?.[0] || undefined,
    endDate: dateRange.value?.[1] || undefined,
  }
}

function emitSearch() {
  const next = currentValue()
  emit('update:modelValue', next)
  emit('search', next)
}

function reset() {
  draft.keyword = ''
  draft.subjectId = null
  dateRange.value = []
  emitSearch()
}
</script>

<style scoped>
.query-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
</style>

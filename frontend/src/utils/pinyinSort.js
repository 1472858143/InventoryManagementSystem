import { pinyin } from 'pinyin-pro'

export function getPinyinInitials(value) {
  const text = String(value ?? '').trim()
  if (!text) return ''
  return pinyin(text, { pattern: 'first', toneType: 'none' }).replace(/\s+/g, '').toUpperCase()
}

export function compareByPinyinInitials(left, right) {
  const leftInitials = getPinyinInitials(left)
  const rightInitials = getPinyinInitials(right)
  const initialsResult = leftInitials.localeCompare(rightInitials, 'zh-CN', {
    numeric: true,
    sensitivity: 'base',
  })
  if (initialsResult !== 0) return initialsResult
  return String(left ?? '').localeCompare(String(right ?? ''), 'zh-CN', {
    numeric: true,
    sensitivity: 'base',
  })
}

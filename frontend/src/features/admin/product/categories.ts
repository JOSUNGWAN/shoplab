import type { CategoryNode, CategoryOption } from './types'

/** 트리를 평평한 옵션 목록으로: 의류 / └ 상의 / └ 하의 ... */
export function toCategoryOptions(nodes: CategoryNode[], depth = 0): CategoryOption[] {
  return nodes.flatMap((node) => [
    { id: node.id, label: depth === 0 ? node.name : `${'\u00A0\u00A0'.repeat(depth)}└ ${node.name}` },
    ...toCategoryOptions(node.children ?? [], depth + 1),
  ])
}
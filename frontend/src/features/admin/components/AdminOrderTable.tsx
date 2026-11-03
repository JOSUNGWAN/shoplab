import { useState, type InputHTMLAttributes } from 'react'
import {
  createColumnHelper,
  flexRender,
  getCoreRowModel,
  useReactTable,
  type RowSelectionState,
} from '@tanstack/react-table'
import { getErrorMessage } from '../../../api/client'
import { formatDateTime, formatPrice } from '../../../lib/format'
import type { PageResponse } from '../../../types/api'
import { PROGRESS_STATUS_OPTIONS } from '../constants'
import { useChangeOrderStatus } from '../hooks'
import type { AdminOrder, AdminProgressStatus, BulkStatusResult } from '../types'
import { OrderStatusBadge } from '../../order/components/OrderStatusBadge'

type Props = {
  data: PageResponse<AdminOrder> | undefined
  isLoading: boolean
  isFetching: boolean
  onPageChange: (page: number) => void
}

const EMPTY: AdminOrder[] = [] // 매 렌더마다 새 배열을 만들지 않도록 밖에 둔다

function Checkbox({ indeterminate, ...props }: InputHTMLAttributes<HTMLInputElement> & { indeterminate?: boolean }) {
  return (
    <input
      type="checkbox"
      ref={(el) => {
        if (el) el.indeterminate = !!indeterminate && !props.checked
      }}
      className="size-4 cursor-pointer accent-gray-900"
      {...props}
    />
  )
}

const column = createColumnHelper<AdminOrder>()

const columns = [
  column.display({
    id: 'select',
    header: ({ table }) => (
      <Checkbox
        aria-label="현재 페이지 전체 선택"
        checked={table.getIsAllPageRowsSelected()}
        indeterminate={table.getIsSomePageRowsSelected()}
        onChange={table.getToggleAllPageRowsSelectedHandler()}
      />
    ),
    cell: ({ row }) => (
      <Checkbox
        aria-label={`${row.original.orderNumber} 선택`}
        checked={row.getIsSelected()}
        onChange={row.getToggleSelectedHandler()}
      />
    ),
  }),
  column.accessor('orderNumber', {
    header: '주문번호',
    cell: (info) => <span className="font-mono text-xs">{info.getValue()}</span>,
  }),
  column.accessor('orderedAt', {
    header: '주문일시',
    cell: (info) => <span className="whitespace-nowrap">{formatDateTime(info.getValue())}</span>,
  }),
  column.accessor('status', {
    header: '상태',
    cell: (info) => <OrderStatusBadge status={info.getValue()} />,
  }),
  column.accessor('title', {
    header: '상품',
    cell: ({ row }) => (
      <div className="flex items-center gap-1.5">
        {row.original.timeDeal && (
          <span className="rounded bg-red-100 px-1.5 py-0.5 text-xs font-semibold text-red-600">타임딜</span>
        )}
        <span className="max-w-56 truncate">{row.original.title}</span>
      </div>
    ),
  }),
  column.accessor('memberName', {
    header: '회원',
    cell: ({ row }) => (
      <div>
        <p>{row.original.memberName}</p>
        <p className="text-xs text-gray-500">{row.original.memberEmail}</p>
      </div>
    ),
  }),
  column.accessor('recipient', {
    header: '받는 분',
    cell: ({ row }) => (
      <div>
        <p>{row.original.recipient}</p>
        <p className="text-xs text-gray-500">{row.original.phone}</p>
      </div>
    ),
  }),
  column.accessor('totalAmount', {
    header: () => <span className="block text-right">금액</span>,
    cell: (info) => <span className="block text-right font-semibold">{formatPrice(info.getValue())}</span>,
  }),
]

export default function AdminOrderTable({ data, isLoading, isFetching, onPageChange }: Props) {
  const [rowSelection, setRowSelection] = useState<RowSelectionState>({})
  const [target, setTarget] = useState<AdminProgressStatus>('PREPARING')
  const [result, setResult] = useState<BulkStatusResult | null>(null)
  const changeStatus = useChangeOrderStatus()

  // eslint-disable-next-line react-hooks/incompatible-library
  const table = useReactTable({
    data: data?.content ?? EMPTY,
    columns,
    getRowId: (row) => row.orderNumber, // 선택 상태를 주문번호로 기억 (행 번호 X)
    state: { rowSelection },
    onRowSelectionChange: setRowSelection,
    getCoreRowModel: getCoreRowModel(),
  })

  const selected = Object.keys(rowSelection)
  const targetLabel = PROGRESS_STATUS_OPTIONS.find((option) => option.value === target)?.label

  function handleChangeStatus() {
    if (selected.length === 0) return
    if (!window.confirm(`선택한 ${selected.length}건을 '${targetLabel}'(으)로 변경할까요?`)) return

    setResult(null)
    changeStatus.mutate(
      { orderNumbers: selected, status: target },
      {
        onSuccess: (res) => {
          setResult(res)
          setRowSelection({})
        },
      },
    )
  }

  const page = data?.page ?? 0
  const totalPages = data?.totalPages ?? 0

  return (
    <div className="space-y-3">
      {/* 일괄 처리 영역 */}
      <div className="flex flex-wrap items-center gap-2">
        <p className="text-sm text-gray-600">
          총 <b>{data?.totalElements ?? 0}</b>건 · 선택 <b className="text-gray-900">{selected.length}</b>건
          {isFetching && <span className="ml-2 text-gray-400">불러오는 중…</span>}
        </p>
        <div className="ml-auto flex items-center gap-2">
          <select
            value={target}
            onChange={(e) => setTarget(e.target.value as AdminProgressStatus)}
            className="rounded border border-gray-300 px-2 py-1.5 text-sm"
          >
            {PROGRESS_STATUS_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
          <button
            type="button"
            onClick={handleChangeStatus}
            disabled={selected.length === 0 || changeStatus.isPending}
            className="rounded bg-blue-600 px-4 py-1.5 text-sm font-semibold text-white disabled:bg-gray-300"
          >
            {changeStatus.isPending ? '변경 중…' : '선택 주문 상태 변경'}
          </button>
        </div>
      </div>

      {/* 처리 결과 */}
      {changeStatus.isError && (
        <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{getErrorMessage(changeStatus.error)}</p>
      )}
      {result && (
        <div className="space-y-1 rounded border border-gray-200 bg-white px-3 py-2 text-sm">
          <p>
            <span className="font-semibold text-green-700">✔ {result.successCount}건 변경 완료</span>
            {result.failures.length > 0 && (
              <span className="ml-3 font-semibold text-red-600">✖ {result.failures.length}건 실패</span>
            )}
          </p>
          {result.failures.map((failure) => (
            <p key={failure.orderNumber} className="text-red-600">
              <span className="font-mono text-xs">{failure.orderNumber}</span> · {failure.reason}
            </p>
          ))}
        </div>
      )}

      {/* 그리드 */}
      <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
        <table className="w-full text-sm">
          <thead className="border-b border-gray-200 bg-gray-50 text-left text-gray-600">
            {table.getHeaderGroups().map((headerGroup) => (
              <tr key={headerGroup.id}>
                {headerGroup.headers.map((header) => (
                  <th key={header.id} className="px-3 py-2 font-medium whitespace-nowrap">
                    {flexRender(header.column.columnDef.header, header.getContext())}
                  </th>
                ))}
              </tr>
            ))}
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={columns.length} className="py-16 text-center text-gray-400">
                  불러오는 중…
                </td>
              </tr>
            ) : table.getRowModel().rows.length === 0 ? (
              <tr>
                <td colSpan={columns.length} className="py-16 text-center text-gray-400">
                  검색 조건에 맞는 주문이 없습니다.
                </td>
              </tr>
            ) : (
              table.getRowModel().rows.map((row) => (
                <tr
                  key={row.id}
                  className={`border-b border-gray-100 last:border-0 ${row.getIsSelected() ? 'bg-blue-50' : 'hover:bg-gray-50'}`}
                >
                  {row.getVisibleCells().map((cell) => (
                    <td key={cell.id} className="px-3 py-2 align-middle">
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* 페이지 이동 */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-3 text-sm">
          <button
            type="button"
            onClick={() => onPageChange(page - 1)}
            disabled={page === 0}
            className="rounded border border-gray-300 px-3 py-1 disabled:opacity-40"
          >
            이전
          </button>
          <span>
            {page + 1} / {totalPages}
          </span>
          <button
            type="button"
            onClick={() => onPageChange(page + 1)}
            disabled={!data?.hasNext}
            className="rounded border border-gray-300 px-3 py-1 disabled:opacity-40"
          >
            다음
          </button>
        </div>
      )}
    </div>
  )
}

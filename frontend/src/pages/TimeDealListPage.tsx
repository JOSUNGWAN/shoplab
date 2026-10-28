import { useQuery } from '@tanstack/react-query'
import { getErrorMessage } from '../api/client'
import { getTimeDeals } from '../features/timedeal/api'
import { TimeDealCard } from '../features/timedeal/components/TimeDealCard'

export default function TimeDealListPage() {
  const {
    data: deals,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ['time-deals'],
    queryFn: getTimeDeals,
    refetchInterval: 10_000, // 판매 진행률을 10초마다 갱신
  })

  if (isLoading) return <p className="py-20 text-center text-gray-500">불러오는 중...</p>
  if (isError) return <p className="py-20 text-center text-red-600">{getErrorMessage(error)}</p>

  return (
    <div className="py-10">
      <h1 className="mb-2 text-2xl font-bold">타임딜</h1>
      <p className="mb-8 text-sm text-gray-500">한정 수량 특가, 1인 1개 구매할 수 있어요.</p>

      {deals && deals.length === 0 ? (
        <p className="py-20 text-center text-gray-500">진행 중인 타임딜이 없습니다.</p>
      ) : (
        <div className="grid grid-cols-2 gap-x-5 gap-y-8 md:grid-cols-4">
          {deals?.map((deal) => (
            <TimeDealCard key={deal.id} deal={deal} />
          ))}
        </div>
      )}
    </div>
  )
}

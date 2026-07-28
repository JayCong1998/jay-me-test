import { useEffect, useState } from 'react'
import { fetchDashboardOverview } from '@/api/dashboardApi'
import { Panel } from '@/components/ui/Panel'
import type { DashboardOverview } from '@/types'

export function DashboardPage() {
  const [overview, setOverview] = useState<DashboardOverview | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchDashboardOverview()
      .then(setOverview)
      .catch((err) => setError(err instanceof Error ? err.message : '加载失败'))
  }, [])

  const stats = overview
    ? [
        ['题目总数', overview.totalQuestions],
        ['注册用户', overview.totalUsers],
        ['答题记录', overview.totalRecords],
        ['今日答题', overview.todayRecords],
        ['平均答对', overview.averageCorrectCount.toFixed(1)],
      ]
    : []

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-semibold">工作台</h1>
        <p className="mt-1 text-sm text-muted-foreground">查看题库、用户和答题数据概览</p>
      </div>

      {error && <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>}

      <div className="grid grid-cols-5 gap-4">
        {stats.map(([label, value]) => (
          <Panel key={label}>
            <div className="text-sm text-muted-foreground">{label}</div>
            <div className="mt-2 text-2xl font-semibold">{value}</div>
          </Panel>
        ))}
      </div>

      <Panel>
        <h2 className="mb-4 text-base font-semibold">模式分布</h2>
        <div className="space-y-3">
          {Object.entries(overview?.modeDistribution || {}).map(([mode, count]) => (
            <div key={mode} className="flex items-center justify-between border-b border-border pb-2 last:border-0 last:pb-0">
              <span className="text-sm font-medium">{mode}</span>
              <span className="text-sm text-muted-foreground">{count} 次</span>
            </div>
          ))}
        </div>
      </Panel>
    </div>
  )
}

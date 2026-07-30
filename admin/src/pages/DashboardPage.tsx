import { useEffect, useState } from 'react'
import { BarChartOutlined, DatabaseOutlined, FileTextOutlined, TrophyOutlined, UserOutlined } from '@ant-design/icons'
import { Alert, Card, Empty, Statistic, Typography } from 'antd'
import { fetchDashboardOverview } from '@/api/dashboardApi'
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
        { label: '题目总数', value: overview.totalQuestions, icon: <DatabaseOutlined /> },
        { label: '注册用户', value: overview.totalUsers, icon: <UserOutlined /> },
        { label: '答题记录', value: overview.totalRecords, icon: <FileTextOutlined /> },
        { label: '今日答题', value: overview.todayRecords, icon: <BarChartOutlined /> },
        { label: '平均答对', value: overview.averageCorrectCount.toFixed(1), icon: <TrophyOutlined /> },
      ]
    : []

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">工作台</h1>
        <p className="mt-1 text-sm text-muted-foreground">查看题库、用户和答题数据概览</p>
      </div>

      {error && <Alert type="error" message={error} showIcon />}

      <div className="grid grid-cols-5 gap-5">{stats.map((stat) => <Card className="h-full shadow-sm" key={stat.label}><Statistic title={stat.label} value={stat.value} prefix={stat.icon} /></Card>)}</div>

      <Card title="模式分布" className="shadow-sm">{Object.keys(overview?.modeDistribution || {}).length ? <div className="grid grid-cols-3 gap-4">{Object.entries(overview?.modeDistribution || {}).map(([mode, count]) => <div className="rounded-lg bg-slate-50 p-5" key={mode}><Typography.Text type="secondary">{mode}</Typography.Text><div className="mt-2 text-2xl font-semibold text-slate-800">{count}</div></div>)}</div> : <Empty description="暂无答题数据" />}</Card>
    </div>
  )
}

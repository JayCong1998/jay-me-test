import { SearchOutlined } from '@ant-design/icons'
import { Alert, Button, Card, DatePicker, Input, Pagination, Select, Space, Table, Tag, Typography } from 'antd'
import dayjs from 'dayjs'
import { type ChangeEvent, useEffect, useState } from 'react'
import { fetchRecords } from '@/api/recordApi'
import type { GameRecord, PageResponse } from '@/types'

function toApiDate(value?: string) {
  return value ? `${value.replace('T', ' ')}:00` : undefined
}

export function RecordPage() {
  const [keyword, setKeyword] = useState('')
  const [mode, setMode] = useState('')
  const [startAt, setStartAt] = useState<string | undefined>()
  const [endAt, setEndAt] = useState<string | undefined>()
  const [page, setPage] = useState(1)
  const [data, setData] = useState<PageResponse<GameRecord> | null>(null)
  const [error, setError] = useState('')

  async function load() {
    setError('')
    try {
      setData(await fetchRecords({
        keyword,
        mode,
        startAt: toApiDate(startAt),
        endAt: toApiDate(endAt),
        page,
        size: 10,
      }))
    } catch (err) {
      setError(err instanceof Error ? err.message : '加载失败')
    }
  }

  useEffect(() => {
    load()
  }, [page])

  return (
    <div className="space-y-4">
      <div className="space-y-1">
        <h1 className="text-2xl font-semibold">答题记录</h1>
        <p className="mt-1 text-sm text-muted-foreground">按模式、用户和时间查看提交记录</p>
      </div>

      <Card size="small">
        <div className="grid grid-cols-[minmax(220px,1.2fr)_150px_220px_220px_auto] gap-3">
          <Input value={keyword} onChange={(event: ChangeEvent<HTMLInputElement>) => setKeyword(event.target.value)} placeholder="昵称或 roundId" allowClear />
          <Select value={mode || undefined} onChange={(value: string | undefined) => setMode(value ?? '')} placeholder="全部模式" allowClear options={['CLASSIC', 'ALBUM', 'ABYSS'].map((value) => ({ value, label: value }))} />
          <DatePicker className="w-full" showTime value={startAt ? dayjs(startAt) : null} onChange={(value: dayjs.Dayjs | null) => setStartAt(value?.format('YYYY-MM-DDTHH:mm') || undefined)} placeholder="开始时间" />
          <DatePicker className="w-full" showTime value={endAt ? dayjs(endAt) : null} onChange={(value: dayjs.Dayjs | null) => setEndAt(value?.format('YYYY-MM-DDTHH:mm') || undefined)} placeholder="结束时间" />
          <Button type="primary" icon={<SearchOutlined />} onClick={() => { setPage(1); load() }}>查询</Button>
        </div>
      </Card>

      {error && <Alert type="error" message={error} showIcon />}

      <Card size="small" className="shadow-sm"><Table<GameRecord> rowKey="id" loading={!data && !error} dataSource={data?.records ?? []} pagination={false} scroll={{ x: 1050 }} columns={[
        { title: 'ID', dataIndex: 'id', width: 75 }, { title: '模式', dataIndex: 'mode', width: 100, render: (value: string) => <Tag color="geekblue">{value}</Tag> }, { title: '昵称', dataIndex: 'nickname', render: (value: string | null) => value || '-' }, { title: '用户', dataIndex: 'userId', render: (value: number | null) => value || '游客' }, { title: '成绩', width: 90, render: (_: unknown, record: GameRecord) => `${record.correctCount}/${record.totalQuestions}` }, { title: '用时', dataIndex: 'timeSpentSecs', width: 85, render: (value: number) => `${value}s` }, { title: '复活', dataIndex: 'usedRevival', width: 80, render: (value: boolean) => value ? '是' : '否' }, { title: '专辑', dataIndex: 'albumKey', render: (value: string | null) => value || '-' }, { title: '提交时间', dataIndex: 'createdAt', width: 190 },
      ]} />
      <div className="mt-5 flex items-center justify-between"><Typography.Text type="secondary">共 {data?.total ?? 0} 条</Typography.Text><Pagination current={data?.page ?? page} pageSize={data?.size ?? 10} total={data?.total ?? 0} showSizeChanger={false} onChange={setPage} /></div></Card>
    </div>
  )
}

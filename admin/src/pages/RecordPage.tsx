import { Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { fetchRecords } from '@/api/recordApi'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Panel } from '@/components/ui/Panel'
import { Select } from '@/components/ui/Select'
import { Table, Td, Th } from '@/components/ui/Table'
import type { GameRecord, PageResponse } from '@/types'

function toApiDate(value: string) {
  return value ? `${value.replace('T', ' ')}:00` : undefined
}

export function RecordPage() {
  const [keyword, setKeyword] = useState('')
  const [mode, setMode] = useState('')
  const [startAt, setStartAt] = useState('')
  const [endAt, setEndAt] = useState('')
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
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-semibold">答题记录</h1>
        <p className="mt-1 text-sm text-muted-foreground">按模式、用户和时间查看提交记录</p>
      </div>

      <Panel>
        <div className="flex flex-wrap items-center gap-3">
          <Input className="w-72" value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="昵称或 roundId" />
          <Select className="w-40" value={mode} onChange={(event) => setMode(event.target.value)}>
            <option value="">全部模式</option>
            <option value="CLASSIC">CLASSIC</option>
            <option value="ALBUM">ALBUM</option>
            <option value="ABYSS">ABYSS</option>
          </Select>
          <Input className="w-64" type="datetime-local" value={startAt} onChange={(event) => setStartAt(event.target.value)} />
          <Input className="w-64" type="datetime-local" value={endAt} onChange={(event) => setEndAt(event.target.value)} />
          <Button className="min-w-24 whitespace-nowrap" onClick={() => { setPage(1); load() }}>
            <Search className="h-4 w-4" />
            查询
          </Button>
        </div>
      </Panel>

      {error && <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>}

      <Panel className="p-0">
        <Table>
          <thead>
            <tr>
              <Th>ID</Th>
              <Th>模式</Th>
              <Th>昵称</Th>
              <Th>用户</Th>
              <Th>成绩</Th>
              <Th>用时</Th>
              <Th>复活</Th>
              <Th>专辑</Th>
              <Th>提交时间</Th>
            </tr>
          </thead>
          <tbody>
            {(data?.records || []).map((record) => (
              <tr key={record.id}>
                <Td>{record.id}</Td>
                <Td>{record.mode}</Td>
                <Td>{record.nickname || '-'}</Td>
                <Td>{record.userId || '游客'}</Td>
                <Td>{record.correctCount}/{record.totalQuestions}</Td>
                <Td>{record.timeSpentSecs}s</Td>
                <Td>{record.usedRevival ? '是' : '否'}</Td>
                <Td>{record.albumKey || '-'}</Td>
                <Td>{record.createdAt}</Td>
              </tr>
            ))}
          </tbody>
        </Table>
        <div className="flex items-center justify-between px-4 py-3 text-sm text-muted-foreground">
          <span>共 {data?.total || 0} 条</span>
          <div className="flex gap-2">
            <Button variant="secondary" disabled={page <= 1} onClick={() => setPage(page - 1)}>上一页</Button>
            <Button variant="secondary" disabled={!data || page * data.size >= data.total} onClick={() => setPage(page + 1)}>下一页</Button>
          </div>
        </div>
      </Panel>
    </div>
  )
}

import { Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { fetchUsers } from '@/api/userApi'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Panel } from '@/components/ui/Panel'
import { Table, Td, Th } from '@/components/ui/Table'
import type { PageResponse, UserRecord } from '@/types'

export function UserPage() {
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(1)
  const [data, setData] = useState<PageResponse<UserRecord> | null>(null)
  const [error, setError] = useState('')

  async function load() {
    setError('')
    try {
      setData(await fetchUsers({ keyword, page, size: 10 }))
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
        <h1 className="text-2xl font-semibold">用户列表</h1>
        <p className="mt-1 text-sm text-muted-foreground">查看注册用户基础信息</p>
      </div>

      <Panel>
        <div className="flex gap-3">
          <Input className="max-w-xs" value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="邮箱或昵称" />
          <Button onClick={() => { setPage(1); load() }}>
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
              <Th>邮箱</Th>
              <Th>昵称</Th>
              <Th>创建时间</Th>
              <Th>更新时间</Th>
            </tr>
          </thead>
          <tbody>
            {(data?.records || []).map((user) => (
              <tr key={user.id}>
                <Td>{user.id}</Td>
                <Td>{user.email}</Td>
                <Td>{user.nickname}</Td>
                <Td>{user.createdAt}</Td>
                <Td>{user.updatedAt}</Td>
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

import { SearchOutlined } from '@ant-design/icons'
import { Alert, Button, Card, Input, Pagination, Space, Table, Typography } from 'antd'
import { type ChangeEvent, useEffect, useState } from 'react'
import { fetchUsers } from '@/api/userApi'
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
    <div className="space-y-4">
      <div className="space-y-1">
        <h1 className="text-2xl font-semibold">用户列表</h1>
        <p className="mt-1 text-sm text-muted-foreground">查看注册用户基础信息</p>
      </div>

      <Card size="small"><Space><Input className="w-72" value={keyword} onChange={(event: ChangeEvent<HTMLInputElement>) => setKeyword(event.target.value)} placeholder="邮箱或昵称" allowClear /><Button type="primary" icon={<SearchOutlined />} onClick={() => { setPage(1); load() }}>查询</Button></Space></Card>

      {error && <Alert type="error" message={error} showIcon />}

      <Card size="small" className="shadow-sm"><Table<UserRecord> rowKey="id" loading={!data && !error} dataSource={data?.records ?? []} pagination={false} columns={[
        { title: 'ID', dataIndex: 'id', width: 90 }, { title: '邮箱', dataIndex: 'email' }, { title: '昵称', dataIndex: 'nickname' }, { title: '创建时间', dataIndex: 'createdAt', width: 190 }, { title: '更新时间', dataIndex: 'updatedAt', width: 190 },
      ]} />
      <div className="mt-5 flex items-center justify-between"><Typography.Text type="secondary">共 {data?.total ?? 0} 条</Typography.Text><Pagination current={data?.page ?? page} pageSize={data?.size ?? 10} total={data?.total ?? 0} showSizeChanger={false} onChange={setPage} /></div></Card>
    </div>
  )
}

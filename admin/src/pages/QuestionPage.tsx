import { PlusOutlined, SearchOutlined } from '@ant-design/icons'
import { Alert, Button, Card, Input, Pagination, Popconfirm, Select, Space, Table, Tag, Typography } from 'antd'
import { type ChangeEvent, useEffect, useState } from 'react'
import { createQuestion, fetchQuestions, rebalanceQuestionOptions, updateQuestion } from '@/api/questionApi'
import { QuestionForm } from '@/components/questions/QuestionForm'
import type { PageResponse, Question } from '@/types'

export function QuestionPage() {
  const [data, setData] = useState<PageResponse<Question> | null>(null)
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [difficulty, setDifficulty] = useState('')
  const [page, setPage] = useState(1)
  const [editingId, setEditingId] = useState<number | undefined>()
  const [formOpen, setFormOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [rebalancing, setRebalancing] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  async function load() {
    setError('')
    try {
      setData(await fetchQuestions({ keyword, category, difficulty, page, size: 10 }))
    } catch (err) {
      setError(err instanceof Error ? err.message : '加载失败')
    }
  }

  useEffect(() => {
    load()
  }, [page])

  async function handleSubmit(question: Question) {
    setSubmitting(true)
    try {
      if (question.id) {
        await updateQuestion(question.id, question)
      } else {
        await createQuestion(question)
      }
      setFormOpen(false)
      setEditingId(undefined)
      await load()
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRebalance() {
    setRebalancing(true)
    setError('')
    setNotice('')
    try {
      const result = await rebalanceQuestionOptions()
      const { A, B, C, D } = result.answerDistribution
      setNotice(`已调整 ${result.adjustedCount} 题；A/B/C/D：${A}/${B}/${C}/${D}`)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : '均衡答案分布失败')
    } finally {
      setRebalancing(false)
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-semibold">题库管理</h1>
          <p className="mt-1 text-sm text-muted-foreground">维护经典模式和专辑模式题目</p>
        </div>
        <Space>
          <Popconfirm title="均衡答案分布" description="将随机重排全部题目的选项位置，是否继续？" okText="确认" cancelText="取消" onConfirm={handleRebalance}>
            <Button loading={rebalancing}>均衡答案分布</Button>
          </Popconfirm>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingId(undefined); setFormOpen(true) }}>
            新增题目
          </Button>
        </Space>
      </div>

      <Card size="small"><Space wrap>
        <Input className="w-72" value={keyword} onChange={(event: ChangeEvent<HTMLInputElement>) => setKeyword(event.target.value)} placeholder="题干或选项关键词" allowClear />
        <Select className="w-32" value={category || undefined} onChange={(value: string | undefined) => setCategory(value ?? '')} placeholder="全部分类" allowClear options={[{ value: 'LYRICS', label: '歌词' }, { value: 'WORKS', label: '作品' }, { value: 'SCREEN', label: '影视' }, { value: 'KNOWLEDGE', label: '知识' }]} />
        <Select className="w-32" value={difficulty || undefined} onChange={(value: string | undefined) => setDifficulty(value ?? '')} placeholder="全部难度" allowClear options={[{ value: 'EASY', label: '简单' }, { value: 'MEDIUM', label: '中等' }, { value: 'HARD', label: '困难' }]} />
        <Button type="primary" icon={<SearchOutlined />} onClick={() => { setPage(1); load() }}>查询</Button>
      </Space></Card>

      {error && <Alert type="error" message={error} showIcon />}
      {notice && <Alert type="success" message={notice} showIcon />}

      <Card size="small" className="shadow-sm"><Table<Question> rowKey="id" loading={!data && !error} dataSource={data?.records ?? []} pagination={false} columns={[
        { title: 'ID', dataIndex: 'id', width: 72 },
        { title: '题干', dataIndex: 'questionText', ellipsis: true },
        { title: '分类', dataIndex: 'category', width: 110, render: (value: string) => <Tag color="blue">{value}</Tag> },
        { title: '难度', dataIndex: 'difficulty', width: 100, render: (value: string) => <Tag color={value === 'HARD' ? 'red' : value === 'MEDIUM' ? 'orange' : 'green'}>{value}</Tag> },
        { title: '专辑', dataIndex: 'album', width: 160, render: (value: string | null) => value || '-' },
        { title: '答案', dataIndex: 'correctOption', width: 80 },
        { title: '操作', width: 90, render: (_: unknown, question: Question) => <Button type="link" onClick={() => { setEditingId(question.id); setFormOpen(true) }}>编辑</Button> },
      ]} />
        <div className="mt-5 flex items-center justify-between"><Typography.Text type="secondary">共 {data?.total ?? 0} 条</Typography.Text><Pagination current={data?.page ?? page} pageSize={data?.size ?? 10} total={data?.total ?? 0} showSizeChanger={false} onChange={setPage} /></div>
      </Card>

      {formOpen && (
        <QuestionForm
          questionId={editingId}
          submitting={submitting}
          onCancel={() => { setFormOpen(false); setEditingId(undefined) }}
          onSubmit={handleSubmit}
        />
      )}
    </div>
  )
}

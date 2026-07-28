import { Plus, Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { createQuestion, fetchQuestions, updateQuestion } from '@/api/questionApi'
import { QuestionForm } from '@/components/questions/QuestionForm'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Panel } from '@/components/ui/Panel'
import { Select } from '@/components/ui/Select'
import { Table, Td, Th } from '@/components/ui/Table'
import type { PageResponse, Question } from '@/types'

export function QuestionPage() {
  const [data, setData] = useState<PageResponse<Question> | null>(null)
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [difficulty, setDifficulty] = useState('')
  const [page, setPage] = useState(1)
  const [editing, setEditing] = useState<Question | null>(null)
  const [formOpen, setFormOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

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
      if (editing?.id) {
        await updateQuestion(editing.id, question)
      } else {
        await createQuestion(question)
      }
      setFormOpen(false)
      setEditing(null)
      await load()
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">题库管理</h1>
          <p className="mt-1 text-sm text-muted-foreground">维护经典模式和专辑模式题目</p>
        </div>
        <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
          <Plus className="h-4 w-4" />
          新增题目
        </Button>
      </div>

      <Panel>
        <div className="flex gap-3">
          <Input className="max-w-xs" value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="题干或选项关键词" />
          <Select value={category} onChange={(event) => setCategory(event.target.value)}>
            <option value="">全部分类</option>
            <option value="LYRICS">歌词</option>
            <option value="ALBUM">专辑</option>
          </Select>
          <Select value={difficulty} onChange={(event) => setDifficulty(event.target.value)}>
            <option value="">全部难度</option>
            <option value="EASY">简单</option>
            <option value="MEDIUM">中等</option>
            <option value="HARD">困难</option>
          </Select>
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
              <Th>题干</Th>
              <Th>分类</Th>
              <Th>难度</Th>
              <Th>专辑</Th>
              <Th>答案</Th>
              <Th className="w-24">操作</Th>
            </tr>
          </thead>
          <tbody>
            {(data?.records || []).map((question) => (
              <tr key={question.id}>
                <Td>{question.id}</Td>
                <Td className="max-w-xl">{question.questionText}</Td>
                <Td>{question.category}</Td>
                <Td>{question.difficulty}</Td>
                <Td>{question.album || '-'}</Td>
                <Td>{question.correctOption}</Td>
                <Td>
                  <Button variant="secondary" onClick={() => { setEditing(question); setFormOpen(true) }}>编辑</Button>
                </Td>
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

      {formOpen && (
        <QuestionForm
          initial={editing}
          submitting={submitting}
          onCancel={() => { setFormOpen(false); setEditing(null) }}
          onSubmit={handleSubmit}
        />
      )}
    </div>
  )
}

import { FormEvent, useEffect, useState } from 'react'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Textarea } from '@/components/ui/Textarea'
import type { Question } from '@/types'

const emptyQuestion: Question = {
  category: 'LYRICS',
  album: '',
  difficulty: 'EASY',
  questionText: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  correctOption: 'A',
  explanation: '',
}

interface QuestionFormProps {
  initial?: Question | null
  submitting: boolean
  onCancel: () => void
  onSubmit: (question: Question) => Promise<void>
}

export function QuestionForm({ initial, submitting, onCancel, onSubmit }: QuestionFormProps) {
  const [form, setForm] = useState<Question>(emptyQuestion)

  useEffect(() => {
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [])

  useEffect(() => {
    setForm(initial ? { ...initial, album: initial.album || '' } : emptyQuestion)
  }, [initial])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    await onSubmit({
      ...form,
      album: form.album?.trim() || null,
    })
  }

  function patch<K extends keyof Question>(key: K, value: Question[K]) {
    setForm((current) => ({ ...current, [key]: value }))
  }

  return (
    <div className="fixed inset-0 z-30 flex items-start justify-center overflow-y-auto overscroll-contain bg-slate-950/30 px-6 py-6">
      <form className="max-h-[calc(100vh-3rem)] w-full max-w-5xl overflow-y-auto overscroll-contain rounded-lg border border-border bg-white p-5 shadow-xl" onSubmit={handleSubmit}>
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-lg font-semibold">{initial ? '编辑题目' : '新增题目'}</h2>
          <Button type="button" variant="ghost" onClick={onCancel}>关闭</Button>
        </div>

        <div className="grid grid-cols-3 gap-4">
          <label className="space-y-1.5">
            <span className="text-sm font-medium">分类</span>
            <Select value={form.category} onChange={(event) => patch('category', event.target.value as Question['category'])}>
              <option value="LYRICS">歌词</option>
              <option value="WORKS">作品</option>
              <option value="SCREEN">影视</option>
              <option value="KNOWLEDGE">知识</option>
            </Select>
          </label>
          <label className="space-y-1.5">
            <span className="text-sm font-medium">难度</span>
            <Select value={form.difficulty} onChange={(event) => patch('difficulty', event.target.value as Question['difficulty'])}>
              <option value="EASY">简单</option>
              <option value="MEDIUM">中等</option>
              <option value="HARD">困难</option>
            </Select>
          </label>
          <label className="space-y-1.5">
            <span className="text-sm font-medium">专辑</span>
            <Input value={form.album || ''} onChange={(event) => patch('album', event.target.value)} placeholder="可为空" />
          </label>
        </div>

        <label className="mt-4 block space-y-1.5">
          <span className="text-sm font-medium">题干</span>
          <Textarea value={form.questionText} onChange={(event) => patch('questionText', event.target.value)} required />
        </label>

        <div className="mt-4 grid grid-cols-2 gap-4">
          {(['A', 'B', 'C', 'D'] as const).map((option) => (
            <label key={option} className="space-y-1.5">
              <span className="text-sm font-medium">选项 {option}</span>
              <Input
                value={form[`option${option}`]}
                onChange={(event) => patch(`option${option}`, event.target.value)}
                required
              />
            </label>
          ))}
        </div>

        <div className="mt-4 grid grid-cols-[160px_1fr] gap-4">
          <label className="space-y-1.5">
            <span className="text-sm font-medium">正确答案</span>
            <Select value={form.correctOption} onChange={(event) => patch('correctOption', event.target.value as Question['correctOption'])}>
              <option value="A">A</option>
              <option value="B">B</option>
              <option value="C">C</option>
              <option value="D">D</option>
            </Select>
          </label>
          <label className="space-y-1.5">
            <span className="text-sm font-medium">解析</span>
            <Input value={form.explanation} onChange={(event) => patch('explanation', event.target.value)} required />
          </label>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onCancel}>取消</Button>
          <Button disabled={submitting}>{submitting ? '保存中' : '保存'}</Button>
        </div>
      </form>
    </div>
  )
}

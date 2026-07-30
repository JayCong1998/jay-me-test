import { useEffect, useState } from 'react'
import { Alert, Button, Form, Input, Modal, Select, Spin } from 'antd'
import { fetchQuestion } from '@/api/questionApi'
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
  questionId?: number
  submitting: boolean
  onCancel: () => void
  onSubmit: (question: Question) => Promise<void>
}

export function QuestionForm({ questionId, submitting, onCancel, onSubmit }: QuestionFormProps) {
  const [form] = Form.useForm<Question>()
  const [loading, setLoading] = useState(Boolean(questionId))
  const [error, setError] = useState('')

  useEffect(() => {
    if (!questionId) {
      form.setFieldsValue(emptyQuestion)
      setError('')
      setLoading(false)
      return
    }

    let active = true
    setLoading(true)
    setError('')
    void fetchQuestion(questionId)
      .then((question) => {
        if (active) form.setFieldsValue({ ...question, album: question.album || '' })
      })
      .catch((err: unknown) => {
        if (active) setError(err instanceof Error ? err.message : '加载题目详情失败')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => { active = false }
  }, [form, questionId])

  async function handleSubmit(values: Question) {
    await onSubmit({
      ...values,
      album: values.album?.trim() || null,
    })
  }

  return (
    <Modal title={questionId ? '编辑题目' : '新增题目'} open onCancel={onCancel} footer={null} width={880} destroyOnHidden>
      {loading && <div className="py-12 text-center"><Spin tip="正在加载题目详情..." /></div>}
      {error && <Alert type="error" message={error} showIcon />}
      {!loading && !error && <Form form={form} layout="vertical" initialValues={emptyQuestion} onFinish={handleSubmit}>
        <div className="grid grid-cols-3 gap-4">
          <Form.Item name="category" label="分类" rules={[{ required: true }]}><Select options={[{ value: 'LYRICS', label: '歌词' }, { value: 'WORKS', label: '作品' }, { value: 'SCREEN', label: '影视' }, { value: 'KNOWLEDGE', label: '知识' }]} /></Form.Item>
          <Form.Item name="difficulty" label="难度" rules={[{ required: true }]}><Select options={[{ value: 'EASY', label: '简单' }, { value: 'MEDIUM', label: '中等' }, { value: 'HARD', label: '困难' }]} /></Form.Item>
          <Form.Item name="album" label="专辑"><Input placeholder="可为空" /></Form.Item>
        </div>
        <Form.Item name="questionText" label="题干" rules={[{ required: true, message: '请输入题干' }]}><Input.TextArea rows={3} /></Form.Item>
        <div className="grid grid-cols-2 gap-4">{(['A', 'B', 'C', 'D'] as const).map((option) => <Form.Item key={option} name={`option${option}`} label={`选项 ${option}`} rules={[{ required: true, message: `请输入选项 ${option}` }]}><Input /></Form.Item>)}</div>
        <div className="grid grid-cols-[minmax(0,7fr)_minmax(0,17fr)] gap-4">
          <Form.Item name="correctOption" label="正确答案" rules={[{ required: true }]}><Select options={['A', 'B', 'C', 'D'].map((value) => ({ value, label: value }))} /></Form.Item>
          <Form.Item name="explanation" label="解析" rules={[{ required: true, message: '请输入解析' }]}><Input /></Form.Item>
        </div>
        <div className="flex justify-end gap-3"><Button onClick={onCancel}>取消</Button><Button type="primary" htmlType="submit" loading={submitting}>保存</Button></div>
      </Form>}
    </Modal>
  )
}

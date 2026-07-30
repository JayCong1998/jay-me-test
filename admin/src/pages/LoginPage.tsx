import { type ChangeEvent, useState } from 'react'
import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { Alert, Button, Card, Form, Input, Typography } from 'antd'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'

export function LoginPage() {
  const { token, signIn } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (token) {
    return <Navigate to="/dashboard" replace />
  }

  async function handleSubmit() {
    setError('')
    setLoading(true)
    try {
      await signIn(username, password)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : '登录失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-gradient-to-br from-indigo-950 via-indigo-800 to-violet-700 px-4">
      <Card className="w-full max-w-md !border-0 !p-3 shadow-2xl">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-indigo-600 text-xl font-bold text-white">J</div>
          <Typography.Title level={2} className="!mb-1 !text-2xl">Jay Me Admin</Typography.Title>
          <Typography.Text type="secondary">使用管理员账号登录运营后台</Typography.Text>
        </div>
        <Form layout="vertical" onFinish={handleSubmit} requiredMark={false}>
          <Form.Item label="账号"><Input prefix={<UserOutlined />} value={username} onChange={(event: ChangeEvent<HTMLInputElement>) => setUsername(event.target.value)} autoComplete="username" size="large" /></Form.Item>
          <Form.Item label="密码"><Input.Password prefix={<LockOutlined />} value={password} onChange={(event: ChangeEvent<HTMLInputElement>) => setPassword(event.target.value)} autoComplete="current-password" size="large" /></Form.Item>
          {error && <Alert className="mb-5" type="error" message={error} showIcon />}
          <Button htmlType="submit" type="primary" size="large" block loading={loading} disabled={!username || !password}>登录</Button>
        </Form>
      </Card>
    </main>
  )
}

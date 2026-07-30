import { DashboardOutlined, DatabaseOutlined, FileTextOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons'
import { Avatar, Button, Layout, Menu, Space, Typography } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'

const navItems = [
  { key: '/dashboard', label: '工作台', icon: <DashboardOutlined /> },
  { key: '/questions', label: '题库管理', icon: <DatabaseOutlined /> },
  { key: '/users', label: '用户列表', icon: <UserOutlined /> },
  { key: '/records', label: '答题记录', icon: <FileTextOutlined /> },
]

export function AppLayout() {
  const { admin, signOut } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  async function handleLogout() {
    await signOut()
    navigate('/login', { replace: true })
  }

  return (
    <Layout className="min-h-screen">
      <Layout.Sider width={248} theme="dark" className="!bg-[#171a3a]">
        <div className="flex h-20 items-center gap-3 px-6 text-white">
          <Avatar shape="square" size={36} className="!bg-violet-500">J</Avatar>
          <div>
            <Typography.Text className="!text-base !font-semibold !text-white">Jay Me Admin</Typography.Text>
            <div className="mt-0.5 text-xs text-indigo-200">运营管理中心</div>
          </div>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={navItems}
          className="!border-0 !bg-transparent px-3"
          onClick={({ key }: { key: string }) => navigate(key)}
        />
      </Layout.Sider>
      <Layout>
        <Layout.Header className="flex h-16 items-center justify-end border-b border-slate-200 !bg-white px-8">
          <Space size="middle">
            <Avatar icon={<UserOutlined />} className="!bg-indigo-100 !text-indigo-600" />
            <div className="leading-tight">
              <div className="font-medium text-slate-800">{admin?.nickname || admin?.username}</div>
              <Typography.Text type="secondary" className="!text-xs">{admin?.role}</Typography.Text>
            </div>
            <Button type="text" icon={<LogoutOutlined />} onClick={handleLogout}>退出</Button>
          </Space>
        </Layout.Header>
        <Layout.Content className="p-6">
          <div className="mx-auto max-w-[1600px]"><Outlet /></div>
        </Layout.Content>
      </Layout>
    </Layout>
  )
}

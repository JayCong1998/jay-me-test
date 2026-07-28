import { BarChart3, ClipboardList, Database, LogOut, Users } from 'lucide-react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { useAuth } from '@/context/AuthContext'
import { cn } from '@/lib/utils'

const navItems = [
  { to: '/dashboard', label: '工作台', icon: BarChart3 },
  { to: '/questions', label: '题库管理', icon: Database },
  { to: '/users', label: '用户列表', icon: Users },
  { to: '/records', label: '答题记录', icon: ClipboardList },
]

export function AppLayout() {
  const { admin, signOut } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await signOut()
    navigate('/login', { replace: true })
  }

  return (
    <div className="flex min-h-screen">
      <aside className="fixed inset-y-0 left-0 w-64 border-r border-border bg-white">
        <div className="flex h-16 items-center border-b border-border px-5">
          <div>
            <div className="text-base font-semibold">Jay Me Admin</div>
            <div className="text-xs text-muted-foreground">运营管理端</div>
          </div>
        </div>
        <nav className="space-y-1 p-3">
          {navItems.map((item) => {
            const Icon = item.icon
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  cn(
                    'flex h-10 items-center gap-3 rounded-md px-3 text-sm font-medium text-muted-foreground transition',
                    isActive ? 'bg-muted text-foreground' : 'hover:bg-muted hover:text-foreground',
                  )
                }
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </NavLink>
            )
          })}
        </nav>
      </aside>

      <div className="ml-64 flex min-h-screen flex-1 flex-col">
        <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b border-border bg-background/95 px-6 backdrop-blur">
          <div className="text-sm text-muted-foreground">管理端 API：/api/admin</div>
          <div className="flex items-center gap-3">
            <div className="text-right">
              <div className="text-sm font-medium">{admin?.nickname || admin?.username}</div>
              <div className="text-xs text-muted-foreground">{admin?.role}</div>
            </div>
            <Button variant="secondary" onClick={handleLogout}>
              <LogOut className="h-4 w-4" />
              退出
            </Button>
          </div>
        </header>
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

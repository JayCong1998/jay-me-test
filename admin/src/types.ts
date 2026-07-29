export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
  timestamp: number
}

export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface AdminProfile {
  id: number
  username: string
  nickname: string
  role: 'SUPER_ADMIN' | 'OPERATOR'
}

export interface AdminLoginResponse {
  token: string
  admin: AdminProfile
}

export interface DashboardOverview {
  totalQuestions: number
  totalUsers: number
  totalRecords: number
  todayRecords: number
  averageCorrectCount: number
  modeDistribution: Record<string, number>
}

export interface Question {
  id?: number
  category: 'LYRICS' | 'WORKS' | 'SCREEN' | 'KNOWLEDGE'
  album?: string | null
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  questionText: string
  optionA: string
  optionB: string
  optionC: string
  optionD: string
  correctOption: 'A' | 'B' | 'C' | 'D'
  explanation: string
  createdAt?: string
  updatedAt?: string
}

export interface UserRecord {
  id: number
  email: string
  nickname: string
  createdAt: string
  updatedAt: string
}

export interface GameRecord {
  id: number
  roundId: string
  mode: 'CLASSIC' | 'ALBUM' | 'ABYSS'
  albumKey?: string | null
  userId?: number | null
  nickname?: string | null
  totalQuestions: number
  correctCount: number
  timeSpentSecs: number
  usedRevival: number
  createdAt: string
}

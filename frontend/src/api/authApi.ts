import client from './client'
import type { R } from '@/utils/constants'

export interface UserInfo {
  id: number
  email: string
  nickname: string
}

export interface LoginResponse {
  token: string
  user: UserInfo
}

export interface RegisterRequest {
  email: string
  password: string
  nickname: string
}

export interface LoginRequest {
  email: string
  password: string
}

/**
 * 注册
 */
export async function register(req: RegisterRequest): Promise<LoginResponse> {
  const res = await client.post<R<LoginResponse>>('/auth/register', req)
  return res.data.data
}

/**
 * 登录
 */
export async function login(req: LoginRequest): Promise<LoginResponse> {
  const res = await client.post<R<LoginResponse>>('/auth/login', req)
  return res.data.data
}

/**
 * 获取当前用户信息
 */
export async function fetchMe(): Promise<UserInfo> {
  const res = await client.get<R<UserInfo>>('/auth/me')
  return res.data.data
}

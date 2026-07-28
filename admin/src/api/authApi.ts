import { apiClient } from '@/api/client'
import type { AdminLoginResponse, AdminProfile } from '@/types'

export function login(username: string, password: string) {
  return apiClient.post<never, AdminLoginResponse>('/auth/login', { username, password })
}

export function fetchMe() {
  return apiClient.get<never, AdminProfile>('/auth/me')
}

export function logout() {
  return apiClient.post<never, void>('/auth/logout')
}

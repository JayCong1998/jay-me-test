import { apiClient } from '@/api/client'
import type { PageResponse, Question } from '@/types'

export interface QuestionQuery {
  keyword?: string
  category?: string
  difficulty?: string
  album?: string
  page?: number
  size?: number
}

export function fetchQuestions(params: QuestionQuery) {
  return apiClient.get<never, PageResponse<Question>>('/questions', { params })
}

export function createQuestion(payload: Question) {
  return apiClient.post<never, Question>('/questions', payload)
}

export function updateQuestion(id: number, payload: Question) {
  return apiClient.put<never, Question>(`/questions/${id}`, payload)
}

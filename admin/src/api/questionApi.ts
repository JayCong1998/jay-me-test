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

export interface QuestionOptionRebalanceResult {
  adjustedCount: number
  answerDistribution: Record<'A' | 'B' | 'C' | 'D', number>
}

export function fetchQuestions(params: QuestionQuery) {
  return apiClient.get<never, PageResponse<Question>>('/questions', { params })
}

export function fetchQuestion(id: number) {
  return apiClient.get<never, Question>(`/questions/${id}`)
}

export function createQuestion(payload: Question) {
  return apiClient.post<never, Question>('/questions', payload)
}

export function updateQuestion(id: number, payload: Question) {
  return apiClient.put<never, Question>(`/questions/${id}`, payload)
}

export function rebalanceQuestionOptions() {
  return apiClient.post<never, QuestionOptionRebalanceResult>('/questions/rebalance-options')
}

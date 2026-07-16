import client from './client'
import type { R } from '@/utils/constants'

export interface QuestionDTO {
  id: number
  category: string
  difficulty: string
  questionText: string
  options: string[]
}

export interface RoundData {
  roundId: string
  questions: QuestionDTO[]
}

export interface AnswerRequest {
  roundId: string
  questionId: number
  selectedOption: string
}

export interface AnswerResult {
  correct: boolean
  correctOption: string
  explanation: string
}

export interface ReviveRequest {
  roundId: string
  questionId: number
}

export interface ReviveResult {
  revived: boolean
  remainingRevivals: number
}

/**
 * 获取一局题目
 */
export async function fetchRound(count = 10): Promise<RoundData> {
  const res = await client.get<R<RoundData>>('/questions/round', {
    params: { count },
  })
  return res.data.data
}

/**
 * 校验答案
 */
export async function checkAnswer(req: AnswerRequest): Promise<AnswerResult> {
  const res = await client.post<R<AnswerResult>>('/questions/check', req)
  return res.data.data
}

/**
 * 复活
 */
export async function revive(req: ReviveRequest): Promise<ReviveResult> {
  const res = await client.post<R<ReviveResult>>('/questions/revive', req)
  return res.data.data
}

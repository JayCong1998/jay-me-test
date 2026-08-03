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
  correctOption?: string
  explanation?: string
  canRevive: boolean
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
export async function fetchRound(): Promise<RoundData> {
  const res = await client.get<R<RoundData>>('/classic/round')
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
// ============================================================
// 无尽深渊模式
// ============================================================

export interface AbyssStepData {
  roundId: string
  questions: QuestionDTO[]
  streak: number
  revivalRemaining: number
}

/**
 * 开始深渊挑战，返回首批题目
 */
export async function startAbyss(): Promise<AbyssStepData> {
  const res = await client.post<R<AbyssStepData>>('/abyss/start')
  return res.data.data
}

/**
 * 获取下一批深渊题目（前端静默预加载）
 */
export async function fetchAbyssBatch(roundId: string): Promise<AbyssStepData> {
  const res = await client.post<R<AbyssStepData>>('/abyss/batch', { roundId })
  return res.data.data
}

/**
 * 深渊模式校验答案（答对自动累加 streak）
 */
export async function checkAbyssAnswer(req: AnswerRequest): Promise<AnswerResult> {
  const res = await client.post<R<AnswerResult>>('/abyss/check', req)
  return res.data.data
}

export async function reviveAbyss(req: ReviveRequest): Promise<ReviveResult> {
  const res = await client.post<R<ReviveResult>>('/abyss/revive', req)
  return res.data.data
}

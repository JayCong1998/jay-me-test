import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const feedbackSource = readFileSync(resolve(__dirname, 'FeedbackBar.vue'), 'utf-8')
const quizSource = readFileSync(resolve(__dirname, '../../pages/QuizPage.vue'), 'utf-8')

describe('compact quiz feedback', () => {
  it('keeps explanations collapsed until the player asks to view them', () => {
    expect(feedbackSource).toContain('const showExplanation = ref(false)')
    expect(feedbackSource).toContain('v-if="explanation && showExplanation"')
    expect(feedbackSource).toContain('v-if="explanation && !showExplanation"')
  })

  it('uses one short feedback entrance animation', () => {
    expect(feedbackSource).toContain('animation: feedback-fade-in 0.18s ease-out')
    expect(feedbackSource).not.toContain('feedback-enter')
    expect(quizSource).not.toContain('<Transition name="feedback-enter">')
  })

  it('centers the result summary for both correct and wrong feedback', () => {
    expect(feedbackSource).toContain('.feedback-summary')
    expect(feedbackSource).toContain('justify-content: center')
    expect(feedbackSource).toContain('.feedback-summary > div:last-child')
    expect(feedbackSource).toContain('text-align: center')
  })

  it('gives the result card enough visual presence without changing its structure', () => {
    expect(feedbackSource).toContain('padding: 16px')
    expect(feedbackSource).toContain('border-radius: 14px')
    expect(feedbackSource).toContain('width: 36px')
    expect(feedbackSource).toContain('height: 44px')
  })
})

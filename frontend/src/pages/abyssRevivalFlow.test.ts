import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const quizSource = readFileSync(resolve(__dirname, 'QuizPage.vue'), 'utf-8')
const feedbackSource = readFileSync(resolve(__dirname, '../components/quiz/FeedbackBar.vue'), 'utf-8')

describe('abyss revival flow', () => {
  it('offers a retry without rendering the answer or explanation on the first wrong attempt', () => {
    expect(quizSource).toContain(':can-revive="lastResult.canRevive"')
    expect(feedbackSource).toContain('v-if="!correct && correctOption"')
    expect(feedbackSource).toContain('v-if="explanation && !showExplanation"')
  })

  it('prevents repeated revival requests while one is in progress', () => {
    expect(quizSource).toContain('if (loading.value) return')
    expect(feedbackSource).toContain(':disabled="reviving"')
  })
})

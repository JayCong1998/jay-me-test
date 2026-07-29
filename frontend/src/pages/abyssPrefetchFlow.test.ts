import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const quizSource = readFileSync(resolve(__dirname, 'QuizPage.vue'), 'utf-8')
const composableSource = readFileSync(resolve(__dirname, '../composables/useQuiz.ts'), 'utf-8')

describe('abyss batch loading flow', () => {
  it('loads the next batch only after the current final answer is confirmed', () => {
    expect(quizSource).toContain('if (isAbyss.value && gameStore.isLastQuestion)')
    expect(quizSource).toContain('await prefetchAbyssBatch()')
  })

  it('does not overwrite the local streak with a batch response', () => {
    expect(composableSource).not.toContain('setAbyssStreak(data.streak)')
  })
})

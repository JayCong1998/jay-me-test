import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const quizSource = readFileSync(resolve(__dirname, 'QuizPage.vue'), 'utf-8')

describe('quiz final submit flow', () => {
  it('shows feedback before the player submits the final classic or album answer', () => {
    const handleSubmitBlock = quizSource.match(/async function handleSubmit\(\) \{[\s\S]*?\n\}/)?.[0] ?? ''

    expect(handleSubmitBlock).not.toContain('gameStore.isLastQuestion')
    expect(handleSubmitBlock).toContain('lastResult.value = result')
    expect(handleSubmitBlock).toContain('showFeedback.value = true')
  })
})

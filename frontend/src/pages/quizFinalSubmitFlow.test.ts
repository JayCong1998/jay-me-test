import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const quizSource = readFileSync(resolve(__dirname, 'QuizPage.vue'), 'utf-8')

describe('quiz final submit flow', () => {
  it('submits the whole paper directly on the final classic or album question', () => {
    const handleSubmitBlock = quizSource.match(/async function handleSubmit\(\) \{[\s\S]*?\n\}/)?.[0] ?? ''

    expect(handleSubmitBlock).toContain('!isAbyss.value && gameStore.isLastQuestion')
    expect(handleSubmitBlock).toContain('await finishAndSubmit()')
    expect(handleSubmitBlock).toContain("router.replace('/result')")
    expect(handleSubmitBlock.indexOf('await finishAndSubmit()')).toBeLessThan(
      handleSubmitBlock.indexOf('showFeedback.value = true')
    )
  })
})

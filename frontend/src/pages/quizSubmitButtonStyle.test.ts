import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const quizSource = readFileSync(resolve(__dirname, 'QuizPage.vue'), 'utf-8')

describe('quiz submit button style', () => {
  it('does not create a raised duplicate-layer effect for the ready submit button', () => {
    const readyBlock = quizSource.match(/&\.btn-ready\s*\{[\s\S]*?&:active/)?.[0] ?? ''

    expect(readyBlock).not.toContain('rgba(var(--app-accent-rgb)')
    expect(readyBlock).not.toContain('translateY(-2px)')
  })
})

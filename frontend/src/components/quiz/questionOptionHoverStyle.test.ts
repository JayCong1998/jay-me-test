import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const questionCardSource = readFileSync(resolve(__dirname, 'QuestionCard.vue'), 'utf-8')

describe('question option hover style', () => {
  it('does not move options horizontally on hover', () => {
    expect(questionCardSource).not.toContain('translateX(4px)')
  })

  it('keeps the selected option clear after answers are locked', () => {
    expect(questionCardSource).not.toContain('box-shadow: 0 0 20px')
    expect(questionCardSource).not.toContain('opacity: 0.55')
  })
})

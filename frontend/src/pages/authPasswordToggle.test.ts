import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const authPageSources = [
  readFileSync(resolve(__dirname, 'LoginPage.vue'), 'utf-8'),
  readFileSync(resolve(__dirname, 'RegisterPage.vue'), 'utf-8'),
]
const authStyleSource = readFileSync(resolve(__dirname, 'auth-common.scss'), 'utf-8')

describe('auth password visibility toggle', () => {
  it('uses icon-only buttons instead of visible show/hide text', () => {
    authPageSources.forEach((source) => {
      const passwordFieldBlock = source.match(/<div class="input-wrap input-wrap--password"[\s\S]*?<button class="password-toggle"/)?.[0] ?? ''

      expect(passwordFieldBlock).not.toBe('')
      expect(passwordFieldBlock).not.toContain('class="input-icon"')
      expect(source).toContain('class="password-toggle__icon"')
      expect(source).toContain(`:aria-label="showPassword ? '隐藏密码' : '显示密码'"`)
      expect(source).not.toContain(`{{ showPassword ? '隐藏' : '显示' }}`)
    })
  })

  it('hides the browser native password reveal control', () => {
    expect(authStyleSource).toContain('::-ms-reveal')
    expect(authStyleSource).toContain('::-ms-clear')
  })
})

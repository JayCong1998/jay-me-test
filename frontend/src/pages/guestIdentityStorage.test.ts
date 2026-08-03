import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const frontendRoot = resolve(__dirname, '..')
const quizSource = readFileSync(resolve(frontendRoot, 'composables/useQuiz.ts'), 'utf-8')
const pageSources = [
  'HomePage.vue',
  'LoginPage.vue',
  'RegisterPage.vue',
  'ProfilePage.vue',
  'CertificatePage.vue',
].map(file => readFileSync(resolve(__dirname, file), 'utf-8')).join('\n')

describe('guest identity storage', () => {
  it('does not retain a separate user nickname store or generate guest nicknames', () => {
    expect(existsSync(resolve(frontendRoot, 'stores/userStore.ts'))).toBe(false)
    expect(existsSync(resolve(frontendRoot, 'utils/nickname.ts'))).toBe(false)
    expect(pageSources).not.toContain('useUserStore')
    expect(quizSource).not.toContain('generateGuestNickname')
  })

  it('submits a nickname only for logged-in users', () => {
    const submitBlock = quizSource.match(/async function finishAndSubmit\(\) \{[\s\S]*?\n  \}/)?.[0] ?? ''

    expect(submitBlock).toContain('...(authStore.isLoggedIn')
    expect(submitBlock).toContain('nickname: authStore.user!.nickname')
  })
})

import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const homeSource = readFileSync(resolve(__dirname, 'HomePage.vue'), 'utf-8')
const profileSource = readFileSync(resolve(__dirname, 'ProfilePage.vue'), 'utf-8')

describe('home and profile identity copy', () => {
  it('keeps account identity out of the home page', () => {
    expect(homeSource).not.toContain('游客模式')
    expect(homeSource).not.toContain('Hi,')
    expect(homeSource).not.toContain('handleLogout')
    expect(homeSource).not.toContain('btn-logout')
  })

  it('shows guest login prompts on the profile page', () => {
    expect(profileSource).toContain('登录后保存成绩、查看记录、参与排行')
    expect(profileSource).toContain('登录后可查看你的考试记录')
    expect(profileSource).toContain("router.push('/login?redirect=/profile')")
  })
  it('loads exam records when login state changes on the profile page', () => {
    expect(profileSource).toContain('watch(')
    expect(profileSource).toContain('authStore.isLoggedIn')
    expect(profileSource).toContain('recordStore.fetchMyRecords()')
  })
})

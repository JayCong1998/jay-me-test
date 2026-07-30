import { describe, expect, it } from 'vitest'
import { validateEmail, validateNickname, validatePassword } from './authValidation'

describe('authentication form validation', () => {
  it('rejects malformed email addresses', () => {
    expect(validateEmail('jay')).toBe('请输入正确的邮箱地址')
  })

  it('requires passwords to contain 6 to 10 characters', () => {
    expect(validatePassword('12345')).toBe('密码长度为 6–10 位')
    expect(validatePassword('12345678901')).toBe('密码长度为 6–10 位')
    expect(validatePassword('123456')).toBe('')
  })

  it('requires nicknames to contain 2 to 10 trimmed characters', () => {
    expect(validateNickname('周')).toBe('昵称长度为 2–10 个字符')
    expect(validateNickname('周杰伦JayChou!')).toBe('昵称长度为 2–10 个字符')
    expect(validateNickname('  Jay  ')).toBe('')
  })
})

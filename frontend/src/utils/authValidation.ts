const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function validateEmail(value: string): string {
  const email = value.trim()
  if (!email) return '邮箱不能为空'
  return EMAIL_PATTERN.test(email) ? '' : '请输入正确的邮箱地址'
}

export function validatePassword(value: string): string {
  if (!value) return '密码不能为空'
  return value.length >= 6 && value.length <= 10 ? '' : '密码长度为 6–10 位'
}

export function validateNickname(value: string): string {
  const nickname = value.trim()
  if (!nickname) return '昵称不能为空'
  return nickname.length >= 2 && nickname.length <= 10 ? '' : '昵称长度为 2–10 个字符'
}

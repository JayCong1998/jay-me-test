/**
 * 生成游客昵称：游客 + 时间戳36进制（保证唯一）
 */
export function generateGuestNickname(): string {
  const ts = Date.now().toString(36).toUpperCase()
  return `游客${ts}`
}

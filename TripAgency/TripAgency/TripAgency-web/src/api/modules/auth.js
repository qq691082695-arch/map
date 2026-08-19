// 管理员登录由前端完成，后端不提供登录接口（AGENTS.md §5.1）
// 固定演示账号 + 本地 token，仅属于前端实现
const ACCOUNT = { username: 'admin', password: '123456', nickname: '系统管理员', role: '超级管理员' }

export const login = ({ username, password }) =>
  new Promise((resolve, reject) => {
    setTimeout(() => {
      if (username === ACCOUNT.username && password === ACCOUNT.password) {
        resolve({
          token: `local-token-${Date.now()}`,
          user: { username: ACCOUNT.username, nickname: ACCOUNT.nickname, role: ACCOUNT.role }
        })
      } else {
        reject(new Error('账号或密码错误，请使用 admin / 123456 登录'))
      }
    }, 200)
  })

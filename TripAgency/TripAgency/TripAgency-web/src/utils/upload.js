import { ElMessage } from 'element-plus'

const MAX_SIZE = 5 * 1024 * 1024
export const validateImage = (file) => {
  if (file.size > MAX_SIZE) {
    ElMessage.warning('图片大小不能超过 5MB')
    return false
  }
  return true
}

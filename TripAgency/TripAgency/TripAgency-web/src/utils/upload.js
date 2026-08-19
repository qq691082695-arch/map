import { ElMessage } from 'element-plus'

const MAX_SIZE = 5 * 1024 * 1024
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']

export const validateImage = (file) => {
  if (!ALLOWED_TYPES.includes(file.type)) {
    ElMessage.warning('仅支持 JPG / PNG / WebP 图片')
    return false
  }
  if (file.size > MAX_SIZE) {
    ElMessage.warning('图片大小不能超过 5MB')
    return false
  }
  return true
}
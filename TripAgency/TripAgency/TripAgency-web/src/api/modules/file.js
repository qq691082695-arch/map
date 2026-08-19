import { http } from '../request'

export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http('post', '/api/v1/admin/files/images', { data: formData })
}

<template>
  <div class="multi-image-uploader">
    <div class="image-grid">
      <div v-for="(image, index) in images" :key="image.resourceId || image.url" class="image-tile">
        <el-image
          class="image-preview"
          :src="image.url"
          :preview-src-list="previewUrls"
          :initial-index="index"
          preview-teleported
          fit="contain"
        />
        <span v-if="index === 0" class="cover-badge">封面</span>
        <div class="image-actions">
          <el-tooltip content="左移" placement="top">
            <button class="icon-action" :disabled="index === 0 || disabled" type="button" @click="move(index, -1)">
              <el-icon><ArrowLeft /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="预览原图" placement="top">
            <button class="icon-action" type="button" @click="preview(index)"><el-icon><ZoomIn /></el-icon></button>
          </el-tooltip>
          <el-tooltip content="删除图片" placement="top">
            <button class="icon-action danger" :disabled="disabled" type="button" @click="remove(index)">
              <el-icon><Delete /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="右移" placement="top">
            <button class="icon-action" :disabled="index === images.length - 1 || disabled" type="button" @click="move(index, 1)">
              <el-icon><ArrowRight /></el-icon>
            </button>
          </el-tooltip>
        </div>
      </div>

      <el-upload
        v-if="images.length < max"
        class="upload-entry"
        accept="image/jpeg,image/png,image/webp"
        multiple
        :disabled="disabled || uploadingCount > 0"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :http-request="handleUpload"
      >
        <div class="upload-tile" :class="{ disabled: disabled || uploadingCount > 0 }">
          <el-icon v-if="!uploadingCount" class="upload-icon"><Plus /></el-icon>
          <el-icon v-else class="upload-icon is-loading"><Loading /></el-icon>
          <span>{{ uploadingCount ? `上传中 ${uploadingCount}` : '上传图片' }}</span>
          <small>还可上传 {{ remaining }} 张</small>
        </div>
      </el-upload>
    </div>

    <div class="upload-help">
      支持 JPG、PNG、WebP，单张不超过 5MB；第一张作为封面，可使用箭头调整顺序。
    </div>

    <el-image-viewer
      v-if="viewerVisible"
      :url-list="previewUrls"
      :initial-index="viewerIndex"
      @close="viewerVisible = false"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Delete, Loading, Plus, ZoomIn } from '@element-plus/icons-vue'
import { validateImage } from '@/utils/upload'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  upload: { type: Function, required: true },
  max: { type: Number, default: 20 },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])
const uploadingCount = ref(0)
const viewerVisible = ref(false)
const viewerIndex = ref(0)

const images = computed(() => props.modelValue || [])
const previewUrls = computed(() => images.value.map(image => image.url).filter(Boolean))
const remaining = computed(() => Math.max(0, props.max - images.value.length - uploadingCount.value))

const beforeUpload = (file) => {
  if (images.value.length + uploadingCount.value >= props.max) {
    ElMessage.warning(`最多上传 ${props.max} 张图片`)
    return false
  }
  return validateImage(file)
}

const handleUpload = async ({ file }) => {
  if (images.value.length + uploadingCount.value >= props.max) return
  uploadingCount.value += 1
  try {
    const uploaded = await props.upload(file)
    if (!uploaded) return
    const duplicated = images.value.some(image => image.resourceId === uploaded.resourceId)
    if (duplicated) {
      ElMessage.warning('该图片已存在')
      return
    }
    emit('update:modelValue', [...images.value, uploaded])
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error(error?.message || '图片上传失败，请重试')
  } finally {
    uploadingCount.value -= 1
  }
}

const move = (index, offset) => {
  const target = index + offset
  if (target < 0 || target >= images.value.length) return
  const next = [...images.value]
  const [item] = next.splice(index, 1)
  next.splice(target, 0, item)
  emit('update:modelValue', next)
}

const preview = (index) => {
  viewerIndex.value = index
  viewerVisible.value = true
}

const remove = async (index) => {
  try {
    await ElMessageBox.confirm(
      index === 0 && images.value.length > 1 ? '删除封面后，下一张图片将成为新封面。确定删除吗？' : '确定从当前服务商移除这张图片吗？',
      '删除图片',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' }
    )
    emit('update:modelValue', images.value.filter((_, current) => current !== index))
    ElMessage.success('图片已移除，保存服务商后生效')
  } catch (_) {
    // 用户取消删除。
  }
}
</script>

<style scoped>
.multi-image-uploader { width: 100%; }
.image-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(138px, 1fr)); gap: 14px; width: 100%; }
.image-tile { position: relative; overflow: hidden; width: 100%; aspect-ratio: 4 / 3; border: 1px solid #dcdfe6; border-radius: 10px; background: #f5f7fa; }
.image-preview { width: 100%; height: 100%; }
.cover-badge { position: absolute; top: 8px; left: 8px; padding: 2px 8px; border-radius: 10px; color: #fff; background: #409eff; font-size: 12px; }
.image-actions { position: absolute; right: 0; bottom: 0; left: 0; display: flex; justify-content: center; gap: 5px; padding: 8px; background: linear-gradient(transparent, rgba(0, 0, 0, .72)); opacity: 0; transition: opacity .18s; }
.image-tile:hover .image-actions { opacity: 1; }
.icon-action { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; padding: 0; border: 0; border-radius: 50%; color: #fff; background: rgba(255, 255, 255, .18); cursor: pointer; }
.icon-action:hover:not(:disabled) { background: rgba(255, 255, 255, .34); }
.icon-action.danger:hover:not(:disabled) { background: #f56c6c; }
.icon-action:disabled { opacity: .35; cursor: not-allowed; }
.upload-entry { width: 100%; }
.upload-tile { display: flex; box-sizing: border-box; width: 100%; aspect-ratio: 4 / 3; flex-direction: column; align-items: center; justify-content: center; border: 1px dashed #b8c0cc; border-radius: 10px; color: #606266; background: #fafcff; cursor: pointer; transition: all .18s; }
.upload-tile:hover { border-color: #409eff; color: #409eff; background: #f2f8ff; }
.upload-tile.disabled { opacity: .65; cursor: not-allowed; }
.upload-icon { margin-bottom: 7px; font-size: 25px; }
.upload-tile small { margin-top: 4px; color: #a0a5ad; font-size: 12px; }
.upload-help { margin-top: 10px; color: #909399; font-size: 12px; line-height: 1.5; }
</style>

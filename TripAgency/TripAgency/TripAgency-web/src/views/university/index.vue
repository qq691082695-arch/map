<template>
  <div class="page-container">
    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索高校名称"
        clearable
        style="width: 260px"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-select v-model="query.show" placeholder="是否展示" clearable style="width: 140px" @change="handleSearch">
        <el-option label="展示" :value="true" />
        <el-option label="隐藏" :value="false" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="toolbar-right">
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          新增高校
        </el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" stripe>
        <template #empty><el-empty description="暂无高校数据" :image-size="90" /></template>
        <el-table-column prop="id" label="ID" width="55" />
        <el-table-column label="高校名称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="univ-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="坐标点数" width="90">
          <template #default="{ row }">
            <span class="coord-count">{{ row.polygonPoints ? row.polygonPoints.length : 0 }} 点</span>
          </template>
        </el-table-column>
        <el-table-column label="高校介绍" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.intro }}</template>
        </el-table-column>
        <el-table-column label="是否展示" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 'ENABLED'"
              inline-prompt
              active-text="展示"
              inactive-text="隐藏"
              @change="toggleShow(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="total"
          :page-sizes="[5, 10, 20]"
          :page-size="query.size"
          v-model:current-page="query.page"
          @current-change="fetchList"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑高校' : '新增高校'" width="620px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="高校名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入高校名称" />
        </el-form-item>
        <el-form-item label="坐标区域" required>
          <div class="area-editor">
            <div v-for="(p, idx) in form.polygonPoints" :key="idx" class="area-row">
              <span class="area-index">{{ idx + 1 }}</span>
              <el-input-number
                v-model="p.longitude"
                :min="-180"
                :max="180"
                :precision="6"
                :step="0.000001"
                controls-position="right"
                placeholder="经度"
                style="width: 180px"
              />
              <el-input-number
                v-model="p.latitude"
                :min="-90"
                :max="90"
                :precision="6"
                :step="0.000001"
                controls-position="right"
                placeholder="纬度"
                style="width: 180px"
              />
              <el-button link type="danger" :icon="Delete" @click="removePoint(idx)" />
            </div>
            <div class="area-add" @click="addPoint">
              <el-icon><Plus /></el-icon>
              <span>添加坐标点</span>
            </div>
            <div class="area-tip">按顺序添加至少 3 个不同坐标点，无需重复首点。</div>
          </div>
        </el-form-item>
        <el-form-item label="高校介绍" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="请输入高校介绍" />
        </el-form-item>
        <el-form-item label="展示图片">
          <div class="university-images">
            <div v-for="(image, idx) in form.images" :key="image.resourceId" class="university-image">
              <el-image :src="image.url" :preview-src-list="form.images.map(item => item.url)" preview-teleported :initial-index="idx" fit="cover" />
              <el-button link type="danger" @click="form.images.splice(idx, 1)">移除</el-button>
            </div>
            <el-upload accept="image/jpeg,image/png,image/webp" :show-file-list="false" :before-upload="validateImage" :http-request="uploadImage">
              <el-button :loading="uploading" :icon="Plus">上传图片</el-button>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import { api } from '@/api'
import { validateImage } from '@/utils/upload'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', show: null, page: 1, size: 10 })

const dialogVisible = ref(false)
const formRef = ref()
const emptyForm = () => ({ id: null, name: '', intro: '', polygonPoints: [], images: [] })
const form = reactive(emptyForm())
const uploading = ref(false)

const formRules = {
  name: [{ required: true, message: '请输入高校名称', trigger: 'blur' }],
  intro: [{ required: true, message: '请输入高校介绍', trigger: 'blur' }]
}

// ============ 坐标区域编辑 ============
const addPoint = () => {
  form.polygonPoints.push({ longitude: null, latitude: null })
}

const removePoint = (idx) => {
  form.polygonPoints.splice(idx, 1)
}

// ============ 高校列表 ============
const fetchList = async () => {
  loading.value = true
  try {
    const data = await api.getUniversities(query)
    list.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchList()
}

const handleReset = () => {
  query.keyword = ''
  query.show = null
  query.page = 1
  fetchList()
}

const handleSizeChange = () => {
  query.page = 1
  fetchList()
}

const openDialog = (row) => {
  Object.assign(form, emptyForm(), row ? JSON.parse(JSON.stringify(row)) : {})
  if (!row) {
    addPoint()
    addPoint()
    addPoint()
  }
  dialogVisible.value = true
}

const uploadImage = async ({ file }) => {
  if (form.images.length >= 20) return ElMessage.warning('最多上传 20 张图片')
  uploading.value = true
  try {
    const image = await api.uploadImage(file)
    form.images.push(image)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    uploading.value = false
  }
}

const handleSave = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const areaMsg = checkArea()
    if (areaMsg) {
      ElMessage.warning(areaMsg)
      return
    }
    saving.value = true
    try {
      const payload = { name: form.name, intro: form.intro, polygonPoints: form.polygonPoints, imageResourceIds: form.images.map(image => image.resourceId) }
      if (form.id) {
        await api.updateUniversity({ id: form.id, ...payload })
        ElMessage.success('保存成功')
      } else {
        await api.addUniversity(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchList()
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      saving.value = false
    }
  })
}

const checkArea = () => {
  const area = form.polygonPoints
  if (!area.length) return '请添加坐标点，圈定高校区域'
  if (area.length < 3) return '坐标区域至少需要 3 个点'
  for (let i = 0; i < area.length; i++) {
    const p = area[i]
    if (p.longitude == null || p.latitude == null || p.longitude === '' || p.latitude === '') return `第 ${i + 1} 个坐标点未填写完整`
    if (p.longitude < -180 || p.longitude > 180) return `第 ${i + 1} 个坐标点经度超出范围`
    if (p.latitude < -90 || p.latitude > 90) return `第 ${i + 1} 个坐标点纬度超出范围`
  }
  const seen = new Set()
  for (const p of area) {
    const key = `${p.longitude},${p.latitude}`
    if (seen.has(key)) return '坐标区域存在重复点，请重新确认'
    seen.add(key)
  }
  return ''
}

const toggleShow = async (row) => {
  try {
    const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    await api.updateUniversityStatus(row.id, nextStatus)
    ElMessage.success(nextStatus === 'ENABLED' ? '已展示' : '已隐藏')
  } catch (error) {
    ElMessage.error(error.message)
  }
  fetchList()
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除高校「${row.name}」吗？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await api.deleteUniversity(row.id)
        ElMessage.success('删除成功')
        fetchList()
      } catch (error) {
        ElMessage.error(error.message)
      }
    })
    .catch(() => {})
}

onMounted(fetchList)
</script>

<style scoped>
.toolbar-right {
  margin-left: auto;
}
.univ-name {
  font-weight: 600;
}
.coord-count {
  color: #909399;
  font-size: 12px;
}
.university-images { display: flex; flex-wrap: wrap; gap: 12px; }
.university-image { display: flex; flex-direction: column; width: 96px; }
.university-image .el-image { width: 96px; height: 64px; border-radius: 6px; }

/* 坐标区域编辑器 */
.area-editor {
  width: 100%;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  padding: 10px;
  background: #fafafa;
}
.area-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.area-index {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #1677ff;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.area-add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 36px;
  border: 1px dashed #c0c4cc;
  border-radius: 6px;
  color: #8c8c8c;
  font-size: 13px;
  cursor: pointer;
  background: #fff;
}
.area-add:hover {
  border-color: #1677ff;
  color: #1677ff;
}
.area-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>

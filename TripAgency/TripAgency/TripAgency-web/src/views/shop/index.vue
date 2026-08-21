<template>
  <div class="page-container">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="名称 / 地址" clearable style="width:240px" :prefix-icon="Search" @keyup.enter="search" @clear="search" />
      <el-select v-model="query.type" placeholder="全部分类" clearable style="width:130px" @change="search">
        <el-option v-for="item in types" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width:130px" @change="search">
        <el-option label="启用" :value="1" /><el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="search"><el-icon><Search /></el-icon>查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="toolbar-right"><el-button type="primary" @click="openForm()"><el-icon><Plus /></el-icon>新增服务商</el-button></div>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="65" />
        <el-table-column prop="name" label="服务商名称" min-width="150" />
        <el-table-column label="类型" width="90"><template #default="{row}"><el-tag>{{ typeLabel(row.businessType) }}</el-tag></template></el-table-column>
        <el-table-column label="图片" width="120"><template #default="{row}">
          <div v-if="row.images?.length" class="table-image-cell">
            <el-image :src="row.images[0].url" :preview-src-list="row.images.map(i => i.url)" preview-teleported :initial-index="0" class="thumb" fit="contain" />
            <span v-if="row.images.length > 1" class="image-count">共 {{ row.images.length }} 张</span>
          </div>
          <span v-else class="no-image">暂无图片</span>
        </template></el-table-column>
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="坐标" min-width="150"><template #default="{row}">{{ row.longitude }}, {{ row.latitude }}</template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{row}"><el-switch :model-value="row.status === 'ENABLED'" @change="toggleBusiness(row)" /></template></el-table-column>
        <el-table-column label="操作" width="190" fixed="right"><template #default="{row}">
          <el-button link type="primary" @click="openForm(row)">编辑</el-button>
          <el-button link type="primary" @click="openResources(row)">附属资源</el-button>
          <el-button link type="danger" @click="removeBusiness(row)">删除</el-button>
        </template></el-table-column>
      </el-table>
      <div class="pagination-row"><el-pagination background layout="total,prev,pager,next,sizes" :total="total" :page-sizes="[10,20,50]" :page-size="query.size" v-model:current-page="query.page" @current-change="fetchList" @size-change="sizeChange" /></div>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑服务商' : '新增服务商'" width="760px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="105px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型" prop="businessType"><el-select v-model="form.businessType" :disabled="!!form.id" style="width:100%"><el-option v-for="item in types" :key="item.api" :label="item.label" :value="item.api" /></el-select></el-form-item>
        <el-form-item label="地址" prop="address"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="经度" prop="longitude"><el-input-number v-model="form.longitude" :min="-180" :max="180" :precision="6" /></el-form-item>
        <el-form-item label="纬度" prop="latitude"><el-input-number v-model="form.latitude" :min="-90" :max="90" :precision="6" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.intro" type="textarea" :rows="3" /></el-form-item>
        <template v-if="form.businessType === 'FOOD'">
          <el-form-item label="餐饮联系人"><el-input v-model="form.foodContactName" /></el-form-item>
          <el-form-item label="联系电话"><el-input v-model="form.foodContactPhone" /></el-form-item>
        </template>
        <el-form-item label="展示图片">
          <MultiImageUploader v-model="form.images" :upload="api.uploadImage" :max="20" :disabled="saving" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="formVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="saveBusiness">保存</el-button></template>
    </el-dialog>

    <el-drawer v-model="resourceVisible" :title="resourceTitle" size="720px">
      <div class="resource-head"><span>只展示当前服务商的{{ resourceLabel }}</span><el-button type="primary" size="small" @click="openResourceForm()"><el-icon><Plus /></el-icon>新增{{ resourceLabel }}</el-button></div>
      <el-table v-loading="resourceLoading" :data="resources" stripe>
        <template #empty><el-empty description="暂无资源，点击右上角新增" :image-size="80" /></template>
        <el-table-column label="图片" width="92"><template #default="{row}"><el-image v-if="row.imageUrl" :src="row.imageUrl" :preview-src-list="[row.imageUrl]" preview-teleported class="resource-thumb" fit="contain" /><span v-else class="no-image">暂无</span></template></el-table-column>
        <el-table-column prop="name" :label="resourceLabel" min-width="130" />
        <el-table-column v-if="current?.businessType === 'TRAVEL'" prop="seatNum" label="座位数" width="85" />
        <el-table-column v-if="current?.businessType === 'HOTEL'" prop="bedSpec" label="床型规格" min-width="120" />
        <el-table-column v-if="current?.businessType === 'FOOD'" prop="sortNo" label="排序" width="75" />
        <el-table-column v-if="current?.businessType === 'FOOD'" label="推荐" width="75"><template #default="{row}"><el-tag :type="row.recommended ? 'success' : 'info'">{{ row.recommended ? '推荐' : '普通' }}</el-tag></template></el-table-column>
        <el-table-column prop="description" label="说明" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="80"><template #default="{row}"><el-switch :model-value="row.status === 'ENABLED'" @change="toggleResource(row)" /></template></el-table-column>
        <el-table-column label="操作" width="120"><template #default="{row}"><el-button link type="primary" @click="openResourceForm(row)">编辑</el-button><el-button link type="danger" @click="removeResource(row)">删除</el-button></template></el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="resourceFormVisible" :title="resourceForm.id ? `编辑${resourceLabel}` : `新增${resourceLabel}`" width="760px">
      <el-form ref="resourceFormRef" :model="resourceForm" label-width="90px">
        <el-form-item :label="resourceLabel" required><el-input v-model="resourceForm.name" /></el-form-item>
        <el-form-item v-if="current?.businessType === 'TRAVEL'" label="座位数" required><el-input-number v-model="resourceForm.seatNum" :min="1" /></el-form-item>
        <el-form-item v-if="current?.businessType === 'HOTEL'" label="床铺配置" required>
          <div class="bed-editor">
            <div v-for="(bed,index) in resourceForm.beds" :key="index" class="bed-row">
              <el-input-number v-model="bed.length" :min="50" :max="300" :step="10" controls-position="right" /><span>长(cm)</span>
              <el-input-number v-model="bed.width" :min="50" :max="300" :step="10" controls-position="right" /><span>宽(cm)</span>
              <el-input-number v-model="bed.quantity" :min="1" :max="20" controls-position="right" /><span>张</span>
              <el-button v-if="resourceForm.beds.length > 1" link type="danger" @click="removeBed(index)">删除</el-button>
            </div>
            <el-button type="primary" plain :icon="Plus" @click="addBed">添加一行床铺</el-button>
          </div>
        </el-form-item>
        <el-form-item v-if="current?.businessType === 'FOOD'" label="排序" required><el-input-number v-model="resourceForm.sortNo" :min="0" /></el-form-item>
        <el-form-item v-if="current?.businessType === 'FOOD'" label="推荐菜品"><el-switch v-model="resourceForm.recommended" active-text="推荐" inactive-text="普通" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="resourceForm.description" type="textarea" /></el-form-item>
        <el-form-item label="图片">
          <div class="resource-image-wrap">
            <el-image v-if="resourceForm.imageUrl" :src="resourceForm.imageUrl" :preview-src-list="[resourceForm.imageUrl]" preview-teleported class="resource-image" fit="cover" />
            <el-upload accept="image/jpeg,image/png,image/webp" :show-file-list="false" :before-upload="validateImage" :http-request="uploadResourceImage"><el-button :loading="uploading">{{ resourceForm.imageUrl ? '替换图片' : '上传图片' }}</el-button></el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="resourceFormVisible=false">取消</el-button><el-button type="primary" :loading="resourceSaving" @click="saveResource">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { api } from '@/api'
import { validateImage } from '@/utils/upload'
import MultiImageUploader from '@/components/MultiImageUploader.vue'

const types = [{ value:'travel', api:'TRAVEL', label:'出行' },{ value:'hotel', api:'HOTEL', label:'住宿' },{ value:'food', api:'FOOD', label:'餐饮' }]
const typeLabel = (type) => types.find(i => i.api === type)?.label || type
const typeValue = (type) => types.find(i => i.api === type)?.value
const loading=ref(false), saving=ref(false), uploading=ref(false), list=ref([]), total=ref(0)
const query=reactive({keyword:'',type:'',status:'',page:1,size:10})
const emptyForm=()=>({id:null,name:'',address:'',longitude:null,latitude:null,businessType:'TRAVEL',intro:'',foodContactName:'',foodContactPhone:'',images:[]})
const form=reactive(emptyForm()), formVisible=ref(false), formRef=ref()
const rules={name:[{required:true,message:'请输入名称'}],address:[{required:true,message:'请输入地址'}],businessType:[{required:true,message:'请选择类型'}],longitude:[{required:true,message:'请输入经度'}],latitude:[{required:true,message:'请输入纬度'}]}

const run=async(fn)=>{try{return await fn()}catch(e){ElMessage.error(e.message);return null}}
const fetchList=async()=>{loading.value=true;try{const data=await run(()=>api.getBusinesses(query));if(data){list.value=data.items;total.value=data.total}}finally{loading.value=false}}
const search=()=>{query.page=1;fetchList()}; const reset=()=>{Object.assign(query,{keyword:'',type:'',status:'',page:1});fetchList()}; const sizeChange=()=>{query.page=1;fetchList()}
const openForm=(row)=>{Object.assign(form,emptyForm(),row?JSON.parse(JSON.stringify(row)):{});formVisible.value=true}
const businessPayload=()=>({name:form.name,address:form.address,longitude:form.longitude,latitude:form.latitude,businessType:form.businessType,intro:form.intro||null,foodContactName:form.businessType==='FOOD'?(form.foodContactName||null):null,foodContactPhone:form.businessType==='FOOD'?(form.foodContactPhone||null):null,foodRecommendedDishes:null,imageResourceIds:form.images.map(i=>i.resourceId)})
const saveBusiness=()=>formRef.value.validate(async valid=>{if(!valid)return;saving.value=true;try{const payload=businessPayload();const r=await run(()=>form.id?api.updateBusiness({id:form.id,...payload}):api.addBusiness(payload));if(r){ElMessage.success(form.id?'保存成功':'新增成功');formVisible.value=false;fetchList()}}finally{saving.value=false}})
const toggleBusiness=async row=>{const r=await run(()=>api.updateBusinessStatus(row.id,row.status==='ENABLED'?'DISABLED':'ENABLED'));if(r){ElMessage.success('操作成功');fetchList()}}
const removeBusiness=row=>ElMessageBox.confirm(`确定逻辑删除「${row.name}」吗？`,'提示',{type:'warning'}).then(async()=>{const r=await run(()=>api.deleteBusiness(row.id));if(r){ElMessage.success('删除成功');fetchList()}}).catch(()=>{})

const current=ref(null), resourceVisible=ref(false), resources=ref([]), resourceLoading=ref(false), resourceSaving=ref(false), resourceFormVisible=ref(false)
const resourceLabel=computed(()=>({TRAVEL:'车辆',HOTEL:'房型',FOOD:'菜品'}[current.value?.businessType]||'资源'))
const resourceTitle=computed(()=>current.value?`${current.value.name} - ${resourceLabel.value}管理`:'附属资源')
const emptyBed=()=>({length:180,width:200,quantity:1})
const parseBeds=value=>{const matches=[...String(value||'').matchAll(/(\d+)\s*[×x]\s*(\d+)\s*cm\s*[×x]\s*(\d+)\s*张?/gi)];return matches.length?matches.map(item=>({length:Number(item[1]),width:Number(item[2]),quantity:Number(item[3])})):[emptyBed()]}
const formatBeds=beds=>beds.map(item=>`${item.length}×${item.width}cm×${item.quantity}张`).join('；')
const emptyResource=()=>({id:null,name:'',seatNum:1,bedSpec:'',beds:[emptyBed()],description:'',imageResourceId:null,imageUrl:null,sortNo:0,recommended:false})
const resourceForm=reactive(emptyResource())
const loadResources=async()=>{resourceLoading.value=true;try{const data=await run(()=>api.getBusinessResources(current.value.id,typeValue(current.value.businessType),{page:1,size:100}));if(data)resources.value=data.items}finally{resourceLoading.value=false}}
const openResources=row=>{current.value=row;resourceVisible.value=true;loadResources()}; const openResourceForm=row=>{Object.assign(resourceForm,emptyResource(),row||{});resourceForm.beds=parseBeds(row?.bedSpec);resourceFormVisible.value=true}
const addBed=()=>resourceForm.beds.push(emptyBed())
const removeBed=index=>resourceForm.beds.splice(index,1)
const uploadResourceImage=async({file})=>{uploading.value=true;try{const data=await run(()=>api.uploadImage(file));if(data){resourceForm.imageResourceId=data.resourceId;resourceForm.imageUrl=data.url}}finally{uploading.value=false}}
const resourcePayload=()=>current.value.businessType==='TRAVEL'?{model:resourceForm.name,seatNum:resourceForm.seatNum,description:resourceForm.description||null,imageResourceId:resourceForm.imageResourceId}:current.value.businessType==='HOTEL'?{name:resourceForm.name,bedSpec:formatBeds(resourceForm.beds),description:resourceForm.description||null,imageResourceId:resourceForm.imageResourceId}:{name:resourceForm.name,description:resourceForm.description||null,imageResourceId:resourceForm.imageResourceId,sortNo:resourceForm.sortNo,recommended:resourceForm.recommended}
const saveResource=async()=>{if(!resourceForm.name)return ElMessage.warning(`请输入${resourceLabel.value}`);resourceSaving.value=true;try{const type=typeValue(current.value.businessType),payload=resourcePayload();const r=await run(()=>resourceForm.id?api.updateBusinessResource(current.value.id,type,{id:resourceForm.id,...payload}):api.addBusinessResource(current.value.id,type,payload));if(r){ElMessage.success(resourceForm.id?'保存成功':'新增成功');resourceFormVisible.value=false;loadResources()}}finally{resourceSaving.value=false}}
const toggleResource=async row=>{const r=await run(()=>api.updateBusinessResourceStatus(current.value.id,typeValue(current.value.businessType),row.id,row.status==='ENABLED'?'DISABLED':'ENABLED'));if(r){ElMessage.success('操作成功');loadResources()}}
const removeResource=row=>ElMessageBox.confirm(`确定逻辑删除「${row.name}」吗？`,'提示',{type:'warning'}).then(async()=>{const r=await run(()=>api.deleteBusinessResource(current.value.id,typeValue(current.value.businessType),row.id));if(r){ElMessage.success('删除成功');loadResources()}}).catch(()=>{})
onMounted(fetchList)
</script>

<style scoped>
.toolbar-right{margin-left:auto}.table-image-cell{display:flex;align-items:center;gap:8px}.thumb{width:64px;height:48px;border:1px solid #ebeef5;border-radius:7px;background:#f5f7fa}.image-count{font-size:12px;color:#606266;white-space:nowrap}.no-image{font-size:12px;color:#a8abb2}.resource-thumb{width:68px;height:51px;border:1px solid #ebeef5;border-radius:6px;background:#f5f7fa}.resource-image-wrap{display:flex;flex-direction:column;gap:8px}.resource-image{width:120px;height:80px;border-radius:6px}.resource-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}.bed-editor{display:flex;flex-direction:column;gap:10px;width:100%}.bed-row{display:flex;align-items:center;gap:7px;white-space:nowrap}.bed-row .el-input-number{width:112px}.bed-row span{margin-left:-3px;color:#606266;font-size:12px}
</style>

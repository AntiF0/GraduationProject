<template>
  <div style="width: 1020px;height: 670px;position: relative;text-align: center;overflow: hidden">

    <div style="margin: 15px 20px 15px 20px;">
      <!-- 新增按钮 -->
<!--      <el-button type="primary" @click="add">-->
<!--        <el-icon><plus /></el-icon>&nbsp;-->
<!--        新增-->
<!--      </el-button>-->
      <!-- 搜索区域 -->
      <el-input v-model="search" placeholder="请输入关键字" style="width: 20%;margin-left: 10px" clearable @input="load"/>
      <el-button type="primary" style="margin-left: 10px" @click="load">
        <el-icon><search /></el-icon>&nbsp;
        查询
      </el-button>
      <!-- 重置按钮 -->
      <el-button type="primary" @click="reset">重置</el-button>
    </div>

    <!-- 数据展示区域 -->
    <div style="background-color: khaki;position: absolute;left: 50%;transform: translate(-50%)">
      <el-table :data="tableData" border stripe style="width: 900px;">
        <!-- 数据展示 -->
        <el-table-column prop="tid" label="任务Id" width="90px" sortable/>
        <el-table-column prop="tuserId" label="用户Id" width="70px"/>
        <el-table-column prop="tname" label="任务名" />
        <el-table-column prop="ttaskMode" label="任务类型" width="90px" />
        <el-table-column prop="tfocusedNum" label="已专注次数" width="100px" />
        <el-table-column prop="tfocusedTime" label="已专注时长" width="100px" />
        <!-- 表格功能 -->
        <el-table-column fixed="right" label="操作" width="100px">
          <template #default="scope">
            <!-- 详情 -->
            <el-button type="primary" @click="handleEdit(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页区域 -->
    <div style="margin-top: 15px;background-color: aqua;position: relative">
      <el-pagination
          v-model:currentPage="currentPage"
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          style="position: absolute;left: 50%;transform: translate(-50%, 560px)">
      </el-pagination>
    </div>

    <!-- 弹窗 功能:查看/编辑任务 -->
    <el-dialog v-model="dialogVisible" width="60%">
      <!-- 任务细节展示 -->
      <el-descriptions title="任务详情" border :column="2">
        <el-descriptions-item label="任务名" :span="2"> {{ form.tname }}</el-descriptions-item>
        <!--        <el-descriptions-item label="所属用户"> {{ currentUserName }} </el-descriptions-item>-->
        <el-descriptions-item label="计时方法">
          <el-tag >{{ form.ttaskMode }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间"> {{form.tcreateDate}} </el-descriptions-item>
        <el-descriptions-item label="专注次数">共 {{ form.tfocusedNum }} 次</el-descriptions-item>
        <el-descriptions-item label="专注时长">共 {{ form.tfocusedTime }} 分钟</el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2"> {{ form.tdescribe }} </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span>
          <el-button @click="isShowDetail = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 弹窗区域 -->
    <el-dialog title="提示" width="50%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="用户名">
          <el-input v-model="form.uname" style="width: 80%"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.upassword" style="width: 80%"></el-input>
        </el-form-item>
        <el-form-item label="名言">
          <el-input
              autosize
              type="textarea"
              v-model="form.usaying"
              style="width: 80%"
          >
          </el-input>
        </el-form-item>
        <el-form-item label="管理员">
          <el-input v-model="form.usuper" style="width: 80%"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";
import { Plus,Search } from '@element-plus/icons'
export default {
  name: "TaskInfo",
  components: {
    Plus,
    Search
  },
  data() {
    return {
      form: {},
      dialogVisible: false,
      search: '',
      currentPage: 1,
      pageSize: 10,
      total: 0, // 从后台获取
      tableData: [

      ],
      currentUserID: 0
    }
  },
  created() {
    this.load()
  },
  watch: {

  },
  methods: {
    load() {
      this.currentUserID = JSON.parse(window.sessionStorage.getItem('userinfo')).uid;
      request.get("/alltaskinfopage",{
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          search: this.search,
          userId: this.currentUserID
        }
      }).then(res => {
        this.tableData = res.data.records
        this.total = res.data.total
        console.log(this.tableData)
      })
    },
    add() {
      this.dialogVisible = true; // 显示表单
      this.form = {}; // 清空表单
    },
    save() {
      //如果有id,那么更新
      if (this.form.uid) {
        request.put("/userinfo", this.form).then(res => {
          console.log(res)
          if (res.code === '0') {
            this.$message({
              type: "success",
              message: "更新成功"
            })
          }
          else {
            this.$message({
              type: "error",
              message: res.msg
            })
          }
          this.load() //更新后刷新表格数据
          this.dialogVisible = false //关闭弹窗
        })
      }
      //如果没有,那么新增
      else {
        //引入request对象,this.form作为请求参数
        request.post("/userinfo", this.form).then(res => {
          console.log(res)
          if (res.code === '0') {
            this.$message({
              type: "success",
              message: "新增成功"
            })
          }
          else {
            this.$message({
              type: "error",
              message: res.msg
            })
          }
          this.load() //更新后刷新表格数据
          this.dialogVisible = false //关闭弹窗
        })
      }
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row)) //深拷贝,使得此时form对象与表格里的数据隔离开
      this.dialogVisible = true
    },
    handleDelete(uid) {
      // console.log(uid)
      request.delete("/userinfo/" + uid).then(res => {
        if (res.code === '0') {
          this.$message({
            type: "success",
            message: "删除成功"
          })
        }
        else {
          this.$message({
            type: "error",
            message: res.msg
          })
        }
        this.load() //删除后刷新表格数据
      })
    },
    handleSizeChange(pageSize) { //改变当前每页个数触发
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum) { //改变当前页码触发
      this.currentPage = pageNum
      this.load()
    },
    reset() {
      this.search = '';
      this.load();
    }
  }
}
</script>

<style scoped>

</style>
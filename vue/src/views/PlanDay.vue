<template>
  <div style="position: relative">
    <!-- 上方功能区域 -->
    <div style="position: relative; height: 60px;width: 1020px;">

      <!-- 日期选择器 -->
      <div class="Center">
        <div style="display: inline-block;margin-right: 10px;">
          {{pickedDate}} 想要做什么
        </div>
        <el-date-picker
            v-model="pickedDate"
            type="date"
            placeholder="请选择日期"
            value-format="YYYY-MM-DD"
        />
        <el-button style="margin-left: 10px" @click="chooseToday">选择今天</el-button>
      </div>
    </div>
    <!-- 任务展示区域 -->
    <div>
      <!-- 日程规划区域 -->
      <el-card style="width: 600px;height: 600px;margin-left: 30px;display: inline-block;">
        <div style="margin-bottom: 20px;overflow: auto">
          <el-scrollbar height="570px">
            <el-card v-for="item in pickedDateTaskInfo" style="font-size: 20px;margin-bottom: 10px;position: relative">
              {{item.tname}}
              <el-button style="position: absolute;right: 20px;top: 15px" @click="deleteTask(item)">删除</el-button>
            </el-card>
          </el-scrollbar>

        </div>
      </el-card>
      <!-- 显示未完成未删除区域 -->
      <el-card style="width: 320px;height: 600px;display: inline-block;margin-left: 30px;">
        <!-- 上方搜索框 -->
        <div style="padding-top: 10px;">
          <el-input style="width: 280px;" v-model="taskSearch" @input="getTaskData" placeholder="任务列表" clearable>
            <!--搜索icon 3行-->
            <template #prefix>
              <el-icon style="margin-top: 8px"><search /></el-icon>
            </template>
          </el-input>
        </div>
        <!-- 左侧任务展示列表 -->
        <el-card style="max-width: 280px; height: 510px; margin-top: 10px;">
          <el-scrollbar height="480px">
            <el-button v-for="item in taskData" @click="addTask(item)" style="font-size: 20px;width: 229px;height: 60px;font-size: 20px;margin-left: 0;margin-bottom: 10px">
              <div style="width: 200px; text-align: left;text-overflow:ellipsis;overflow: hidden;"> {{item.tname}} </div>
            </el-button>
          </el-scrollbar>
        </el-card>
      </el-card>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
import { Search } from '@element-plus/icons'

export default {
  name: "PlanDay",
  components: {
    Search
  },
  data() {
    return {
      pickedDate: " ",
      currentUserID: 0,
      pickedDateInfo: {},
      pickedDateTask: [], // 选中日期的当天任务数组
      pickedDateTaskInfo: [], // 任务数组对应的任务信息
      taskData: [], // 全部的任务信息
      taskSearch: "", // 全部任务信息的搜索
      existDate: [], // 已经存在数据库的日期信息,用于判断是更新还是新增
      tempDailyInfo: [], // 测试
    }
  },
  watch: {
    // pickedDate数据更改时触发,配合"今天"按钮
    pickedDate() {
      this.getPickedDateInfo();
    },
    pickedDateTask: {
      handler(newVal) {
        // console.log("检测到pickedDateTask更改");
        this.pickedDateTaskInfo = []; // 初始化任务数组对应的任务信息数组
        // console.log("pickedDateTask数组更新了");
        let len = this.pickedDateTask.length;
        for (let i=0; i<len; i++) {
          request.get("/alltaskinfo", {
            params: {
              userId: this.currentUserID,
              taskId: this.pickedDateTask[i]
            }
          }).then(res => {
            this.pickedDateTaskInfo.push(res.data[0]); // 这里的数据有问题
          })
        }
        // console.log(this.pickedDateTaskInfo);
      },
      deep: true,
      immediate: true // 立即响应
    },
    tempDailyInfo() {
      // console.log("检测到tempDailyInfo信息更改!");
      // 获取全部信息后填入日期信息
      for (let i=0; i<this.tempDailyInfo.length; i++) {
        this.existDate.push(this.tempDailyInfo[i].dDate);
      }
    }
  },
  created() {
    // 获取用户ID
    this.currentUserID = JSON.parse(window.sessionStorage.getItem('userinfo')).uid;
    this.getTaskData();
    this.getExistDateArr();
  },
  methods: {
    // 获取已经存在数据库的日期信息
    getExistDateArr() {
      request.get("/alldailyrecord",{
        params: {
          userId: this.currentUserID
        }
      }).then(res => {
        this.tempDailyInfo = res.data;
      })

      // // 获取全部信息后填入日期信息
      // for (let i=0; i<this.tempDailyInfo.length; i++) {
      //   console.log('存入一个日期');
      //   this.existDate.push(this.tempDailyInfo[i].dDate);
      // }
      // console.log(this.existDate);

    },
    // 日期选择器显示今天的日期
    chooseToday() {
      let today = new Date().toJSON().slice(0,10); // .replace(/-/g,'-');
      this.pickedDate = today;
    },
    // 获取被选择日期的信息
    getPickedDateInfo() {
      // 下面这种情况是有日期存在的
      if (this.existDate.indexOf(this.pickedDate) !== -1) {
        // 获取日期选择框内日期对应的数据
        request.get("/alldailyrecord",{
          params: {
            search: this.search,
            userId: this.currentUserID,
            date: this.pickedDate
          }
        }).then(res => {
          this.pickedDateInfo = res.data[0];
          // console.log(this.pickedDateInfo);
          // console.log("Success:数据库中有这个日期的数据!");
          this.pickedDateTask = JSON.parse(this.pickedDateInfo.dplanTask);
        })
      } else { // 没有日期存在的情况
        // console.log("Failed:数据库中没有这个日期的数据!");
        this.pickedDateTask = [];
      }
    },
    // 点击右侧任务列表加任务
    addTask(item) {
      // this.pickedDateTask.push(item.tid);
      this.pickedDateTask.push(JSON.parse(JSON.stringify(item.tid)));
      if (this.existDate.indexOf(this.pickedDate) !== -1) {
        // console.log("已有日期,更新!");
        // console.log(this.pickedDateTask);
        // 如果数组添加了数据,就要在数据库更新(不添加也更新)
        this.pickedDateInfo.dplanTask = "[" + this.pickedDateTask.toString() + "]";

        // 这种情况是刚刚新增了一条dailyrecord并且没有刷新页面的情况下,没有did就不能更新
        if (!this.pickedDateInfo.did) {
          // 获取did
          request.get("/dailyrecord",{
            params: {
              userId: this.currentUserID,
              date: this.pickedDate
            }
          }).then(res => {
            this.pickedDateInfo.did = res.data.records[0].did;
          })
        }
        // console.log(this.pickedDateInfo);
        request.put("/dailyrecord", this.pickedDateInfo).then(res => {
          if (res.code === '0') {
            // console.log("更新成功");
          } else {
            this.$message({
              type: "error",
              message: res.msg
            })
          }
        })
      } else { // 新建
        // console.log("没有日期,新增!");
        // console.log(this.existDate);
        // this.pickedDateInfo = [];
        this.pickedDateInfo.duserId = this.currentUserID;
        this.pickedDateInfo.ddate = this.pickedDate;
        this.pickedDateInfo.dplanTask = "[" + this.pickedDateTask.toString() + "]";
        this.pickedDateInfo.did = null;
        this.pickedDateInfo.dId = null;
        this.pickedDateInfo.dcontent = "";
        this.existDate.push(this.pickedDate);
        console.log(this.pickedDateInfo);
        request.post("/dailyrecord", this.pickedDateInfo).then(res => {
          if (res.code === '0') {
            this.$message({
              type: "success",
              message: "新增成功"
            })
          } else {
            this.$message({
              type: "error",
              message: res.msg
            })
          }
        })
        // this.tempDailyInfo.push(this.pickedDateInfo);
      }
    },
    // 获取全部的任务信息
    getTaskData() {
      request.get("/alltaskinfo",{
        params: {
          userId: this.currentUserID,
          search: this.taskSearch
        }
      }).then(res => {
        this.taskData = res.data
        // console.log(this.taskData)
      })
    },
    deleteTask(item) {
      // 修改pickedDateTask数组
      console.log(item.tid);
      let arr = Object.values(this.pickedDateTask);
      console.log(arr);
      for (let i=0; i<arr.length; i++) {
        if (arr[i] === item.tid) {
          arr.splice(i, 1);
          break;
        }
      }
      this.pickedDateTask = arr;

      // 将数据返回到后端
      // console.log(this.pickedDateInfo);
      this.pickedDateInfo.dplanTask = "[" + this.pickedDateTask.toString() + "]";
      request.put("/dailyrecord", this.pickedDateInfo).then(res => {
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
      })
    }
  }
}
</script>

<style scoped>
.Center {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}
</style>
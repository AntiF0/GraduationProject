<template>
  <div>
    <el-scrollbar height="670px">
    <!-- 总的数据展示 -->
    <el-card style="position: relative;width: 680px;margin-top: 10px;margin-left: 170px;height: 70px">
      <div style="position: absolute;top: 5px;width: 200px;text-align: center;line-height: 30px;">
        <span style="font-size: 14px">总专注次数</span><br>
        {{ userInfo.ufocusedNum }} 次
      </div>
      <div style="position: absolute;left: 240px;top: 5px;width: 200px;text-align: center;line-height: 30px">
        <span style="font-size: 14px">总专注时长</span><br>
        {{ userInfo.ufocusedTime }} 分钟
      </div>
      <div style="position: absolute;left: 460px;top: 5px;width: 200px;text-align: center;line-height: 30px">
        <span style="font-size: 14px">平均专注时长</span><br>
        {{ Math.floor(userInfo.ufocusedTime / userInfo.ufocusedNum) }} 分钟
      </div>
    </el-card>
    <!-- 图表类型选择区域 -->
    <el-card style="position: relative;margin-top: 10px;width: 800px;margin-left: 110px;height: 50px">
      <!-- 选择年月日 -->
      <div style="position: absolute;left: 50px;top: 5px" >
        <el-radio v-model="timeRangeOfChart" label="1" size="large" @click="clickRadioYear">年</el-radio>
        <el-radio v-model="timeRangeOfChart" label="2" size="large" @click="clickRadioMonth">月</el-radio>
        <el-radio v-model="timeRangeOfChart" label="3" size="large" @click="clickRadioDay" id="firstclick">日</el-radio>
      </div>
      <!-- 3个时间选择器 -->
      <div style="position: absolute;left: 290px;top: 10px">
        <!-- 年选择器 -->
        <el-date-picker
            v-if="isShowYearPicker"
            v-model="selectYear"
            type="year"
            placeholder="选择某年"
            value-format="YYYY"
            @change="drawChartPie"
        />
        <!-- 月选择器 -->
        <el-date-picker
            v-if="isShowMonthPicker"
            v-model="selectMonth"
            type="month"
            placeholder="选择某月"
            value-format="MM"
            @change="drawChartPie"
        />
        <!-- 日选择器 -->
        <el-date-picker
            v-if="isShowDayPicker"
            v-model="selectDay"
            type="date"
            placeholder="选择某天"
            value-format="YYYY-MM-DD"
            @change="drawChartPie"
        />
      </div>
      <!-- 选择图表类型 -->
      <div style="position: absolute;left: 570px;top: 10px;">
        <el-button type="primary" @click="drawChartPie">饼图</el-button>
        <el-button type="primary" @click="drawChartColumn">曲线图</el-button>
      </div>
    </el-card>

    <el-card style="position: relative;margin-top: 20px;margin-left: 35px;width: 950px;">
      <div v-show="isShowColumn" id="chartColumn" style="width: 700px;height: 450px;margin-left: 105px"></div>
      <div v-show="isShowPie" id="chartPie" style="width: 700px;height: 450px;margin-left: 105px"></div>
    </el-card>

    <el-card v-show="isShowDayDetail" style="width: 950px;margin-top: 20px;margin-left: 35px;margin-bottom: 20px">
      <!-- 汇总后的时间 -->
      <div style="background-color: #8c939d;width: 600px;margin-left: 170px">
        <el-descriptions border v-for="item in focusPieDayInfoSummary">
          <el-descriptions-item label="任务名" width="150px">{{item.name}}</el-descriptions-item>
          <el-descriptions-item label="持续时间" width="150px">{{item.value}} 分钟</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-card v-for="item in focusPieDayInfo" style="width: 600px;margin-top: 10px;margin-left: 170px;position: relative">
        {{item.ftaskName}}
        <div style="display: inline-block;position: absolute;right: 20px;top: 20px">
          {{item.fStartTime}} ~ {{item.fEndTime}} 持续了 {{item.flastedTime}} 分钟
        </div>
      </el-card>
    </el-card>
    </el-scrollbar>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { onMounted } from 'vue'
import request from "@/utils/request";

let pie
let column

export default {
  name: "Data-analysis",
  data() {
    return {
      userInfo: [ ],
      currentUserID: 0,
      timeRangeOfChart: 0, // 1表示年,2表示月,3表示日
      isShowYearPicker: false,
      isShowMonthPicker: false,
      isShowDayPicker: false,
      isShowColumn: false,
      isShowPie: false,
      selectYear: "",
      selectMonth: "",
      selectDay: "",
      focusInfo: { },
      focusColumnYear: [ 0,0,0,0,0,0,0,0,0,0,0,0 ],
      focusColumnMonth: [ 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 ],
      focusColumnDay: [ 0,0,0,0,0,0,0,0 ],
      focusPieYear: [ ],
      focusPieMonth: [ ],
      focusPieDay: [ ],
      defaultDate: null, // 最后没用上
      isShowDayDetail: false, // 选择日时出现的详细信息
      focusPieDayInfo: [], // 日-饼图的附加显示
      focusPieDayInfoSummary: [], // 每天信息的汇总(即把相同的工作加在一起)
    }
  },
  created() {
    // this.userInfo = JSON.parse(window.sessionStorage.getItem('userinfo'));
    this.getUserINfo();
    // if (this.userInfo) {
    //   this.getFocusInfo();
    // }
    this.getFocusInfo();

    // 计算今天日期(没用上)
    // let today = new Date();
    // let dd = String(today.getDate()).padStart(2, '0');
    // let mm = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
    // let yyyy = today.getFullYear();
    // this.defaultDate = new Date(2022, 5, 17);
    // console.log(this.defaultDate);
  },
  mounted() {
    // let ff = document.getElementById('firstclick');
    // ff.click();
  },
  watch: {
    selectYear() {
      this.initArray()
      // 曲线图-年的数据
      this.focusInfo.forEach((item) => {
        if (this.getStringYear(item.fDate) === parseInt(this.selectYear)) {
          let m = this.getStringMonth(item.fDate)
          this.focusColumnYear[m-1] += item.flastedTime
        }
      })
      // 饼图-年的数据
      let i = 0;
      this.focusInfo.forEach((item) => {
        if (this.getStringYear(item.fDate) === parseInt(this.selectYear)) {
          this.focusPieYear.push({ })
          this.focusPieYear[i]= {value:item.flastedTime, name:item.ftaskName}
          i++;
        }
      })
    },
    selectMonth() {
      this.initArray()
      // 曲线图-月的数据
      this.focusInfo.forEach((item) => {
        if (this.getStringMonth(item.fDate) === parseInt(this.selectMonth)) {
          let d = this.getStringDay(item.fDate)
          this.focusColumnMonth[d-1] += item.flastedTime
        }
      })
      // 饼图-月的数据
      let i = 0;
      this.focusInfo.forEach((item) => {
        if (this.getStringMonth(item.fDate) === parseInt(this.selectMonth)) {
          this.focusPieMonth.push({ })
          this.focusPieMonth[i]= {value:item.flastedTime, name:item.ftaskName}
          i++;
        }
      })
    },
    selectDay() {
      this.initArray()
      // 曲线图-日的数据
      this.focusInfo.forEach((item) => {
        if (item.fDate === this.selectDay) {
          let d = this.getStringHour(item.fStartTime)
          if (d<3) this.focusColumnDay[0] += item.flastedTime
          else if (d>=3 && d<6) this.focusColumnDay[1] += item.flastedTime
          else if (d>=6 && d<9) this.focusColumnDay[2] += item.flastedTime
          else if (d>=9 && d<12) this.focusColumnDay[3] += item.flastedTime
          else if (d>=12 && d<15) this.focusColumnDay[4] += item.flastedTime
          else if (d>=15 && d<18) this.focusColumnDay[5] += item.flastedTime
          else if (d>=18 && d<21) this.focusColumnDay[6] += item.flastedTime
          else if (d>=21 && d<24) this.focusColumnDay[7] += item.flastedTime
        }
      })
      // 饼图-日的数据
      let i = 0;
      this.focusInfo.forEach((item) => {
        if (item.fDate === this.selectDay) {
          this.focusPieDay.push({ })
          this.focusPieDay[i]= {value:item.flastedTime, name:item.ftaskName}
          this.focusPieDayInfo.push(item);
          i++;
        }
      })
    },
    focusPieDayInfo() {
    }
  },
  methods: {
    getUserINfo() {
      // 读取session中的用户信息并将 用户id 赋予currentUserID
      this.currentUserID = JSON.parse(window.sessionStorage.getItem('userinfo')).uid;
      if (this.currentUserID !== 0) {
        request.get("/userinfo",{
          params: {
            userId: this.currentUserID
          }
        }).then(res => {
          this.userInfo = res.data.records[0]
          // console.log(this.userInfo)
        })
      }
    },
    // 初始化保存各个图表数据的数组
    initArray() {
      this.focusColumnYear = [ 0,0,0,0,0,0,0,0,0,0,0,0 ]
      this.focusColumnMonth = [ 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 ]
      this.focusColumnDay = [ 0,0,0,0,0,0,0,0 ]
      this.focusPieYear = [ ]
      this.focusPieMonth = [ ]
      this.focusPieDay = [ ]
      this.focusPieDayInfo = [ ]
      this.focusPieDayInfoSummary = [ ]
    },
    getFocusInfo() {
      this.currentUserID = JSON.parse(window.sessionStorage.getItem('userinfo')).uid;
      request.get("/allfocusinfo",{
        params: {
          // userId: this.userInfo.uid
          userId: this.currentUserID
        }
      }).then(res => {
        this.focusInfo = res.data

      })
      // console.log(this.focusInfo);
    },
    getStringYear(s) {
      return parseInt(s.substr(0,4))
    },
    getStringMonth(s) {
      return parseInt(s.substr(5,2))
    },
    getStringDay(s) {
      return parseInt(s.substr(8,2))
    },
    getStringHour(s) {
      return parseInt(s.substr(0,2))
    },
    // 下面三个是单选器相关的函数
    clickRadioYear() {
      this.isShowYearPicker = true
      this.isShowMonthPicker =false
      this.isShowDayPicker = false
      // console.log(this.timeRangeOfChart); // 测试用
    },
    clickRadioMonth() {
      this.isShowYearPicker = false
      this.isShowMonthPicker =true
      this.isShowDayPicker = false
    },
    clickRadioDay() {
      this.isShowYearPicker = false
      this.isShowMonthPicker =false
      this.isShowDayPicker = true
    },
    drawChartColumn() {
      this.isShowColumn = true
      this.isShowPie = false
      this.isShowDayDetail = false;
      if (column != null && column !== "" && column !== undefined) {
        column.dispose()
      }
      // 曲线图-年
      if (this.timeRangeOfChart === "1") {
        console.log("画曲线图-年")
        console.log(this.focusColumnYear)
        column = echarts.init(document.getElementById('chartColumn'));
        column.setOption({
          title: {
            text: '一年的专注趋势',
            left: 'center',
          },
          xAxis: {
            name: '月份',
            type: 'category',
            data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
          },
          yAxis: {
            name: '分钟',
            type: 'value'
          },
          series: [
            {
              data: this.focusColumnYear,
              type: 'line',
              smooth: true,
              itemStyle : { normal: {label : {show: true}}},
            }
          ]
        })
      }
      // 曲线图-月
      else if (this.timeRangeOfChart === "2") {
        console.log("画曲线图-月")
        column = echarts.init(document.getElementById('chartColumn'));
        column.setOption({
          title: {
            text: '一个月的专注趋势',
            left: 'center',
          },
          xAxis: {
            name: '日期',
            type: 'category',
            data: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10',
              '11', '12', '13', '14', '15', '16', '17', '18', '19', '20',
              '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31']
          },
          yAxis: {
            name: '分钟',
            type: 'value'
          },
          series: [
            {
              data: this.focusColumnMonth,
              type: 'line',
              smooth: true,
              itemStyle : { normal: {label : {show: true}}},
            }
          ]
        })
      }
      // 曲线图-日
      else if (this.timeRangeOfChart === "3") {
        console.log("画曲线图-日")
        column = echarts.init(document.getElementById('chartColumn'));
        column.setOption({
          title: {
            text: '不同时间段的专注时间',
            left: 'center',
          },
          xAxis: {
            name: '小时区间',
            type: 'category',
            data: ['0~3', '3~6', '6~9', '9~12', '12~15', '15~18', '18~21', '21~24']
          },
          yAxis: {
            name: '分钟',
            type: 'value'
          },
          series: [
            {
              data: this.focusColumnDay,
              type: 'line',
              smooth: true,
              itemStyle : { normal: {label : {show: true}}},
            }
          ]
        })
      }
    },
    drawChartPie() {
      this.isShowColumn = false
      this.isShowPie = true
      this.isShowDayDetail = false;
      if (pie != null && pie !== "" && pie !== undefined) {
        pie.dispose()
      }
      // 饼图-年
      if (this.timeRangeOfChart === "1") {
        console.log("画饼图-年")
        console.log(this.focusColumnYear)
        pie = echarts.init(document.getElementById('chartPie'));
        pie.setOption({
          tooltip: {
            trigger: 'item'
          },
          legend: {
            top: '5%',
            left: 'center'
          },
          toolbox: {
            show: true,
            feature: {
              mark: { show: true },
              dataView: { show: true, readOnly: false },
              restore: { show: true },
              saveAsImage: { show: true }
            }
          },
          series: [
            {
              name: '专注时间',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '20',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: this.focusPieYear
            }
          ]
        })
      }
      // 饼图-月
      else if (this.timeRangeOfChart === "2") {
        console.log("画饼图-月")
        pie = echarts.init(document.getElementById('chartPie'));
        pie.setOption({
          tooltip: {
            trigger: 'item'
          },
          legend: {
            top: '5%',
            left: 'center'
          },
          toolbox: {
            show: true,
            feature: {
              mark: { show: true },
              dataView: { show: true, readOnly: false },
              restore: { show: true },
              saveAsImage: { show: true }
            }
          },
          series: [
            {
              name: '专注时间',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '20',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: this.focusPieMonth
            }
          ]
        })
      }
      // 饼图-日
      else if (this.timeRangeOfChart === "3") {
        console.log("画饼图-日")
        console.log(this.focusPieDay);
        console.log(this.focusPieDayInfo);

        // 根据focusPieDayInfo得到不重复任务名的专注时长信息
        // 先加上第一个信息(这里不会有重复)
        this.focusPieDayInfoSummary.push(
            {name: this.focusPieDayInfo[0].ftaskName, value: this.focusPieDayInfo[0].flastedTime}
        );
        // 从focusPieDayInfo的第二个数组开始检查
        const len = this.focusPieDayInfo.length;
        let temp = 0; // 0表示可以加,1表示不可以加
        for (let i = 1; i < len; i++) {
          temp = 0;
          // 在focusPieDayInfoSummary已经存在的里面找到相同的名称
          for (let j = 0; j < this.focusPieDayInfoSummary.length; j++) {
            // 找到相同的就加上
            if (this.focusPieDayInfo[i].ftaskName === this.focusPieDayInfoSummary[j].name) {
              this.focusPieDayInfoSummary[j].value += this.focusPieDayInfo[i].flastedTime;
              temp = 1;
              break;
            }
          }
          if (temp === 0) {
            this.focusPieDayInfoSummary.push(
                {name: this.focusPieDayInfo[i].ftaskName, value: this.focusPieDayInfo[i].flastedTime}
            );
          } else {
            continue;
          }
        }
        console.log(this.focusPieDayInfoSummary);

        pie = echarts.init(document.getElementById('chartPie'));
        pie.setOption({
          tooltip: {
            trigger: 'item'
          },
          legend: {
            top: '5%',
            left: 'center'
          },
          toolbox: {
            show: true,
            feature: {
              mark: { show: true },
              dataView: { show: true, readOnly: false },
              restore: { show: true },
              saveAsImage: { show: true }
            }
          },
          series: [
            {
              name: '专注时间',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '20',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: this.focusPieDay
            }
          ]
        })
        // 显示更多信息
        this.isShowDayDetail = true;
      }
    }
  }
}
</script>

<style scoped>
div::-webkit-scrollbar {
  width: 0;
}
el-card::-webkit-scrollbar {
  width: 0;
}
</style>
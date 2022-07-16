# LatestGraduationProject
基于时间统计的待办事项管理系统

## 主要功能:
1. 登陆注册
2. 添加、修改、删除、查找任务
3. 通过编辑器编辑修改文字
4. 对某个任务进行专注
5. 以图表形式展示数据

## 如何导入数据
1. 本项目使用Mysql数据库
2. 可以在 `springboot/src/main/resources/application.properties` 内修改数据库url、用户名、密码
3. 可导入sql文件，`springboot-vue.sql` 在根目录下

## 加载依赖
1. 在 `Vue` 文件夹下终端内输入 `npm install` 安装前端依赖

## 如何运行
1. 后端：进入 `springboot/src/main/java/com.example.springboot` 目录下的 `SpringbootApplication` 文件运行后端程序；
2. 前端：在终端输入 `cd vue` ，输入 `npm run serve` 运行至页面，输入 `npm run electron:serve` 运行为Electron封装后的应用。

## 界面截图
1. 日程规划
    [![j5kG1P.png](https://s1.ax1x.com/2022/07/16/j5kG1P.png)](https://imgtu.com/i/j5kG1P)
2. 待办事项
    [![j5kNnS.png](https://s1.ax1x.com/2022/07/16/j5kNnS.png)](https://imgtu.com/i/j5kNnS)
3. 每日记录
    [![j5kBhn.png](https://s1.ax1x.com/2022/07/16/j5kBhn.png)](https://imgtu.com/i/j5kBhn)
4. 数据分析
    [![j5k6XT.png](https://s1.ax1x.com/2022/07/16/j5k6XT.png)](https://imgtu.com/i/j5k6XT)
5. 简介模式
    [![j5kgnU.png](https://s1.ax1x.com/2022/07/16/j5kgnU.png)](https://imgtu.com/i/j5kgnU)
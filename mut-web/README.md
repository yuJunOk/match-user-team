# Vue 3 + TypeScript + Vite

This template should help get you started developing with Vue 3 and TypeScript in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about the recommended Project Setup and IDE Support in the [Vue Docs TypeScript Guide](https://vuejs.org/guide/typescript/overview.html#project-setup).

# 技术架构过程

vite + vue3 + ts

1. **初始化项目**

官网：[开始 | Vite 官方中文文档](https://vitejs.cn/vite3-cn/guide/)

刚开始想用yarn初始化的，但是yarn的创建命令文件所处的文件夹路径有空格，执行会出错。避免麻烦，转用下面的npm命令了。

```
npm create vite@latest
```

2. **整合组件库vant**

官网：[Vant 4 - 轻量、可定制的移动端组件库](https://vant-ui.github.io/vant/#/zh-CN)

安装vite

```
npm i vant
```

在快速上手里找基于vite的引入方法。

```
npm i @vant/auto-import-resolver unplugin-vue-components unplugin-auto-import -D
```

修改vite.config.ts

```
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import { VantResolver } from '@vant/auto-import-resolver';

export default {
  plugins: [
    vue(),
    AutoImport({
      resolvers: [VantResolver()],
    }),
    Components({
      resolvers: [VantResolver()],
    }),
  ],
};
```

完成以上三步，就可以直接在模板中使用 Vant 组件了，`unplugin-vue-components` 会解析模板并自动注册对应的组件, `@vant/auto-import-resolver` 会自动引入对应的组件样式。

3. **引入vue router**

官网：[Vue Router](https://v3.router.vuejs.org/zh/)

安装vue router

```
npm install vue-router
```

在main.ts修改引入

```
import { createApp } from 'vue'
import App from './App.vue'

import * as VueRouter from 'vue-router';
import routes from "./config/route";

let app = createApp(App);

const router = VueRouter.createRouter({
    // 4. 内部提供了 history 模式的实现。为了简单起见，我们在这里使用 hash 模式。
    history: VueRouter.createWebHashHistory(),
    routes, // `routes: routes` 的缩写
})
app.use(router);

app.mount('#app');
```

4. 引入axios请求

```
npm install axios
```

5. 引入qs做请求参数序列化

在请求参数为数组类型时，能派上用场。

```
npm install qs
```




<script setup>
import { useRouter, RouterLink, RouterView } from 'vue-router';
import notice from './components/notice.vue';
import axios from 'axios';
import qs from 'qs';
import { h, onMounted, ref } from "vue";
import { NSkeleton, NThing, NBackTop, NAvatar, NNotificationProvider, NMessageProvider, NModal, NCard, NTabs, NForm, NFormItem, NTabPane, NLayout, NLayoutHeader, NLayoutSider, NDivider, NMenu, NConfigProvider, NPopover, NIcon, NButton, NInput } from "naive-ui";

const showLogin = ref(false);
const Userinfo = ref({
  UserImg: null,
  UserName: null,
  UserSignature: null,
});
const Userno = ref({
  UserToken: null,
  UserId: null,
});
const hasLogin = ref(false);
const searchText = ref('');
const router = useRouter();

axios.defaults.timeout = 5000;                         //请求超时时间
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';//请求头
axios.defaults.headers['Accept'] = '*/*';

function toSearch()
{
  // 当用户没有输入内容就按enter键时，就return，不做任何操作，不去搜索
  if (searchText.value === '')
  {
    return;
  } else
  {
    router.push({
      name: 'search',
      query: {
        key: searchText.value,
      }
    });
    searchText.value = '';
  }
}

// 定义注册函数
async function register(phone, password)
{
  try
  {
    // 发送请求
    const registerurl = '/azaz/user/login/register?' + qs.stringify({
      phone: phone,
      password: password,
    });
    const response = await axios.post(registerurl);

    // 处理响应
    const responseData = response.data;
    if (responseData.code === 2)
    {
      // 成功注册
      window.$message.success(
        '注册成功，正在自动登录'
      );
      login(phone, password);
    } else
    {
      window.$message.error(
        responseData.message
      );
    }
  } catch (error)
  {
    // 网络请求失败
    window.$message.error(
      '网络异常，请稍后重试。'
    );
  }
}

// 定义登录函数
async function login(phone, password)
{
  try
  {
    // 发送登录请求
    const loginurl = '/azaz/user/login?' + qs.stringify({
      phone: phone,
      password: password,
    });
    const response = await axios.post(loginurl);

    // 处理响应
    const responseData = response.data;
    if (responseData.code === 2)
    {
      // 登录成功
      Userno.value.UserToken = responseData.data.token;
      Userno.value.UserId = responseData.data.userId;
      localStorage.setItem("Useraccount", JSON.stringify({
        UserToken: Userno.value.UserToken,
        UserId: Userno.value.UserId,
      }));
      axios.defaults.headers.common['token'] = Userno.value.UserToken;//登陆后更新token
      window.$message.success(
        '登录成功'
      );
      hasLogin.value = true;
      showLogin.value = false;
      getPersonalInfo(null);
      setTimeout(() => { location.reload(true); }, 2000);
    } else
    {
      window.$message.error(
        responseData.message
      );
    }
  } catch (error)
  {
    // 网络请求失败
    window.$message.error(
      '网络异常，请稍后重试。'
    );
  }
}

//请求用户信息，为空则是查自己
async function getPersonalInfo(Id)
{
  try
  {
    // 发送GET请求
    const getPersonalInfourl = '/azaz/user/personal?' + qs.stringify({
      userId: Id,
    });
    const response = await axios.get(getPersonalInfourl);

    // 检查响应中的数据
    const responseData = response.data;
    if (responseData.code === 2 && Id === null)
    {
      // 从响应数据中提取用户信息
      Userinfo.value.UserName = responseData.data.username;
      Userinfo.value.UserSignature = responseData.data.signature;
      Userinfo.value.UserImg = responseData.data.image;

      // 使用 localStorage 存储数据
      localStorage.setItem("Userdata", JSON.stringify({
        UserName: Userinfo.value.UserName,
        UserSignature: Userinfo.value.UserSignature,
        UserImg: Userinfo.value.UserImg,
      }));
      return responseData.data;
    } else if (responseData.code === 2 && Id !== null)
    {
      return responseData.data;
    } else
    {
      // 未成功获取信息，报错
      window.$message.error(
        response.data.message
      );
      return null;
    }
  } catch (error)
  {
    // 请求失败，报错
    window.$message.error(
      '网络异常，与服务器失联。'
    );
    return null;
  }
}

function checkLogin()
{
  const account = localStorage.getItem('Useraccount');
  if (account)
  {
    const userAccount = JSON.parse(account);
    axios.defaults.headers.common['token'] = userAccount.UserToken;//登陆后更新token
    Userno.value.UserToken = userAccount.UserToken;
    Userno.value.UserId = userAccount.UserId;
    getPersonalInfo(null);
    const userData = JSON.parse(localStorage.getItem("Userdata"));
    Userinfo.value.UserImg = userData.UserImg;
    Userinfo.value.UserName = userData.UserName;
    Userinfo.value.UserSignature = userData.UserSignature;
    hasLogin.value = true;
  } else
  {
    showLogin.value = true;
  }
}

const getQuery = (url, query) =>
{
  // str为？之后的参数部分字符串
  const str = url.substr(url.indexOf('?') + 1);
  // arr每个元素都是完整的参数键值
  const arr = str.split('&');
  // result为存储参数键值的集合
  const result = {};
  for (let i = 0; i < arr.length; i++)
  {
    // item的两个元素分别为参数名和参数值
    const item = arr[i].split('=');
    result[item[0]] = item[1];
  }
  return result[query];
};

onMounted(() =>
{
  axios.defaults.baseURL = window.config.APIUrl;    //API接口地址
  checkLogin();
});

const loginRef = ref(null);
const registerRef = ref(null);
const rPasswordFormItemRef = ref(null);
const loginmodel = ref({
  number: null,
  password: null
});
const registermodel = ref({
  number: null,
  password: null,
  repassword: null
});
function validatePasswordStartWith(rule, value)
{
  return !!registermodel.value.password && registermodel.value.password.startsWith(value) && registermodel.value.password.length >= value.length;
}
function validatePasswordSame(rule, value)
{
  return value === registermodel.value.password;
}
function handlePasswordInput()
{
  if (registerRef.value.repassword)
  {
    rPasswordFormItemRef.value?.validate({ trigger: "password-input" });
  }
}
function registerButtonClick(e)
{
  e.preventDefault();
  registerRef.value?.validate((errors) =>
  {
    if (!errors)
    {
      register(registermodel.value.number, registermodel.value.password);
    } else
    {
      window.$message.error(
        '请先检查填写的内容'
      );
    }
  });
}
function loginButtonClick(e)
{
  e.preventDefault();
  loginRef.value?.validate((errors) =>
  {
    if (!errors)
    {
      login(loginmodel.value.number, loginmodel.value.password);
    } else
    {
      window.$message.error(
        '请先检查填写的内容'
      );
    }
  });
}
const loginrules = {
  number: [
    {
      required: true,
      validator(rule, value)
      {
        if (!value)
        {
          return new Error("请输入电话号码");
        } else if (!/^\d*$/.test(value))
        {
          return new Error("电话号码应该只含数字");
        } else if (value.length != 11)
        {
          return new Error("电话号码应为11位");
        }
        return true;
      },
      trigger: ["input", "blur"]
    }
  ],
  password: [
    {
      required: true,
      validator(rule, value)
      {
        if (!value)
        {
          return new Error("请输入密码");
        } else if (value.length < 6)
        {
          return new Error("密码应该至少有6位");
        } else if (value.length > 20)
        {
          return new Error("密码应该不超过20位");
          //} else if (!/^(?=.*\d)(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).+$/.test(value))
          //{
          //  return new Error("密码应该同时包含数字、字母和特殊字符");
        } else if (!/^[a-zA-Z0-9!@#$%,.?]+$/.test(value))
        {
          return new Error("密码不应含有非法字符");
        }
        return true;
      },
      trigger: ["input", "blur"]
    }
  ]
};
const morerules = {
  repassword: [
    {
      required: true,
      message: "请再次输入密码",
      trigger: ["input", "blur"]
    },
    {
      validator: validatePasswordStartWith,
      message: "两次密码输入不一致",
      trigger: "input"
    },
    {
      validator: validatePasswordSame,
      message: "两次密码输入不一致",
      trigger: ["blur", "password-input"]
    }
  ]
};
const registerrules = { ...morerules, ...loginrules };

//接受子组件传递来的消息并触发行为
function wanted(message)
{
  if (message === 'checkLogin')
  {
    checkLogin();
  }
}
</script>

<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider><n-notification-provider>
        <notice />
      </n-notification-provider></n-message-provider>
    <div style="position: relative">
      <n-modal v-model:show="showLogin" style="max-width: 350px;" :mask-closable="false">
        <n-card title="欢迎来到AZAZ">
          <n-tabs default-value="signin" size="large" justify-content="space-evenly">
            <n-tab-pane name="signin" tab="登录">
              <n-form ref="loginRef" :model="loginmodel" :rules="loginrules">
                <n-form-item label="手机号" path="number">
                  <n-input placeholder="请输入手机号码" v-model:value="loginmodel.number" @keydown.enter.prevent />
                </n-form-item>
                <n-form-item label="密码" path="password">
                  <n-input v-model:value="loginmodel.password" type="password" show-password-on="mousedown"
                    placeholder="请输入密码" />
                </n-form-item>
              </n-form>
              <n-button type="primary" block secondary strong
                :disabled="loginmodel.number === null || loginmodel.password === null" @click="loginButtonClick">
                登录
              </n-button>
            </n-tab-pane>
            <n-tab-pane name="signup" tab="注册">
              <n-form ref="registerRef" :model="registermodel" :rules="registerrules">
                <n-form-item label="手机号" path="number">
                  <n-input placeholder="请输入手机号码" v-model:value="registermodel.number" @keydown.enter.prevent />
                </n-form-item>
                <n-form-item label="密码" path="password">
                  <n-input v-model:value="registermodel.password" @input="handlePasswordInput" @keydown.enter.prevent
                    type="password" show-password-on="mousedown" placeholder="请输入密码" />
                </n-form-item>
                <n-form-item ref="rPasswordFormItemRef" first path="repassword" label="确认密码">
                  <n-input v-model:value="registermodel.repassword" :disabled="!registermodel.password" type="password"
                    show-password-on="mousedown" placeholder="请再次输入密码" />
                </n-form-item>
              </n-form>
              <n-button type="primary" block secondary strong
                :disabled="registermodel.number === null || registermodel.password === null" @click="registerButtonClick">
                注册
              </n-button>
            </n-tab-pane>
          </n-tabs>
        </n-card>
      </n-modal>
      <n-layout position="absolute" style="height: 100vh;">
        <n-layout-header style="width: 100vw;height: 64px; border-bottom: 2px solid #dbdbdb;">
          <div class="title-container">
            <div class="title-left"><span id="web-title"><svg width="78" height="22" viewBox="0 0 78 22" fill="none">
                  <mask id="logo_svg__a" style="mask-type:alpha" maskUnits="userSpaceOnUse" x="0" y="0" width="31"
                    height="21">
                    <path d="M0 .08h30.52v20.33H0V.08z" fill="#fff"></path>
                  </mask>
                  <g mask="url(#logo_svg__a)">
                    <path fill-rule="evenodd" clip-rule="evenodd"
                      d="M30.52.316c-.513-.177-.938.038-1.182.221A18.392 18.392 0 0115.26 7.071c-1.94 0-3.809-.3-5.565-.855l-.78-2.837s-.38-1.156-1.665-.958l.37 2.995A18.495 18.495 0 011.182.536C.94.355.515.14 0 .317A16.343 16.343 0 008.092 9.24l1.013 8.202s.452 3.138 3.397 3.138H18.8c2.944 0 3.397-3.138 3.397-3.138l.712-5.878c-1.912-.155-3.115 1.177-3.496 2.492-.64 2.214-.64 2.356-.766 2.742-.258.79-1.107.885-1.107.885h-3.778s-.849-.094-1.107-.885c-.167-.51-.998-3.488-1.835-6.51 1.412.399 2.901.613 4.44.613 6.99 0 12.949-4.403 15.26-10.585z"
                      fill="#07BEFF"></path>
                  </g>
                  <path fill-rule="evenodd" clip-rule="evenodd"
                    d="M77.835 12.624v-2.203H64.11v2.203h2.525l-1.952 4.908h-.034v2.203h11.743c.737-.053 1.4-.709 1.4-1.388 0-.338-.109-.723-.132-.8h.002l-.994-2.738h-2.43l.988 2.723h-8.16l1.952-4.908h8.816zm-21.374-3.3h4.893v-2.15h-4.893v-2.18h-2.203v2.18h-3.384l.283-1.616h-2.303l-1.176 6.729h.936c.312-.031 1.359-.23 1.624-1.476l.26-1.487h3.76v6.563h-6.57v2.203h6.57v2.167h2.203V18.09h5.449v-2.203h-5.449V9.324zM35.713 5.017H33.51v2.147H31.32v2.22h2.189v8.967c.062.74.656 1.33 1.398 1.384h8.653a1.518 1.518 0 001.447-1.428v-3.505H42.81v2.678h-7.096V9.385h9.826v-2.22h-9.827V5.016zm28.97 2.664h12.553v-2.15H64.684v2.15z"
                    fill="#07BEFF"></path>
                </svg> AZAZ</span></div>
            <div class="title-center">
              <n-input round clearable placeholder="搜索" style="max-width: 500px;" v-model:value="searchText"
                @keyup.enter.native="toSearch">
                <!-- TODO:搜索路由拼接 -->
                <template #prefix>
                  <n-icon>
                    <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                      viewBox="0 0 32 32">
                      <g fill="none">
                        <path
                          d="M13.5 3C7.701 3 3 7.701 3 13.5S7.701 24 13.5 24c2.54 0 4.868-.901 6.684-2.402l7.109 7.11a1 1 0 0 0 1.414-1.415l-7.109-7.11A10.457 10.457 0 0 0 24 13.5C24 7.701 19.299 3 13.5 3zM5 13.5a8.5 8.5 0 1 1 17 0a8.5 8.5 0 0 1-17 0z"
                          fill="currentColor"></path>
                      </g>
                    </svg>
                  </n-icon>
                </template>
              </n-input>
            </div>
            <div class="title-right">
              <div class="title-box" style="height: 29px;">
                <RouterLink to="/upload">
                  <n-popover trigger="hover">
                    <template #trigger>
                      <n-button text>
                        <n-icon size="29">
                          <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                            viewBox="0 0 1024 1024">
                            <path
                              d="M400 317.7h73.9V656c0 4.4 3.6 8 8 8h60c4.4 0 8-3.6 8-8V317.7H624c6.7 0 10.4-7.7 6.3-12.9L518.3 163a8 8 0 0 0-12.6 0l-112 141.7c-4.1 5.3-.4 13 6.3 13zM878 626h-60c-4.4 0-8 3.6-8 8v154H214V634c0-4.4-3.6-8-8-8h-60c-4.4 0-8 3.6-8 8v198c0 17.7 14.3 32 32 32h684c17.7 0 32-14.3 32-32V634c0-4.4-3.6-8-8-8z"
                              fill="currentColor"></path>
                          </svg>
                        </n-icon>
                      </n-button>
                    </template>
                    <span>上传视频</span>
                  </n-popover>
                </RouterLink>
              </div>
              <div class="title-box title-user">
                <n-button v-if="!hasLogin" strong secondary type="primary" @click="{ showLogin = true; }">
                  登录/注册
                </n-button>
                <RouterLink :to="'/user/' + Userno.UserId" class="title-user"><n-avatar v-if="hasLogin" round :size="35"
                    :src="Userinfo.UserImg" /></RouterLink>
              </div>
            </div>
          </div>
        </n-layout-header>
        <n-layout has-sider position="absolute" style="top: 64px">
          <n-layout-sider content-style="padding-top: 20px">
            <n-menu :options="menuOptions" />
            <n-divider />
            <p style="margin-left: 20px;color: #8a8b9f;">Designed by AZAZ Team</p>
          </n-layout-sider>
          <n-layout content-style="padding: 24px;">
            <RouterView v-slot:="{ Component, route }"><n-back-top />
              <component @wanted="wanted" :is="Component" :key="route.path" />
            </RouterView>
          </n-layout>
        </n-layout>
      </n-layout>

    </div>

  </n-config-provider>
</template>

<script>
const themeOverrides = {
  common: {
    primaryColor: '#0750ff',
    primaryColorHover: '#0783ff',
    primaryColorPressed: '#0438b2',
    primaryColorSuppl: '#0783ff'
  },
  Menu: {
    itemHeight: '42px'
  },
};
const menuOptions = [
  {
    label: () => h(
      RouterLink,
      {
        to: {
          path: "/"
        }
      },
      { default: () => h('h2', { class: 'menu-item-h' }, "主页") }
    ),
    key: "home",
    icon: () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 32 32',
    }, [
      h('path', {
        d: 'M16.612 2.214a1.01 1.01 0 0 0-1.242 0L1 13.419l1.243 1.572L4 13.621V26a2.004 2.004 0 0 0 2 2h20a2.004 2.004 0 0 0 2-2V13.63L29.757 15L31 13.428zM18 26h-4v-8h4zm2 0v-8a2.002 2.002 0 0 0-2-2h-4a2.002 2.002 0 0 0-2 2v8H6V12.062l10-7.79l10 7.8V26z',
        fill: 'currentColor',
      })
    ])
  },
  {
    label: () => h(
      RouterLink,
      {
        to: {
          path: "/following"
        }
      },
      { default: () => h('h2', { class: 'menu-item-h' }, "关注") }
    ),
    key: "following",
    icon: () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 32 32',
    }, [
      h('path', {
        d: 'M32 14h-4v-4h-2v4h-4v2h4v4h2v-4h4v-2z',
        fill: 'currentColor',
      }),
      h('path', {
        d: 'M12 4a5 5 0 1 1-5 5a5 5 0 0 1 5-5m0-2a7 7 0 1 0 7 7a7 7 0 0 0-7-7z',
        fill: 'currentColor',
      }),
      h('path', {
        d: 'M22 30h-2v-5a5 5 0 0 0-5-5H9a5 5 0 0 0-5 5v5H2v-5a7 7 0 0 1 7-7h6a7 7 0 0 1 7 7z',
        fill: 'currentColor',
      })
    ])
  },
  {
    label: () => h('h2', { class: 'menu-item-h' }, "探索"),
    key: "explore",
    children: [
      {
        label: () => h(
          RouterLink,
          {
            to: {
              path: "/explore/liveshow"
            }
          },
          { default: () => h('h2', { class: 'menu-item-h-children' }, "直播") }
        ),
        key: "liveshow",
      },
      {
        label: () => h(
          RouterLink,
          {
            to: {
              path: "/explore/sports"
            }
          },
          { default: () => h('h2', { class: 'menu-item-h-children' }, "体育") }
        ),
        key: "sports",
      },
      {
        label: () => h(
          RouterLink,
          {
            to: {
              path: "/explore/games"
            }
          },
          { default: () => h('h2', { class: 'menu-item-h-children' }, "游戏") }
        ),
        key: "games",
      },
      {
        label: () => h(
          RouterLink,
          {
            to: {
              path: "/explore/drama"
            }
          },
          { default: () => h('h2', { class: 'menu-item-h-children' }, "番剧") }
        ),
        key: "drama",
      },
      {
        label: () => h(
          RouterLink,
          {
            to: {
              path: "/explore/knowledge"
            }
          },
          { default: () => h('h2', { class: 'menu-item-h-children' }, "知识") }
        ),
        key: "knowledge",
      },
      {
        label: () => h(
          RouterLink,
          {
            to: {
              path: "/explore/entertainment"
            }
          },
          { default: () => h('h2', { class: 'menu-item-h-children' }, "娱乐") }
        ),
        key: "entertainment",
      },
      {
        label: () => h(
          RouterLink,
          {
            to: {
              path: "/explore/food"
            }
          },
          { default: () => h('h2', { class: 'menu-item-h-children' }, "美食") }
        ),
        key: "food",
      },
      {
        label: () => h(
          RouterLink,
          {
            to: {
              path: "/explore/fashion"
            }
          },
          { default: () => h('h2', { class: 'menu-item-h-children' }, "时尚") }
        ),
        key: "fashion",
      },
      {
        label: () => h(
          RouterLink,
          {
            to: {
              path: "/explore/hot"
            }
          },
          { default: () => h('h2', { class: 'menu-item-h-children' }, "热点") }
        ),
        key: "hot",
      },

    ],
    icon: () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '2 0 32 32',
    }, [
      h('path', {
        d: 'M22.707 9.293a1 1 0 0 0-1.023-.242l-9 3a1.001 1.001 0 0 0-.633.633l-3 9a1 1 0 0 0 1.265 1.265l9-3a1.001 1.001 0 0 0 .633-.633l3-9a1 1 0 0 0-.242-1.023zM11.581 20.42l2.21-6.628l4.419 4.419z',
        fill: 'currentColor',
      }),
      h('path', {
        d: 'M16 30a14 14 0 1 1 14-14a14.016 14.016 0 0 1-14 14zm0-26a12 12 0 1 0 12 12A12.014 12.014 0 0 0 16 4z',
        fill: 'currentColor',
      })
    ])
  },
  {
    label: () => h(
      RouterLink,
      {
        to: {
          path: "/messages"
        }
      },
      { default: () => h('h2', { class: 'menu-item-h' }, "消息") }
    ),
    key: "messages",
    icon: () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '50 50 1024 1024',
    }, [
      h('path', {
        d: 'M464 512a48 48 0 1 0 96 0a48 48 0 1 0-96 0zm200 0a48 48 0 1 0 96 0a48 48 0 1 0-96 0zm-400 0a48 48 0 1 0 96 0a48 48 0 1 0-96 0zm661.2-173.6c-22.6-53.7-55-101.9-96.3-143.3a444.35 444.35 0 0 0-143.3-96.3C630.6 75.7 572.2 64 512 64h-2c-60.6.3-119.3 12.3-174.5 35.9a445.35 445.35 0 0 0-142 96.5c-40.9 41.3-73 89.3-95.2 142.8c-23 55.4-34.6 114.3-34.3 174.9A449.4 449.4 0 0 0 112 714v152a46 46 0 0 0 46 46h152.1A449.4 449.4 0 0 0 510 960h2.1c59.9 0 118-11.6 172.7-34.3a444.48 444.48 0 0 0 142.8-95.2c41.3-40.9 73.8-88.7 96.5-142c23.6-55.2 35.6-113.9 35.9-174.5c.3-60.9-11.5-120-34.8-175.6zm-151.1 438C704 845.8 611 884 512 884h-1.7c-60.3-.3-120.2-15.3-173.1-43.5l-8.4-4.5H188V695.2l-4.5-8.4C155.3 633.9 140.3 574 140 513.7c-.4-99.7 37.7-193.3 107.6-263.8c69.8-70.5 163.1-109.5 262.8-109.9h1.7c50 0 98.5 9.7 144.2 28.9c44.6 18.7 84.6 45.6 119 80c34.3 34.3 61.3 74.4 80 119c19.4 46.2 29.1 95.2 28.9 145.8c-.6 99.6-39.7 192.9-110.1 262.7z',
        fill: 'currentColor',
      })
    ])
  },
];
</script>

<style>
#web-title {
  font-size: 26px;
  font-weight: bold;
}

.title-user {
  display: flex;
  align-items: center;
}

.title-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.title-left {
  flex: 0 0 auto;
  margin-left: 20px;
  width: 220px;
}

.title-center {
  flex: 1 0 auto;
  display: flex;
  justify-content: center;
}

.title-right {
  flex: 0 0 auto;
  margin-right: 20px;
  width: 220px;
  text-align: right;
  display: flex;
  justify-content: right;
  align-items: center;
}

.title-box {
  display: inline-block;
  margin-left: 20px;
}

.menu-item-h {
  padding-left: 10px;
  font-size: 19px;
}

.menu-item-h-children {
  padding-left: 10px;
  font-size: 18px;
}

a {
  text-decoration: none;
  color: black;
}
</style>
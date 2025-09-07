<script setup lang="ts">
import { ref } from 'vue'
import axiosInstance from '../api/axiosRequest'
import { login } from '../api/auth/login'
import { useRouter } from 'vue-router'
import { parseJwt, deriveRole, routeForRole } from '../utils/auth'

const router = useRouter()

const username = ref('')
const password = ref('')
const captchaUrl = ref('')
const graphCaptchaKey = ref('')
const graphCaptchaCode = ref('')
const loading = ref(false)
const errorMsg = ref('')

const loadCaptcha = async () => {
  try {
    const { data } = await axiosInstance.get('/auth/GraphCaptcha')
    const body: any = data?.data ?? data
    graphCaptchaKey.value = body?.captchaKey || body?.graphCaptchaKey || ''
    const img = body?.captchaImage || body?.image || body?.img
    if (typeof img === 'string') {
      captchaUrl.value = img.startsWith('data:image') ? img : `data:image/png;base64,${img}`
    } else {
      captchaUrl.value = ''
    }
  } catch (e) {
    captchaUrl.value = ''
  }
}

const submit = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const { data } = await login({
      username: username.value,
      password: password.value,
      graphCaptchaKey: graphCaptchaKey.value,
      graphCaptchaCode: graphCaptchaCode.value
    })
    const body: any = data?.data ?? data
    const access: string | undefined = body?.accessToken || body?.token
    const refresh: string | undefined = body?.refreshToken
    if (!access) throw new Error('登录响应缺少 accessToken')

    localStorage.setItem('accessToken', access)
    if (refresh) localStorage.setItem('refreshToken', refresh)

    const payload = parseJwt(access)
    const role = deriveRole(payload, body?.role || null)
    router.replace(routeForRole(role))
  } catch (e: any) {
    errorMsg.value = e?.message || '登录失败'
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}

loadCaptcha()
</script>
<template>
  <div class="page">
    <div class="card">
      <h1>登录</h1>
      <div class="form">
        <div>
          <label>用户名</label>
          <input v-model="username" class="input" placeholder="请输入用户名" />
        </div>
        <div>
          <label>密码</label>
          <input v-model="password" type="password" class="input" placeholder="请输入密码" />
        </div>
        <div>
          <label>验证码</label>
          <div style="display:flex; align-items:center; gap:10px;">
            <input v-model="graphCaptchaCode" class="input" placeholder="输入验证码" style="flex:1" />
            <img v-if="captchaUrl" :src="captchaUrl" alt="captcha" style="height:40px; border-radius:8px; border:1px solid var(--border)" />
            <button class="btn ghost" type="button" @click="loadCaptcha" style="white-space:nowrap;">换一张</button>
          </div>
        </div>
        <button class="btn" :disabled="loading" @click="submit">{{ loading ? '登录中...' : '登录' }}</button>
        <p v-if="errorMsg" style="color:#d33;margin-top:8px;">{{ errorMsg }}</p>
      </div>
    </div>
  </div>
</template>
<style scoped>
.page{display:flex;justify-content:center;align-items:center;min-height:100vh;background:#f6f7fb}
.card{width:360px;background:#fff;border:1px solid #eee;border-radius:16px;box-shadow:0 8px 24px rgba(0,0,0,.06);padding:24px}
h1{font-size:20px;margin:0 0 16px}
.form{display:flex;flex-direction:column;gap:12px}
label{display:block;margin-bottom:6px;color:#555}
.input{width:100%;padding:10px 12px;border:1px solid #ddd;border-radius:10px;outline:none}
.btn{width:100%;padding:10px 12px;border-radius:10px;border:1px solid #2b7cff;background:#2b7cff;color:#fff;cursor:pointer}
.btn[disabled]{opacity:.6;cursor:not-allowed}
.btn.ghost{background:#fff;color:#2b7cff}
</style>
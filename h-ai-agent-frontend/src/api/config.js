import axios from 'axios'

/** * 精准适配开发与生产环境：
 * 1. 如果有明确的环境变量（例如配置了 .env.production），则使用环境变量。
 * 2. 如果是开发环境 (development) 且未配置变量，使用相对路径 '/api'，以便走 Vite 的 local proxy 代理。
 * 3. 如果是生产环境 (production) 且未配置变量，直接使用相对路径 '/api'。
 * * 这样当手机访问时，浏览器会自动根据当前域名拼接请求：http://47.118.84.108/api/... 
 * 从而完美打进阿里云服务器的 80 端口，被 Nginx 逮住并安全分流。
 */
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || '/api'

const http = axios.create({
  baseURL: API_BASE_URL,
  // 💡 优化点：由于是 Spring AI 大模型项目，大模型思考和流式吐字需要较长时间，
  // 必须将原本的 30 秒（30000）大幅度调大到 5 分钟（300000），防止大模型话还没说完连接就被前端单方面掐断！
  timeout: 300000,
})

// 响应拦截器（保持你原本的逻辑不变，仅作格式规范）
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('API请求发生错误:', error)
    return Promise.reject(error)
  }
)

export default http
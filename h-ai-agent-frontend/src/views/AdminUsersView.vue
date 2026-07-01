<template>
  <div class="admin-page">
    <div class="admin-header">
      <h1 class="page-title">用户管理</h1>
      <p class="page-subtitle">管理所有注册用户</p>
    </div>

    <div class="admin-table-wrap">
      <table class="admin-table" v-if="users.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>
              <span v-if="editingId !== u.id">{{ u.username }}</span>
              <input v-else v-model="editForm.username" class="edit-input" />
            </td>
            <td>
              <span v-if="editingId !== u.id">{{ u.email || '—' }}</span>
              <input v-else v-model="editForm.email" class="edit-input" placeholder="邮箱" />
            </td>
            <td>
              <select v-model="u.role" @change="changeRole(u)" class="role-select">
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </td>
            <td>{{ formatDate(u.createdAt) }}</td>
            <td>
              <template v-if="editingId === u.id">
                <button class="btn-ok" @click="saveEdit(u)">保存</button>
                <button class="btn-cancel" @click="editingId = null">取消</button>
              </template>
              <template v-else>
                <button class="btn-edit" @click="startEdit(u)" :disabled="u.id === currentUser?.id">编辑</button>
                <button class="btn-warn" @click="resetPassword(u)">重置密码</button>
                <button class="btn-danger" @click="deleteUser(u)" :disabled="u.id === currentUser?.id">删除</button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty-msg">加载中…</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import http from '@/api/config'
import { useAuth } from '@/stores/auth'

const { currentUser } = useAuth()
const users = ref([])
const editingId = ref(null)
const editForm = reactive({ username: '', email: '' })

onMounted(fetchUsers)

async function fetchUsers() {
  try {
    users.value = await http.get('/admin/users')
  } catch (e) {
    console.error('获取用户列表失败', e)
  }
}

async function changeRole(u) {
  try {
    await http.put(`/admin/users/${u.id}/role`, { role: u.role })
  } catch (e) {
    console.error('修改角色失败', e)
  }
}

function startEdit(u) {
  editingId.value = u.id
  editForm.username = u.username
  editForm.email = u.email || ''
}

async function saveEdit(u) {
  try {
    // 管理员通过 UserController 的 profile 接口间接修改
    // 这里模拟更新: 先调用用户资料接口（实际可建专用管理端接口）
    await http.put(`/admin/users/${u.id}/profile`, {
      username: editForm.username,
      email: editForm.email,
    })
    u.username = editForm.username
    u.email = editForm.email
    editingId.value = null
  } catch (e) {
    console.error('编辑用户失败', e)
  }
}

async function resetPassword(u) {
  if (!confirm(`确定重置用户「${u.username}」的密码为 init123456 ？`)) return
  try {
    await http.put(`/admin/users/${u.id}/password`)
    alert('密码已重置为 init123456')
  } catch (e) {
    console.error('重置密码失败', e)
  }
}

async function deleteUser(u) {
  if (!confirm(`确定删除用户「${u.username}」？`)) return
  try {
    await http.delete(`/admin/users/${u.id}`)
    users.value = users.value.filter((x) => x.id !== u.id)
  } catch (e) {
    console.error('删除用户失败', e)
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.admin-page {
  flex: 1;
  padding: 32px 16px;
  background: var(--color-bg);
}

.admin-header {
  max-width: 900px;
  margin: 0 auto 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
}

.page-subtitle {
  font-size: 14px;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.admin-table-wrap {
  max-width: 900px;
  margin: 0 auto;
  overflow-x: auto;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 16px;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.admin-table th {
  text-align: left;
  padding: 10px 12px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--color-border);
}

.admin-table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text);
}

.admin-table tr:last-child td {
  border-bottom: none;
}

.role-select {
  padding: 4px 8px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 13px;
}

.btn-danger {
  padding: 5px 12px;
  border: 1px solid rgba(248, 113, 113, 0.3);
  border-radius: 4px;
  background: rgba(248, 113, 113, 0.1);
  color: var(--color-danger);
  font-size: 12px;
  font-weight: 600;
}

.btn-danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-danger:not(:disabled):hover {
  background: rgba(248, 113, 113, 0.2);
}

.btn-warn {
  padding: 5px 12px;
  border: 1px solid rgba(251, 191, 36, 0.3);
  border-radius: 4px;
  background: rgba(251, 191, 36, 0.1);
  color: #fbbf24;
  font-size: 12px;
  font-weight: 600;
  margin-right: 6px;
}

.btn-warn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-warn:not(:disabled):hover {
  background: rgba(251, 191, 36, 0.2);
}

.btn-edit {
  padding: 5px 10px;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 4px;
  background: rgba(0, 212, 255, 0.08);
  color: var(--color-accent);
  font-size: 12px;
  font-weight: 600;
  margin-right: 4px;
}

.btn-edit:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-edit:not(:disabled):hover { background: rgba(0, 212, 255, 0.15); }

.btn-ok {
  padding: 5px 10px;
  border: none;
  border-radius: 4px;
  background: var(--color-success);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  margin-right: 4px;
}

.btn-cancel {
  padding: 5px 10px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: transparent;
  color: var(--color-text-dim);
  font-size: 12px;
}

.edit-input {
  padding: 4px 8px;
  border: 1px solid var(--color-accent);
  border-radius: 4px;
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 13px;
  width: 100%;
  box-sizing: border-box;
}

.empty-msg {
  text-align: center;
  color: var(--color-text-dim);
  padding: 40px;
}
</style>

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCurrentUserMenusService } from '@/api/admin/menu'

export const useMenuStore = defineStore('menu', () => {
  const menus = ref([])

  // 核心逻辑：组装树形结构并排序
  const buildTree = (list) => {
    // 1. 排序
    const sortedList = list.sort((a, b) => (a.sort || 0) - (b.sort || 0))

    const tree = []
    const map = {}

    // 2. 建立映射
    sortedList.forEach(item => {
      map[item.id] = { ...item, children: [] }
    })

    // 3. 组装树
    sortedList.forEach(item => {
      const node = map[item.id]
      const pId = item.parent_id !== undefined ? item.parent_id : item.parentId
      if (pId === 0 || pId === null) {
        tree.push(node)
      } else {
        const parent = map[pId]
        if (parent) {
          parent.children.push(node)
        }
      }
    })

    return tree
  }

  // 刷新页面时
  const fetchMenus = async () => {
    try {
      const res = await getCurrentUserMenusService()
      menus.value = buildTree(res.data)
    } catch (error) {
      console.error('获取菜单失败', error)
      menus.value = []
    }
  }

  const setMenus = (rawMenus) => {
    menus.value = buildTree(rawMenus)
  }

  const clearMenus = () => {
    menus.value = []
  }

  return {
    menus,
    setMenus,
    clearMenus,
    fetchMenus
  }
})
import Login from "../views/admin/Login.vue";
import Layout from "../views/admin/Layout.vue";
import Home from "../views/admin/Home.vue";
import NotFound from "../views/404.vue";

import AdminManagent from "../views/admin/adminmanagent/AdminManagent.vue";

import LogisticsManagement from "../views/admin/LogisticsManagent.vue";
import UpdatePassword from "../views/admin/UpdatePassword.vue";
import Log from "../views/admin/Log.vue"
import Carousel from "../views/admin/Carousel.vue";
import Notice from "../views/admin/Notice.vue";
import NoticeDetail from "../views/admin/NoticeDetail.vue";
import OrderDetail from "../views/admin/order/OrderDetail.vue";
import SkuStockWarning from "../views/admin/SkuStockWarning.vue";

//Product模块子组件
import ProductList from "../views/admin/product/ProductList.vue";
import ProductCategory from "../views/admin/product/ProductCategory.vue";
import ProductAttr from "../views/admin/product/ProductAttr.vue";
import ProductSku from "../views/admin/product/ProductSku.vue";
import ProductAttrValue from "../views/admin/product/ProductAttrValue.vue";


//Order模块子组件
import OrderManagement from "../views/admin/order/OrderManagement.vue";
import AfterSale from "../views/admin/order/AfterSale.vue";

//AdminManagent模块子组件
import Role from "../views/admin/adminmanagent/Role.vue";
import Menu from "../views/admin/adminmanagent/Menu.vue";

export const adminRoutes = [
  { path: '/admin/login', component: Login },
  {
    path: '/admin',
    component: Layout,
    redirect: '/admin/home',
    meta: { title: "首页" },
    children: [
      { path: 'home', component: Home, meta: { title: "控制台" } },
      { path: 'order', component: OrderManagement, meta: { title: "订单管理" } },
      { path: 'after-sale', component: AfterSale, meta: { title: "售后管理" } },
      { path: 'list', component: ProductList, meta: { title: "商品列表" } },
      { path: 'category', component: ProductCategory, meta: { title: "商品分类" } },
      { path: 'attr', component: ProductAttr, meta: { title: "商品规格" } },
      { path: 'sku', component: ProductSku, meta: { title: "SKU管理" } },
      { path: 'stock-warning', component: SkuStockWarning, meta: { title: "库存预警" } },
      { path: 'logistics', component: LogisticsManagement, meta: { title: "物流管理" } },
      { path: 'notice', component: Notice, meta: { title: "消息通知" } },
      { path: 'admin', component: AdminManagent, meta: { title: "管理员管理" } },
      { path: 'role', component: Role, meta: { title: "角色管理" } },
      { path: 'menu', component: Menu, meta: { title: "菜单管理" } },
      { path: 'update-password', component: UpdatePassword, meta: { title: "修改密码" } },
      { path: 'carousel', component: Carousel, meta: { title: "轮播图管理" } },
      { path: 'log', component: Log, meta: { title: "日志记录" } },
      { path: 'notice/detail', component: NoticeDetail, meta: { title: "消息详情" } },
      { path: 'product/attrValue', component: ProductAttrValue, meta: { title: "规格值管理" } },
      { path: '/order/admin/detail/', component: OrderDetail, meta: { title: "订单详情" } },
      { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFound, hidden: true }
    ]
  }
]
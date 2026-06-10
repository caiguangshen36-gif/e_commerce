import Layout from '../views/user/Layout.vue'
import Home from '../views/user/Home.vue'
import Category from '../views/user/Category.vue'
import Cart from '../views/user/Cart.vue'
import UserInfo from '../views/user/UserInfo.vue'
import Login from '../views/user/Login.vue'
import productDetail from '../views/user/productDetail.vue'
// //主页模块子组件
// import Search from '../views/user/home/Search.vue'
import OrderFirm from '../views/user/orderFirm.vue'
import Pay from '../views/user/pay.vue'
import PayResult from '../views/user/payResult.vue'
import OrderDetail from '../views/user/orderDetail.vue'
import Help from '../views/user/Help.vue'
import CommentPage from '../views/user/CommentPage.vue'
import Search from '../views/user/home/Search.vue'
import SearchResult from '../views/user/SearchResult.vue'
import CategoryList from '../views/user/CategoryList.vue'
import Notice from '../views/user/Notice.vue'
import NoticeDetail from '../views/user/NoticeDetail.vue'

//售后模块子组件
import AfterSaleApply from '../views/user/afterSale/AfterSaleApply.vue'
import AfterSaleResult from '../views/user/afterSale/AfterSaleResult.vue'
import AfterSaleDelivery from '../views/user/afterSale/AfterSaleDelivery.vue'
import AfterSaleDetail from '../views/user/afterSale/AfterSaleDetail.vue'

//商品详情模块子组件
import Detail from '../views/user/detail/detail.vue'
import Comment from '../views/user/detail/Comment.vue'
import ReplyComment from '../views/user/detail/ReplyComment.vue'

// 用户info模块子组件
import Info from '../views/user/info/Info.vue'
import UserCollect from '../views/user/info/UserCollect.vue'
import UserFoot from '../views/user/info/UserFoot.vue'
import UserOrder from '../views/user/info/UserOrder.vue'
import AfterSale from '../views/user/info/AfterSale.vue'
import UserAddress from '../views/user/info/UserAddress.vue'
import UserUpdatePassword from '../views/user/info/UserUpdatePassword.vue'

export const userRoutes = [
  { path: '/login', component: Login },
  { path: '/toSearch', component: Search },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      { path: '/home', component: Home },
      { path: '/category', component: Category },
      { path: '/cart', component: Cart },
      { path: '/categoryList', component: CategoryList },
      { path: '/searchResult', component: SearchResult },
      { path: '/help', component: Help },
      { path: '/product/detail', component: productDetail },
      { path: '/order/confirm', component: OrderFirm },
      { path: '/pay', component: Pay },
      { path: '/pay/result', component: PayResult },
      { path: '/order/detail/', component: OrderDetail },
      { path: '/commentPage', component: CommentPage },
      { path: '/afterSaleApply', component: AfterSaleApply },
      { path: '/afterSaleResult', component: AfterSaleResult },
      { path: '/afterSaleDelivery', component: AfterSaleDelivery },
      { path: '/afterSaleDetail', component: AfterSaleDetail },
      {
        path: '/userinfo',
        component: UserInfo,
        redirect: '/userinfo/info',
        children: [
          { path: 'info', component: Info },
          { path: 'notice', component: Notice },
          { path: 'notice/detail', component: NoticeDetail },
          { path: 'collect', component: UserCollect },
          { path: 'foot', component: UserFoot },
          { path: 'order', component: UserOrder },
          { path: 'afterSale', component: AfterSale },
          { path: 'address', component: UserAddress },
          { path: 'updatePassword', component: UserUpdatePassword }
        ]
      }
    ]
  }
]
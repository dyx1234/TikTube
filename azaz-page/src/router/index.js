import { createRouter, createWebHashHistory } from 'vue-router';
import HomeView from '../views/Home.vue';
import FollowingView from '../views/Following.vue';
import LiveView from '../views/Live.vue';
import UploadView from '../views/Upload.vue';
import MessagesView from '../views/Messages.vue';
import UserView from '../views/User.vue';
import SearchView from '../views/Search.vue';
import ErrorView from '../views/Error.vue';
import DramaView from '../views/Drama.vue';
import EntertainmentView from '../views/Entertainment.vue';
import FashionView from '../views/Fashion.vue';
import FoodView from '../views/Food.vue';
import GamesView from '../views/Games.vue';
import HotView from '../views/Hot.vue';
import KnowledgeView from '../views/Knowledge.vue';
import LiveshowView from '../views/Liveshow.vue';
import SportsView from '../views/Sports.vue';

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/following',
      name: 'following',
      component: FollowingView
    },
    {
      path: '/live',
      name: 'live',
      component: LiveView
    },
    {
      path: '/upload',
      name: 'upload',
      component: UploadView
    },
    {
      path: '/messages',
      name: 'messages',
      component: MessagesView
    },
    {
      path: '/user/:id',
      name: 'user',
      props: true,
      component: UserView
    },
    {
      path: '/search',
      name: 'search',
      component: SearchView
    },
    {
      path: '/explore/drama',
      name: 'drama',
      component: DramaView
    },
    {
      path: '/explore/entertainment',
      name: 'entertainment',
      component: EntertainmentView
    },
    {
      path: '/explore/fashion',
      name: 'fashion',
      component: FashionView
    },
    {
      path: '/explore/food',
      name: 'food',
      component: FoodView
    },
    {
      path: '/explore/games',
      name: 'games',
      component: GamesView
    },
    {
      path: '/explore/hot',
      name: 'hot',
      component: HotView
    },
    {
      path: '/explore/knowledge',
      name: 'knowledge',
      component: KnowledgeView
    },
    {
      path: '/explore/liveshow',
      name: 'liveshow',
      component: LiveshowView
    },
    {
      path: '/explore/sports',
      name: 'sports',
      component: SportsView
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'error',
      component: ErrorView
    }
  ]
});

export default router;

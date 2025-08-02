const CACHE_NAME = 'express-scanner-v1.0.0';
const urlsToCache = [
  './',
  './index.html',
  './manifest.json',
  './assets/icon-192x192.png',
  './assets/icon-512x512.png'
];

// 安装事件 - 缓存资源
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('Opened cache');
        return cache.addAll(urlsToCache);
      })
  );
});

// 激活事件 - 清理旧缓存
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== CACHE_NAME) {
            console.log('Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});

// 拦截网络请求
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        // 如果缓存中有响应，则返回缓存的版本
        if (response) {
          return response;
        }

        // 否则，从网络获取
        return fetch(event.request).then(response => {
          // 检查是否收到有效响应
          if (!response || response.status !== 200 || response.type !== 'basic') {
            return response;
          }

          // 重要：克隆响应。响应是一个流，
          // 只能被消费一次。由于我们想要缓存响应以及
          // 将其返回给浏览器，我们需要克隆它，
          // 以便我们可以发送一个到浏览器，一个到缓存。
          const responseToCache = response.clone();

          caches.open(CACHE_NAME)
            .then(cache => {
              cache.put(event.request, responseToCache);
            });

          return response;
        });
      }
    )
  );
});

// 处理消息事件
self.addEventListener('message', event => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});
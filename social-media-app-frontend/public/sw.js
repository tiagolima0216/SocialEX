self.addEventListener('push', function (event) {
    const data = event.data ? event.data.json() : { title: 'Notification', body: '' };
    const title = data.title || 'Notification';
    const options = {
        body: data.body,
        data: data, // 👈 so we can use later
        icon: '/notification-icon.png'
    };
    event.waitUntil(self.registration.showNotification(title, options));
});

self.addEventListener('notificationclick', function (event) {
    event.notification.close();
    const urlToOpen = event.notification.data?.url || '/'; // 👈 FIX
    event.waitUntil(clients.openWindow(urlToOpen));
});

from django.urls import path
from . import views

urlpatterns = [
    path('posters/', views.get_posters, name='get_posters'),
    path('news/', views.get_news, name='get_news'),
    path('auth/register/', views.register_user, name='register'),
    path('auth/login/', views.login_user, name='login'),
    path('sync/', views.sync_performance, name='sync'),
    path('reviews/', views.performance_reviews, name='reviews'),
]

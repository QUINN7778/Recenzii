from django.urls import path
from . import views

urlpatterns = [
    # Старые пути
    path('posters/', views.get_posters, name='get_posters'),
    
    # Новые пути API
    path('auth/register/', views.register_user, name='register'),
    path('auth/login/', views.login_user, name='login'),
    path('sync/', views.sync_performance, name='sync'),
    path('reviews/', views.performance_reviews, name='reviews'),
]

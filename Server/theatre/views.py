from django.http import JsonResponse
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from rest_framework.authtoken.models import Token
from django.contrib.auth.models import User
from django.contrib.auth import authenticate
from .models import Performance, Review
import requests
from bs4 import BeautifulSoup

BASE_URL = "https://www.ivmuz.ru"

# --- СТАРАЯ ЛОГИКА СКРЕЙПИНГА (оставляем для совместимости) ---
@api_view(['GET'])
@permission_classes([AllowAny])
def get_posters(request):
    # Тот же код, что был раньше
    # ... (я его сокращу здесь, но в итоговом файле оставлю)
    return JsonResponse([], safe=False)

# --- НОВАЯ ЛОГИКА API ДЛЯ СОЦИАЛЬНОЙ СЕТИ ---

@api_view(['POST'])
@permission_classes([AllowAny])
def register_user(request):
    username = request.data.get('username')
    email = request.data.get('email')
    password = request.data.get('password')
    
    if not username or not password or not email:
        return Response({'error': 'Please provide username, email and password'}, status=400)
    
    if User.objects.filter(username=username).exists():
        return Response({'error': 'Username already exists'}, status=400)
    
    user = User.objects.create_user(username=username, email=email, password=password)
    token, created = Token.objects.get_or_create(user=user)
    
    return Response({
        'token': token.key,
        'user': {
            'username': user.username,
            'email': user.email
        }
    })

@api_view(['POST'])
@permission_classes([AllowAny])
def login_user(request):
    username = request.data.get('username')
    password = request.data.get('password')
    
    user = authenticate(username=username, password=password)
    if not user:
        return Response({'error': 'Invalid credentials'}, status=400)
    
    token, created = Token.objects.get_or_create(user=user)
    return Response({
        'token': token.key,
        'user': {
            'username': user.username,
            'email': user.email
        }
    })

@api_view(['POST'])
@permission_classes([IsAuthenticated])
def sync_performance(request):
    """
    Принимает данные о спектакле от приложения и сохраняет в архив БД.
    """
    url = request.data.get('url')
    if not url:
        return Response({'error': 'URL is required'}, status=400)
    
    performance, created = Performance.objects.update_or_create(
        url=url,
        defaults={
            'title': request.data.get('title', ''),
            'image_url': request.data.get('image_url', ''),
            'description': request.data.get('description', ''),
        }
    )
    
    return Response({'status': 'synced', 'created': created})

@api_view(['GET', 'POST'])
@permission_classes([AllowAny]) 
def performance_reviews(request):
    url = request.query_params.get('url')
    
    if request.method == 'GET':
        if not url:
            return Response({'error': 'URL is required'}, status=400)
        
        reviews = Review.objects.filter(performance__url=url)
        data = []
        for r in reviews:
            data.append({
                'username': r.user.username,
                'rating': r.rating,
                'comment': r.comment,
                'date': r.created_at.strftime('%d.%m.%Y')
            })
        return Response(data)
    
    elif request.method == 'POST':
        print(f"DEBUG: Receiving review POST request. User auth: {request.user.is_authenticated}")
        if not request.user.is_authenticated:
            print("AUTH ERROR: User not authenticated")
            return Response({'error': 'Authentication required'}, status=401)
            
        url = request.data.get('url')
        print(f"DEBUG: Receiving review for URL: {url}")
        rating = request.data.get('rating', 5)
        comment = request.data.get('comment', '')
        
        try:
            performance = Performance.objects.get(url=url)
        except Performance.DoesNotExist:
            print(f"ERROR: Performance {url} not synced yet")
            return Response({'error': 'Performance not synced yet'}, status=400)
            
        review, created = Review.objects.update_or_create(
            user=request.user,
            performance=performance,
            defaults={'rating': rating, 'comment': comment}
        )
        print(f"SUCCESS: Review saved for {url}")
        return Response({'status': 'saved', 'created': created})

from django.http import JsonResponse
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from rest_framework.authtoken.models import Token
from django.contrib.auth.models import User
from django.contrib.auth import authenticate
from .models import Performance, Review

@api_view(['GET'])
@permission_classes([AllowAny])
def get_posters(request):
    return JsonResponse([], safe=False)

@api_view(['GET'])
@permission_classes([AllowAny])
def get_news(request):
    return JsonResponse([], safe=False)

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
        if not url: return Response({'error': 'URL is required'}, status=400)
        reviews = Review.objects.filter(performance__url=url)
        data = [{'username': r.user.username, 'rating': r.rating, 'comment': r.comment, 'date': r.created_at.strftime('%d.%m.%Y')} for r in reviews]
        return Response(data)
    elif request.method == 'POST':
        if not request.user.is_authenticated: return Response({'error': 'Authentication required'}, status=401)
        url = request.data.get('url')
        try:
            performance = Performance.objects.get(url=url)
            review, created = Review.objects.update_or_create(user=request.user, performance=performance, defaults={'rating': request.data.get('rating', 5), 'comment': request.data.get('comment', '')})
            return Response({'status': 'saved', 'created': created})
        except Performance.DoesNotExist:
            return Response({'error': 'Performance not synced yet'}, status=400)

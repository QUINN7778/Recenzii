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
import re

BASE_URL = "https://www.ivmuz.ru"

def clean_title(title):
    t = title.lower()
    t = re.sub(r'(мюзикл|оперетта|комедия|сказка|драма|фантазия|детский|спектакль|для детей|по мотивам)', '', t)
    t = re.sub(r'[^а-яa-z0-9]', '', t)
    return t.strip()

@api_view(['GET'])
@permission_classes([AllowAny])
def get_posters(request):
    try:
        headers = {'User-Agent': 'Mozilla/5.0'}
        response = requests.get(f"{BASE_URL}/ticket_online/", headers=headers, timeout=15)
        soup = BeautifulSoup(response.content, 'html.parser')
        
        items = []
        for element in soup.select(".cell.active"):
            title_el = element.select_one(".name")
            if title_el:
                title = title_el.text.strip()
                day = element.select_one(".day").text.strip() if element.select_one(".day") else ""
                month = element.select_one(".month").text.strip() if element.select_one(".month") else ""
                time = element.select_one(".time").text.strip() if element.select_one(".time") else ""
                date_str = f"{day} {month}, {time}".strip()
                
                style = element.select_one(".performance_unit")['style'] if element.select_one(".performance_unit") else ""
                img = ""
                if "url(" in style:
                    img = style.split("url(")[1].split(")")[0].strip("'\"")
                if img.startswith("/"):
                    img = BASE_URL + img
                
                url = ""
                link_el = element.select_one("a")
                if link_el:
                    url = link_el['href']
                if url.startswith("/"):
                    url = BASE_URL + url
                
                items.append({
                    "title": title,
                    "description": "",
                    "date": date_str,
                    "imageUrl": img,
                    "detailUrl": url
                })
        
        # Убираем дубликаты по названию
        unique_items = []
        seen_titles = set()
        for item in items:
            clean = clean_title(item['title'])
            if clean not in seen_titles:
                unique_items.append(item)
                seen_titles.add(clean)
                
        return JsonResponse(unique_items, safe=False)
    except Exception as e:
        return JsonResponse([], safe=False)

@api_view(['GET'])
@permission_classes([AllowAny])
def get_news(request):
    try:
        headers = {'User-Agent': 'Mozilla/5.0'}
        response = requests.get(f"{BASE_URL}/news/", headers=headers, timeout=15)
        soup = BeautifulSoup(response.content, 'html.parser')
        
        items = []
        for element in soup.select(".cell"):
            text_el = element.select_one(".text")
            if not text_el:
                text_el = element.select_one("a")
            
            title = text_el.text.strip() if text_el else ""
            date_el = element.select_one(".date")
            date = date_el.text.strip() if date_el else ""
            
            if title and date and not re.match(r'^\d{2}.*', title):
                img_el = element.select_one("img")
                img = img_el['src'] if img_el else ""
                if img.startswith("/"):
                    img = BASE_URL + img
                
                items.append({
                    "title": title,
                    "description": "",
                    "date": date,
                    "imageUrl": img,
                    "detailUrl": ""
                })
        return JsonResponse(items, safe=False)
    except Exception as e:
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
        if not request.user.is_authenticated:
            return Response({'error': 'Authentication required'}, status=401)
            
        url = request.data.get('url')
        rating = request.data.get('rating', 5)
        comment = request.data.get('comment', '')
        
        try:
            performance = Performance.objects.get(url=url)
        except Performance.DoesNotExist:
            return Response({'error': 'Performance not synced yet'}, status=400)
            
        review, created = Review.objects.update_or_create(
            user=request.user,
            performance=performance,
            defaults={'rating': rating, 'comment': comment}
        )
        return Response({'status': 'saved', 'created': created})

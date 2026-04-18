from django.db import models
from django.contrib.auth.models import User

# Модель для "архива" спектаклей
class Performance(models.Model):
    url = models.URLField(unique=True, primary_key=True) # Ссылка на ivmuz.ru как уникальный ID
    title = models.CharField(max_length=255)
    image_url = models.URLField(null=True, blank=True)
    description = models.TextField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.title

# Модель для рецензий (отзывов)
class Review(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='reviews')
    performance = models.ForeignKey(Performance, on_delete=models.CASCADE, related_name='reviews')
    rating = models.IntegerField(default=5) # Оценка 1-5
    comment = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        # Один пользователь - одна рецензия на один спектакль
        unique_together = ('user', 'performance')
        ordering = ['-created_at']

    def __str__(self):
        return f"{self.user.username} - {self.performance.title}"

# Модель для билетов (опционально, если захотим хранить историю покупок)
class Ticket(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='tickets')
    performance = models.ForeignKey(Performance, on_delete=models.CASCADE)
    purchase_date = models.DateTimeField(auto_now_add=True)
    file_url = models.FileField(upload_to='tickets/', null=True, blank=True) # Если пользователь загрузит PDF

    def __str__(self):
        return f"Ticket for {self.performance.title} - {self.user.username}"

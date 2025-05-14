import joblib
import io

def check_url(url, model_bytes):
    """
    تحميل النموذج من Bytes، ثم التنبؤ بنوع الرابط.
    """
    model = joblib.load(io.BytesIO(model_bytes))  # تحميل النموذج من البايتات

    # التأكد من أن النموذج يحتوي على الـ Vectorizer و Classifier
    if not isinstance(model, dict) or 'vectorizer' not in model or 'classifier' not in model:
        return "❌ خطأ: النموذج غير صالح!"

    vectorizer = model['vectorizer']
    classifier = model['classifier']

    # التحقق من صلاحية الـ Vectorizer
    if not hasattr(vectorizer, 'vocabulary_'):
        return "❌ خطأ: TfidfVectorizer لم يتم تدريبه بشكل صحيح!"

    # تحويل الرابط إلى بيانات رقمية ثم التنبؤ بالفئة
    url_vector = vectorizer.transform([url])
    prediction = classifier.predict(url_vector)[0]

    return f"🔍 الرابط: {url} -> التوقع: {prediction}"

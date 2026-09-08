import json
import logging
from typing import Dict, Any, Optional
from app.core.config import settings

logger = logging.getLogger(__name__)

def analyze_privacy_policy_with_ai(policy_text: str, website_url: Optional[str] = None) -> Dict[str, Any]:
    """
    Analyzes raw Privacy Policy text using Google Gemini API.
    Falls back gracefully to rule-based evaluation if GEMINI_API_KEY is not configured.
    """
    if settings.GEMINI_API_KEY and len(settings.GEMINI_API_KEY.strip()) > 5:
        try:
            from google import genai
            client = genai.Client(api_key=settings.GEMINI_API_KEY)
            prompt = f"""
            Analyze the following Privacy Policy text for security and privacy risks.
            Respond strictly with valid JSON without markdown wrapping. Format:
            {{
                "risk_score": <number 0-100>,
                "risk_level": "<LOW | MEDIUM | HIGH | CRITICAL>",
                "summary": "<concise 2-sentence summary>",
                "data_collection": ["<collected data item 1>", "<collected data item 2>"],
                "third_party_sharing": ["<sharing practice 1>", "<sharing practice 2>"],
                "security_concerns": ["<concern 1>", "<concern 2>"],
                "recommendations": ["<recommendation 1>", "<recommendation 2>"]
            }}

            Privacy Policy Text:
            {policy_text[:4000]}
            """
            
            response = client.models.generate_content(
                model="gemini-2.5-flash",
                contents=prompt
            )
            raw_response = response.text.strip()
            if raw_response.startswith("```json"):
                raw_response = raw_response.replace("```json", "").replace("```", "").strip()
            
            parsed = json.loads(raw_response)
            parsed["website_url"] = website_url
            return parsed
        except Exception as e:
            logger.error(f"Gemini API Privacy Policy Analysis Error: {e}")

    # Fallback deterministic analysis
    text_lower = policy_text.lower()
    data_collected = []
    sharing = []
    concerns = []
    recommendations = []
    score = 25.0

    if "location" in text_lower or "gps" in text_lower:
        data_collected.append("Geographical location data")
        score += 20
        concerns.append("Tracks user location")
    if "contact" in text_lower or "address book" in text_lower:
        data_collected.append("Contacts list and phone directory")
        score += 15
        concerns.append("Accesses contacts")
    if "third party" in text_lower or "partners" in text_lower or "advertiser" in text_lower:
        sharing.append("Shares analytics and user demographics with third-party partners")
        score += 20
        concerns.append("Third-party data sharing")
    if "cookie" in text_lower or "tracking" in text_lower:
        data_collected.append("Browsing history and online cookies")
        score += 10
    if "device id" in text_lower or "imei" in text_lower or "advertising id" in text_lower:
        data_collected.append("Unique hardware identifiers and Ad ID")
        score += 15

    score = min(100.0, score)
    risk_level = "LOW" if score <= 30 else ("MEDIUM" if score <= 60 else ("HIGH" if score <= 80 else "CRITICAL"))

    if not data_collected:
        data_collected.append("General website usage statistics")

    recommendations.append("Opt-out of third-party advertising tracking where available.")
    recommendations.append("Review cookie preferences and account privacy settings.")

    return {
        "website_url": website_url,
        "risk_score": round(score, 1),
        "risk_level": risk_level,
        "summary": "Privacy policy analyzed. Contains standard data collection and analytics disclosure.",
        "data_collection": data_collected,
        "third_party_sharing": sharing if sharing else ["No explicit third-party sale mentioned"],
        "security_concerns": concerns if concerns else ["Standard data retention policy"],
        "recommendations": recommendations
    }

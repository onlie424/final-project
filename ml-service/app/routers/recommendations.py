from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Optional
from app.services.recommender import RecommenderService

router = APIRouter()
recommender = RecommenderService()


class UserContext(BaseModel):
    user_id: int
    completed_courses: List[int] = []
    completed_lessons: List[int] = []
    quiz_scores: dict = {}  # {quiz_id: score}
    interests: List[str] = []
    difficulty_preference: Optional[str] = None


class CourseRecommendation(BaseModel):
    course_id: int
    title: str
    score: float
    reason: str


class LessonRecommendation(BaseModel):
    lesson_id: int
    title: str
    score: float
    reason: str


@router.post("/courses", response_model=List[CourseRecommendation])
async def get_course_recommendations(context: UserContext, limit: int = 5):
    """
    Get personalized course recommendations for a user based on their learning history.
    """
    try:
        recommendations = recommender.recommend_courses(context, limit)
        return recommendations
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/next-lesson", response_model=LessonRecommendation)
async def get_next_lesson(context: UserContext, course_id: int):
    """
    Get the recommended next lesson for a user within a specific course.
    """
    try:
        recommendation = recommender.recommend_next_lesson(context, course_id)
        return recommendation
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/similar-courses/{course_id}", response_model=List[CourseRecommendation])
async def get_similar_courses(course_id: int, limit: int = 5):
    """
    Get courses similar to a given course.
    """
    try:
        similar = recommender.find_similar_courses(course_id, limit)
        return similar
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
import os

# Load environment variables
load_dotenv()

app = FastAPI(
    title="BrainPath ML Service",
    description="Machine Learning service for course recommendations and learning analytics",
    version="1.0.0"
)

# CORS configuration - allow requests from your React frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "http://localhost:5173",
        os.getenv("FRONTEND_URL", "http://localhost:3000")
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Import routers
from app.routers import recommendations, training

app.include_router(recommendations.router, prefix="/api/recommendations", tags=["Recommendations"])
app.include_router(training.router, prefix="/api/training", tags=["Training"])


@app.get("/")
def root():
    return {"message": "BrainPath ML Service is running"}


@app.get("/health")
def health_check():
    return {"status": "healthy"}

from fastapi import APIRouter, HTTPException, UploadFile, File, BackgroundTasks
from pydantic import BaseModel
from typing import Optional
import pandas as pd
import io
from app.services.trainer import ModelTrainer

router = APIRouter()
trainer = ModelTrainer()


class TrainingStatus(BaseModel):
    status: str
    message: str
    model_version: Optional[str] = None


class DatasetInfo(BaseModel):
    rows: int
    columns: int
    features: list


@router.post("/upload-dataset")
async def upload_dataset(file: UploadFile = File(...)):
    """
    Upload a CSV dataset for training.
    """
    if not file.filename.endswith('.csv'):
        raise HTTPException(status_code=400, detail="Only CSV files are supported")

    try:
        contents = await file.read()
        df = pd.read_csv(io.StringIO(contents.decode('utf-8')))

        # Save to data folder
        dataset_path = f"data/{file.filename}"
        df.to_csv(dataset_path, index=False)

        return DatasetInfo(
            rows=len(df),
            columns=len(df.columns),
            features=list(df.columns)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/train", response_model=TrainingStatus)
async def train_model(background_tasks: BackgroundTasks, dataset_name: str = "training_data.csv"):
    """
    Start model training in the background.
    """
    try:
        background_tasks.add_task(trainer.train, dataset_name)
        return TrainingStatus(
            status="started",
            message="Model training has been started in the background"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/status", response_model=TrainingStatus)
async def get_training_status():
    """
    Get the current training status.
    """
    return trainer.get_status()


@router.get("/model-info")
async def get_model_info():
    """
    Get information about the currently loaded model.
    """
    return trainer.get_model_info()

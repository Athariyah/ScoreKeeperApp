from fastapi import FastAPI

from backend.src.api.routes import router as match_router


app = FastAPI(title= "ScoreKeeperApp API")
app.include_router(match_router)

@app.get("/")
def root():
    return {"message" : "ScoreKeeperApp backend is running"}
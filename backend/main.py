from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional
import models, schemas, database

app = FastAPI(
    title="Quiz App API",
    version="1.3.0"
)

models.Base.metadata.create_all(bind=database.engine)

@app.get("/categories", response_model=List[schemas.Category], tags=["Catégories"])
def read_categories(db: Session = Depends(database.get_db)):
    return db.query(models.Category).all()

@app.post("/categories", response_model=schemas.Category, tags=["Catégories"])
def create_category(category: schemas.CategoryBase, db: Session = Depends(database.get_db)):
    db_exists = db.query(models.Category).filter(models.Category.name == category.name).first()
    if db_exists:
        raise HTTPException(status_code=400, detail="Cette catégorie existe déjà")
    db_cat = models.Category(name=category.name)
    db.add(db_cat)
    db.commit()
    db.refresh(db_cat)
    return db_cat

@app.put("/categories/{id}", response_model=schemas.Category, tags=["Catégories"])
def update_category(id: int, category: schemas.CategoryBase, db: Session = Depends(database.get_db)):
    db_cat = db.query(models.Category).filter(models.Category.id == id).first()
    if not db_cat:
        raise HTTPException(status_code=404, detail="Catégorie non trouvée")
    db_cat.name = category.name
    db.commit()
    db.refresh(db_cat)
    return db_cat

@app.delete("/categories/{id}", tags=["Catégories"])
def delete_category(id: int, db: Session = Depends(database.get_db)):
    db_cat = db.query(models.Category).filter(models.Category.id == id).first()
    if not db_cat:
        raise HTTPException(status_code=404, detail="Catégorie non trouvée")
    db.delete(db_cat)
    db.commit()
    return {"message": "Catégorie supprimée avec succès"}

@app.get("/questions", response_model=List[schemas.Question], tags=["Questions"])
def read_questions(category_id: Optional[int] = None, db: Session = Depends(database.get_db)):
    query = db.query(models.Question)
    if category_id:
        query = query.filter(models.Question.category_id == category_id)
    return query.all()

@app.post("/question", response_model=schemas.Question, tags=["Questions"])
def create_question(question: schemas.QuestionCreate, db: Session = Depends(database.get_db)):
    db_cat = db.query(models.Category).filter(models.Category.id == question.category_id).first()
    if not db_cat:
        raise HTTPException(status_code=404, detail="Catégorie non trouvée")
    db_q = models.Question(**question.dict())
    db.add(db_q)
    db.commit()
    db.refresh(db_q)
    return db_q

@app.put("/question/{id}", response_model=schemas.Question, tags=["Questions"])
def update_question(id: int, question: schemas.QuestionCreate, db: Session = Depends(database.get_db)):
    db_q = db.query(models.Question).filter(models.Question.id == id).first()
    if not db_q:
        raise HTTPException(status_code=404, detail="Question non trouvée")
    for key, value in question.dict().items():
        setattr(db_q, key, value)
    db.commit()
    db.refresh(db_q)
    return db_q

@app.delete("/question/{id}", tags=["Questions"])
def delete_question(id: int, db: Session = Depends(database.get_db)):
    db_q = db.query(models.Question).filter(models.Question.id == id).first()
    if not db_q:
        raise HTTPException(status_code=404, detail="Question non trouvée")
    db.delete(db_q)
    db.commit()
    return {"message": "Question supprimée avec succès"}

@app.get("/scores", response_model=List[schemas.Score], tags=["Scores"])
def read_scores(db: Session = Depends(database.get_db)):
    return db.query(models.Score).order_by(models.Score.score_value.desc()).all()

@app.post("/score", response_model=schemas.Score, tags=["Scores"])
def post_score(score: schemas.ScoreCreate, db: Session = Depends(database.get_db)):
    db_score = db.query(models.Score).filter(
        models.Score.username == score.username,
        models.Score.category_name == score.category_name
    ).first()
    if db_score:
        if score.score_value > db_score.score_value:
            db_score.score_value = score.score_value
            db_score.profile_image_url = score.profile_image_url
            db.commit()
            db.refresh(db_score)
        return db_score
    new_score = models.Score(**score.dict())
    db.add(new_score)
    db.commit()
    db.refresh(new_score)
    return new_score

@app.put("/score/{id}", response_model=schemas.Score, tags=["Scores"])
def update_score_manual(id: int, score: schemas.ScoreBase, db: Session = Depends(database.get_db)):
    db_s = db.query(models.Score).filter(models.Score.id == id).first()
    if not db_s:
        raise HTTPException(status_code=404, detail="Score non trouvé")
    for key, value in score.dict().items():
        setattr(db_s, key, value)
    db.commit()
    db.refresh(db_s)
    return db_s

@app.delete("/score/{id}", tags=["Scores"])
def delete_score(id: int, db: Session = Depends(database.get_db)):
    db_s = db.query(models.Score).filter(models.Score.id == id).first()
    if not db_s:
        raise HTTPException(status_code=404, detail="Score non trouvé")
    db.delete(db_s)
    db.commit()
    return {"message": "Score supprimé avec succès"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

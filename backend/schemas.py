from pydantic import BaseModel
from typing import List, Optional

class QuestionBase(BaseModel):
    text: str
    image_url: Optional[str] = None
    option_a: str
    option_b: str
    option_c: str
    option_d: str
    correct_answer: str
    category_id: int

class QuestionCreate(QuestionBase):
    pass

class Question(QuestionBase):
    id: int
    class Config:
        from_attributes = True

class CategoryBase(BaseModel):
    name: str

class CategoryCreate(CategoryBase):
    pass

class Category(CategoryBase):
    id: int
    questions: List[Question] = []
    class Config:
        from_attributes = True

class ScoreBase(BaseModel):
    username: str
    score_value: int
    profile_image_url: Optional[str] = None
    category_name: str

class ScoreCreate(ScoreBase):
    pass

class Score(ScoreBase):
    id: int
    class Config:
        from_attributes = True

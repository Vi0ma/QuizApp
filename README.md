# Quiz App - Android & FastAPI Backend

A complete and interactive quiz application developed for Android, featuring an expert recommendation system, voice recognition for answers, and real-time rankings.

## Project Description

This project is a dynamic Quiz platform consisting of an Android mobile application (Client) and a REST API developed with FastAPI (Backend). The application allows users to test their knowledge in various categories, take on daily challenges, and track their progress against the community.

### Highlights:
- **Expert Recommendation System**: "AI" logic that analyzes your scores to suggest categories best suited to your progress.
- **Voice Command Mode**: Answer questions simply using your voice (Google Voice Recognition).
- **Modern Architecture**: Uses Firebase for authentication and social data, paired with a FastAPI backend for quiz content management.

---

## Features

### Android Application
- **Authentication**: Registration and login via Firebase Auth.
- **User Profile**: Full personalization (profile picture via camera/gallery, favorite themes).
- **Dynamic Quizzes**: Timed questions with image support.
- **Daily Challenges**: A new challenge every day to earn points.
- **Rankings**: Global and daily leaderboards.
- **Review System**: Rate and comment on quiz categories.

### Backend (Python/FastAPI)
- **RESTful API**: Full CRUD for categories, questions, and scores.
- **Database**: Local management via SQLite and SQLAlchemy.
- **Automatic Documentation**: Integrated Swagger UI.

---

## Tech Stack

- **Frontend**: Java, Android SDK, Retrofit 2, Glide, Firebase (Auth/Firestore), Material Design.
- **Backend**: Python 3, FastAPI, SQLAlchemy, Pydantic, Uvicorn.
- **Tools**: Git, Android Studio, SQLite.

---

## Setup

### 1. Backend
```bash
cd backend
pip install -r requirements.txt
python main.py
```
*The API will be available at `http://localhost:8000`.*

### 2. Android
- Open the project in **Android Studio**.
- Update the IP address in `RetrofitClient.java` to point to your machine's local IP address.
- Compile and run the app on an emulator or physical device.

---

## Project Structure

- `/app`: Android application source code (Java).
- `/backend`: REST API source code (Python).
- `/.gitignore`: Clean repository configuration (excludes temporary files and local databases).

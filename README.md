# AI-Powered Quiz Question Generation System

## Project Overview
A backend microservice that automatically generates educational quiz questions using AI. The system enables educators to create diverse, topic-specific questions with minimal effort, enhancing learning experiences through AI-assisted content creation.

## Core Problem Statement
Educators spend significant time creating quiz questions, which often leads to:
- Limited question variety
- Time constraints reducing question quality
- Difficulty creating questions at different difficulty levels
- Lack of explanation for correct answers

## Solution
An automated system that:
1. Generates questions in multiple formats (MCQ, True/False, Short Answer)
2. Provides difficulty level customization
3. Includes answer explanations
4. Supports topic-based generation
5. Ensures educational accuracy through AI

## Key Users & Use Cases
1. **Educators**: Generate quiz questions for their courses
2. **Platform Administrators**: Moderate and approve generated content
3. **Students** (future): Take quizzes with AI-generated questions

## Technical Architecture
- **Backend**: Java Spring Boot (REST APIs)
- **AI Integration**: HuggingFace Inference API
- **Database**: PostgreSQL (structured data) + Redis (caching)
- **Security**: JWT-based authentication with role-based access
- **Documentation**: OpenAPI/Swagger

## Must-Have Features
1. **Question Generation API**
   - Accepts: Topic, question count, difficulty level, question type
   - Returns: Generated questions with options and correct answers
   - Rate limiting: Max 20 questions per request

2. **Question Management**
   - Store generated questions
   - Approval workflow (pending → approved → archived)
   - Search/filter by topic, difficulty, status

3. **Security & Access Control**
   - JWT authentication
   - Role-based permissions (Admin, Teacher, Student)
   - Secure API key management via environment variables

4. **Error Handling & Resilience**
   - Fallback question generation
   - AI service failure handling
   - Input validation

## Technical Requirements
- **Framework**: Spring Boot 3.x with Spring Security
- **Database**: PostgreSQL with JPA/Hibernate
- **Caching**: Redis for rate limiting and session management
- **API Client**: Spring WebClient for HuggingFace API calls
- **Documentation**: OpenAPI 3.0 with Swagger UI
- **Testing**: JUnit 5, Mockito, Integration tests

## Success Metrics
- Question generation success rate: >90%
- Response time: <5 seconds per question
- API availability: 99.9%
- User satisfaction: >4/5 rating from educators

## Future Extensions
1. **Batch Processing**: Generate questions in bulk for entire courses
2. **Quality Scoring**: AI evaluates question quality
3. **Multi-language Support**: Generate questions in different languages
4. **Analytics Dashboard**: Track question usage and effectiveness
5. **Export Options**: Export to various formats (JSON, CSV, Moodle XML)

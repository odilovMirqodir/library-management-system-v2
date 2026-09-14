# Library Management System V2

Library Management System — Spring Boot asosida yaratilgan REST API loyiha.

Tizim kutubxonadagi mualliflar, kategoriyalar, kitoblar, kitobxonlar va kitob berish/qaytarish jarayonlarini boshqaradi.

## Technologies

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Hibernate
- Jakarta Validation
- PostgreSQL
- Flyway
- Maven
- SpringDoc OpenAPI / Swagger
- JUnit 5
- Mockito
- MockMvc

## Features

Loyihada quyidagi imkoniyatlar mavjud:

- Author CRUD
- Category CRUD
- Book CRUD
- Reader CRUD
- Kitob berish
- Kitobni qaytarish
- Overdue loans
- Library summary report
- Search
- Filtering
- Sorting
- Pagination
- Validation
- Global exception handling
- Flyway database migrations
- Swagger / OpenAPI documentation
- Integration tests
- Mockito unit tests
- Demo data profile

## Project Structure

```text
src/main/java/uz/example/library
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── enums
├── exception
├── mapper
├── repository
└── service
```

## Database

Loyiha PostgreSQL ishlatadi.

Default database:

```text
library_v2_db
```

Test database:

```text
library_test_db
```

Database yaratish:

```sql
CREATE DATABASE library_v2_db;
CREATE DATABASE library_test_db;
```

Database schema Flyway orqali avtomatik yaratiladi.

Migration fayli:

```text
src/main/resources/db/migration/V1__create_initial_schema.sql
```

## Environment Variables

Loyiha quyidagi environment variablelardan foydalanadi:

```env
DB_URL=jdbc:postgresql://localhost:5432/library_v2_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

`DB_URL` va `DB_USERNAME` uchun default qiymatlar mavjud.

`DB_PASSWORD` berilishi kerak.

Namuna:

```text
.env.example
```

> Spring Boot `.env` faylni avtomatik o‘qimaydi. Environment variablelarni operatsion tizim yoki IDE orqali berish kerak.

### PowerShell

```powershell
$env:DB_PASSWORD="your_password"
```

Kerak bo‘lsa:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/library_v2_db"
$env:DB_USERNAME="postgres"
```

## Run Application

Windows PowerShell:

```powershell
$env:DB_PASSWORD="your_password"
.\mvnw.cmd spring-boot:run
```

Application ishga tushgandan keyin:

```text
http://localhost:8080
```

## Swagger

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Swagger orqali barcha REST endpointlarni ko‘rish va test qilish mumkin.

## API Endpoints

### Authors

```text
POST   /api/v1/authors
GET    /api/v1/authors
GET    /api/v1/authors/{id}
PUT    /api/v1/authors/{id}
DELETE /api/v1/authors/{id}
```

### Categories

```text
POST   /api/v1/categories
GET    /api/v1/categories
GET    /api/v1/categories/{id}
PUT    /api/v1/categories/{id}
DELETE /api/v1/categories/{id}
```

### Books

```text
POST   /api/v1/books
GET    /api/v1/books
GET    /api/v1/books/{id}
PUT    /api/v1/books/{id}
DELETE /api/v1/books/{id}
```

Book list quyidagi filterlarni qo‘llab-quvvatlaydi:

```text
title
isbn
authorId
categoryId
status
available
page
size
sort
```

Default holatda faqat `ACTIVE` kitoblar qaytariladi.

### Readers

```text
POST   /api/v1/readers
GET    /api/v1/readers
GET    /api/v1/readers/{id}
PUT    /api/v1/readers/{id}
PATCH  /api/v1/readers/{id}/status
DELETE /api/v1/readers/{id}
```

Reader statuslari:

```text
ACTIVE
BLOCKED
INACTIVE
```

Manual status o‘zgarishi:

```text
ACTIVE
BLOCKED
```

`INACTIVE` status loan history mavjud bo‘lgan kitobxon o‘chirilganda ishlatiladi.

### Loans

Kitob berish:

```text
POST /api/v1/loans
```

Request example:

```json
{
  "readerId": 1,
  "bookId": 1
}
```

Kitobni qaytarish:

```text
POST /api/v1/loans/{id}/return
```

Loan list:

```text
GET /api/v1/loans
```

Overdue loans:

```text
GET /api/v1/loans/overdue
```

### Reports

```text
GET /api/v1/reports/summary
```

Summary quyidagi ma'lumotlarni qaytaradi:

- Book titles
- Total copies
- Available copies
- Borrowed copies
- Active readers
- Overdue loans

## Borrow Rules

Kitob olish uchun:

- Reader mavjud bo‘lishi kerak
- Reader statusi `ACTIVE` bo‘lishi kerak
- Book mavjud bo‘lishi kerak
- Book statusi `ACTIVE` bo‘lishi kerak
- Kamida bitta available copy mavjud bo‘lishi kerak
- Reader bir vaqtning o‘zida maksimum 5 ta kitob olishi mumkin
- Reader bir xil ISBNdagi kitobni qaytarmasdan yana ololmaydi

Kitob berilganda:

```text
status = BORROWED
borrowedAt = current time
dueDate = borrowedAt + 14 days
returnedAt = null
availableCopies = availableCopies - 1
```

Borrow operatsiyasi transactional.

## Return Rules

Faqat `BORROWED` holatidagi loan qaytarilishi mumkin.

Qaytarilganda:

```text
status = RETURNED
returnedAt = current time
availableCopies = availableCopies + 1
```

`availableCopies` hech qachon `totalCopies`dan oshmasligi kerak.

Return operatsiyasi transactional.

## Book Delete Rules

Agar kitobda loan history bo‘lmasa:

```text
physical delete
```

Agar loan history mavjud bo‘lsa:

```text
status = INACTIVE
```

## Reader Delete Rules

Agar readerda active loan mavjud bo‘lsa:

```text
delete forbidden
```

Agar loan history mavjud bo‘lsa:

```text
status = INACTIVE
```

Agar loan history bo‘lmasa:

```text
physical delete
```

## Author and Category Delete Rules

Agar Author kitobga bog‘langan bo‘lsa, uni o‘chirib bo‘lmaydi.

Agar Category kitobga bog‘langan bo‘lsa, uni o‘chirib bo‘lmaydi.

## Pagination

List endpointlarda:

```text
page
size
sort
```

ishlatiladi.

Misol:

```text
GET /api/v1/books?page=0&size=10&sort=title
```

`size`:

```text
1 - 100
```

oralig‘ida bo‘lishi kerak.

## Validation

Asosiy validation qoidalari:

- Blank string qabul qilinmaydi
- String qiymatlar trim qilinadi
- Author/Reader full name: 2–120
- Book title: 2–200
- Category name: 2–80
- ISBN: 10 yoki 13 raqam
- ISBN ichidagi space va `-` olib tashlanadi
- Published year kelajakda bo‘lishi mumkin emas
- `totalCopies >= 1`
- Reader phone unique
- Category name case-insensitive unique
- Book ISBN unique
- Email berilgan bo‘lsa valid formatda bo‘lishi kerak
- Author birth date kelajakdagi sana bo‘lishi mumkin emas

## HTTP Status Codes

```text
200 OK
201 Created
204 No Content
400 Bad Request
404 Not Found
405 Method Not Allowed
409 Conflict
500 Internal Server Error
```

## Error Response

API standart error formatidan foydalanadi.

Example:

```json
{
  "timestamp": "2026-09-14T18:00:00",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation xatosi",
  "path": "/api/v1/books",
  "fieldErrors": {}
}
```

## Demo Data

Loyihada `demo` Spring profile mavjud.

Demo profile quyidagilarni yaratadi:

```text
5 Authors
5 Categories
15 Books
5 Readers
```

Demo rejimda ishga tushirish:

```powershell
$env:SPRING_PROFILES_ACTIVE="demo"
$env:DB_PASSWORD="your_password"

.\mvnw.cmd spring-boot:run
```

Demo data faqat database bo‘sh bo‘lsa yaratiladi.

Demo profile o‘chirish:

```powershell
Remove-Item Env:SPRING_PROFILES_ACTIVE
```

## Tests

Testlar uchun alohida PostgreSQL database ishlatiladi:

```text
library_test_db
```

Testlarni ishga tushirish:

```powershell
$env:DB_PASSWORD="your_password"
.\mvnw.cmd clean test
```

Current test suite:

```text
59 tests
0 failures
0 errors
```

Testlar tarkibida:

- REST API integration tests
- Validation tests
- Business rule tests
- Error handling tests
- Pagination/filter tests
- Loan tests
- Overdue tests
- Report tests
- Mockito unit tests

## Build

Loyihani build qilish:

```powershell
.\mvnw.cmd clean package
```

Testlarni bajarmasdan build qilish:

```powershell
.\mvnw.cmd clean package -DskipTests
```

## Main Business Entities

```text
Author
Category
Book
Reader
Loan
```

Book statuses:

```text
ACTIVE
INACTIVE
```

Reader statuses:

```text
ACTIVE
BLOCKED
INACTIVE
```

Loan statuses:

```text
BORROWED
RETURNED
```

## License

This project was created for educational purposes.
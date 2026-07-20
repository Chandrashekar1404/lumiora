# API Standards

## Base URL

/api/v1

---

## HTTP Methods

GET

POST

PUT

PATCH

DELETE

---

## Success Response

{
  "success": true,
  "message": "",
  "data": {}
}

---

## Error Response

{
  "success": false,
  "message": "",
  "errors": []
}

---

## Authentication

JWT Bearer Token

---

## Naming Convention

Plural resources

/students

/trainers

/courses

/batches

/users
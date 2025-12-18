#curl -i -X POST http://localhost:8080/users   -H 'Content-Type: application/json'   -d '{
#    "email": "dd22dss2@gmail.com",
#    "firstName": "John",
#    "lastName": "Doe",
#    "password": "secret123"
#  }'

curl -i http://localhost:8080/users/me   -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI4Iiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTc2NjA2NDg4OCwiZXhwIjoxNzY2MDY4NDg4fQ.ygrQaAdyGIblY7iKSXKfVwM2bZDDDUrYDPKiTVul_O0ZvgahFPRh9U1ptpq98HY-PkW37lSASdrSTBXYIZVG5Q"

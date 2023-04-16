# Kaizen Chat
## Project properties

- **SpringBoot:** (v3.0.5)
- **Java:** 17

---

## Registration

**Path:** `http://localhost:8080/register`

**Method:** Post

**Format:** JSON

**Body:**

```json
{
    "phoneNumber":"...",
    "nickname":"...",
    "userPhoto":"...",
    "password":"..."
}
```

**Responses:**

- 200:
  
```json
{
    "accessToken": "...",
    "accessTokenExpiration": "2023-04-14T23:40:02+03:00",
    "refreshTokenExpiration": "2023-05-14T23:10:02+03:00",
    "refreshToken": "..."
}
```

- 403:

```json
{
    "message": "invalid request"
}
```

---

## Login

**Path:** `http://localhost:8080/login`

**Method:** Post

**Format:** JSON

**Body:**

```json
{
    "phoneNumber": "...",
    "password": "..."
}
```

**Responses:**

- 200:

```json
{
    "isRegistered": "true",
    "accessToken": "...",
    "accessTokenExpiration": "2023-04-14T23:40:02+03:00",
    "refreshTokenExpiration": "2023-05-14T23:10:02+03:00",
    "refreshToken": "..."
}
```

- 403:

```json
{
    "isRegistered": "false"
}
```

---

## Refresh token

**Path:** `http://localhost:8080/refresh`

**Method:** Post

**Format:** JSON

**Body:**

```json
{
    "oldRefreshToken": "..."
}
```

**Responses:**

- 200:

```json
{
    "accessToken": "...",
    "accessTokenExpiration": "2023-04-14T23:40:02+03:00",
    "refreshTokenExpiration": "2023-05-14T23:10:02+03:00",
    "refreshToken": "..."
}
```

- 403:

```json
{
    "message": "wrong refresh token"
}
```
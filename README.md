# Kaizen Chat
## Project properties

- **SpringBoot:** (v3.0.5)
- **Java:** 17

---

## Registration

**Path:** `http://localhost:8080/auth/register`

**Method:** POST

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

**Path:** `http://localhost:8080/auth/login`

**Method:** POST

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

**Path:** `http://localhost:8080/auth/refresh`

**Method:** POST

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

---

## User information

**Path:** `http://localhost:8080/user/id/{id}`

**Method:** GET

**Responses:**

- 200:

```json
{
  "user": {
    "id": 3,
    "phoneNumber": "...",
    "nickname": "...",
    "avatar": "...",
    "bio": null,
    "registration": "2023-04-21T21:23:54.455804+03:00"
  }
}
```

- 404:

```json
{
  "user": {
    "id": null,
    "phoneNumber": null,
    "nickname": null,
    "avatar": null,
    "bio": null,
    "registration": null
  }
}
```
<br/>

**Path:** `http://localhost:8080/user/phone/{phoneNumber}`

**Method:** GET

**Responses:**

- 200:

```json
{
  "user": {
    "id": 3,
    "phoneNumber": "...",
    "nickname": "...",
    "avatar": "...",
    "bio": null,
    "registration": "2023-04-21T21:23:54.455804+03:00"
  }
}
```

- 404:

```json
{
  "user": {
    "id": null,
    "phoneNumber": null,
    "nickname": null,
    "avatar": null,
    "bio": null,
    "registration": null
  }
}
```
<br/>

**Path:** `http://localhost:8080/user/update`

**Method:** POST

**Format:** JSON

**Body:**

```json
{
  "id":1,
  "nickname":"...",
  "bio":"..."
}
```

**Responses:**

- 200:

```json
{
  "message": "user updated"
}
```

- 403:

```json
{
  "message": "wrong id"
}
```

<br/>

**Path:** `http://localhost:8080/user/upload-avatar`

**Method:** POST

**Format:** form-data

**Body:**

```
key: "avatar"
value: image (jpeg, jpg, png, up to 3 megabytes)
```

**Responses:**

- 200:

```json
{
    "message": "updated"
}
```

- 400:

```json
{
    "message": "file is not present"
}
```

```json
{
    "message": "file size is greater than 3MB"
}
```

```json
{
    "message": "uploaded file is not an image"
}
```

- 403:

```json
{
    "message": "user is not defined"
}
```

<br/>

**Path:** `http://localhost:8080/user/{userId}/download-avatar`

**Method:** GET

**Format:** form-data

**Body:**

```
key: "avatar"
value: image (jpeg, jpg, png, up to 3 megabytes)
```

**Responses:**

- 200:

```
[image]
```

- 404
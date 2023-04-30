# Kaizen Chat
## Project properties

- **SpringBoot:** (v3.0.5)
- **Java:** 17

---

<br/>

- ## REST

## Registration

<details>

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

- 400:

```json
{
    "password": "length should be 8 or longer",
    "phoneNumber": "length should be 13",
    "nickname": "should not be blank"
}
```

- 403:

```json
{
  "message": "invalid request"
}
```

</details>

---

## Login

<details>

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

- 400:

```json
{
  "password": "length should be 8 or longer",
  "phoneNumber": "length should be 13"
}
```

```json
{
  "password": "should not be blank",
  "phoneNumber": "should not be blank"
}
```

- 403:

```json
{
  "isRegistered": "false"
}
```

</details>

---

## Refresh token

<details>

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

- 400:

```json
{
  "oldRefreshToken": "should not be blank"
}
```

- 403:

```json
{
  "message": "wrong refresh token"
}
```

</details>

---

## User information

<details>

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
  "path": "/user/id/546",
  "message": "user with id:546 not found",
  "statusCode": 404,
  "timestamp": "2023-04-30T14:29:13.080851+03:00"
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
  "path": "/user/phone/+38057865890",
  "message": "user with phone-number:[+38057865890] not found",
  "statusCode": 404,
  "timestamp": "2023-04-30T14:30:18.7618307+03:00"
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

- 400:

```json
{
    "nickname": "length should be 4 or longer"
}
```

- 404:

```json
{
  "path": "/user/update",
  "message": "user with id:564 not found",
  "statusCode": 404,
  "timestamp": "2023-04-30T14:32:23.2791826+03:00"
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

<br/>

**Path:** `http://localhost:8080/user/group-chats/all`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 200:

```json
[
  {
    "id": 5,
    "userId": 2,
    "username": "bie3",
    "lastMessage": "yooo",
    "lastMessageTime": "2023-04-28T14:20:26.983+03:00"
  },
  {
    "id": 4,
    "userId": 2,
    "username": "bie3",
    "lastMessage": "hello world",
    "lastMessageTime": "2023-04-25T10:42:09.449007+03:00"
  }
]
```

- 403:

```json
{
  "message": "user is not found"
}
```

</details>

---

<br/>

- ## Web Sockets

## Connection

<details>

**Path:** `http://localhost:8080/ws-open`

**Description:** this end-point establishes real-time connection between client and server. For that purpose client must use SockJS and StompJS client.

When Stomp client is created over web-socket he has to connect to the server with such header:
`Authorization: bearer (jwt)`. When successfully connected (via switching protocols) to the server, client can subscribe on channels and send messages as he needs.

</details>

---

## Subscription

<details>

**Path:** `/chatroom/{chat-id}`

**WS Client:** StompJS

**Headers:** `Authorization: bearer (jwt)`

**Description:** this end-point is used to subscribe only on group chats.

</details>

---

## Group chats

<details>

### Join to chat

**Path:** `/app/join`

**WS Client:** StompJS

**Body format:** JSON

**Headers:** `Authorization: bearer (jwt)`

**Body:**

```json
{
  "chatId": 1,
  "privacyMode": true,
  "password": "password, if privacy mode is true"
}
```

**Responses:**

- Status: `MESSAGE`

```json
{
  "action": "JOIN",
  "body": "bie3 joined to the chat",
  "chatId": 4,
  "senderId": 2,
  "senderNickname": "bie3",
  "timeStamp": "2023-04-25T10:36:34.2459185+03:00"
}
```

---

### Quit from chat

**Path:** `/app/quit/{chat-id}`

**WS Client:** StompJS

**Headers:** `Authorization: bearer (jwt)`


**Responses:**

- Status: `MESSAGE`

```json
{
  "action": "QUIT",
  "body": "bie3 left the chat",
  "chatId": 4,
  "senderId": 2,
  "senderNickname": "bie3",
  "timeStamp": "2023-04-25T10:36:34.2459185+03:00"
}
```

---

### Send message to chat

**Path:** `/app/send`

**WS Client:** StompJS

**Body format:** JSON

**Headers:** `Authorization: bearer (jwt)`

**Body:**

```json
{
  "chatId": 4,
  "body": "hello world"
}
```

**Responses:**

- Status: `MESSAGE`

```json
{
  "action": "SEND",
  "body": "hello world",
  "chatId": 4,
  "senderId": 2,
  "senderNickname": "bie3",
  "timeStamp": "2023-04-25T10:42:09.4639461+03:00"
}
```

</details>
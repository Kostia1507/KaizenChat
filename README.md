# Kaizen Chat
## Project properties

- **SpringBoot:** (v3.0.5)
- **Java:** 17

---

# ***<p style="text-align: center;">REST</p>***

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
  "password":"..."
}
```

### Check if the number exists

**Path:** `http://localhost:8080/user/exists/{phoneNumber}`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 204: no content

- 404:

```json
{
  "path": "/auth/exists/+380500000004",
  "message": "user with phone-number:[+380500000004] not found",
  "statusCode": 404,
  "timestamp": "2023-05-02T21:23:00.8985278+03:00"
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
  "path": "/auth/register",
  "message": "Such phone-number already exists",
  "statusCode": 403,
  "timestamp": "2023-05-02T19:19:42.7020063+03:00"
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
  "isLoggedIn": "false"
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

- 404:

```json
{
  "path": "/auth/refresh",
  "message": "refresh token was not found",
  "statusCode": 404,
  "timestamp": "2023-05-02T19:20:34.7733007+03:00"
}
```

</details>

---

## User information

<details>

**Path:** `http://localhost:8080/user`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 200:

```json
{
  "id": 3
}
```

- 403:

```json
{
  "message": "Invalid token"
}
```

---

**Path:** `http://localhost:8080/user/id/{id}`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 200:

```json
{
  "id": 3,
  "phoneNumber": "...",
  "nickname": "...",
  "bio": null,
  "registration": "2023-05-05T01:09:56.077+03:00"
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

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 200:

```json
{
  "id": 3,
  "phoneNumber": "...",
  "nickname": "...",
  "bio": null,
  "registration": "2023-05-05T01:09:56.077+03:00"
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

<br/>

**Path:** `http://localhost:8080/user/update`

**Method:** POST

**Authorization header format:** `Bearer [access token]`

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

**Authorization header format:** `Bearer [access token]`

**Format:** JSON

**Body:**

```json
{
  "encodedContent": "data:image/jpg;base64,..."
}
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
  "encodedContent": "should be not blank"
}
```

<br/>

**Path:** `http://localhost:8080/user/{userId}/avatar`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Format:** JSON

**Responses:**

- 200:

```
{
  "encodedContent": "data:image/jpg;base64,..."
}
```

- 404:

```json
{
    "path": "/user/555/avatar",
    "message": "user with id:555 not found",
    "statusCode": 404,
    "timestamp": "2023-05-16T12:20:03.0091015+03:00"
}
```

</details>

---

## Duo chats

<details>

### Start a chat

<details>

**Path:** `http://localhost:8080/user/duo-chats/start/{userId}`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 200:

```json
{
  "chatId": 6
}
```

- 404:

```json
{
  "path": "/user/duo-chats/start/545",
  "message": "user with id:545 not found",
  "statusCode": 404,
  "timestamp": "2023-05-02T19:54:35.1147599+03:00"
}
```

</details>

---

### Get all duo chats

<details>

**Path:** `http://localhost:8080/user/duo-chats/all`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 200:

```json
[
  {
    "id": 1,
    "name": null,
    "userId": 1,
    "username": "...",
    "lastMessage": "...",
    "lastMessageTime": "2023-04-23T10:51:36.129+03:00"
  },
  {
    "id": 2,
    "name": null,
    "userId": 1,
    "username": "...",
    "lastMessage": "...",
    "lastMessageTime": "2023-04-23T10:51:36.129+03:00"
  }
]
```

</details>

---

### Get duo-chat by ID

<details>

**Path:** `http://localhost:8080/user/duo-chats/{chatId}`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 200:

```json
{
  "id": 1,
  "name": "Duo",
  "creation": "...",
  "groupChatOptions": null,
  "messages": [],
  "users": [
    {
      "id": 1,
      "phoneNumber": "...",
      "nickname": "...",
      "avatar": "...",
      "bio": "...",
      "registration": "2023-04-23T10:51:36.129+03:00"
    },
    {
      "id": 2,
      "phoneNumber": "...",
      "nickname": "...",
      "avatar": "...",
      "bio": "...",
      "registration": "2023-04-23T10:51:36.129+03:00"
    }
  ],
  "groupChat": false
}
```

- 404:

```json
{
  "path": "/user/duo-chats/24",
  "message": "DUO-chat:24 was not found",
  "statusCode": 404,
  "timestamp": "2023-05-02T19:11:11.3536078+03:00"
}
```

</details>

---

### Get chat with specific User

<details>

**Path:** `http://localhost:8080/user/duo-chats/with/{userId}`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 200:

```json
{
  "id": 1,
  "name": "Duo",
  "creation": "...",
  "groupChatOptions": null,
  "messages": [],
  "users": [
    {
      "id": 1,
      "phoneNumber": "...",
      "nickname": "...",
      "avatar": "...",
      "bio": "...",
      "registration": "2023-04-23T10:51:36.129+03:00"
    },
    {
      "id": 2,
      "phoneNumber": "...",
      "nickname": "...",
      "avatar": "...",
      "bio": "...",
      "registration": "2023-04-23T10:51:36.129+03:00"
    }
  ],
  "groupChat": false
}
```

- 404:

```json
{
  "path": "/user/duo-chats/with/3",
  "message": "duo-chat between [8, 3] was not found",
  "statusCode": 404,
  "timestamp": "2023-05-02T19:17:07.9071073+03:00"
}
```

---

</details>

---

### Get last messages

<details>

**Path:** `localhost:8080/user/duo-chats/messages`

**Method:** POST

**Authorization header format:** `Bearer [access token]`

**Format:** JSON

**Body:**

```json
{
  "chatId":1,
  "time":"2023-04-14T23:40:02+03:00"
}
```
Time can be null

**Responses:**

- 200:

```json
{
  "messages": [
    {
      "id": 1,
      "body": "...",
      "time": "2023-04-23T10:51:36.129+03:00",
      "isPinned": false,
      "likes": 0
    },
    {
      "id": 2,
      "body": "...",
      "time": "2023-04-23T11:53:48.129+03:00",
      "isPinned": false,
      "likes": 0
    }
  ]
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

- 404:

```json
{
  "path": "/user/duo-chats/messages",
  "message": "not member of chat",
  "statusCode": 404,
  "timestamp": "2023-05-02T11:53:49.8606015+03:00"
}

```

- 404:

```json
{
  "path": "/user/duo-chats/messages",
  "message": "DUO-chat:10 was not found",
  "statusCode": 404,
  "timestamp": "2023-05-02T11:58:58.4126182+03:00"
}

```

</details>

</details>

---

## Group chats

<details>

**Path:** `http://localhost:8080/user/group-chats/all`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 200:

```json
[
  {
    "id": 5,
    "name": "Home",
    "adminId": 1,
    "userId": 2,
    "username": "bie3",
    "lastMessage": "yooo",
    "lastMessageTime": "2023-04-28T14:20:26.983+03:00"
  },
  {
    "id": 4,
    "name": "Party",
    "adminId": 1,
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

<br/>

**Path:** `http://localhost:8080/user/group-chats/new`

**Method:** POST

**Authorization header format:** `Bearer [access token]`

**Body:**

```json
{
  "name": "Test 3 group chats",
  "privacyMode": true,
  "password": "r299d"
}
```

Password can be null if privacy mode is false

**Responses:**

- 200:

```json
{
  "id": 6,
  "name": "Test 3 group chats",
  "membersCount": 1,
  "membersLimit": 5000
}
```

- 403:

```json
{
    "name": "should be not blank"
}
```

```json
{
    "path": "/user/group-chats/new",
    "message": "password is not defined",
    "statusCode": 403,
    "timestamp": "2023-05-15T21:22:31.6380158+03:00"
}
```

<br/>

**Path:** `http://localhost:8080/user/group-chats/{chatId}/avatar`

**Method:** GET

**Authorization header format:** `Bearer [access token]`

**Responses:**

- 200:

```json
{
  "encodedContent": "data:image/jpg;base64,..."
}
```

- 404:

```json
{
  "path": "/user/group-chats/4/avatar",
  "message": "GROUP-chat:4 was not found",
  "statusCode": 404,
  "timestamp": "2023-05-03T19:05:42.0903724+03:00"
}
```

<br/>

**Path:** `http://localhost:8080/user/group-chats/{chatId}/upload-avatar`

**Method:** POST

**Format:** JSON

**Body:**

```json
{
  "encodedContent": "data:image/jpg;base64,..."
}
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
  "encodedContent": "should be not blank"
}
```

- 403:

```json
{
  "path": "/user/group-chats/5/upload-avatar",
  "message": "only admin can change avatar",
  "statusCode": 403,
  "timestamp": "2023-05-16T15:15:20.6786852+03:00"
}
```

- 404:

```json
{
  "path": "/user/group-chats/4/upload-avatar",
  "message": "GROUP-chat:4 was not found",
  "statusCode": 404,
  "timestamp": "2023-05-03T19:19:09.0560148+03:00"
}
```

</details>

---

# ***<p style="text-align: center;">Web sockets</p>***

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

**Path:** `/user/{user-id}/start`

**WS Client:** StompJS

**Headers:** `Authorization: bearer (jwt)`

**Description:** this end-point is used to obtain information about new duo chats.

---

**Path:** `/duo-chat/{chat-id}`

**WS Client:** StompJS

**Headers:** `Authorization: bearer (jwt)`

**Description:** this end-point is used to subscribe only on duo chats.

---

**Path:** `/chatroom/{chat-id}`

**WS Client:** StompJS

**Headers:** `Authorization: bearer (jwt)`

**Description:** this end-point is used to subscribe only on group chats.

</details>

---

## Group chats

<details>

### Join to chat

<details>

**Path:** `/app/group-chats/join`

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

</details>

---

### Quit from chat

<details>

**Path:** `/app/group-chats/quit/{chat-id}`

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

</details>

---

### Send message to chat

<details>

**Path:** `/app/group-chats/send`

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

---

### Edit message

<details>

**Path:** `/app/group-chats/edit`

**WS Client:** StompJS

**Body format:** JSON

**Headers:** `Authorization: bearer (jwt)`

**Body:**

```json
{
  "messageId": 4,
  "body": "edited text"
}
```

**Responses:**

- Status: `MESSAGE`

```json
{
  "action": "EDIT",
  "chatId": 4,
  "body": "hello world",
  "messageId": 10
}
```

</details>

---

### Delete message

<details>

**Path:** `/app/group-chats/delete/{messageId}`

**WS Client:** StompJS

**Body format:** JSON

**Headers:** `Authorization: bearer (jwt)`

**Responses:**

- Status: `MESSAGE`

```json
{
  "action": "DELETE",
  "chatId": 4,
  "messageId": 10
}
```

</details>

---

### Kick member from group chat

<details>

**Path:** `/app/group-chats/kick`

**WS Client:** StompJS

**Body format:** JSON

**Headers:** `Authorization: bearer (jwt)`

**Body:**

```json
{
  "chatId": 4,
  "userId": 2
}
```

**Responses:**

- Status: `MESSAGE`

```json
{
  "action": "QUIT",
  "chatId": 4,
  "senderId": 2,
  "body": "user was kicked by adminNickname"
}
```

senderId is an id of kicked member!

</details>

</details>

---

## Duo chats

<details>

### Send message

<details>

**Path:** `/app/duo-chat/send`

**WS Client:** StompJS

**Headers:** `Authorization: bearer (jwt)`

**Body:**

```json
{
  "chatId": 4,
  "body": "..."
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

---

### Edit message

<details>
**Path:** `/app/duo-chat/edit`

**WS Client:** StompJS

**Headers:** `Authorization: bearer (jwt)`

**Body:**

```json
{
  "messageId": 4,
  "body": "..."
}
```

**Responses:**

- Status: `MESSAGE`

```json
{
  "action": "EDIT",
  "body": "hello world",
  "chatId": 3,
  "messageId": 4
}
```

</details>

---

### Delete message

<details>
**Path:** `/app/duo-chat/delete/{messageId}`

**WS Client:** StompJS

**Headers:** `Authorization: bearer (jwt)`

**Responses:**

- Status: `MESSAGE`

```json
{
  "action": "DELETE",
  "chatId": 3,
  "messageId": 4
}
```

</details>

</details>
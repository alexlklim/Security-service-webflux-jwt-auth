AUTH-SERVICE
Pattern:   /api/auth/**
Description: Service for registration and authentication users

FUNCTIONALITIES
method	endpoint	description	return
POST	:/register	Register user, only for auth users with ROLE_ADMIN	UserDTO, Code
POST	:/login	Available for all	Token
POST	:/refresh-token		
GET	:/activate/{token}	Link for activation will be send by email	
POST	:/change-password	Then user confirm his email and make account	
GET	:/logout		


PROCEDURE

PROCEDURE OF USER REGISTRATION
:/register

Registration request
{
  "username": "csmm@gmail.com",
  "password": "1122",
  "role": "ADMIN",
  "first_name": "Csmm",
  "last_name": "Alph"
}

- CALL PROCEDURE OF USER AUTHENTICATION

If user is authenticated and has ROLE_ADMIN, firstly check if certain email already exits in DB. If exists return:
StatusCode: 409 Conflict
{
    "code": "409",
    "message": "USER_ALREADY_EXISTS_EXCEPTION"
}
If email is not taken, create new user and return:
StatusCode: 201 Created
{
    "id": 9,
    "username": "csmm@gmail.com",
    "role": "admin",
    "first_name": "Csmm",
    "last_name": "Alph",
    "enabled": false,
    "created_at": "2024-01-30T07:50:26.784019",
    "updated_at": "2024-01-30T07:50:26.784019"
}

- SEND REQUEST TO EMAIL-SERVICE TO SEND EMAIL TO ACTIVATE ACCOUNT
Login is not available before activating


PROCEDURE OF USER AUTHENTICATION
:/login

Login request
{
  "username": "csmm@gmail.com",
  "password": "1122"
}

Try to find specific user in DB. If user not found:
new AuthException("Incorrect data", ErrorCode.INVALID_USERNAME.name())

If user is found:
Check if user is enabled and  if not
log.error(ErrorCode.USER_ACCOUNT_DISABLED.name());
return Mono.error(new AuthException("Incorrect data", ErrorCode.USER_ACCOUNT_DISABLED.name()));

Check if password is correct with help of PasswordEncoder. We use BCryptPasswordEncoder
log.error(ErrorCode.INVALID_PASSWORD.name());
return Mono.error(new AuthException("Incorrect data", ErrorCode.INVALID_PASSWORD.name()));

If everything is okay -> create a token with help of TokenGenProvider:
TokenDetails look like this:
public class TokenDetails {
    private Long userId;
    private String token;
    private Date issuedAt;
    private Date expiresAt;
    private String role;
    private String refreshToken;
}

For Token Generation we use:


Expiration time is equal 15 minutes and secret is configured in application.yaml

- CALL PROCEDURE OF CREATING OF REFRESH TOKEN

Aftet this SecurityService return Mono<TokenDetails> to controller. In case of incorrect login or pw server return: 
StatusCode: 401Unauthorized
If login and password correct, user get json object with neede data
StatusCode: 200OK
{
    "user_id": 1,
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImNzbW1AZ21haWwuY29tIiwiaXNzIjoiY3NtbSIsInN1YiI6IjEiLCJpYXQiOjE3MDY2MjE0NDEsImV4cCI6MTcwNjYyMjM0MX0.PXgB6HYNEgY88zkT3cxhzjgeH7QrQi6zfD9hty7f_CI",
    "issued_at": "2024-01-30T13:30:41.081+00:00",
    "expires_at": "2024-01-30T13:45:41.081+00:00",
    "refresh_token": "8cb96423-757f-4b06-a2c3-4392b5c2a55f",
    "role": "ADMIN"
}




PROCEDURE OF CREATING OF REFRESH TOKEN
Check if this user has token, if yes delete token from db, create new and return new refresthToken
RefreshToken model
public class RefreshToken {
    @Id
    private Long id;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private Long userId;
}

RefreshToken is UUID generated randomly


Create and return new RefreshToken. The living time for refresh token is 24 hours


PROCEDURE OF TOKEN REFRESHING
:/refresh-token

Refresh-token request
{
  "refresh_token": "42595ba1-c186-4b95-9621-3a01f68f430e",
  "user_id": 1
}

Check if date of expiration before or after.  If „after” return 401Unauthorized
If before It returns a new TokenDetails entity. We use the same method that was used during user’s authentication
StatusCode: 200OK
{
    "user_id": 1,
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImNzbW1AZ21haWwuY29tIiwiaXNzIjoiY3NtbSIsInN1YiI6IjEiLCJpYXQiOjE3MDY2MjE0NDEsImV4cCI6MTcwNjYyMjM0MX0.PXgB6HYNEgY88zkT3cxhzjgeH7QrQi6zfD9hty7f_CI",
    "issued_at": "2024-01-30T13:30:41.081+00:00",
    "expires_at": "2024-01-30T13:45:41.081+00:00",
    "refresh_token": "8cb96423-757f-4b06-a2c3-4392b5c2a55f",
    "role": "ADMIN"
}


REQUESTS AND RESPONSES
REGISTRATION	
Register request:
{
  "username": "cfffl@gmail.com",
  "password": "1122",
  "role": "CLIENT",
  "first_name": "Csmm",
  "last_name": "Alph"
}
 
Register response - Status Code:  201 Created
{
    "id": 7,
    "username": "user@gmail.com",
    "role": "client",
    "first_name": "User",
    "last_name": "Usual",
    "enabled": true,
    "created_at": "2024-01-31T10:31:34.0814566",
    "updated_at": "2024-01-31T10:31:34.0814566"
}

Register response - Status Code:  409 Conflict
{
    "code": "409",
    "message": "USER_ALREADY_EXISTS_EXCEPTION"
}

Register response - Status Code:  401 Unauthorized
{
    "code": "401",
    "message": "UNAUTHORIZED"
}

LOGIN
Login request
{
  "username": "csmm@gmail.com",
  "password": "1122"
}

Login response - Status Code:  200 OK
{
    "user_id": 1,
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImNzbW1AZ21haWwuY29tIiwiaXNzIjoiY3NtbSIsInN1YiI6IjEiLCJpYXQiOjE3MDY2OTM0MzQsImV4cCI6MTcwNjY5NDMzNH0.oOVbxeQ8LTq_xYbe26c955fMf82ItjUkF3IyWnEC2eU",
    "issued_at": "2024-01-31T09:30:34.738+00:00",
    "expires_at": "2024-01-31T09:45:34.738+00:00",
    "refresh_token": "48ce6727-494a-48e0-885e-169a7995fe2d",
    "role": "ADMIN"
}

Login response - Status Code:  401 Unauthorized


REFRESH-TOKEN
Refresh-token request
{
    "refresh_token": "97071184-f4e7-4ee0-8811-4f70997a8e03",
    "user_id": 1
}

Refresh-token response - Status Code:  200 OK
{
    "user_id": 1,
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImNzbW1AZ21haWwuY29tIiwiaXNzIjoiY3NtbSIsInN1YiI6IjEiLCJpYXQiOjE3MDY2OTc2MjYsImV4cCI6MTcwNjY5ODUyNn0.PWzo-184DQ01HQjU3Q0wnGAIEmhNRfQhxoHhFKuWZRY",
    "issued_at": "2024-01-31T10:40:26.003+00:00",
    "expires_at": "2024-01-31T10:55:26.003+00:00",
    "refresh_token": "8a96f119-a599-4d04-a58f-76375efccd22",
    "role": "ADMIN"
}

Refresh-token response - Status Code:  401 Unauthorized

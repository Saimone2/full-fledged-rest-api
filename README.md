# BVP-Software-task
###### REST API with JWT authentication

### Prerequisites
- JDK 17
- Maven

### Technology stack:
* Spring Boot (3.2.4 version)
* Spring Data JPA
* PostgreSQL (42.7.3 version)
* Spring Security
* JWT (JSON Web Tokens)

## Description

This Spring Boot project serves as a backend for user authentication and management. 
It provides endpoints for user registration, login and user management functionalities.

## Endpoints
### User Management

---
<b>GET :</b> '/api/user/current-user'

<b>Description:</b> Retrieves information about the current authenticated user.

<b>Authorization:</b> Requires user authentication (ADMIN or USER role).

<b>Parameters:</b>  
Authorization (Header): Bearer token for authentication.

<b>Response:</b> Returns information about the current user.

---
<b>GET :</b> '/api/user/all'

<b>Description:</b> Retrieves a paginated list of all users.

<b>Authorization:</b> Requires ADMIN role.

<b>Parameters:</b>  
page (int): Page number for pagination.  
size (int): Number of users per page.

<b>Response:</b> Returns a paginated list of users.

---
### Ping

---
<b>GET :</b> '/ping'

<b>Description:</b> Endpoint for testing server availability.

<b>Response:</b> Returns "Pong" with HTTP status OK (200).

---
### Authentication

---
<b>POST :</b> '/api/auth/signup'

<b>Description:</b> Registers a new user.

<b>Request Body:</b> EntryRequest object containing user registration details.

<b>Response:</b> Returns the result of the registration process.

---
<b>POST :</b> '/api/auth/login'

<b>Description:</b> Authenticates a user.

<b>Request Body:</b> EntryRequest object containing user login credentials.

<b>Response:</b> Returns the result of the login process.

---
<b>GET :</b> '/api/auth/resend/email-confirmation/{email}'

<b>Description: Resends email confirmation to the specified email address.</b>

<b>Path Parameter:</b>  
email (str): Email address to resend confirmation.

<b>Response:</b>  Returns the result of the resend email confirmation process.

---
<b>GET :</b> '/api/auth/email-confirm/{token}'

<b>Description:</b> Confirms user email using the provided token.

<b>Path Parameter:</b>
token (str): Token for email confirmation.

<b>Response:</b> Returns the result of the email confirmation process.

---
<b>GET :</b> '/api/auth/send/reset-password-email/{email}'

<b>Description:</b> Sends a reset password email to the specified email address.

<b>Path Parameter:</b>  
email (str): Email address to send reset password email.

<b>Response:</b> Returns the result of the reset password email sending process.

---
<b>POST :</b> '/api/auth/change-password'

<b>Description:</b> Changes user password.

<b>Request Body:</b> ResetPasswordRequest object containing the new password.

<b>Response:</b> Returns the result of the password change process.

---
<h3>I am grateful to BVP Software for providing this task. Look forward to new challenges and discoveries. 🚀🌟</h3>
## Getting Started

---
### Deploying a project on a local machine

<b>Step 1: Create the database</b>  
Ensure that your local machine has PostgreSQL version 14 or higher installed.
Open a terminal and authenticate with your PostgreSQL user account:  
`psql -U postgres`, and then enter the password.  
After successfully authenticating, create the database using the following command:  
`CREATE DATABASE user_api;`

<b>Step 2: Configure project variables</b>
Open the `application-local.yml` file in the `src/main/resources` directory of the project.
Modify the user parameters to configure your database connection settings.  
`spring.datasource.url:jdbc:mysql://localhost:3306/user_api`  
`spring.datasource.username=db_username`  
`spring.datasource.password=db_password`  

Modify the parameters of the email used to send emails. (Please note that the project can be fully utilized 
without the need to send any messages. This can be achieved by simply commented the line:
`javaMailSender.send(mailMessage);` within MailSenderService).

`mail.host=smtp.gmail.com`  
`mail.port=587`  
`mail.username=local@gmail.com`  
`mail.password=password`  

The secret key and token lifetime can also be modified.

<b>Step 3: Run the application</b>  
Open a terminal in the root directory of the project.
Run the command to build and run the application:

`mvn spring-boot:run`

After successful launch, the application will be available at `http://localhost:8080`.

---
### Deploying a project in a Docker container

<b>Step 1: Run the Docker Desktop</b>  
Ensure that Docker is installed on your local machine and execute it. 
You can download and install Docker Desktop from the official Docker website.

<b>Step 2: Select the required profile</b>  
In `docker-compose.yml` change profile:  
`- SPRING_PROFILES_ACTIVE=prod`

<b>Step 3: Build the project and containers</b>  
Open a terminal and enter the following two commands in sequence (application is tested only with the database up):  
`mvn clean package -DskipTests=true`  
`docker-compose up`  

Containers will be created and run automatically.

---
### Deploying a project on Amazon Elastic Compute Cloud (EC2)
After creating the Docker container as per the previous instructions, follow the steps outlined in this document:  
https://docs.google.com/document/d/1HnlPsvRj-iq9mm-xgicXeCXhXqqEz7vOsryk0ZwP1Lk/edit?usp=sharing

---
ToDoManager — Setup & Run (Windows)
1) Download the Project

Download the .zip file for the repository:

https://github.com/eelittle-USC2023/ToDoManager/tree/main

Unzip the project and open the folder in Visual Studio Code.

2) Install Required Software (Windows)
Visual Studio Code

Download: https://code.visualstudio.com/Download

Java 21 (JDK)

Download and install Java 21 JDK

Verify installation:

java -version

Expected output:

openjdk version "21.x"
MySQL 8.0

Install MySQL Server Community Edition 8.0.45

Create database:

CREATE DATABASE todomanager CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

Create user:

CREATE USER 'todo_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'yourpassword';
GRANT ALL ON todomanager.* TO 'todo_user'@'localhost';
FLUSH PRIVILEGES;
Node.js & npm

Download: https://nodejs.org/en/download/

Verify:

node -v
npm -v
Allow npm to run in VS Code

If npm is blocked in the VS Code terminal:

Run PowerShell as Administrator:

Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
3) Backend Configuration (Spring Boot)
3.1 Configure Database

Open:

toDoManagerBackEnd/src/main/resources/application.properties

Add your database configuration:

spring.datasource.url=jdbc:mysql://localhost:3306/todomanager?useSSL=false&serverTimezone=UTC
spring.datasource.username=todo_user
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
3.2 Run Backend

From workspace root:

.\mvnw.cmd -f toDoManagerBackEnd\pom.xml spring-boot:run -Dspring-boot.run.arguments="--debug"

Or from backend folder:

cd toDoManagerBackEnd
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--debug"

VS Code option:

Open toDoManagerBackEnd as a Java project

Run main class:

com.example.todomanager.TodomanagerApplication
4) Frontend (Vite + React)
4.1 Install Dependencies
cd todoManagerClient
npm install
4.2 Start Frontend
npm run dev
5) Database Seeds & Schema

If SQL scripts exist:

mysql -u root -p todomanager < path\to\schema.sql
mysql -u root -p todomanager < path\to\seed.sql

Or run scripts in MySQL Workbench.

6) Running Full Stack

Open two terminals:

Terminal A — Backend
.\mvnw.cmd -f toDoManagerBackEnd\pom.xml spring-boot:run -Dspring-boot.run.arguments="--debug"
Terminal B — Frontend
cd todoManagerClient
npm install
npm run dev

Open browser:

http://localhost:5173

(or http://localhost:3000 if configured)

7) Troubleshooting
Backend Issues

Ensure commands are run from correct directory

Verify pom.xml is valid

Import Maven project in VS Code:

Command Palette → “Maven: Update Project”

Classpath Issues in VS Code

Refresh Java Projects view

Re-import Maven project

8) Useful Commands
Run Backend
cd <workspace root>
.\mvnw.cmd -f toDoManagerBackEnd\pom.xml spring-boot:run
Build Backend
.\mvnw.cmd -f toDoManagerBackEnd\pom.xml clean package
Run Frontend
cd todoManagerClient
npm run dev
Run SQL Scripts
mysql -u todo_user -p todomanager < toDoManagerBackEnd/sql/schema.sql
9) Final Checklist

Before first run, ensure:

 MySQL is running

 Database todomanager exists

 application.properties is configured correctly

 mysql-connector-j is in pom.xml

 Backend connects successfully to database

 Frontend index.html is located at:

todoManagerClient/index.html

 Ran:

npm install

 CORS configured to allow:

http://localhost:5173
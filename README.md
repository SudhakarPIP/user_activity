User Management - Java Spring Boot
This is a Java Spring Boot–based User Activity Management & Timeline API that supports to 
•	Record user activities
•	Update & delete activities
•	Retrieve a paginated & sorted timeline

The project implements secure authentication using JSON Web Tokens (JWT) and runs on the default server port 8080. 

Features
User activityType =  LOGIN, LOGOUT, PASSWORD_CHANGE, PROFILE_UPDATE

REST API Endpoints
Method	        Endpoint	                                              Description
POST	          /api/v1/users/{userId}/activities	                      Create new activity
DELETE	        /api/v1/activities/{activityId}	                        Soft delete
GET	            /api/v1/users/{userId}/activities/timeline?page=&size=	Paginated timeline

Default Port: 8080

Getting Started
Prerequisites
Make sure you have the following installed:

Java Development Kit (JDK)
Maven
MSSQL
Installation
Clone the repository:

git clone https://github.com/SudhakarPIP/user_activity.git
Navigate to the project directory:

cd user_activity
Build the project using Maven:

Run the application:

The application will start on the default port: http://localhost:8080.

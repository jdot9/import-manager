# Import Manager

This application enables users to schedule automated import sessions for importing HubSpot CRM contact lists into Five9 Virtual Contact Center outbound dialing lists.

# Demonstration
[Insert video here]

# System Architecture

<img width="1035" height="403" alt="import-manager-architecture" src="https://github.com/user-attachments/assets/00944f64-f218-40e2-b262-3dd5465edd3e" />


<img width="1035" height="403" alt="import-manager-architecture2" src="https://github.com/user-attachments/assets/160f6601-a205-4d16-bd6a-ef5337cc4eff" />

# Set Up
Open a terminal

Navigate to application.properties 

server -> importscheduler -> src -> main -> resources -> application.properties

## Database Configuration
Set your database credentials (url, username, password):

spring.datasource.url={Insert database URL here}

spring.datasource.username={Insert database username here}

spring.datasource.password={Insert database password here}

## Email Configuration
This application uses Gmail SMTP. You will need to use an App Password, not your regular Gmail password. 

To generate an App Password: Google Account > Security > 2-Step Verification > App passwords

Once you have your App password, set your email credentials:

spring.mail.username={Insert your gmail username here}

spring.mail.password={Insert your App password here}

## Google OAuth2.0 Configuration
You will need a Client ID and a Client Secret from Google Cloud to configure OAuth2.0. 

Go to Google Cloud Console: https://console.cloud.google.com/

### Step 1: Create a Project
Every set of credentials must live inside a "Project."

Go to the Google Cloud Console.

Click the Project Dropdown (top left, next to the Google Cloud logo).

Click New Project.

Enter a name (e.g., "My Web App") and click Create. Ensure this new project is selected in the dropdown before continuing.

### Step 2: Configure the OAuth Consent Screen
Google won't let you create credentials until you define what users will see when they try to log in.

Open the Navigation Menu (☰) and go to APIs & Services > OAuth consent screen.

User Type: Choose External (if anyone with a Google account can use it) or Internal (if only people in your company/workspace can use it). Click Create.

App Information: Fill in the basics:

App name: The name users see.

User support email: Your email.

Developer contact info: Your email again.

Click Save and Continue through the "Scopes" and "Test Users" sections (you can leave these as default for now).

### Step 3: Generate the Client ID & Secret
In the left sidebar, click Credentials.

Click + Create Credentials at the top of the screen.

Select OAuth client ID.

Application Type: Select the one that matches your project (e.g., Web application).

Authorized Redirect URIs: This is critical. Add the URL where Google should send the user after they log in (e.g., http://localhost:3000/auth/callback for local testing).

Click Create.

### Step 4 (Final): Set your OAuth2.0 credentials in the application.properties file

spring.security.oauth2.client.registration.google.client-id={Insert Google Client ID here}

spring.security.oauth2.client.registration.google.client-secret={Insert Google Client Secret here}


# Tech Stack

- React
- Spring Boot
- MySQL

# Experience Gained

- OAuth 2.0
- REST API Integration 
- SOAP API Integration
- Rate Limiting
- Web Sockets
- Cron Jobs
- Virtual Threads
- BCrypt Hashing 
- AES Encryption 
- Form Validation
- Typeahead pattern
- Dual ID pattern








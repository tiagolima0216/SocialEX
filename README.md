# SocialEX
Social Media Web App made with Spring Boot in the backend and React in the frontend.

Project made by: Tiago Lima | 
www.linkedin.com/in/tiago-lima-56392a399

## Run on localhost or run on a network

The app now runs on localhost
To change that, inside the file social-media-app/src/main/resources/application.properties change:

      # Local dev
      api.location.frontend=http://localhost:5173

      # Or when testing on LAN:
      #api.location.frontend=http://...:5173

To:

      # Local dev
      #api.location.frontend=http://localhost:5173

      # Or when testing on LAN:
      api.location.frontend=http://<IP_OF_THE_NETWORK>:5173

And inside the file social-media-app-frontend/.env change:

      # Local dev
      VITE_API_URL=http://localhost:8080

      # Or when testing on LAN:
      #VITE_API_URL=http://...:8080

To:

      # Local dev
      #VITE_API_URL=http://localhost:8080

      # Or when testing on LAN:
      VITE_API_URL=http://<IP_OF_THE_NETWORK>:8080

## How to run

1. Run docker server

2. Inside the social-media-app folder run:


      docker-compose up
3. To build the backend, inside the social-media-app folder run:

       
      mvn clean install
4. To run the backend, inside the social-media-app/target folder run:


      java -jar .\social-media-app-0.0.1-SNAPSHOT.jar
5. To install the necessary dependencies for the frontend, inside the social-media-app-frontend folder run:

   
      npm install
6. To run the frontend, inside the social-media-app-frontend folder:
   1. if project is setted to localhost, run:

            npm run dev
   2. if project is setted to Ip network, run:

            npm run dev -- --host
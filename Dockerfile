FROM nginx:1.13.7-alpine

COPY target /usr/share/nginx/html 
RUN rm -rf /usr/share/nginx/html/app.out

# Credits microservice

The scope and the description of the hackday task: https://docs.google.com/document/d/12WsFC-RYKTLjGYAwbCdCWm2ce-O3KBI1lmdR2uIKLig/edit?usp=sharing

## Initial configuration
    sdk default grails 4.0.10;
    sdk default gradle 3.5.1;
    sdk default groovy 2.5.7;
    
    docker-compose up;
    
## Running the webapplication

### Development

Run from command line in project directory:

`docker-compose start;` #initiates dev MySQL db
`grails run-app` #starts an application

### Docker
`./gradlew buildImage` generates a docker image.

...



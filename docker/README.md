# docker-compose
Docker Compose configuration and scripts to run the server side components locally

## Running
To run the environment you must have built the APIs to generate the latest docker images (because we don't currently push our
docker images to docker hub or similar. This may change in the future):

    (from the project root directory)
    $ ./gradlew clean check bootBuildImage

Once gradle has built the docker images of the API components, start the environment with the command:

    (in this directory)
    $ ./start-docker.sh

**Important** - Do not run `docker-compose up` - this might not work as the localstack container needs time to start.  

### Maintaining container state
When the containers start via the shell script `start-docker.sh` there is some setup that happens - specifically for localstack.

To maintain state, do not use `docker-compose down` when stopping the containers. As per the docker compose documentation
the `down` command stops the containers *AND* removes the containers, which in turn deletes their state.  
Instead, issue the command `docker-compose stop` or `./stop-docker.sh` to stop the containers, then `./start-docker.sh` 
to re-start them complete with their state.

### Sending application messages

Use the `send-postal-message.sh` and `send-proxy-application.sh` scripts to push example applications onto the incoming queues.
These will generate random application and elector ids each time they are run.

The applications can then be retrieved by calling the get endpoint for each and ensuring the header `client-cert-serial` is set with a value of `543219999`. 


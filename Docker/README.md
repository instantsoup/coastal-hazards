# Note this readme is outdated but kept for reference purposes. See the project readme for updated instructions for using docker. If you'd like more information about how the docker files work then read this readme.

## Coastal Hazards Docker Containers
---------------------------------

Contents:
- Docker
- Docker Compose
- Docker Machine
- Building on the DOI network
- Parameterized Environment Variables
- Geoserver
- RServe
- 52N WPS
- Postgres/PostGIS
- PyCSW
- Troubleshooting

#### Docker
======

The minimum version required for creating and running these containers is 1.12 or higher.

#### Docker Compose
==============

While Docker Compose is not strictly necessary to run this series of containers, it does make life much easier. Ideally you will only need a single command to build all of the containers and then one command to run all of the containers without having to know much more about their inner workings.

#### Docker Machine
==============

This set of containers was built against Docker Machine but I don't think that there's anything specific to using Docker Machine at this time. The only difference you should experience if using just Docker is that instead of connecting to containers via the IP of the Docker Machine, you'll connect to localhost (unless you have some funky networking setup happening).

#### Building on the DOI network
===========================

If you are building these containers on a machine on the DOI network, there is a modification needed in order to properly finish the build. The reason is that the DOI network has implemented an SSL intercept certificate that performs MITM inspection to any HTTPS traffic. This causes many errors when dependencies are attempting to be downloaded from GitHub, Python repositories and more. By default, these containers are assuming they are not being built on the DOI network. To build on the DOI network, you should preface `doi_network="true"` prior to building:

`$ doi_network="true" docker-compose build [container name]`

With this modification, the containers that need it will include pulling the SSL root certificate from DOI and install it into openssl as a valid certificate.

#### Parameterized Environment Variables
===============================
Several of the containers accept parameterized environment files. By default, `docker-compose` will use `compose.env`. To customize the parameters, first create a copy of `compose.env`. Make sure the copy's file name starts with`compose` and ends with `.env`. An example valid custom env file name is `compose_johns_laptop.env`. To take advantage of your custom env file, prepend each of your `docker-compose` commands with an assignment to the `CCH_ENV_LOCAL` variable. Use the middle portion of your custom env file name as the value. For example:

```
$ CCH_ENV_LOCAL="_johns_laptop" doi_network="true" docker-compose up cch_db cch_rserve cch_n52_wps cch_pycsw
```

Known Bugs:
Not all parameterizations work. In particular, most version numbers in custom env files are ignored in favor of the default values. This is detailed further [on Slack](https://usgs-cida.slack.com/archives/cch/p1476487434000753).

#### Building Everything
==========
Clone the docker-rserve repo and build it as described in the first paragraph of the RServe section below.
After, change to the coastal-hazards/Docker directory and run:

`
doi_network="true" docker-compose up
`

If you are not on the doi network, do not specify that variable. If you need parameterized environments, add the appropriate `CCH_ENV_LOCAL` value as described in the "Parameterized Environment Variables" section.


#### Running The Portal and GeoServer locally with all other services in containers
================

Run a command like the following to stand up the relevant containers:

`
CCH_ENV_LOCAL="_local" doi_network="true" docker-compose up cch_db cch_rserve cch_n52_wps cch_pycsw
`

Manually set up you tomcat instances locally for the Portal and GeoServer. Use the dev tier's context.xml's or the Dockerized context.xml's as a starting point.

Modify the context.xml's for those tomcat instances. Most references to services should use either `localhost` or your docker machine vm's IP. Ports for containerized services are defined in `coastal-hazards/Docker/docker-compose.yml`.


#### Geoserver
=========

The Geoserver container runs standalone and does not require other containers to be running in order to run.
This container does not need DOI SSL certificates for building. You can build the container by issuing:

`$ docker-compose build cch_geoserver`

You will end up with an image named cch_geoserver in your Docker repository. The port exposed to the host will be 8081 and it will route to port 8080 on the actual server. You can run the container by issuing:

`docker-compose up cch_geoserver`

Once the server is up, you will be able to connect to it by pointing a browser to `http://<docker ip>:8081/geoserver`

#### RServe
======

In order to build the CCH RServe container, you will first need to build the base rserve container which is also produced by us. To build the base rserve container, clone the GitHub repository at [https://github.com/USGS-CIDA/docker-rserve](https://github.com/USGS-CIDA/docker-rserve). When in the directory you've cloned to, just issue `docker-compose build`. The DOI SSL issue also applies to this container, so if you are on the DOI network, issue `doi_network="true" docker-compose build` instead.

Once the base container is built, you may then build the CCH RServe container by issuing:

`doi_network="true" docker-compose build cch_rserve`

This container is standalone and does not depend on any other containers to run. You can run the container by issuing:

`docker-compose up cch_rserve`

The container exposes port 6311 if you need to work with this RServe standalone

#### 52N WPS
=======

The 52 North WPS container takes advantage of the RServe container to create the WPS4R service. The RServe container should already be running in order for this container to run properly. There is configuration in Docker Compose that causes this container to wait until the CCH RServe container is operational before launching this one. The DOI SSL issue also applies to this container, so if you are on the DOI network, issue `doi_network="true" docker-compose build cch_52n_wps`. Otherwise, `docker-compose build cch_52n_wps` is sufficient.

When running this container from Docker Compose, RServe will automatically be started if it is not currently running.

Once the server is up and running, you can access the server by pointing your web browser at `http://<docker ip>:8082/wps`

#### Postgres/PostGIS
================


This container is standalone and does not depend on any other containers to run. You can run the container by issuing:

`docker-compose up cch_db`

The DOI SSL issue also applies to this container, so if you are on the DOI network, issue:

`doi_network="true" docker-compose build cch_db`

You will then be able to connect to the database via the ports and credentials described in `docker-compose.yml` and `postgres/Dockerfile`

#### PyCSW
=====

PyCSW requires a database. Accordingly, this container requires the postgres (`cch_db`) container.

You can run the container and its dependent db container by issuing:

`docker-compose up cch_pycsw`

The DOI SSL issue also applies to this container, so if you are on the DOI network, issue:

`doi_network="true" docker-compose build cch_pycsw`


#### Troubleshooting
=====================


##### Stale Container Contents

Not seeing the changes you expect? Try building without a cache by using `--build` and/or `--force-recreate`.

Example:

```
CCH_ENV_LOCAL="_johns_laptop" doi_network="true" docker-compose up --build --force-recreate cch_db cch_rserve cch_n52_wps cch_pycsw
```

##### DB-related Startup Errors

The postgres and PyCSW containers may fail to start the first time you try to `docker-compose up` them, logging messages like:

`
docker_cch_db_1 exited with code 255
docker_cch_pycsw_1 exited with code 1
`

In that case, try running the same command again. You may get an error like this:

`
ERROR: for cch_db  oci runtime error: container with id exists: 5ca07708b997ab70562ae32f79d4925b0bf7e35c7997a00ded90cf416685e038
Traceback (most recent call last):
  File "/usr/bin/docker-compose", line 9, in <module>
    load_entry_point('docker-compose==1.7.1', 'console_scripts', 'docker-compose')()
  File "/usr/lib/python2.7/site-packages/compose/cli/main.py", line 63, in main
    log.error(e.msg)
AttributeError: 'ProjectError' object has no attribute 'msg'
`

In that case, try the same command again.

##### 52N WPS Difficulties
If the 52N WPS is giving you a hard time, you can safely use the dev tier instead.

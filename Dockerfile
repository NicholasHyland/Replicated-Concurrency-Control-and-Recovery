FROM frolvlad/alpine-oraclejdk8
MAINTAINER Palak Bhasin, pb1881@nyu.edu
WORKDIR /app
ADD . /app
CMD ["sh", "run.sh"]
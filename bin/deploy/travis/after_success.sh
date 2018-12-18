#!/usr/bin/env bash

if [ "$TRAVIS_PULL_REQUEST_SLUG" == "${GIT_ORGANIZATION_NAME}/${GIT_REPO_NAME}" ] \
    && [ "push" == "$TRAVIS_EVENT_TYPE" ] \
    && [ "master" == "$TRAVIS_BRANCH" ]
then
    mvn appengine:deploy -Dmaven.test.skip=true
fi

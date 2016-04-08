#!/usr/bin/env bash

# because YAML sux with bash syntax
[[ $TRAVIS_PULL_REQUEST == true ]] && {
    [[ $TRAVIS_BRANCH == 'dev/version/'* ]] || [[ $TRAVIS_BRANCH == 'master' ]]
} && ./gradlew uploadArchives -PossrhUsername=kenzierocks
#!/usr/bin/env bash

# because YAML sux with bash syntax
if [[ $TRAVIS_PULL_REQUEST == true ]]; then
    echo 'Aborting due to PR status.'
    exit 0
fi
if [[ $TRAVIS_BRANCH == 'dev/version/'* ]] || || [[ $TRAVIS_BRANCH == 'master' ]]; then
    echo 'Aborting due to branch being '"$TRAVIS_BRANCH"'.'
    exit 0
fi
echo 'Running upload...'
./gradlew uploadArchives -PossrhUsername=kenzierocks
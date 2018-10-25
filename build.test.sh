#!/bin/bash

# uninstall previous builds
gauge uninstall testrail-sync
gauge uninstall testrail

# rebuild
mvn clean package

# install 
gauge install testrail-sync -f gauge-testrail-sync/artifacts/testrail-sync-*.zip
gauge install testrail -f gauge-testrail-report/artifacts/testrail-*.zip

#!/bin/bash

# uninstall previous builds
gauge uninstall testrail-sync
gauge uninstall testrail

# rebuild
mvn clean package

# install 
gauge install testrail-sync -f gauge-testrail-sync/artifacts/testrail-sync-0.0.1.zip
gauge install testrail -f gauge-testrail-report/artifacts/testrail-0.0.1.zip

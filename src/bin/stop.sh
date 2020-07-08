#!/bin/bash

ps -ef | grep logstash-forworder-java | awk '{print $2}' | xargs kill -9
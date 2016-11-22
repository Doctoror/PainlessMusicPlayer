#!/bin/bash
protoc ./src/main/proto/wear_search_data.proto --javanano_out=./src/main/java/ --proto_path=./src/main/proto/

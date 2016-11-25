#!/bin/bash
protoc ./src/main/proto/wear_playlist_from_search.proto --javanano_out=./src/main/java/ --proto_path=./src/main/proto/

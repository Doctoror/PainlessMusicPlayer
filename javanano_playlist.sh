#!/bin/bash
protoc ./app/src/main/proto/persistable_playlist.proto --javanano_out=./app/src/main/java/ --proto_path=./app/src/main/proto/
